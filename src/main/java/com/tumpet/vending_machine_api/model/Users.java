package com.tumpet.vending_machine_api.model;

import com.tumpet.vending_machine_api.enums.Role;
import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import javax.annotation.processing.Generated;
import java.math.BigDecimal;
import java.util.UUID;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
@EqualsAndHashCode
public class Users {
    @Id
    private UUID id = UUID.randomUUID();
    @NotBlank( message = "Firstname is required")
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
    @NotNull(message = "Balance is required")
    @DecimalMin(value = "0.0", inclusive = true, message = "Balance cannot be negative")
    private BigDecimal balance = BigDecimal.ZERO;

    @NotNull(message = "Role is required")
    @Enumerated(EnumType.STRING)
    private Role role;
}
