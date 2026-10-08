package org.example.presentation.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.application.port.in.ManagePasswordUseCase;
import org.example.presentation.mapper.PasswordWebMapper;
import org.example.presentation.request.ChangePasswordRequest;
import org.example.presentation.request.ResetPasswordRequest;
import org.example.presentation.request.SendEmailRequest;
import org.example.response.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.Objects;

@RestController
@RequestMapping( "api/v1/auth/password")
@RequiredArgsConstructor
@Slf4j
public class PasswordController {
    private final ManagePasswordUseCase managePasswordUseCase;
    private final PasswordWebMapper mapper;
    @PreAuthorize(value = "isAuthenticated()")
    @PostMapping(value = "/change-password")
    public ResponseEntity<?> changePassword(@AuthenticationPrincipal Jwt jwt, @RequestBody ChangePasswordRequest changePasswordRequest)
    {
        managePasswordUseCase.changePassword(mapper.toCommand(changePasswordRequest,jwt.getSubject()));
        return ResponseEntity.noContent().build();
    }
    @PostMapping(value = "/forgot-password")
    public ResponseEntity<ApiResponse<?>> sendForgotPasswordEmail(@Valid @RequestBody SendEmailRequest request, HttpServletRequest httpServletRequest){
        managePasswordUseCase.sendForgotPasswordEmail(request.getEmail());
        return ResponseEntity.status(200).body(ApiResponse.success());
    }
    @PostMapping(value = "/reset-password/validate-token")
    public ResponseEntity<ApiResponse<?>> validateToken(@RequestParam String token){
        managePasswordUseCase.validateForgotPasswordToken(token);
        return ResponseEntity.status(200).body(ApiResponse.success());
    }
    @PostMapping(value = "/reset-password")
    public ResponseEntity<ApiResponse<?>> resetPassword(@Valid @RequestBody ResetPasswordRequest request)
    {
        managePasswordUseCase.resetPassword(mapper.toCommand(request));
        return ResponseEntity.status(200).body(ApiResponse.success());
    }
}
