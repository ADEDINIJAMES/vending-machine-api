package com.tumpet.vending_machine_api.serviceImp;

import com.tumpet.vending_machine_api.dto.UserDto;
import com.tumpet.vending_machine_api.enums.Role;
import com.tumpet.vending_machine_api.exceptions.UserNotFoundException;
import com.tumpet.vending_machine_api.model.Users;
import com.tumpet.vending_machine_api.repository.UserRepository;
import com.tumpet.vending_machine_api.request.LoginRequest;
import com.tumpet.vending_machine_api.request.UserRequest;
import com.tumpet.vending_machine_api.request.UserUpdateRequest;
import com.tumpet.vending_machine_api.responses.ApiResponse;
import com.tumpet.vending_machine_api.util.JwtUtils;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataAccessException;
import org.springframework.data.domain.*;
import org.springframework.data.mapping.PropertyReferenceException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;

import javax.swing.plaf.basic.BasicInternalFrameTitlePane;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)

class UserServiceImplTest {
    @Mock
    private UserRepository userRepository;
    @Mock
    private JwtUtils jwtUtils;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private final Map<String, String> activeSessions = new ConcurrentHashMap<>();
    private static final String DENOMINATION_ERROR = "You can only deposit 50, 100, 200, 500, or 1,000 denominations.";

    @InjectMocks
    private UserServiceImpl userService;


    @Test
    void testLoadUserByUsername() {
        // Arrange
        Users users = new Users();
        users.setUsername("adedinijames28@gmail.com");
        users.setLastName("James");
        users.setEmail("adedinijames28@gmail.com");
        users.setPassword("MyPass");

        String username = "adedinijames28@gmail.com";

        Mockito.when(userRepository.findByUsername(Mockito.eq(users.getUsername()))).thenReturn(Optional.of(users));

        //Act
        UserDetails userDetails = userService.loadUserByUsername(username);


//Assert
        Assertions.assertNotNull(userDetails);
        Assertions.assertEquals(username, userDetails.getUsername());

    }

    @Test
    void testUsernameNotFound() {

        //Arrange
        String username = "adedinijames28";
        Mockito.when(userRepository.findByUsername(Mockito.any())).thenReturn(Optional.empty());

        //Act and //Assert

        Assertions.assertThrows(UsernameNotFoundException.class, () -> {
            userService.loadUserByUsername(username);
        });

    }

    @Test
    void testRegisterUserSuccessful() {

        //Arrange
        UserRequest userRequest = new UserRequest();
        userRequest.setPassword("mypass");
        userRequest.setFirstName("James");
        userRequest.setLastName("Peter");
        userRequest.setUsername("adedinijames28@gmail.com");
        userRequest.setEmail("adedinijames28@gmail.com");

        Mockito.when(userRepository.existsByEmail(Mockito.any(String.class))).thenReturn(false);
        Mockito.when(userRepository.existsByUsername(Mockito.eq(userRequest.getUsername()))).thenReturn(false);
        Mockito.when(passwordEncoder.encode(userRequest.getPassword())).thenReturn("Encodedpassword");

        Users users = new Users();
        users.setEmail(userRequest.getEmail());
        users.setPassword(passwordEncoder.encode(userRequest.getPassword()));
        users.setFirstName(userRequest.getFirstName());
        users.setLastName(userRequest.getLastName());
        users.setUsername(userRequest.getUsername());
        users.setRole(Role.BUYER);


        Mockito.when(userRepository.save(Mockito.any(Users.class))).thenReturn(users);

        //Act

        ApiResponse<Object> response = userService.registerUser(userRequest, Role.BUYER);

// ASSERT
        Assertions.assertEquals(201, response.getStatus());
        Assertions.assertEquals("User registered successfully", response.getMessage());
        Assertions.assertNotNull(response.getData());

    }

    @Test
    void testRegisterUserNotSuccessfulUserNameOrEmailExist() {

        //Arrange
        UserRequest userRequest = new UserRequest();
        userRequest.setPassword("mypass");
        userRequest.setFirstName("James");
        userRequest.setLastName("Peter");
        userRequest.setUsername("adedinijames28@gmail.com");
        userRequest.setEmail("adedinijames28@gmail.com");

        Mockito.when(userRepository.existsByEmail(Mockito.any(String.class)) || userRepository.existsByUsername(Mockito.any(String.class))).thenReturn(true);
//        Mockito.when(userRepository.existsByUsername(Mockito.eq(userRequest.getUsername()))).thenReturn(true);
        Mockito.when(passwordEncoder.encode(userRequest.getPassword())).thenReturn("Encodedpassword");

        Users users = new Users();
        users.setEmail(userRequest.getEmail());
        users.setPassword(passwordEncoder.encode(userRequest.getPassword()));
        users.setFirstName(userRequest.getFirstName());
        users.setLastName(userRequest.getLastName());
        users.setUsername(userRequest.getUsername());
        users.setRole(Role.BUYER);


//        Mockito.when(userRepository.save(Mockito.any(Users.class))).thenReturn(users);

        //Act

        ApiResponse<Object> response = userService.registerUser(userRequest, Role.BUYER);

        Assertions.assertEquals(401, response.getStatus());
        Assertions.assertEquals("username or email already registered", response.getMessage());
        Assertions.assertNull(response.getData());


    }

    @Test
    void testLoginUserSuccessful() {
        //Arrange
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setUsername("adedinijames28@gmail.com");
        loginRequest.setPassword("myPasss");

        Users mockUsers = Users.builder()
                .email("adedinijames28@gmail.com")
                .role(Role.BUYER)
                .email("adedinijames28@gmail.com")
                .username("adedinijames28@gmail.com")
                .lastName("Jamae")
                .firstName("James")
                .build();

        Mockito.when(userRepository.findByUsername(Mockito.any(String.class))).thenReturn(Optional.of(mockUsers));
        Mockito.when(passwordEncoder.matches(Mockito.eq(loginRequest.getPassword()), Mockito.eq(mockUsers.getPassword()))).thenReturn(true);
//    Mockito.when(activeSessions.containsKey(loginRequest.getUsername())).thenReturn(false);
        Mockito.when(jwtUtils.jwtCreate(Mockito.any())).thenReturn("mockedJwtToken");

        //Act
        ApiResponse<Object> apiResponse = userService.loginUser(loginRequest);

        //Assert
        Assertions.assertNotNull(apiResponse.getData());
        Assertions.assertEquals("Login Successful", apiResponse.getMessage());
        Assertions.assertEquals(200, apiResponse.getStatus());
    }

    @Test
    void testLoginUser_InCorrectPassword() {
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setUsername("adedinijames28@gmail.com");
        loginRequest.setPassword("myPasss");

        Users mockUser = new Users();

        Mockito.when(userRepository.findByUsername(Mockito.any(String.class))).thenReturn(Optional.of(mockUser));
        Mockito.when(passwordEncoder.matches(Mockito.eq(loginRequest.getPassword()), Mockito.eq(mockUser.getPassword()))).thenReturn(false);

        //Act
        ApiResponse<Object> apiResponse = userService.loginUser(loginRequest);

        //Assert
        Assertions.assertEquals("Password or Username incorrect", apiResponse.getMessage());
        Assertions.assertEquals(403, apiResponse.getStatus());
    }

    @Test
    void testUpdateUser() throws UserNotFoundException {
        UUID id = UUID.randomUUID();
        UserUpdateRequest request = new UserUpdateRequest();
        request.setEmail("adedinijames@gmail.com");
        request.setFirstName("Busayomi");
        request.setLastName("Toba");

        Users mockUsers = Users.builder()
                .firstName("James")
                .lastName("Peter")
                .role(Role.BUYER)
                .username("adedinijames20@gmail.com")
                .email("adedinijames20@gmail.com").build();

        Authentication authentication = Mockito.mock(Authentication.class);
        SecurityContextHolder.getContext().setAuthentication(authentication);
        UserDetails userDetails = Mockito.mock(UserDetails.class);
        Mockito.when(userRepository.findByUsername(Mockito.any(String.class))).thenReturn(Optional.of(mockUsers));
        Mockito.when(userRepository.findById(Mockito.any(UUID.class))).thenReturn(Optional.of(mockUsers));
        Mockito.when(authentication.getPrincipal()).thenReturn(userDetails);
        Mockito.when(userDetails.getUsername()).thenReturn(mockUsers.getUsername());

        Users updated = Users
                .builder()
                .lastName(request.getLastName())
                .email(request.getEmail())
                .firstName(request.getFirstName())
                .build();

        Mockito.when(userRepository.save(Mockito.any(Users.class))).thenReturn(updated);

//Act

        ApiResponse<Object> apiResponse = userService.updateUser(request, id);

        //Assert


        Assertions.assertEquals("User details updated successfully", apiResponse.getMessage());
        Assertions.assertEquals(200, apiResponse.getStatus());
        Assertions.assertNotNull(apiResponse.getData());

    }

    @Test
    void testUpdateUser_InvalidRequest() {
        // Arrange
        UUID id = UUID.randomUUID();
        UserUpdateRequest request = null;
        Users mockUsers = new Users();
        mockUsers.setUsername("adedinijames28@gmail.com");

//        Authentication authentication = Mockito.mock(Authentication.class);
//        SecurityContextHolder.getContext().setAuthentication(authentication);
//        UserDetails userDetails = Mockito.mock(UserDetails.class);
//        Mockito.when(userRepository.findByUsername(Mockito.any(String.class))).thenReturn(Optional.of(mockUsers));
//        Mockito.when(userRepository.findById(Mockito.any(UUID.class))).thenReturn(Optional.of(mockUsers));
//        Mockito.when(authentication.getPrincipal()).thenReturn(userDetails);

        // Act
        ApiResponse<Object> response = userService.updateUser(request, id);

        // Assert
        Assertions.assertEquals(401, response.getStatus());
        Assertions.assertEquals("Please enter the correct details", response.getMessage());
    }

    @Test
    void testMapUserToUserDTo_Success() {
        //Arrange
        Users mockUsers = new Users();
        mockUsers.setUsername("adedinijames28@gmail.com");
        mockUsers.setPassword("adedinijames");

        UserDto userDto = userService.mapUserToUserDto(mockUsers);

        //Assert
        Assertions.assertEquals(mockUsers.getUsername(), userDto.getUsername());

    }

    @Test
    void testLogout_AllSuccess() {

        Users mockUsers = Users.builder()
                .firstName("James")
                .lastName("Peter")
                .role(Role.BUYER)
                .username("adedinijames20@gmail.com")
                .email("adedinijames20@gmail.com").build();

        HttpServletRequest request = Mockito.mock(HttpServletRequest.class);
        HttpSession session = Mockito.mock(HttpSession.class);
        Authentication authentication = Mockito.mock(Authentication.class);
        SecurityContextHolder.getContext().setAuthentication(authentication);
        UserDetails userDetails = Mockito.mock(UserDetails.class);
        Mockito.when(authentication.getPrincipal()).thenReturn(userDetails);
        Mockito.when(userDetails.getUsername()).thenReturn(mockUsers.getUsername());
        Mockito.when(request.getSession()).thenReturn(session);
        ApiResponse<Object> apiResponse = userService.logoutAll(authentication, request);


        Mockito.verify(session).invalidate();
        Assertions.assertEquals("Logout Successful", apiResponse.getMessage());
        Assertions.assertEquals(200, apiResponse.getStatus());

    }

    @Test
    void testLogoutUnsuccessfulAuthenticationNull() {
        Authentication authentication = null;
        SecurityContextHolder.getContext().setAuthentication(authentication);
        HttpServletRequest request = Mockito.mock(HttpServletRequest.class);

        ApiResponse<Object> apiResponse = userService.logoutAll(authentication, request);

        Assertions.assertEquals(403, apiResponse.getStatus());
        Assertions.assertEquals("User not logged in", apiResponse.getMessage());

    }

    @Test
    void TestDeleteUserSuccessful() {
        UUID uuid = UUID.randomUUID();

        Users mockUsers = Users.builder()
                .firstName("James")
                .lastName("Peter")
                .role(Role.ADMIN)
                .username("adedinijames20@gmail.com")
                .email("adedinijames20@gmail.com").build();


        Authentication authentication = Mockito.mock(Authentication.class);
        SecurityContextHolder.getContext().setAuthentication(authentication);
        UserDetails userDetails = Mockito.mock(UserDetails.class);
        Mockito.when(userDetails.getUsername()).thenReturn(mockUsers.getUsername());
        Mockito.when(userRepository.findByUsername(Mockito.any(String.class))).thenReturn(Optional.of(mockUsers));
        Mockito.when(userRepository.findById(Mockito.any(UUID.class))).thenReturn(Optional.of(mockUsers));
        Mockito.when(authentication.getPrincipal()).thenReturn(userDetails);
//        Mockito.verify(userRepository).delete(mockUsers);

        ApiResponse<Object> apiResponse = userService.deleteUser(uuid);

        Assertions.assertEquals(200, apiResponse.getStatus());
        Assertions.assertEquals("Deletion successful", apiResponse.getMessage());

    }


    @Test
    void TestDeleteUserNotSuccessfulNotPermitted() {
        UUID uuid = UUID.randomUUID();

        Users mockUsers = Users.builder()
                .firstName("James")
                .lastName("Peter")
                .role(Role.BUYER) // here
                .username("adedinijames20@gmail.com")
                .email("adedinijames20@gmail.com").build();


        Authentication authentication = Mockito.mock(Authentication.class);
        SecurityContextHolder.getContext().setAuthentication(authentication);
        UserDetails userDetails = Mockito.mock(UserDetails.class);
        Mockito.when(userDetails.getUsername()).thenReturn(mockUsers.getUsername());
        Mockito.when(userRepository.findByUsername(Mockito.any(String.class))).thenReturn(Optional.of(mockUsers));
//        Mockito.when(userRepository.findById(Mockito.any(UUID.class))).thenReturn(Optional.of(mockUsers));
        Mockito.when(authentication.getPrincipal()).thenReturn(userDetails);
//        Mockito.verify(userRepository).delete(mockUsers);

        ApiResponse<Object> apiResponse = userService.deleteUser(uuid);

        Assertions.assertEquals(401, apiResponse.getStatus());
        Assertions.assertEquals("You are not permitted !!", apiResponse.getMessage());

    }

    @Test
    void TestDeleteUserNotSuccessfulNotInternalServer() {
        UUID uuid = UUID.randomUUID();

        Users mockUsers = Users.builder()
                .firstName("James")
                .lastName("Peter")
                .role(Role.ADMIN)
                .username("adedinijames20@gmail.com")
                .email("adedinijames20@gmail.com").build();


        Authentication authentication = Mockito.mock(Authentication.class);
        SecurityContextHolder.getContext().setAuthentication(authentication);
        UserDetails userDetails = Mockito.mock(UserDetails.class);
        Mockito.when(userDetails.getUsername()).thenReturn(mockUsers.getUsername());
        Mockito.when(userRepository.findByUsername(Mockito.any(String.class))).thenReturn(Optional.of(mockUsers));
        Mockito.when(userRepository.findById(Mockito.any(UUID.class))).thenReturn(null); // Here
        Mockito.when(authentication.getPrincipal()).thenReturn(userDetails);
//        Mockito.verify(userRepository).delete(mockUsers);

        ApiResponse<Object> apiResponse = userService.deleteUser(uuid);

        Assertions.assertEquals(500, apiResponse.getStatus());
        Assertions.assertEquals("AN ERROR OCCURRED", apiResponse.getMessage());
    }

    @Test
    void testGetAllUsers() {

        List<Users> mockusesList = List.of(
                Users.builder()
                        .firstName("James")
                        .lastName("Peter")
                        .role(Role.ADMIN)
                        .username("adedinijames20@gmail.com")
                        .email("adedinijames20@gmail.com").build());

        int pageNumber = 1;
        int pageSize = 5;
        String sortBy = "firstName";
        String sortDirection = "ASC";

        Pageable pageable = PageRequest.of(pageNumber, pageSize, Sort.by(sortBy).ascending());
        Mockito.when(userRepository.findAll(pageable)).thenReturn(new PageImpl<Users>(mockusesList));


        ApiResponse<Object> apiResponse = userService.getAllUsers(pageNumber, pageSize, sortBy, sortDirection);

        Assertions.assertEquals(200, apiResponse.getStatus());
        Assertions.assertEquals("Users fetched successfully", apiResponse.getMessage());
    }

    @Test
    void testGetAllUsersNotSuccessfulIllegalArgumentExceptionOrNullPointerExceptionOrPropertyReferenceException() {
        int pageNumber = 1;
        int pageSize = 5;
        String sortBy = "firstName";
        String sortDirection = "ASC";

//    Pageable pageable = PageRequest.of(pageNumber,pageSize,Sort.by(sortBy).ascending());
        Mockito.when(userRepository.findAll(Mockito.any(Pageable.class))).thenReturn(null);


        ApiResponse<Object> apiResponse = userService.getAllUsers(pageNumber, pageSize, sortBy, sortDirection);

        Assertions.assertEquals(400, apiResponse.getStatus());
        Assertions.assertTrue(apiResponse.getMessage().contains("Invalid request parameters"));
        Mockito.verify(userRepository, Mockito.times(1)).findAll(Mockito.any(Pageable.class));
    }

    @Test
    void testGetAllUsersNotSuccessfulDatabaseError() {
        int pageNumber = 1;
        int pageSize = 5;
        String sortBy = "firstName";
        String sortDirection = "ASC";

//    Pageable pageable = PageRequest.of(pageNumber,pageSize,Sort.by(sortBy).ascending());
        Mockito.when(userRepository.findAll(Mockito.any(Pageable.class))).thenThrow(new DataAccessException("Database connection failed") {
        });

        ApiResponse<Object> apiResponse = userService.getAllUsers(pageNumber, pageSize, sortBy, sortDirection);

        Assertions.assertEquals(500, apiResponse.getStatus());
        Assertions.assertTrue(apiResponse.getMessage().contains("Database error"));
        Mockito.verify(userRepository, Mockito.times(1)).findAll(Mockito.any(Pageable.class));
    }

    @Test
    void testGetAllUsers_InvalidPaginationParameters() {
        // Arrange
        int pageNumber = -1; // Invalid page number
        int pageSize = 10;
        String sortBy = "firstName";
        String sortDirection = "ASC";

        // Act
        ApiResponse<Object> response = userService.getAllUsers(pageNumber, pageSize, sortBy, sortDirection);

        // Assert
        Assertions.assertEquals(400, response.getStatus());
        Assertions.assertTrue(response.getMessage().contains("Invalid request parameters"));
    }


    @Test
    void testDeposit_Success() {
        int amount = 200;
        Users mockUser = Users.builder()
                .id(UUID.randomUUID())
                .balance(BigDecimal.ZERO)
                .email("adedinijames28@gmail.com")
                .firstName("James")
                .role(Role.BUYER)
                .password("MyPasss")
                .build();

        Users updatedUbser = Users.builder().
                id(UUID.randomUUID())
                .balance(mockUser.getBalance().add(BigDecimal.valueOf(amount)))
                .email("adedinijames28@gmail.com")
                .firstName("James")
                .role(Role.BUYER)
                .password("MyPasss")
                .build();

        Mockito.when(userRepository.save(mockUser)).thenReturn(updatedUbser);

        ApiResponse<Object> apiResponse = userService.depositFund(amount, mockUser);

        Assertions.assertEquals(200, apiResponse.getStatus());
        Assertions.assertTrue(apiResponse.getMessage().contains("User balance updated from"));
        Assertions.assertNotNull(apiResponse.getData());
        Mockito.verify(userRepository, Mockito.times(1)).save(mockUser);
    }

    @Test
    void testDeposit_NotSuccessfull_RoleNOtBuyer() {
        int amount = 200;
        Users mockUser = Users.builder()
                .id(UUID.randomUUID())
                .balance(BigDecimal.ZERO)
                .email("adedinijames28@gmail.com")
                .firstName("James")
                .role(Role.SELLER)
                .password("MyPasss")
                .build();

        ApiResponse<Object> apiResponse = userService.depositFund(amount, mockUser);

        Assertions.assertEquals(403, apiResponse.getStatus());
        Assertions.assertTrue(apiResponse.getMessage().contains("Only users with 'buyer' role can deposit funds."));
        Assertions.assertNull(apiResponse.getData());
        Mockito.verify(userRepository, Mockito.times(0)).save(mockUser);

    }

    @Test
    void testDepositFund_InvalidDenomination() {
        int amount = 20; // invalid
        Users mockUser = Users.builder()
                .id(UUID.randomUUID())
                .balance(BigDecimal.ZERO)
                .email("adedinijames28@gmail.com")
                .firstName("James")
                .role(Role.BUYER)
                .password("MyPasss")
                .build();

        ApiResponse<Object> apiResponse = userService.depositFund(amount, mockUser);

        Assertions.assertEquals(400, apiResponse.getStatus());
        Assertions.assertTrue(apiResponse.getMessage().contains(DENOMINATION_ERROR));
        Assertions.assertNull(apiResponse.getData());
        Mockito.verify(userRepository, Mockito.never()).save(mockUser);

    }

    @Test
    void testDepositFund_NullUser() {
        int amount = 200;
        Users mockUsers = null;

        ApiResponse<Object> apiResponse = userService.depositFund(amount,mockUsers);

        Assertions.assertEquals(403, apiResponse.getStatus());
        Assertions.assertEquals("User not present", apiResponse.getMessage());
        Mockito.verify(userRepository, Mockito.never()).save(mockUsers);

    }
    @Test
    void testDepositFund_DatabaseError() {
        // Arrange
        int amount = 100; // Allowed denomination
        Users mockUser = Users.builder()
                .id(UUID.randomUUID())
                .username("buyer@example.com")
                .role(Role.BUYER)
                .balance(BigDecimal.valueOf(500))
                .build();

        Mockito.when(userRepository.save(mockUser)).thenThrow(new DataAccessException("Database connection failed") {});

        // Act & Assert
        Assertions.assertThrows(DataAccessException.class, () -> {
            userService.depositFund(amount, mockUser);
        });

        Mockito.verify(userRepository, Mockito.times(1)).save(mockUser);
    }

    @Test
    void testResetBalanceSuccess (){
        int amount = 200;
        Users mockUser = Users.builder()
                .id(UUID.randomUUID())
                .balance(BigDecimal.valueOf(2000))
                .email("adedinijames28@gmail.com")
                .firstName("James")
                .role(Role.BUYER)
                .password("MyPasss")
                .build();

        Users updatedUbser = Users.builder().
                id(UUID.randomUUID())
                .balance(BigDecimal.ZERO)
                .email("adedinijames28@gmail.com")
                .firstName("James")
                .role(Role.BUYER)
                .password("MyPasss")
                .build();
        Mockito.when(userRepository.save(mockUser)).thenReturn(updatedUbser);

        ApiResponse<Object>apiResponse = userService.resetBalance(mockUser);

        Assertions.assertEquals(200,apiResponse.getStatus());
        Assertions.assertNotNull(apiResponse.getData());
Assertions.assertTrue(apiResponse.getMessage().contains("Balance reset successful. OldBalance"));
Mockito.verify(userRepository, Mockito.times(1)).save(mockUser);
    }
    @Test
    void testResetBalanceNotSuccessException () {
        Users mockUser = new Users();
Mockito.when(userRepository.save(mockUser)).thenThrow(new DataAccessException("Database Error") {
});

ApiResponse<Object>apiResponse = userService.resetBalance(mockUser);

Assertions.assertEquals(500,apiResponse.getStatus());
Assertions.assertEquals("An Error occurred !!",apiResponse.getMessage());


    }

    @Test
    void testResetBalancesSuccess () {

        int amount = 200;
        UUID id =UUID.randomUUID();

        Users mockUser = Users.builder()
                .id(UUID.randomUUID())
                .balance(BigDecimal.valueOf(2000))
                .email("adedinijames28@gmail.com")
                .firstName("James")
                .role(Role.ADMIN)
                .password("MyPasss")
                .build();

        Users updatedUbser = Users.builder().
                id(UUID.randomUUID())
                .balance(BigDecimal.ZERO)
                .email("adedinijames28@gmail.com")
                .firstName("James")
                .role(Role.BUYER)
                .password("MyPasss")
                .build();

        Mockito.when(userRepository.findById(Mockito.any(UUID.class))).thenReturn(Optional.of(mockUser));
        Mockito.when(userRepository.save(mockUser)).thenReturn(updatedUbser);

       String apiResponse = userService.resetBalances(mockUser,amount,id);

        Assertions.assertTrue(apiResponse.contains("User's"));
        Mockito.verify(userRepository, Mockito.times(1)).save(mockUser);
    }

    @Test
    void testGetUser_Success(){
        UUID id = UUID.randomUUID();

        Users mockUser = Users.builder()
                .id(UUID.randomUUID())
                .balance(BigDecimal.valueOf(2000))
                .email("adedinijames28@gmail.com")
                .username("adedinijames28@gmail.com")
                .firstName("James")
                .role(Role.ADMIN)
                .password("MyPasss")
                .build();


        Authentication authentication = Mockito. mock(Authentication.class);
        SecurityContextHolder.getContext().setAuthentication(authentication);
        UserDetails userDetails = Mockito.mock(UserDetails.class);
        Mockito.when(authentication.getPrincipal()).thenReturn(userDetails);
        Mockito.when(userRepository.findById(Mockito.any(UUID.class))).thenReturn(Optional.of(mockUser));
        Mockito.when(userDetails.getUsername()).thenReturn(mockUser.getUsername());


        ApiResponse<Object> apiResponse= userService.getUser(id);

        Assertions.assertEquals(200,apiResponse.getStatus());
        Assertions.assertEquals("User Details Fetched Successfully", apiResponse.getMessage());
        Assertions.assertNotNull(apiResponse.getData());
        Mockito.verify(userRepository, Mockito.times(1)).findById(id);


    }

@Test
        void testGetUser_NotSuccessful_UnAuthorizedAccess () {
    UUID id = UUID.randomUUID();
    String authUserName = "adedinijames";

    Users mockUser = Users.builder()
            .id(UUID.randomUUID())
            .balance(BigDecimal.valueOf(2000))
            .email("adedinijames28@gmail.com")
            .username("adedinijames28@gmail.com")
            .firstName("James")
            .role(Role.ADMIN)
            .password("MyPasss")
            .build();


    Authentication authentication = Mockito.mock(Authentication.class);
    SecurityContextHolder.getContext().setAuthentication(authentication);
    UserDetails userDetails = Mockito.mock(UserDetails.class);
    Mockito.when(authentication.getPrincipal()).thenReturn(userDetails);
    Mockito.when(userRepository.findById(Mockito.any(UUID.class))).thenReturn(Optional.of(mockUser));
    Mockito.when(userDetails.getUsername()).thenReturn(authUserName);


    ApiResponse<Object> apiResponse = userService.getUser(id);

    Assertions.assertEquals(401, apiResponse.getStatus());
    Assertions.assertEquals("Unauthorized Access", apiResponse.getMessage());
    Assertions.assertNull(apiResponse.getData());
    Mockito.verify(userRepository, Mockito.times(1)).findById(id);
}


    @Test
    void testGetUser_NotSuccessful_UserNotFound () {
        UUID id = UUID.randomUUID();
        String authUserName = "adedinijames";
        Users adminUser = new Users();
        adminUser.setUsername("adedinijames");




        Authentication authentication = Mockito.mock(Authentication.class);
        SecurityContextHolder.getContext().setAuthentication(authentication);
        UserDetails userDetails = Mockito.mock(UserDetails.class);
        Mockito.when(authentication.getPrincipal()).thenReturn(userDetails);
        Mockito.when(userRepository.findById(Mockito.any(UUID.class))).thenReturn(Optional.empty());
        Mockito.when(userDetails.getUsername()).thenReturn(adminUser.getUsername());


        ApiResponse<Object> apiResponse = userService.getUser(id);

        Assertions.assertEquals(404, apiResponse.getStatus());
        Assertions.assertEquals("User not Found ", apiResponse.getMessage());
        Assertions.assertNull(apiResponse.getData());
        Mockito.verify(userRepository, Mockito.times(1)).findById(id);
    }

    @Test
    void testGetUser_NotSuccessful_Exception () {
        UUID id = UUID.randomUUID();
        String authUserName = "adedinijames";
        Users adminUser = new Users();
        adminUser.setUsername("adedinijames");




        Authentication authentication = Mockito.mock(Authentication.class);
        SecurityContextHolder.getContext().setAuthentication(authentication);
        UserDetails userDetails = Mockito.mock(UserDetails.class);
        Mockito.when(authentication.getPrincipal()).thenReturn(userDetails);
        Mockito.when(userRepository.findById(Mockito.any(UUID.class))).thenThrow(new DataAccessException("Database Error") {
        } );
        Mockito.when(userDetails.getUsername()).thenReturn(adminUser.getUsername());


        ApiResponse<Object> apiResponse = userService.getUser(id);

        Assertions.assertEquals(500, apiResponse.getStatus());
        Assertions.assertEquals("An Unexpected error occurred", apiResponse.getMessage());
        Assertions.assertNull(apiResponse.getData());
        Mockito.verify(userRepository, Mockito.times(1)).findById(id);
    }

}


