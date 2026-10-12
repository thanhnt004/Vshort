package com.example.controller;

import com.example.dto.request.UpdateProfileRequest;
import com.example.dto.response.ProfileResponse;
import com.example.dto.response.PublicProfileResponse;
import com.example.service.ProfileService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.example.response.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("api/v1/profile")
@RequiredArgsConstructor
public class ProfileController {
    private final ProfileService profileService;
    @PostMapping
    @PreAuthorize(value = "hasRole('USER')")
    public ResponseEntity<ApiResponse<ProfileResponse>> updateProfile(@AuthenticationPrincipal Jwt jwt,
                                                                      @RequestBody UpdateProfileRequest request)
    {
        var  result = profileService.updateProfile(request,jwt);
        return ResponseEntity.ok(ApiResponse.success(result));
    }
    @GetMapping( "/me")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<ProfileResponse>> getProfile(@AuthenticationPrincipal Jwt jwt)
    {
        var response = profileService.getProfile(jwt);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<PublicProfileResponse>> getPublicProfile(@Valid @PathVariable @NotBlank String id){
        var response = profileService.getProfile(id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
