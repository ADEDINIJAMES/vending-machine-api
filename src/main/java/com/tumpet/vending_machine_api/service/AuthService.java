package com.tumpet.vending_machine_api.service;

import com.tumpet.vending_machine_api.dto.UserDto;
import com.tumpet.vending_machine_api.enums.Role;
import com.tumpet.vending_machine_api.request.LoginRequest;
import com.tumpet.vending_machine_api.request.UserRequest;
import com.tumpet.vending_machine_api.responses.ApiResponse;
import org.springframework.security.core.userdetails.UserDetailsService;

public interface AuthService extends UserDetailsService {
    ApiResponse<Object> registerUser (UserRequest request, Role role);
}
