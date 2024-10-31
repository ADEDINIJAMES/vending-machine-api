package com.tumpet.vending_machine_api.repository;

import com.tumpet.vending_machine_api.model.Users;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface UserRepository extends JpaRepository<Users, UUID> {
}
