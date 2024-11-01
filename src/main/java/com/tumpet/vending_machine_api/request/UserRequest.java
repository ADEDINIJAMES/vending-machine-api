package com.tumpet.vending_machine_api.request;

import jakarta.persistence.Column;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserRequest {
    private String firstName;
    @NotBlank( message = "Lastname is required")
    private String lastName;
    @NotBlank( message = "Username is required")
    @Column(unique = true)
    private String username;
    @NotBlank( message = "Password is required")
    private String password;
    @NotBlank( message = "Email is required")
    @Column(unique = true)
    @Email(message = "Email should be valid")
    private String email;
}
