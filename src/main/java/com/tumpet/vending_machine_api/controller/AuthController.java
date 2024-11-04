package com.tumpet.vending_machine_api.controller;

import com.tumpet.vending_machine_api.enums.Role;
import com.tumpet.vending_machine_api.model.Users;
import com.tumpet.vending_machine_api.request.LoginRequest;
import com.tumpet.vending_machine_api.request.UserRequest;
import com.tumpet.vending_machine_api.request.UserUpdateRequest;
import com.tumpet.vending_machine_api.responses.ApiResponse;
import com.tumpet.vending_machine_api.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
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
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<Object>> GetAUser (@PathVariable UUID id) {
        ApiResponse<Object> response = authService.getUser(id);
        return ResponseEntity.status(response.getStatus()).body(response);
    }
    @GetMapping("/all")
    public ResponseEntity<ApiResponse<Object>> GetAllUser (
            @RequestParam(defaultValue = "0", name = "pageNumber") int pageNumber,
            @RequestParam(defaultValue = "10", name = "pageSize") int pageSize,
            @RequestParam (defaultValue = "username", name = "sortBy") String sortBy,
            @RequestParam(defaultValue = "ASC", name = "sortDirection") String sortDirection
    ) {
        ApiResponse<Object> response = authService.getAllUsers(pageNumber,pageSize,sortBy,sortDirection);
        return ResponseEntity.status(response.getStatus()).body(response);
    }

@PostMapping("/logout")
    public ResponseEntity<ApiResponse<Object>> logoutAll (Authentication authentication, HttpServletRequest request) {
    ApiResponse<Object> response = authService.logoutAll(authentication,request);
    return ResponseEntity.status(response.getStatus()).body(response);
    }

    @PostMapping("/deposit")
    public ResponseEntity<ApiResponse<Object>> deposit (@RequestParam (name = "amount") int amount, @AuthenticationPrincipal Users users) {
        ApiResponse<Object> response = authService.depositFund(amount,users);
        return ResponseEntity.status(response.getStatus()).body(response);
    }

    @PostMapping("/reset")
    public ResponseEntity<ApiResponse<Object>> resetDeposit (@AuthenticationPrincipal Users users) {
        ApiResponse<Object> response = authService.resetBalance(users);
        return ResponseEntity.status(response.getStatus()).body(response);
    }
}
