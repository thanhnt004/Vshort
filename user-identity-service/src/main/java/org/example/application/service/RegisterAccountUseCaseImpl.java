package org.example.application.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.example.application.dto.command.RegisterCommand;
import org.example.application.dto.eventpayload.AccountCreatedPayload;
import org.example.application.dto.result.RegisterResult;
import org.example.application.port.in.RegisterAccountUseCase;
import org.example.application.port.out.*;
import org.example.domain.entity.Account;
import org.example.domain.entity.Role;
import org.example.domain.exception.AccountErrorCode;
import org.example.domain.exception.AccountException;
import org.example.domain.valueobject.Email;
import org.example.domain.valueobject.Password;
import org.example.infrastructure.cache.RedisCacheAdapter;
import org.example.infrastructure.persistence.entity.OutBoxEventJpaEntity;
import org.example.utils.CryptoUtils;
import org.springframework.stereotype.Service;
import tools.jackson.databind.ObjectMapper;

import java.time.Instant;
import java.util.HashSet;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class RegisterAccountUseCaseImpl implements RegisterAccountUseCase {
    private final AccountRepositoryPort accountRepositoryPort;
    private final PasswordEncoderPort passwordEncoderPort;
    private final IdGeneratorPort idGeneratorPort;
    private final RoleRepositoryPort roleRepositoryPort;
    private final OutBoxEventRepositoryPort eventRepositoryPort;
    private final ObjectMapper objectMapper;
    private final CachePort cachePort;
    @Override
    @Transactional
    @SneakyThrows
    public RegisterResult execute(RegisterCommand command) {
        //validate
        if (accountRepositoryPort.isEmailExisted(command.email()))
            throw new AccountException(AccountErrorCode.EMAIL_EXISTS);
        if (accountRepositoryPort.isPhoneExisted(command.phoneNumber()))
            throw new AccountException(AccountErrorCode.PHONE_EXISTS);
        if (accountRepositoryPort.isUsernameExisted(command.username()))
            throw new AccountException(AccountErrorCode.USER_NAME_EXISTS);
        //hash password
        String encodedPassword = passwordEncoderPort.encode(command.password());
        //generate id
        Long id = idGeneratorPort.generateId();
        //get defautl role
        Role defaultRole = roleRepositoryPort.getDefailtRole();
        Account newAccount = Account.create(id,
                command.username(),
                new Email(command.email()),
                command.phoneNumber(),
                new Password(encodedPassword),
                defaultRole == null?new HashSet<>():Set.of(defaultRole)
        );
        //luu tai khoan
        accountRepositoryPort.saveAccount(newAccount);
        //lưu outbox event
        String emailVerifyToken = CryptoUtils.generateSecureTokenRaw();
        String hashToken = CryptoUtils.hash(emailVerifyToken);
        AccountCreatedPayload eventPayload = new AccountCreatedPayload(
                newAccount.getId().toString(),
                newAccount.getUsername(),
                newAccount.getEmail().getValue(),
                emailVerifyToken
        );
        cachePort.set(RedisCacheAdapter.verifyEmailKey +hashToken,newAccount.getId(),360);
        String payloadJson = objectMapper.writeValueAsString(eventPayload);
        eventRepositoryPort.saveEvent("account",newAccount.getId().toString(),payloadJson,"ACCOUNT_CREATED");
        return new RegisterResult(id.toString(), command.username(), command.email());
    }

}
