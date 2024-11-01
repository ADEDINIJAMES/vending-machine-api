package com.tumpet.vending_machine_api.repository;

import com.tumpet.vending_machine_api.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface ProductRepository  extends JpaRepository<Product, UUID> {
}
