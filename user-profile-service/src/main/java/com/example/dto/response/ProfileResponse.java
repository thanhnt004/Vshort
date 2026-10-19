package com.example.dto.response;


import java.time.LocalDate;

public record ProfileResponse(
        Long userId,
        String username,
        String fullName,
        String avatarUrl,
        LocalDate birthDate,
        String bio,
        Boolean isVerified,
        Integer followerCount,
        Integer followingCount
) {
}
