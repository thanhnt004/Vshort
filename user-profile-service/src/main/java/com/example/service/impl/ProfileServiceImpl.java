package com.example.service.impl;

import com.example.dto.request.UpdateProfileRequest;
import com.example.dto.response.ProfileResponse;
import com.example.dto.response.PublicProfileResponse;
import com.example.exception.BusinessException;
import com.example.exception.ErrorCode;
import com.example.mapper.ProfileMapper;
import com.example.model.Profile;
import com.example.model.UserSettings;
import com.example.repository.ProfileRepository;
import com.example.repository.UserSettingRepository;
import com.example.service.ProfileService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class ProfileServiceImpl implements ProfileService {
    private final ProfileRepository profileRepository;
    private final ProfileMapper profileMapper;
    @Override
    @Transactional
    public void createDefaultProfile(Long userId, String userName) {
        if(profileRepository.existsById(userId))
            return;
        Profile newProfile = Profile.builder()
                .userId(userId)
                .username(userName)
                .fullName(userName)
                .build();
        UserSettings userSettings = UserSettings.builder()
                .userId(userId)
                .profile(newProfile)
                .build();
        newProfile.setUserSettings(userSettings);
        profileRepository.save(newProfile);
    }

    @Override
    public ProfileResponse updateProfile(UpdateProfileRequest request, Jwt principal) {
        if (principal.getSubject() == null)
            throw new BusinessException(ErrorCode.PROFILE_NOT_FOUND);
        Long userId = Long.valueOf(principal.getSubject().trim());
        Profile profile = profileRepository.findById(userId)
                .orElseThrow(()->new BusinessException(ErrorCode.PROFILE_NOT_FOUND));
        profileMapper.updateProfile(profile,request);
        profileRepository.save(profile);
        return profileMapper.toResponse(profile);
    }

    @Override
    public ProfileResponse getProfile(Jwt principal) {
        if (principal.getSubject() == null)
            throw new BusinessException(ErrorCode.PROFILE_NOT_FOUND);
        Long userId = Long.valueOf(principal.getSubject().trim());
        log.info("Get profile for user: {}", userId);
        Profile profile = profileRepository.findById(userId)
                .orElseThrow(()->new BusinessException(ErrorCode.PROFILE_NOT_FOUND));
        return profileMapper.toResponse(profile);
    }

    @Override
    public PublicProfileResponse getProfile(String id) {

        return profileRepository.findProfileById(id).orElseThrow(
                ()->new BusinessException(ErrorCode.PROFILE_NOT_FOUND)
        );
    }
}
