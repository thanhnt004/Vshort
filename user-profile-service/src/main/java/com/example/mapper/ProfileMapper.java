package com.example.mapper;

import com.example.dto.request.UpdateProfileRequest;
import com.example.dto.response.ProfileResponse;
import com.example.model.Profile;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel ="spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface ProfileMapper {

    void updateProfile(@MappingTarget Profile profile, UpdateProfileRequest updateProfileRequest);
    ProfileResponse toResponse(Profile profile);
}
