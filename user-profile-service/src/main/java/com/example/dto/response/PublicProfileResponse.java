package com.example.dto.response;

import java.time.LocalDate;

public record PublicProfileResponse (
        Long userId,
        String fullName,
        String avatarUrl,
        String bio,
        Boolean isVerified,
        Integer followerCount,
        Integer followingCount
){
}
