package dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class AccountCreatedEvent {
    private String accountId;
    private String username;
    private String email;
    private String emailVerifyToken;
}