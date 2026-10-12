package com.example.dto.request;

import java.time.LocalDate;

public record UpdateProfileRequest(
        String fullName,
        String avatarUrl,
        LocalDate birthDate,
        String bio
) {
}
