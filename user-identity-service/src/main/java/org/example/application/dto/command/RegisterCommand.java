package org.example.application.dto.command;

import lombok.*;

@Builder
public record RegisterCommand(String username, String password, String email, String phoneNumber) {
}
