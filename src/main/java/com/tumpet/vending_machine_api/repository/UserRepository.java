package com.tumpet.vending_machine_api.repository;

import com.tumpet.vending_machine_api.model.Users;
import com.tumpet.vending_machine_api.responses.ApiResponse;
import org.hibernate.query.Page;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<Users, UUID> {
    Optional<Users> findByUsername (String username);
    Boolean existsByEmail (String email);
    Boolean existsByUsername (String username);

}
