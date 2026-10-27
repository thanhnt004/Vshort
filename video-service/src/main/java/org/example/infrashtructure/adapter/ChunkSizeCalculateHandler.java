package org.example.infrashtructure.adapter;

import lombok.RequiredArgsConstructor;
import org.example.application.port.out.ChunkSizeCalculatePort;
import org.example.infrashtructure.config.MultipartUploadProperties;
import org.springframework.stereotype.Component;
import org.springframework.util.unit.DataSize;

@Component
@RequiredArgsConstructor
public class ChunkSizeCalculateHandler implements ChunkSizeCalculatePort {
    private static final long TARGET_BATCH_BYTES = DataSize.ofMegabytes(320).toBytes();
    private final MultipartUploadProperties props;
    public record ChunkPlan(
            boolean isMultipart,
            long chunkSize,
            int totalChunks,
            int recommendedPresignBatchSize
    ) {}
    @Override
    public ChunkPlan calculateOptimalPlan(long fileSize) {
        long maxFileBytes = props.getMaxFileSize().toBytes();
        if (fileSize <= 0 || fileSize > maxFileBytes) {
            throw new IllegalArgumentException(
                    "Dung lượng file không hợp lệ hoặc vượt quá giới hạn: " + props.getMaxFileSize()
            );
        }

        // 1. Bypass Multipart nếu file <= single-upload-threshold (16 MiB)
        if (fileSize <= props.getSingleUploadThreshold().toBytes()) {
            return new ChunkPlan(false, fileSize, 1, 1);
        }

        long minPartBytes = props.getMinPartSize().toBytes();
        long basePartBytes = props.getBasePartSize().toBytes();
        long optimalMaxBytes = props.getOptimalMaxPartSize().toBytes();
        long hardMaxBytes = props.getHardMaxPartSize().toBytes();
        long alignmentBytes = props.getAlignmentBlock().toBytes();

        // Tính kích thước thô theo target-parts
        long rawForTarget = ceilDiv(fileSize, props.getTargetParts());
        long idealSize = Math.max(basePartBytes, rawForTarget);

        // Giữ kích thước chunk <= optimal-max-part-size nếu chưa chạm trần max-parts-limit
        long minRequiredForHardLimit = ceilDiv(fileSize, props.getMaxPartsLimit());
        long adjustedSize = Math.max(
                Math.min(idealSize, optimalMaxBytes),
                minRequiredForHardLimit
        );

        //Làm tròn lên theo bội số của alignment-block (4 MiB)
        long alignedSize = alignUp(adjustedSize, alignmentBytes);

        // Kẹp trong khoảng [min-part-size, hard-max-part-size]
        long finalChunkSize = Math.min(hardMaxBytes, Math.max(minPartBytes, alignedSize));

        // Tính tổng số lượng chunk và kích thước Batch Presigned URL
        int totalChunks = (int) ceilDiv(fileSize, finalChunkSize);
        int batchSize = calculateBatchSize(finalChunkSize, totalChunks);

        return new ChunkPlan(true, finalChunkSize, totalChunks, batchSize);
    }

    private int calculateBatchSize(long chunkSize, int totalChunks) {
        if (totalChunks <= 50) {
            return totalChunks;
        }
        int dynamicBatch = (int) Math.max(10, Math.min(50, TARGET_BATCH_BYTES / chunkSize));
        return Math.min(totalChunks, dynamicBatch);
    }

    private long ceilDiv(long x, long y) {
        return (x + y - 1) / y;
    }

    private long alignUp(long value, long alignment) {
        return ceilDiv(value, alignment) * alignment;
    }
}
