package com.tumpet.vending_machine_api.controller;

import com.tumpet.vending_machine_api.enums.Role;
import com.tumpet.vending_machine_api.request.LoginRequest;
import com.tumpet.vending_machine_api.request.UserRequest;
import com.tumpet.vending_machine_api.request.UserUpdateRequest;
import com.tumpet.vending_machine_api.responses.ApiResponse;
import com.tumpet.vending_machine_api.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

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
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<Object>> loginUser (@Valid @RequestBody LoginRequest loginRequest){
        ApiResponse<Object> response=authService.loginUser(loginRequest);
        return  ResponseEntity.status(response.getStatus()).body(response);
    }
    @PatchMapping("/update/{id}")
    public ResponseEntity<ApiResponse<Object>> updateUser (@Valid @RequestBody UserUpdateRequest request, @PathVariable UUID id){
    ApiResponse<Object> response = authService.updateUser(request, id);
    return ResponseEntity.status(response.getStatus()).body(response);
}
@DeleteMapping("/delete/{id}")
    public ResponseEntity<ApiResponse<Object>> deleteUser (@PathVariable UUID id){
    ApiResponse<Object> response = authService.deleteUser(id);
    return ResponseEntity.status(response.getStatus()).body(response);
}
@PostMapping("/logout")
    public ResponseEntity<ApiResponse<Object>> logoutAll (Authentication authentication, HttpServletRequest request) {
    ApiResponse<Object> response = authService.logoutAll(authentication,request);
    return ResponseEntity.status(response.getStatus()).body(response);
    }

}
