package com.tumpet.vending_machine_api.controller;

import com.tumpet.vending_machine_api.dto.UserDto;
import com.tumpet.vending_machine_api.enums.Role;
import com.tumpet.vending_machine_api.request.UserRequest;
import com.tumpet.vending_machine_api.responses.ApiResponse;
import com.tumpet.vending_machine_api.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/user")
public class AuthController {
    private final AuthService authService;
@PostMapping
    public ResponseEntity<ApiResponse<Object>> RegisterUser (@Valid @RequestBody UserRequest request,
    @RequestParam( required=true, name="role") Role role
    ){
      ApiResponse<Object> response = authService.registerUser(request,role);
      return ResponseEntity.status(response.getStatus()).body(response);
    }

}
