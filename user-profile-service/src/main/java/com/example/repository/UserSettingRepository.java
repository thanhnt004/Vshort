package com.example.repository;

import com.example.model.UserSettings;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserSettingRepository extends JpaRepository<UserSettings,Long> {
}
