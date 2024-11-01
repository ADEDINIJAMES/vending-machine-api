package com.tumpet.vending_machine_api.serviceImp;

import com.tumpet.vending_machine_api.dto.UserDto;
import com.tumpet.vending_machine_api.enums.Role;
import com.tumpet.vending_machine_api.model.Users;
import com.tumpet.vending_machine_api.repository.UserRepository;
import com.tumpet.vending_machine_api.request.UserRequest;
import com.tumpet.vending_machine_api.responses.ApiResponse;
import com.tumpet.vending_machine_api.service.AuthService;
import com.tumpet.vending_machine_api.util.JwtUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthServiceImpl implements AuthService {
    private final UserRepository userRepository;
    private final JwtUtils jwtUtils;
    private final PasswordEncoder passwordEncoder;

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
                        .data(null)
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
                    .data(MapUserToUserDto(savedUser))
                    .timestamp(LocalDateTime.now())
                    .build();

        }catch (Exception e){
            e.printStackTrace();
            log.error(e.getMessage());
            return ApiResponse.builder()
                    .status(500)
                    .message("An Error Occurred !!")
                    .data(null)
                    .timestamp(LocalDateTime.now())
                    .build();
        }
    }

    private UserDto MapUserToUserDto (Users users){
        return UserDto.builder()
                .id(users.getId())
                .email(users.getEmail())
                .firstName(users.getFirstName())
                .username(users.getUsername())
                .balance(users.getBalance())
                .lastName(users.getLastName())
                .build();

    }

}
