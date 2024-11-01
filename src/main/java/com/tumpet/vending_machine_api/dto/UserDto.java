package com.tumpet.vending_machine_api.dto;

import com.tumpet.vending_machine_api.enums.Role;
import jakarta.persistence.Column;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserDto {
    private UUID id;
    @NotBlank( message = "Firstname is required")
    private String firstName;
    @NotBlank( message = "Lastname is required")
    private String lastName;
    @NotBlank( message = "Username is required")
    @Column(unique = true)
    private String username;
    @NotBlank( message = "Email is required")
    @Column(unique = true)
    @Email(message = "Email should be valid")
    private String email;
    @NotNull(message = "Balance is required")
    @DecimalMin(value = "0.0", inclusive = true, message = "Balance cannot be negative")
    private BigDecimal balance = BigDecimal.ZERO;

    @NotNull(message = "Role is required")
    private Role role;
}
