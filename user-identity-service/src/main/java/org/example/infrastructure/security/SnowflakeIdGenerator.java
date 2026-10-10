package org.example.infrastructure.security;

import org.example.application.port.out.IdGeneratorPort;
import org.springframework.stereotype.Component;

@Component
public class SnowflakeIdGenerator implements IdGeneratorPort {
    // Mốc thời gian Epoch tùy chỉnh (Ví dụ: 2010-11-04T01:42:54.657Z)
    private static final long EPOCH = 1288834974657L;

    // Số bit dành cho từng phần
    private static final long WORKER_ID_BITS = 5L;
    private static final long DATACENTER_ID_BITS = 5L;
    private static final long SEQUENCE_BITS = 12L;

    // Giá trị tối đa của Worker và Datacenter (31)
    private static final long MAX_WORKER_ID = ~(-1L << WORKER_ID_BITS);
    private static final long MAX_DATACENTER_ID = ~(-1L << DATACENTER_ID_BITS);

    // Vị trí dịch bit (Bit Shift) cho từng phần để ghép thành 64-bit
    private static final long WORKER_ID_SHIFT = SEQUENCE_BITS;
    private static final long DATACENTER_ID_SHIFT = SEQUENCE_BITS + WORKER_ID_BITS;
    private static final long TIMESTAMP_LEFT_SHIFT = SEQUENCE_BITS + WORKER_ID_BITS + DATACENTER_ID_BITS;

    // Mặt nạ Sequence (4095) để đảm bảo không vượt quá 12 bit
    private static final long SEQUENCE_MASK = ~(-1L << SEQUENCE_BITS);
    private final long workerId = 1;
    private final long datacenterId = 1;
    private long sequence = 0L;
    private long lastTimestamp = -1L;

    public synchronized Long generateId() {
        long timestamp = timeGen();

        // Xử lý lỗi đồng hồ chạy ngược (Clock backward)
        if (timestamp < lastTimestamp) {
            throw new RuntimeException(
                    String.format("Đồng hồ hệ thống bị lùi lại. Từ chối tạo ID trong %d mili-giây", lastTimestamp - timestamp));
        }

        // Nếu cùng 1 mili-giây, tăng sequence lên
        if (lastTimestamp == timestamp) {
            sequence = (sequence + 1) & SEQUENCE_MASK;

            // Nếu sequence vượt qua 4095, tràn bit -> Chờ đến mili-giây tiếp theo
            if (sequence == 0) {
                timestamp = tilNextMillis(lastTimestamp);
            }
        } else {
            // Bước sang mili-giây mới, reset sequence về 0
            sequence = 0L;
        }

        lastTimestamp = timestamp;

        // Dịch bit và dùng phép OR (|) để nối các phần lại với nhau thành số 64-bit
        return ((timestamp - EPOCH) << TIMESTAMP_LEFT_SHIFT) |
                (datacenterId << DATACENTER_ID_SHIFT) |
                (workerId << WORKER_ID_SHIFT) |
                sequence;
    }

    // Hàm vòng lặp chờ cho đến khi thời gian thực sự bước sang mili-giây mới
    private long tilNextMillis(long lastTimestamp) {
        long timestamp = timeGen();
        while (timestamp <= lastTimestamp) {
            timestamp = timeGen();
        }
        return timestamp;
    }

    // Hàm lấy thời gian hiện tại
    private long timeGen() {
        return System.currentTimeMillis();
    }
}
