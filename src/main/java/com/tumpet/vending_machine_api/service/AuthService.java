package com.tumpet.vending_machine_api.service;

import com.tumpet.vending_machine_api.dto.UserDto;
import com.tumpet.vending_machine_api.enums.Role;
import com.tumpet.vending_machine_api.request.LoginRequest;
import com.tumpet.vending_machine_api.request.UserRequest;
import com.tumpet.vending_machine_api.request.UserUpdateRequest;
import com.tumpet.vending_machine_api.responses.ApiResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetailsService;

import java.util.UUID;

public interface AuthService extends UserDetailsService {
    ApiResponse<Object> registerUser (UserRequest request, Role role);
    ApiResponse<Object> loginUser (LoginRequest request);
    ApiResponse<Object> updateUser(UserUpdateRequest request, UUID id);
     ApiResponse<Object> logoutAll (Authentication authentication, HttpServletRequest request) ;
     ApiResponse<Object> deleteUser (UUID id);
    ApiResponse<Object> getUser(UUID id);
        ApiResponse<Object> getAllUsers (int pageNumber, int pageSize, String sortBy, String sortDirection);

}