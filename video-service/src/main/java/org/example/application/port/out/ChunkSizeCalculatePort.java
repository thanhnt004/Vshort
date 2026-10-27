package org.example.application.port.out;

import org.example.infrashtructure.adapter.ChunkSizeCalculateHandler;

public interface ChunkSizeCalculatePort {
    ChunkSizeCalculateHandler.ChunkPlan calculateOptimalPlan(long fileSize);
}
