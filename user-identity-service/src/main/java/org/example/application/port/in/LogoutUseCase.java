package org.example.application.port.in;

public interface LogoutUseCase {
    void logout(String refreshToken);
    void logoutAll(String refreshToken);
}
