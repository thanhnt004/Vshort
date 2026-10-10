package org.example.application.dto.command;

import lombok.Builder;

@Builder
public record LoginCommand(String identity, String password) {
}
