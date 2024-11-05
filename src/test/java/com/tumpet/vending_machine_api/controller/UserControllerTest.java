package com.tumpet.vending_machine_api.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

import com.tumpet.vending_machine_api.enums.Role;
import com.tumpet.vending_machine_api.model.Users;

import com.tumpet.vending_machine_api.repository.UserRepository;
import com.tumpet.vending_machine_api.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.UUID;

@SpringBootTest
@AutoConfigureMockMvc
public class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private UserService userService;

    @Test
    @WithMockUser(username = "Peter", roles = {"BUYER"})
    public void depositFunds_shouldReturn200_whenAmountIsAllowedAndUserIsBuyer() throws Exception {
        Users buyerUser = new Users();
        buyerUser.setRole(Role.BUYER);
        buyerUser.setId(UUID.randomUUID());
        buyerUser.setEmail("adedinijames30@gmail.com");
        buyerUser.setFirstName("James");
        buyerUser.setLastName("Peter");
        buyerUser.setUsername("Peter");
        buyerUser.setPassword(passwordEncoder.encode("Peter"));
        buyerUser.setBalance(BigDecimal.valueOf(100));
        Users savedUser = userRepository.save(buyerUser);

       mockMvc.perform(post("/api/v1/user/deposit")
                        .param("amount", "50")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("User balance updated from 100 to 150"));
    }

    @Test
    @WithMockUser(username = "testuser", roles = {"ADMIN"})
    public void depositFunds_shouldReturn403_whenUserIsNotBuyer() throws Exception {
        // Arrange: Create a user with a role other than BUYER
        Users adminUser = new Users();
        adminUser.setRole(Role.ADMIN);
        adminUser.setBalance(BigDecimal.valueOf(100));
        userRepository.save(adminUser);

        // Act & Assert: Test deposit endpoint expecting a 403 response
        mockMvc.perform(post("/deposit")
                        .param("amount", "50")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").value("Only users with 'buyer' role can deposit funds."));
    }

    @Test
    @WithMockUser(username = "myuser", roles = {"BUYER"})
    public void depositFunds_shouldReturn400_whenAmountIsNotAllowed() throws Exception {
        // Arrange: Create a user with BUYER role
        Users buyerUser = new Users();
        buyerUser.setRole(Role.BUYER);
        buyerUser.setBalance(BigDecimal.valueOf(100));
        userRepository.save(buyerUser);

        mockMvc.perform(post("/deposit")
                        .param("amount", "3") // Assuming 3 is not in ALLOWED_DENOMINATION
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Invalid denomination"));
    }
}
