package org.example.infrastructure.adapter;

import lombok.RequiredArgsConstructor;
import org.example.application.port.out.AccountRepositoryPort;
import org.example.constant.RegexConstants;
import org.example.domain.entity.Account;
import org.example.infrastructure.persistence.entity.AccountJpaEntity;
import org.example.infrastructure.persistence.mapper.AccountMapper;
import org.example.infrastructure.persistence.repository.AccountRepository;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.regex.Pattern;

@Component
@RequiredArgsConstructor
public class AccountRepositoryAdapter implements AccountRepositoryPort {
    private static final Pattern EMAIL_PATTERN = Pattern.compile(RegexConstants.EMAIL_PATTERN);
    private static final Pattern PHONE_PATTERN = Pattern.compile(RegexConstants.PHONE_VN_PATTERN);
    private final AccountRepository accountRepository;
    private final AccountMapper accountMapper;
    @Override
    public boolean isEmailExisted(String email) {
        return accountRepository.existsAccountJpaEntityByEmail(email);
    }

    @Override
    public boolean isUsernameExisted(String userName) {
        return accountRepository.existsAccountJpaEntityByUsername(userName);
    }

    @Override
    public boolean isPhoneExisted(String phoneNumber) {
        return accountRepository.existsAccountJpaEntityByPhoneNumber(phoneNumber);
    }

    @Override
    public void saveAccount(Account account) {
        accountRepository.save(accountMapper.toJpaEntity(account));
    }

    @Override
    public Optional<Account> findById(Long id) {
        return accountRepository.findById(id)
                .map(accountMapper::toDomainEntity);
    }

    @Override
    public Optional<Account> findByEmail(String email) {
        return accountRepository.findByEmail(email)
                .map(accountMapper::toDomainEntity);
    }

    @Override
    public Optional<Account> findByUserName(String userName) {
        return accountRepository.findByUsername(userName)
                .map(accountMapper::toDomainEntity);
    }

    @Override
    public Optional<Account> findByIdentity(String identity) {
        Optional<AccountJpaEntity> accountJpaEntity;
       if (EMAIL_PATTERN.matcher(identity).matches())
       {
           accountJpaEntity = accountRepository.findByEmail(identity);
       }
       else if (PHONE_PATTERN.matcher(identity).matches())
           accountJpaEntity = accountRepository.findByPhoneNumber(identity);
       else
           accountJpaEntity = accountRepository.findByUsername(identity);
        return accountJpaEntity.map(accountMapper::toDomainEntity);
    }
}
