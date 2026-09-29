package org.example.application.dto.eventpayload;

import org.example.domain.entity.Account;

public record AccountCreatedPayload(String accountId,
                                  String username,
                                  String email,String verifyToken) {
}