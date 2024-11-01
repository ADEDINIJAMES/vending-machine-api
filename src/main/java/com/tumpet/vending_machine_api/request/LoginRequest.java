package com.tumpet.vending_machine_api.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor

public class LoginRequest {
    @NotBlank(message = "Username must be present")
    private String username;
    @NotBlank(message = "password must be present")
    private String password;
}
