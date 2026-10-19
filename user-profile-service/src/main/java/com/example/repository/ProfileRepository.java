package com.example.repository;

import com.example.dto.response.PublicProfileResponse;
import com.example.model.Profile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface ProfileRepository extends JpaRepository<Profile,Long> {
    @Query("SELECT new com.example.dto.response.PublicProfileResponse(p.userId,p.fullName,p.avatarUrl,p.bio,p.isVerified,p.followerCount,p.followingCount) from Profile p where p.userId = :id")
    Optional<PublicProfileResponse> findProfileById(@Param("id") String id);
}
