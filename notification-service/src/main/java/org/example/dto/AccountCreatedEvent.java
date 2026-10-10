package org.example.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class AccountCreatedEvent {
    @JsonProperty("accountId")
    @JsonAlias({"accountId", "account_id"})
    private String accountId;

    private String username;

    private String email;

    @JsonProperty("emailVerifyToken")
    @JsonAlias({"emailVerifyToken", "verifyToken", "email_verify_token", "verify_token"})
    private String emailVerifyToken;
}