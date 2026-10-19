package com.example.service;

import com.example.dto.request.UpdateProfileRequest;
import com.example.dto.response.ProfileResponse;
import com.example.dto.response.PublicProfileResponse;
import org.springframework.security.oauth2.jwt.Jwt;

public interface ProfileService {
    void createDefaultProfile(Long userId, String userName);

    ProfileResponse updateProfile(UpdateProfileRequest request, Jwt principal);

    ProfileResponse getProfile(Jwt jwt);
    PublicProfileResponse getProfile(String id);
}
