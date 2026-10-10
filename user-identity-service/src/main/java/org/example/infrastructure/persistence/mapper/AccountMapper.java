package org.example.infrastructure.persistence.mapper;

import org.example.domain.entity.Account;
import org.example.domain.valueobject.Email;
import org.example.domain.valueobject.Password;
import org.example.infrastructure.persistence.entity.AccountJpaEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import javax.swing.text.html.Option;
import java.util.Optional;

@Mapper(componentModel = "spring", imports = {Email.class, Password.class})
public interface AccountMapper {

    // 1. Domain -> JPA Entity
    @Mapping(target = "email", source = "email.value")
    @Mapping(target = "passwordHash", source = "password.hash")
    AccountJpaEntity toJpaEntity(Account account);
    // 2. JPA Entity -> Domain
    @Mapping(target = "email", expression = "java(new Email(accountJpaEntity.getEmail()))")
    @Mapping(target = "password", expression = "java(new Password(accountJpaEntity.getPasswordHash()))")
    Account toDomainEntity(AccountJpaEntity accountJpaEntity);

}
