package com.tumpet.vending_machine_api.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tumpet.vending_machine_api.controller.UserController;
import com.tumpet.vending_machine_api.dto.UserDto;
import com.tumpet.vending_machine_api.enums.Role;
import com.tumpet.vending_machine_api.exceptions.UserNotFoundException;
import com.tumpet.vending_machine_api.model.Users;
import com.tumpet.vending_machine_api.repository.ProductRepository;
import com.tumpet.vending_machine_api.repository.UserRepository;
import com.tumpet.vending_machine_api.request.LoginRequest;
import com.tumpet.vending_machine_api.request.UserRequest;
import com.tumpet.vending_machine_api.request.UserUpdateRequest;
import com.tumpet.vending_machine_api.responses.ApiResponse;
import com.tumpet.vending_machine_api.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import org.glassfish.jaxb.runtime.v2.runtime.BinderImpl;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.core.Authentication;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import static com.tumpet.vending_machine_api.enums.Role.ADMIN;
import static org.mockito.ArgumentMatchers.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UserController.class)
@AutoConfigureMockMvc(addFilters = false)// Disable  security filters
 public class UserControllerTest {

    @Autowired
    MockMvc mockMvc;

    @MockBean
    private ProductRepository productRepository;
    @MockBean
    private  JavaMailSender mailSender;

    @MockBean
    private UserRepository userRepository;

    @MockBean
    private  RabbitTemplate rabbitTemplate;
    @MockBean
    private UserService authService;

    @Test
    void testRegisterUserMethod() throws Exception {
        // Arrange: Prepare UserRequest
        UserRequest request = new UserRequest();
        request.setEmail("adedinijames28@gmail.com");
        request.setUsername("adedinijames28@gmail.com");
        request.setFirstName("James");
        request.setLastName("Adedini");
        request.setPassword("IamJames");

        // Mock the service response
        Mockito.when(authService.registerUser(any(UserRequest.class), eq(Role.BUYER)))
                .thenReturn(ApiResponse.builder()
                        .message("User Registered successfully")
                        .timestamp(LocalDateTime.now())
                        .status(201)
                        .build());

        // Act: Perform POST request and assert response
        mockMvc.perform(post("/api/v1/user")
                        .param("role", "BUYER") // Mock role as parameter
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.message").value("User Registered successfully"));





    }
    @Test
    void TestLogin() throws Exception {
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setPassword("Iamjay");
        loginRequest.setUsername("adedinijames28@gmail.com");
        Mockito.when(authService.loginUser(any(LoginRequest.class)))
                .thenReturn(ApiResponse.builder()
                                .status(200)
                                .message("User Login successful")
                                .timestamp(LocalDateTime.now())
                                .build()
                        );
        mockMvc.perform(post("/api/v1/user/login").contentType(MediaType.APPLICATION_JSON).content(new ObjectMapper().writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("User Login successful"));
    }

    @Test
    @WithMockUser(username="adedinijames28@gmail.com", roles = {"BUYER"})
    void testUpdateUser() throws Exception {
        // Arrange
        UUID id = UUID.randomUUID();
        UserUpdateRequest userUpdateRequest = new UserUpdateRequest();
        userUpdateRequest.setFirstName("James");
        userUpdateRequest.setLastName("Peter");
        userUpdateRequest.setEmail("james.peter@example.com");

        ApiResponse<Object> apiResponse = ApiResponse.builder()
                .message("User Updated Successfully")
                .status(200)
                .timestamp(LocalDateTime.now())
                .build();

        Mockito.when(authService.updateUser(any(UserUpdateRequest.class), eq(id)))
                .thenReturn(apiResponse);

        // Act & Assert
        mockMvc.perform(MockMvcRequestBuilders.patch("/api/v1/user/update/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(userUpdateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("User Updated Successfully"));
    }

    @Test
    @WithMockUser(username = "adedinijames28@gmail.com", roles = {"ADMIN"})
    void TestDeleteById () throws Exception {
        UUID id = UUID.randomUUID();
        Mockito.when(authService.deleteUser(any(UUID.class))).thenReturn(
                ApiResponse.builder()
                        .status(200)
                        .message("User Deleted Successfully")
                        .timestamp(LocalDateTime.now())
                        .build());
        mockMvc.perform(MockMvcRequestBuilders.delete("/api/v1/user/delete/{id}",id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("User Deleted Successfully"));
    }

    @Test
    @WithMockUser(username = "adedinijames28@gmail.com", roles = {"ADMIN"})
    void testGetUser() throws Exception {
        UUID id = UUID.randomUUID();

        Mockito.when(authService.getUser(any(UUID.class)))
                .thenReturn(
                        ApiResponse.builder()
                                .status(200)
                                .timestamp(LocalDateTime.now())
                                .build());
        mockMvc.perform(get("/api/v1/user/{id}",id))
                .andExpect(status().isOk());

    }

    @Test
    @WithMockUser(username = "adedinijames28@gmail.com", roles = {"ADMIN"})
    void testGetAllUser () throws Exception {
Mockito.when(authService.getAllUsers(any(int.class), any(int.class), any(String.class), any(String.class))).thenReturn(
        ApiResponse.builder()
                .status(200)
                .timestamp(LocalDateTime.now())
                .build());
mockMvc.perform(get("/api/v1/user/all")).andExpect(status().isOk());
    }


    @Test
    @WithMockUser(username = "adedinijames28@gmail.com", roles = {"ADMIN"})
    void testLogout () throws Exception {
        Mockito.when(authService.logoutAll(any(), any()))
                .thenReturn(
                        ApiResponse.builder()
                                .timestamp(LocalDateTime.now())
                                .status(200)
                                .message("Logout Successful")
                                .build());
        mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/user/logout/all")
                        .with(csrf())).andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Logout Successful"));
    }



    @Test
    @WithMockUser(username = "adedinijames28@gmail.com", roles = {"BUYER"})
    void testDeposit() throws Exception {
        int amount = 50;

        Users mockUser = new Users();
        mockUser.setFirstName("James");
        mockUser.setBalance(BigDecimal.valueOf(2000));
        mockUser.setEmail("adedinijames28@gmail.com");
        mockUser.setLastName("Peter");
        mockUser.setUsername("adedinijames28@gmail.com");
        mockUser.setPassword("Iamjj");
        Users savedMok= userRepository.save(mockUser);


        // Initialize as needed

        ApiResponse<Object> mockResponse = ApiResponse.builder()
                .status(200)
                .message("Deposit successful")
                .timestamp(LocalDateTime.now())
                .build();

        Mockito.when(authService.depositFund(amount, savedMok)).thenReturn(mockResponse);

        mockMvc.perform(post("/api/v1/user/deposit")
                        .param("amount", String.valueOf(amount))
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Deposit successful"));
    }

    @Test
    @WithMockUser(username = "adedinijames28@gmail.com", roles = {"BUYER"})
    void testResetBalance () throws Exception {
Users users= new Users();
users.setPassword("IamJ");
users.setBalance(BigDecimal.valueOf(200));
users.setEmail("adedinijames28@gmail.com");
users.setUsername("adedinijames28@gmail.com");
users.setLastName("James");
Users mockUser = userRepository.save(users);

        Mockito.when(authService.resetBalance(mockUser)).thenReturn(
                ApiResponse.builder()
                        .message("Reset Successful")
                        .status(200)
                        .timestamp(LocalDateTime.now())
                        .build());
        mockMvc.perform(post("/api/v1/user/reset"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Reset Successful"));


    }

    @Test
    @WithMockUser(username = "adedinijames28@gmail.com", roles = {"BUYER"})
    void testResetBalance2() throws Exception {
        UUID id = UUID.randomUUID();
        Users users= new Users();
        users.setPassword("IamJ");
        users.setBalance(BigDecimal.valueOf(200));
        users.setEmail("adedinijames28@gmail.com");
        users.setUsername("adedinijames28@gmail.com");
        users.setLastName("James");
        Users mockUser = userRepository.save(users);

        Mockito.when(authService.resetBalances(eq(mockUser), anyInt(), any(UUID.class))).thenReturn("Reset successful");
        mockMvc.perform(post("/api/v1/user/balance-reset/{id}",id).param("amount", String.valueOf(50))).andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Reset successful"));
    }






    public UserDto mapUserToUserDto(Users users) {
        return UserDto.builder()
                .id(users.getId())
                .email(users.getEmail())
                .firstName(users.getFirstName())
                .username(users.getUsername())
                .balance(users.getBalance())
                .role(users.getRole())
                .lastName(users.getLastName())
                .build();
    }
}
