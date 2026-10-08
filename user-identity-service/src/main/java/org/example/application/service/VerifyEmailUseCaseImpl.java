package org.example.application.service;

import lombok.RequiredArgsConstructor;
import org.example.application.dto.eventpayload.ResendVerifyEmailPayload;
import org.example.application.port.in.VerifyEmailUseCase;
import org.example.application.port.out.AccountRepositoryPort;
import org.example.application.port.out.CachePort;
import org.example.application.port.out.EventPublisherPort;
import org.example.domain.entity.Account;
import org.example.domain.entity.AccountStatus;
import org.example.domain.exception.AccountErrorCode;
import org.example.domain.exception.AccountException;
import org.example.event.EventMetadata;
import org.example.event.EventWrapper;
import org.example.infrastructure.cache.RedisCacheAdapter;
import org.example.utils.CryptoUtils;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class VerifyEmailUseCaseImpl implements VerifyEmailUseCase {
    private final AccountRepositoryPort accountRepositoryPort;
    private final EventPublisherPort eventPublisherPort;
    private final CachePort cachePort;
    @Override
    public Map<String, Object> verifyEmail(String token) {
        //find account
        String hashToken = CryptoUtils.hash(token);
        Long accountId = (Long) cachePort.get(RedisCacheAdapter.verifyEmailKey + hashToken).orElseThrow(()->
                new AccountException(AccountErrorCode.TOKEN_EXPIRE));
        Account account = accountRepositoryPort.findById(accountId).orElseThrow(
                ()->new AccountException(AccountErrorCode.ACCOUNT_NOT_FOUND)
        );
        if (account.isActive())
            return Map.of(
                "status", "already_verified"
        );
        //set and save account
        account.setStatus(AccountStatus.ACTIVE);
        accountRepositoryPort.saveAccount(account);
        return Map.of(
                "status", "verified",
                "userId", account.getId(),
                "email", account.getEmail()
        );
    }
    @Override
    public void resendVerifyEmail(String email) {
        Account account = accountRepositoryPort.findByEmail(email).orElseThrow(
                ()->new AccountException(AccountErrorCode.ACCOUNT_NOT_FOUND)
        );
        if (account.isActive())
            return;
        EventMetadata metadata = new EventMetadata(
                java.util.UUID.randomUUID().toString(), // eventId
                "RESEND_VERIFY_EMAIL_REQUESTED",
                "account-service",
                UUID.randomUUID().toString(),           // traceId
                null,
                System.currentTimeMillis()
        );
        String emailVerifyToken = CryptoUtils.generateSecureTokenRaw();
        String hashToken = CryptoUtils.hash(emailVerifyToken);
        cachePort.set(RedisCacheAdapter.verifyEmailKey +hashToken,account.getId(),360);
        ResendVerifyEmailPayload resendVerifyEmailPayload = new ResendVerifyEmailPayload(
                account.getId().toString(),
                account.getUsername(),
                account.getEmail().getValue(),
                emailVerifyToken
        );
        eventPublisherPort.publishEvent(new EventWrapper<>(metadata,resendVerifyEmailPayload),"email_events");
    }
}
