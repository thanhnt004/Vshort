package org.example.application.service;

import lombok.RequiredArgsConstructor;
import org.example.application.dto.command.ChangePasswordCommand;
import org.example.application.dto.command.ResetPasswordCommand;
import org.example.application.dto.eventpayload.SendForgotPasswordPayload;
import org.example.application.port.in.ManagePasswordUseCase;
import org.example.application.port.out.AccountRepositoryPort;
import org.example.application.port.out.CachePort;
import org.example.application.port.out.EventPublisherPort;
import org.example.application.port.out.PasswordEncoderPort;
import org.example.domain.entity.Account;
import org.example.domain.exception.AccountErrorCode;
import org.example.domain.exception.AccountException;
import org.example.domain.valueobject.Password;
import org.example.event.EventMetadata;
import org.example.event.EventWrapper;
import org.example.infrastructure.adapter.KafkaEventPublisherAdapter;
import org.example.infrastructure.cache.RedisCacheAdapter;
import org.example.utils.CryptoUtils;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class ManagePasswordUseCaseImpl implements ManagePasswordUseCase {
    private final AccountRepositoryPort accountRepositoryPort;
    private final PasswordEncoderPort passwordEncoderPort;
    private final EventPublisherPort eventPublisherPort;
    private final CachePort cachePort;
    @Override
    public void changePassword(ChangePasswordCommand changePasswordCommand) {
        Account account = accountRepositoryPort.findById(Long.valueOf(changePasswordCommand.userId()))
                .orElseThrow(()->new AccountException(AccountErrorCode.ACCOUNT_NOT_FOUND))
                ;
        if (!passwordEncoderPort.matches(changePasswordCommand.currentPassword(),account.getPassword().getHash()))
        {
            throw new AccountException(AccountErrorCode.PASSWORD_NOT_CORRECT);
        }
        String encodedPassword = passwordEncoderPort.encode(changePasswordCommand.newPassword());
        account.setPassword(new Password(encodedPassword));
        accountRepositoryPort.saveAccount(account);
    }

    @Override
    public void sendForgotPasswordEmail(String email) {
        Optional<Account> account = accountRepositoryPort.findByEmail(email);
        if (account.isEmpty())
            return;
        //gen and save token
        String token = CryptoUtils.generateSecureTokenRaw();
        String hashToken = CryptoUtils.hash(token);
        cachePort.set(RedisCacheAdapter.forgotPasswordKey +hashToken,account.get().getId(),360);
        //send kafka event to email
        SendForgotPasswordPayload payload = new SendForgotPasswordPayload(
                email,account.get().getUsername(),token
        );
        EventMetadata eventMetadata = new EventMetadata(
                UUID.randomUUID().toString(),
                "FORGOT_PASSWORD",
                "user-identity-service",
                UUID.randomUUID().toString(),
                account.get().getId().toString(),
                Instant.now().toEpochMilli()
        );
        eventPublisherPort.publishEvent(new EventWrapper<>(eventMetadata,payload),"email_events");
    }

    @Override
    public void validateForgotPasswordToken(String token) {
        String hashToken = CryptoUtils.hash(token);
        String userId = cachePort.get(RedisCacheAdapter.forgotPasswordKey + hashToken).orElseThrow(
                ()->new AccountException(AccountErrorCode.INVALID_TOKEN)
        ).toString();
    }

    @Override
    public void resetPassword(ResetPasswordCommand resetPasswordCommand) {
        String hashToken = CryptoUtils.hash(resetPasswordCommand.token());
        String userId = cachePort.get(RedisCacheAdapter.forgotPasswordKey + hashToken).orElseThrow(
                ()->new AccountException(AccountErrorCode.INVALID_TOKEN)
        ).toString();
        Account account = accountRepositoryPort.findById(Long.valueOf(userId)).orElseThrow(
                ()->new AccountException(AccountErrorCode.ACCOUNT_NOT_FOUND)
        );
        String encodedPassword = passwordEncoderPort.encode(resetPasswordCommand.newPassword());
        account.increaseTokenVersion();
        account.setPassword(new Password(encodedPassword));
        accountRepositoryPort.saveAccount(account);
    }
}
