package org.example.application.port.out;

public interface FileValidatePort {
    void validateFile(long fileSize, String contentType, String fileName);
}
