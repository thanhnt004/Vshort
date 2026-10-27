package org.example.application.port.in;

import java.util.List;
import java.util.Map;
import java.util.TreeSet;
import java.util.UUID;

public interface GetPresignUrlsUseCase {
    Map<Integer, String> getBatchChunkUploadUrls(UUID uploadSessionId, TreeSet<Integer> partNumbers);
}
