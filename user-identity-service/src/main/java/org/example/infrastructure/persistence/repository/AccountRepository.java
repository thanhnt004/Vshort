package org.example.infrastructure.persistence.repository;

import io.micrometer.observation.ObservationFilter;
import org.example.domain.entity.Account;
import org.example.infrastructure.persistence.entity.AccountJpaEntity;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import javax.swing.text.html.Option;
import java.util.Optional;


public interface AccountRepository extends JpaRepository<AccountJpaEntity,Long> {
    boolean existsAccountJpaEntityByEmail(String email);

    boolean existsAccountJpaEntityByPhoneNumber(String phoneNumber);

    boolean existsAccountJpaEntityByUsername(String username);

    Optional<AccountJpaEntity> findByEmail(String email);
    Optional<AccountJpaEntity> findByUsername(String userName);

    Optional<AccountJpaEntity> findByPhoneNumber(String identity);
}
