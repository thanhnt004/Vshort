package com.example.service.impl;

import com.example.model.Profile;
import com.example.model.UserSettings;
import com.example.repository.ProfileRepository;
import com.example.repository.UserSettingRepository;
import com.example.service.ProfileService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class ProfileServiceImpl implements ProfileService {
    private final ProfileRepository profileRepository;
    @Override
    @Transactional
    public void createDefaultProfile(Long userId, String userName) {

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
}
