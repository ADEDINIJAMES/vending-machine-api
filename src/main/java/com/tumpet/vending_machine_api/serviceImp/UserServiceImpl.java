package com.tumpet.vending_machine_api.serviceImp;

import com.tumpet.vending_machine_api.dto.UserDto;
import com.tumpet.vending_machine_api.enums.Role;
import com.tumpet.vending_machine_api.exceptions.ActiveSessionException;
import com.tumpet.vending_machine_api.exceptions.UserNotFoundException;
import com.tumpet.vending_machine_api.model.Users;
import com.tumpet.vending_machine_api.repository.UserRepository;
import com.tumpet.vending_machine_api.request.LoginRequest;
import com.tumpet.vending_machine_api.request.UserRequest;
import com.tumpet.vending_machine_api.request.UserUpdateRequest;
import com.tumpet.vending_machine_api.responses.ApiResponse;
import com.tumpet.vending_machine_api.service.UserService;
import com.tumpet.vending_machine_api.util.JwtUtils;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.mapping.PropertyReferenceException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final JwtUtils jwtUtils;
    private final PasswordEncoder passwordEncoder;
    private final Map<String, String> activeSessions = new ConcurrentHashMap<>();
    private static final List<Integer> ALLOWED_DENOMINATION = Arrays.asList(50,100,200,500,1000);
    private static final String DENOMINATION_ERROR = "You can only deposit 50, 100, 200, 500, or 1,000 denominations.";


    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("Username not Found"));
    }
    @Override
    @Transactional
    public ApiResponse<Object> registerUser(UserRequest request, Role role) {
        try {
            if (userRepository.existsByEmail(request.getEmail()) || userRepository.existsByUsername(request.getUsername())) {
                log.error("Email or username registered already");
                return ApiResponse.builder()
                        .status(401)
                        .message("username or email already registered")
                        .timestamp(LocalDateTime.now())
                        .build();
            }

            Users users = Users.builder()
                    .id(UUID.randomUUID())
                    .email(request.getEmail())
                    .password(passwordEncoder.encode(request.getPassword()))
                    .firstName(request.getFirstName())
                    .lastName(request.getLastName())
                    .role(role)
                    .username(request.getUsername())
                    .balance(BigDecimal.ZERO)
                    .build();

            Users savedUser = userRepository.save(users);

            return ApiResponse.builder()
                    .status(201)
                    .message("User registered successfully")
                    .data(mapUserToUserDto(savedUser))
                    .timestamp(LocalDateTime.now())
                    .build();

        }catch (Exception e){
            e.printStackTrace();
            log.error(e.getMessage());
            return ApiResponse.builder()
                    .status(500)
                    .message("An Error Occurred !!")
                    .timestamp(LocalDateTime.now())
                    .build();
        }
    }

    @Override
    public ApiResponse<Object> loginUser(LoginRequest loginRequest) {
        if(loginRequest!=null){
            String username = loginRequest.getUsername();
            String rawPassword = loginRequest.getPassword();
            Optional<Users> users = userRepository.findByUsername(username);
            if(users.isPresent()){
                String encodedPassword= users.get().getPassword();
                if(checkPassword(rawPassword,encodedPassword)){
                    if (activeSessions.containsKey(username)) {
                        if(jwtUtils.isTokenExpired.apply(activeSessions.get(username))){
                            activeSessions.remove(username);
                        }
                        log.info(activeSessions.get(username));
                        throw new ActiveSessionException("There is already an active session using your account.");

                    }
//                    activeSessions.remove(username);
//                    String tokenGenerated= jwtUtils.createJwt.apply(users.get());
                    String tokenGenerated = jwtUtils.jwtCreate(users.get());
                    activeSessions.put(username,tokenGenerated);
                    Map<String, Object> myData= new HashMap<>();
                    myData.put("AccessToken",tokenGenerated);
                    log.info(tokenGenerated);
                    return ApiResponse.builder()
                            .message("Login Successful")
                            .status(200)
                            .data(myData)
                            .timestamp(LocalDateTime.now())
                            .build();
                }
                return ApiResponse.builder()
                        .message("Password or Username incorrect")
                        .status(403)
                        .timestamp(LocalDateTime.now())
                        .build();

            }
            return ApiResponse.builder()
                    .message("User not found")
                    .status(404)
                    .timestamp(LocalDateTime.now())
                    .build();
        }

        return ApiResponse.builder()
                .message("Login Details needed")
                .status(401)
                .timestamp(LocalDateTime.now())
                .build();
    }

//    public ApiResponse<Object> updateUser(UserUpdateRequest request, UUID id) throws UserNotFoundException {
//        try {
//            // Validate the request
//            if (request == null) {
//                return ApiResponse.builder()
//                        .message("Please enter the correct details")
//                        .status(401)
//                        .timestamp(LocalDateTime.now())
//                        .build();
//            }
//
//            // Check if the user is authenticated
//            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
//            if (authentication == null || !(authentication.getPrincipal() instanceof UserDetails)) {
//                log.info("User not authenticated or not found.");
//                return ApiResponse.builder()
//                        .message("User Not logged in or not found")
//                        .status(401)
//                        .timestamp(LocalDateTime.now())
//                        .build();
//            }
//
//            // Get the authenticated user's details
//            UserDetails userDetails = (UserDetails) authentication.getPrincipal();
//            log.info("User authenticated successfully...");
//
//            // Fetch the authenticated user from the database
//            Optional<Users> dbUser = userRepository.findByUsername(userDetails.getUsername());
//            if (dbUser.isEmpty()) {
//                log.info("PERMISSION NOT GRANTED");
//                return ApiResponse.builder()
//                        .message("You are not permitted !!")
//                        .status(403)
//                        .timestamp(LocalDateTime.now())
//                        .build();
//            }
//
//            // Fetch the user to be updated
//            Optional<Users> users = userRepository.findById(id);
//            if (users.isEmpty()) {
//                throw new UserNotFoundException("USER NOT FOUND");
//            }
//
//            // Check if the authenticated user is authorized to update the target user
//            if (!Objects.equals(dbUser.get().getUsername(), users.get().getUsername())) {
//                log.info("PERMISSION NOT GRANTED");
//                return ApiResponse.builder()
//                        .message("You are not permitted !!")
//                        .status(403)
//                        .timestamp(LocalDateTime.now())
//                        .build();
//            }
//
//            // Update the user details
//            Users userToUpdate = users.get();
//            userToUpdate.setFirstName(request.getFirstName());
//            userToUpdate.setEmail(request.getEmail());
//            userToUpdate.setLastName(request.getLastName());
//            Users savedUser = userRepository.save(userToUpdate);
//
//            // Map the updated user to a DTO
//            UserDto userResponse = mapUserToUserDto(savedUser);
//
//            // Prepare the response
//            Map<String, Object> myData = new HashMap<>();
//            myData.put("UserDetails", userResponse);
//            return ApiResponse.builder()
//                    .message("User details updated successfully")
//                    .data(myData)
//                    .status(200)
//                    .timestamp(LocalDateTime.now())
//                    .build();
//
//        } catch (UserNotFoundException e) {
//            log.error("User not found: {}", e.getMessage());
//            throw e; // Re-throw the exception to be handled by the global exception handler
//        } catch (Exception e) {
//            log.error("An error occurred: {}", e.getMessage(), e);
//            return ApiResponse.builder()
//                    .message("An Error occurred")
//                    .status(500)
//                    .timestamp(LocalDateTime.now())
//                    .build();
//        }
//    }

    @Override
    public ApiResponse<Object> updateUser(UserUpdateRequest request, UUID id) {
        try { if(request==null){
            return ApiResponse.builder()
                    .message("Please enter the correct details")
                    .status(401)
                    .timestamp(LocalDateTime.now())
                    .build();
        }
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication != null && authentication.getPrincipal() instanceof UserDetails userDetails) {
                log.info("User authenticated successfully...");
                Optional<Users> dbUser = userRepository.findByUsername(userDetails.getUsername());
                Optional<Users> users = userRepository.findById(id);
                if (dbUser.isPresent()) {
                    if(users.isPresent() && Objects.equals(dbUser.get().getUsername(), users.get().getUsername())) {

                        users.get().setFirstName(request.getFirstName());
                        users.get().setEmail(request.getEmail());
                        users.get().setLastName(request.getLastName());
                        Users savedUser= userRepository.save(users.get());

                        UserDto userResponse= mapUserToUserDto (savedUser);

                        Map<String, Object> myData = new HashMap<>();
                        myData.put("UserDetails  ", userResponse);
                        return ApiResponse.builder()
                                .message("User details updated successfully")
                                .data(myData)
                                .status(200)
                                .timestamp(LocalDateTime.now())
                                .build();
                    }

                    throw new UserNotFoundException("USER NOT FOUND");

                }
                log.info("PERMISSION NOT GRANTED");
                return ApiResponse.builder()
                        .message("You are not permitted !!")
                        .status(403)
                        .timestamp(LocalDateTime.now())
                        .build();

            }
            log.info("User not authenticated or not found.");
            return ApiResponse.builder()
                    .message("User Not logged in or not found")
                    .status(401)
                    .timestamp(LocalDateTime.now())
                    .build();
        }catch (Exception e){
            e.printStackTrace();
            return ApiResponse.builder()
                    .message("An Error occurred")
                    .status(500)
                    .timestamp(LocalDateTime.now())
                    .build();
        }

    }


    public UserDto mapUserToUserDto (Users users){
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

    private boolean checkPassword (String rawPassword, String dBPassword ){
        return passwordEncoder.matches(rawPassword,dBPassword);
    }

    public ApiResponse<Object> logoutAll(Authentication authentication, HttpServletRequest request) {
       try{
        authentication = SecurityContextHolder.getContext().getAuthentication();

        log.info("Authentication done");

        if (authentication != null) {
            UserDetails userDetails = (UserDetails) authentication.getPrincipal();
            String username = userDetails.getUsername();

            SecurityContextHolder.clearContext();
            request.getSession().invalidate();

            activeSessions.remove(username);

            return ApiResponse.builder()
                    .message("Logout Successful")
                    .status(200)
                    .timestamp(LocalDateTime.now())
                    .build();
        }

        return ApiResponse.builder()
                .message("User not logged in")
                .status(403)
                .timestamp(LocalDateTime.now())
                .build();
    }catch (Exception e){
           e.printStackTrace();
           log.error(e.getMessage());
           return ApiResponse.builder()
                   .message("An Unexpected error occurred !!!")
                   .status(500)
                   .timestamp(LocalDateTime.now())
                   .build();
       }
    }

    public ApiResponse<Object> deleteUser (UUID id){
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication != null && authentication.getPrincipal() instanceof UserDetails userDetails) {
                log.info("User authenticated successfully...");
                Optional<Users> dbUser = userRepository.findByUsername(userDetails.getUsername());

                if (dbUser.isPresent() && dbUser.get().getRole() == Role.ADMIN) {
                    Optional<Users> users = userRepository.findById(id);
                    users.ifPresent(userRepository::delete);
                    log.info("user {} deleted successful", users.get().getId());
                    return ApiResponse.builder()
                            .message("Deletion successful")
                            .status(200)
                            .timestamp(LocalDateTime.now())
                            .build();
                }
                log.info("PERMISSION NOT GRANTED");
                return ApiResponse.builder()
                        .message("You are not permitted !!")
                        .status(401)
                        .timestamp(LocalDateTime.now())
                        .build();

            }
            log.info("User not authenticated or not found.");
            return ApiResponse.builder()
                    .message("User Not logged in or not found")
                    .status(401)
                    .timestamp(LocalDateTime.now())
                    .build();

        }catch (Exception e){
            e.printStackTrace();
            log.error(e.getMessage());
            return ApiResponse.builder()
                    .message("AN ERROR OCCURRED")
                    .status(500)
                    .timestamp(LocalDateTime.now())
                    .build();
        }
    }

    @Override
    public ApiResponse<Object> getAllUsers(int pageNumber, int pageSize, String sortBy, String sortDirection) {
        try {
            Sort sort = sortDirection.equalsIgnoreCase(Sort.Direction.ASC.name()) ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
            Pageable pageable = PageRequest.of(pageNumber, pageSize, sort);
            Page<Users> usersPage = userRepository.findAll(pageable);
            Page<UserDto> userDtoPage = usersPage.map(this::mapUserToUserDto);
            return ApiResponse.builder()
                    .status(200)
                    .message("Users fetched successfully")
                    .data(userDtoPage)
                    .timestamp(LocalDateTime.now())
                    .build();


        } catch (IllegalArgumentException | NullPointerException | PropertyReferenceException e) {
            e.printStackTrace();
            log.error(e.getMessage());
            return ApiResponse.builder()
                    .message("Invalid request parameters: " + e.getMessage())
                    .status(400)
                    .timestamp(LocalDateTime.now())
                    .build();

        } catch (DataAccessException e) {
            e.printStackTrace();
            log.error(e.getMessage());
            return ApiResponse.builder()
                    .message("Database error: " + e.getMessage())
                    .status(500)
                    .timestamp(LocalDateTime.now())
                    .build();
        } catch (Exception e) {
            e.printStackTrace();
            log.error(e.getMessage());
            return ApiResponse.builder()
                    .message("An unexpected error occurred: " + e.getMessage())
                    .status(500)
                    .timestamp(LocalDateTime.now())
                    .build();
        }
    }

    @Override
    @Transactional
    public ApiResponse<Object> depositFund(int amount, Users users) {
        if(users==null){
        return  ApiResponse
                .builder()
                .status(403)
                .message("User not present")
                .timestamp(LocalDateTime.now())
                .build();
        }
        if (!users.getRole().equals(Role.BUYER)) {
            return ApiResponse.builder()
                    .status(403)
                    .message("Only users with 'buyer' role can deposit funds.")
                    .timestamp(LocalDateTime.now())
                    .build();
        }
        if (ALLOWED_DENOMINATION.contains(amount)) {
            BigDecimal oldBalance = users.getBalance();
            BigDecimal newBalance = oldBalance.add(BigDecimal.valueOf(amount));
            users.setBalance(newBalance);
            Users updatedUser = userRepository.save(users);

            return ApiResponse.builder()
                    .status(200)
                    .message("User balance updated from " + oldBalance + " to " + newBalance)
                    .data(mapUserToUserDto(updatedUser))
                    .timestamp(LocalDateTime.now())
                    .build();
        }

        // Invalid denomination response
        return ApiResponse.builder()
                .status(400)
                .message(DENOMINATION_ERROR)
                .timestamp(LocalDateTime.now())
                .build();
    }

    @Override
    public ApiResponse<Object> resetBalance(Users users) {
        try{
        BigDecimal oldBalance = users.getBalance();
        users.setBalance(BigDecimal.ZERO);
        Users updatedUser= userRepository.save(users);
        return ApiResponse.builder()
                .status(200)
                .message("Balance reset successful. OldBalance "+ oldBalance +". Balance now "+ updatedUser.getBalance())
                .data(mapUserToUserDto(updatedUser))
                .timestamp(LocalDateTime.now())
                .build();
        }catch (Exception e){
            e.printStackTrace();
            log.error(e.getMessage());
            return ApiResponse.builder()
                    .status(500)
                    .message("An Error occurred !!")
                    .timestamp(LocalDateTime.now())
                    .build();
        }


    }

    public String resetBalances (Users user, int amount, UUID userId ){
        try{
            if(user.getRole().equals(Role.ADMIN)|| user.getId().equals(userId)){
                Users users = userRepository.findById(userId).orElseThrow(()-> new UserNotFoundException("User not found"));
                BigDecimal oldUserBalance= users.getBalance();
                BigDecimal newBalance = oldUserBalance.add(BigDecimal.valueOf(amount));
                users.setBalance(newBalance);
                Users savedUsers= userRepository.save(users);
                return "User's balance now reset from "+ oldUserBalance +" to "+ savedUsers.getBalance();
            }
            return "You are not permitted to do this ";

        } catch (UserNotFoundException e) {
            e.printStackTrace();
            return e.getMessage();

        }catch (Exception e) {
return "An Error occurred";
        }

    }

    @Override
    public ApiResponse<Object> getUser(UUID id){
        try{
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        String authUsername = userDetails.getUsername();
        Optional<Users> users= userRepository.findById(id);
        if(users.isPresent()) {
            if (users.get().getUsername().equals(authUsername)) {
                UserDto response = mapUserToUserDto(users.get());
                return ApiResponse.builder()
                        .status(200)
                        .message("User Details Fetched Successfully")
                        .data(response)
                        .timestamp(LocalDateTime.now())
                        .build();
            }
            return ApiResponse.builder()
                    .status(401)
                    .message("Unauthorized Access")
                    .data(null)
                    .timestamp(LocalDateTime.now())
                    .build();
        }
     return    ApiResponse.builder()
                .message("User not Found ")
                .status(404)
                .data(null)
                .timestamp(LocalDateTime.now())
                .build();
    }catch(Exception e){
            e.printStackTrace();
            log.error(e.getMessage());
            return ApiResponse.builder()
                    .status(500)
                    .message("An Unexpected error occurred")
                    .data(null)
                    .timestamp(LocalDateTime.now())
                    .build();
        }
    }


}
