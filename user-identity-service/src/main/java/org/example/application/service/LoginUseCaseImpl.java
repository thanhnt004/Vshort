package org.example.application.service;

import lombok.RequiredArgsConstructor;
import org.example.application.dto.command.LoginCommand;
import org.example.application.dto.result.TokenResult;
import org.example.application.port.in.LoginAccountUseCase;
import org.example.application.port.out.AccountRepositoryPort;
import org.example.application.port.out.PasswordEncoderPort;
import org.example.application.port.out.TokenHandlerPort;
import org.example.domain.entity.Account;
import org.example.domain.entity.Permission;
import org.example.domain.entity.Role;
import org.example.domain.exception.AccountErrorCode;
import org.example.domain.exception.AccountException;
import org.example.domain.valueobject.Password;
import org.springframework.stereotype.Service;

import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class LoginUseCaseImpl implements LoginAccountUseCase {
    private final AccountRepositoryPort accountRepositoryPort;
    private final PasswordEncoderPort passwordEncoderPort;
    private final TokenHandlerPort tokenGeneratorPort;
    @Override
    public TokenResult execute(LoginCommand loginCommand) {
        Account account = accountRepositoryPort.findByIdentity(loginCommand.identity()).orElseThrow(
                ()->new AccountException(AccountErrorCode.ACCOUNT_NOT_FOUND)
        );
        //encode password
        var encodedPassword = passwordEncoderPort.encode(loginCommand.password());
        //domain business: check status and password
        account.canLogin();
        if (!passwordEncoderPort.matches(loginCommand.password(),account.getPassword().getHash()))
        {
            throw new AccountException(AccountErrorCode.INVALID_CREDENTIALS);
        }
        //generate pairs of token
        Set<String> permissionNames = account.getRoles().stream()
                .flatMap(role -> role.getPermissions().stream())
                .map(Permission::getName)
                .collect(Collectors.toSet());
        String accessToken = tokenGeneratorPort.generateAccessToken(account.getId().toString(),
                account.getRoles().stream().map(Role::getName).collect(Collectors.toSet()),
                permissionNames
                );
        String refreshToken = tokenGeneratorPort.generateRefreshToken(account.getId().toString(), String.valueOf(UUID.randomUUID()), Integer.toString(account.getTokenVersion()) );
        return TokenResult.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .expiresIn(tokenGeneratorPort.getAccessTokenExpiration())
                .build();
    }
}
