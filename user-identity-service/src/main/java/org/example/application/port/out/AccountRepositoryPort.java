package org.example.application.port.out;

import org.example.domain.entity.Account;

import java.util.Optional;

public interface AccountRepositoryPort {
    //validate
    boolean isEmailExisted(String email);
    boolean isUsernameExisted(String userName);
    boolean isPhoneExisted(String phoneNumber);
    //save
    void saveAccount(Account account);
    //find
    Optional<Account> findById(Long id);
    Optional<Account> findByEmail(String email);
    Optional<Account> findByUserName(String userName);
    Optional<Account> findByIdentity(String identity);
}
