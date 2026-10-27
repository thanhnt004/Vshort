package org.example.application.port.in;

import java.util.Map;
import java.util.UUID;

public interface SaveUploadPartUseCase {
   void execute(UUID sessionId, Map<Integer,String> partNumberTagsMap);
}
