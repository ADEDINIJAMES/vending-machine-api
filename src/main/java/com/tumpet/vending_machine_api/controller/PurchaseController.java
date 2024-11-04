package com.tumpet.vending_machine_api.controller;

import com.tumpet.vending_machine_api.exceptions.ProductNotFoundException;
import com.tumpet.vending_machine_api.model.Users;
import com.tumpet.vending_machine_api.responses.ApiResponse;
import com.tumpet.vending_machine_api.service.ProductService;
import com.tumpet.vending_machine_api.service.PurchaseService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/purchase")
public class PurchaseController {
    private final PurchaseService purchaseService;

    @PostMapping
    public ResponseEntity<ApiResponse<Object>> buyProduct (
            @RequestParam(name = "productId") UUID productId,
            @RequestParam(name="quantity") int quantity,
            @AuthenticationPrincipal Users user ) throws ProductNotFoundException {
        ApiResponse<Object> response = purchaseService.buyProduct(productId,quantity,user);
        return ResponseEntity.status(response.getStatus()).body(response);
    }

}
