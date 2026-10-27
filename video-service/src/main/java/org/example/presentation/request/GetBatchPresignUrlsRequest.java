package org.example.presentation.request;

import jakarta.validation.constraints.*;

import java.util.List;
import java.util.TreeSet;
import java.util.UUID;

public record GetBatchPresignUrlsRequest(
        @NotNull
        UUID uploadSessionId,
        @NotNull
        TreeSet<Integer> partNumbers
) {

}
