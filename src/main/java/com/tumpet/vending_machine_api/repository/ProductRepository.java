package com.tumpet.vending_machine_api.repository;

import com.tumpet.vending_machine_api.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;
@Repository
public interface ProductRepository  extends JpaRepository<Product, UUID> {
}
