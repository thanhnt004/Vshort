package org.example.presentation.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.application.dto.result.RegisterResult;
import org.example.application.port.in.*;
import org.example.presentation.mapper.AccountWebMapper;
import org.example.presentation.request.LoginRequest;
import org.example.presentation.request.RegisterRequest;
import org.example.presentation.request.SendEmailRequest;
import org.example.presentation.response.TokenResponse;
import org.example.response.ApiResponse;
import org.example.utils.CookieUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping( "api/v1/auth")
@RequiredArgsConstructor
public class AccountController {
    private final RegisterAccountUseCase registerAccountUseCase;
    private final LoginAccountUseCase loginAccountUseCase;
    private final RefreshTokenUseCase refreshTokenUseCase;
    private final LogoutUseCase logoutUsecase;
    private final VerifyEmailUseCase verifyEmailUseCase;
    private final AccountWebMapper mapper;
    private final CookieUtils cookieUtils;
    @PostMapping("/register")
    public ResponseEntity<ApiResponse<RegisterResult>> register(@RequestBody @Valid RegisterRequest registerRequest)
    {
        var response = registerAccountUseCase.execute(mapper.toCommand(registerRequest));
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(response));
    }
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<TokenResponse>> login(@RequestBody @Valid LoginRequest loginRequest, HttpServletResponse httpResponse)
    {
        var loginResult = loginAccountUseCase.execute(mapper.toCommand(loginRequest));
        cookieUtils.addCookie(httpResponse,CookieUtils.REFRESH_TOKEN_COOKIE,loginResult.refreshToken());
        var responsePayload = mapper.toResponse(loginResult);
        return ResponseEntity.ok(ApiResponse.success(responsePayload));
    }
    @PostMapping(value = "/refresh")
    public ResponseEntity<ApiResponse<TokenResponse>> refresh(HttpServletRequest request, HttpServletResponse httpResponse)
    {
        String refreshToken = cookieUtils.readCookieValue(request,CookieUtils.REFRESH_TOKEN_COOKIE);
        var refreshTokenResult = refreshTokenUseCase.execute(refreshToken);
        cookieUtils.addCookie(httpResponse,CookieUtils.REFRESH_TOKEN_COOKIE,refreshTokenResult.refreshToken());
        var responsePayload = mapper.toResponse(refreshTokenResult);
        return ResponseEntity
                .status(HttpStatusCode.valueOf(201))
                .body(ApiResponse.success(responsePayload));
    }
    @PostMapping(value = "/logout")
    public ResponseEntity<?> logOut(HttpServletRequest request, HttpServletResponse response)
    {
        String refreshToken = cookieUtils.readCookieValue(request,CookieUtils.REFRESH_TOKEN_COOKIE);;
        logoutUsecase.logout(refreshToken);
        cookieUtils.clearCookie(response,CookieUtils.REFRESH_TOKEN_COOKIE);
        return ResponseEntity.ok(ApiResponse.success("Logout successful"));
    }
    @PostMapping(value = "/logout-all")
    public ResponseEntity<?> logOutAll(HttpServletRequest request, HttpServletResponse response)
    {
        String refreshToken = cookieUtils.readCookieValue(request,CookieUtils.REFRESH_TOKEN_COOKIE);;
        logoutUsecase.logoutAll(refreshToken);
        cookieUtils.clearCookie(response,CookieUtils.REFRESH_TOKEN_COOKIE);
        return ResponseEntity.ok(ApiResponse.success("Logout from all devices successful"));
    }
    @PostMapping(value = "/verify-email")
    public ResponseEntity<?> verifyEmail(@RequestParam(name = "token") String token)
    {
        var result  = verifyEmailUseCase.verifyEmail(token);
        return ResponseEntity.ok(ApiResponse.success(result));
    }
    // POST /api/v1/auth/verify-email/resend
    @PostMapping("/verify-email/resend")
    public ResponseEntity<?> resend(@Valid @RequestBody SendEmailRequest req) {
        verifyEmailUseCase.resendVerifyEmail(req.getEmail());
        return ResponseEntity.accepted().build();
    }
}
