package com.tumpet.vending_machine_api.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Product {
    @Id
    private UUID id = UUID.randomUUID();

    @NotBlank(message = "Product name is required")
    private String name;

    @NotNull(message = "Price is required")
    @Min(value = 50, message = "Price must be at least 50")
    private Integer price;

    @NotNull(message = "Quantity is required")
    @Min(value = 0, message = "Quantity cannot be negative")
    private Integer quantity;

    @NotNull(message = "Seller ID is required")
    private UUID sellerId;

    public void setPrice(Integer price) {
        if (price == null || price < 50 || price % 50 != 0) {
            throw new IllegalArgumentException("Price must be at least 50 and a multiple of 50");
        }
        this.price = price;
    }
}
