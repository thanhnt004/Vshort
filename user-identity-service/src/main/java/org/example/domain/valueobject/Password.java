package org.example.domain.valueobject;

import org.example.domain.exception.AccountErrorCode;
import org.example.domain.exception.AccountException;
import org.example.response.BaseErrorCode;

import java.util.Objects;

public class Password {
    private final String hash;

    public Password(String hash) {
        if (hash == null || hash.trim().isEmpty()) {
            throw new AccountException(AccountErrorCode.PASSWORD_REQUIRE);
        }
        this.hash = hash;
    }

    public String getHash() {
        return hash;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Password password = (Password) o;
        return Objects.equals(hash, password.hash);
    }

    @Override
    public int hashCode() {
        return Objects.hash(hash);
    }
}