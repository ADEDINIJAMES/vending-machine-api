package com.tumpet.vending_machine_api.service;

import com.tumpet.vending_machine_api.enums.Role;
import com.tumpet.vending_machine_api.exceptions.UserNotFoundException;
import com.tumpet.vending_machine_api.model.Users;
import com.tumpet.vending_machine_api.request.LoginRequest;
import com.tumpet.vending_machine_api.request.UserRequest;
import com.tumpet.vending_machine_api.request.UserUpdateRequest;
import com.tumpet.vending_machine_api.responses.ApiResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetailsService;

import java.util.UUID;

public interface UserService extends UserDetailsService {
    ApiResponse<Object> registerUser (UserRequest request, Role role);
    ApiResponse<Object> loginUser (LoginRequest request);
    ApiResponse<Object> updateUser(UserUpdateRequest request, UUID id) throws UserNotFoundException;
     ApiResponse<Object> logoutAll (Authentication authentication, HttpServletRequest request) ;
     ApiResponse<Object> deleteUser (UUID id);
    ApiResponse<Object> getUser(UUID id);
    ApiResponse<Object> getAllUsers (int pageNumber, int pageSize, String sortBy, String sortDirection);
    ApiResponse<Object> depositFund (int amount, Users users);
    ApiResponse<Object> resetBalance (Users users);
//     ApiResponse<Object> resetBalances (Users user, int amount );
 String resetBalances (Users user, int amount, UUID userId );

}