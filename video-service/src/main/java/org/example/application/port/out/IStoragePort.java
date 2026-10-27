package org.example.application.port.out;

import lombok.Getter;
import org.example.application.dto.model.OrphanMultipartUpload;
import org.example.application.dto.model.UploadPartModel;

import java.util.List;
import java.util.Map;
import java.util.TreeSet;

public interface IStoragePort {
    String initUploadSession(String fileName,String contentType);
    String getPresignedUrlForPart(String uploadId, String fileName, int partNumber);
    String completeUploadSession(String fileName,String uploadSessionId, List<ChunkPart> parts,String expectedTag);
    Map<Integer, String> getBatchChunkUploadUrls(String uploadSessionId, String fileName, TreeSet<Integer> partNumbers);
    void deleteFile(String storageKey);
    void abortUploadSession(String uploadSessionId, String storageKey);
    boolean isUploadSessionActive(String fileName, String providerId);
    List<OrphanMultipartUpload> listInProgressMultipartUploads();
    @Getter
    public class ChunkPart {
        private int partNumber;
        private String eTag; // Chuỗi hash xác nhận chunk đã lên S3 thành công
        public ChunkPart(UploadPartModel uploadPartModel) {
            this.partNumber = uploadPartModel.getPartNumber();
            this.eTag = uploadPartModel.getEtag();
        }
    }
}
