package com.tumpet.vending_machine_api.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class ProductRequest {
    @NotBlank(message = "Product name is required")
    private String name;

    @NotNull(message = "Price is required")
    @Min(value = 50, message = "Price must be at least 50")
    private Integer price;

    @NotNull(message = "Quantity is required")
    @Min(value = 0, message = "Quantity cannot be negative")
    private Integer quantity;

    public void setName(String name) {
        this.name = name;
    }

    public void setPrice(Integer price) {
        if (price == null || price < 50 || price % 50 != 0) {
            throw new IllegalArgumentException("Price must be at least 50 and a multiple of 50");
        }
        this.price = price;
    }

    public void setQuantity(Integer quantity) {
        if (quantity==0) {
            throw new IllegalArgumentException("Quantity must be greater than zero");
        }
        this.quantity = quantity;
    }

}
