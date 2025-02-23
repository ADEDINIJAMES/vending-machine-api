package com.tumpet.vending_machine_api.controller;

import com.tumpet.vending_machine_api.enums.Role;
import com.tumpet.vending_machine_api.exceptions.ProductNotFoundException;
import com.tumpet.vending_machine_api.exceptions.UserNotFoundException;
import com.tumpet.vending_machine_api.request.ProductRequest;
import com.tumpet.vending_machine_api.request.ProductUpdateRequest;
import com.tumpet.vending_machine_api.responses.ApiResponse;
import com.tumpet.vending_machine_api.service.ProductService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/product")
public class ProductController {
    @Autowired

    private final ProductService productService;
    @PostMapping
@PreAuthorize("hasRole('SELLER')")
    public ResponseEntity<ApiResponse<Object>> createProduct (@Valid @RequestBody ProductRequest request) throws UserNotFoundException {
        ApiResponse<Object> response = productService.createProduct(request);
        return ResponseEntity.status(response.getStatus()).body(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<Object>> getAProduct (@PathVariable UUID id) throws ProductNotFoundException {
        ApiResponse<Object> response = productService.getProduct(id);
        return ResponseEntity.status(response.getStatus()).body(response);
    }

    @GetMapping
    public ResponseEntity<ApiResponse<Object>> getAllProduct (
            @RequestParam (defaultValue = "0", name = "pageNumber") int pageNumber,
            @RequestParam(defaultValue = "5", name = "pageSize") int pageSize,
            @RequestParam(defaultValue = "name", name = "sortBy") String sortBy,
            @RequestParam(defaultValue = "ASC", name = "sortDirection") String sortDirection
    ) {
        ApiResponse<Object> response = productService.getAllProduct(pageNumber,pageSize,sortBy,sortDirection);
        return ResponseEntity.status(response.getStatus()).body(response);
    }
    @PatchMapping("/{id}")
    @PreAuthorize("hasRole('SELLER')")
    public ResponseEntity<ApiResponse<Object>> updateProduct (@PathVariable UUID id, @RequestBody ProductUpdateRequest request) throws ProductNotFoundException {
        ApiResponse<Object> response = productService.updateProduct(id,request);
        return ResponseEntity.status(response.getStatus()).body(response);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('SELLER')")
    public ResponseEntity<ApiResponse<Object>> deleteProduct (@PathVariable UUID id){
        ApiResponse<Object> response = productService.deleteProduct(id);
        return ResponseEntity.status(response.getStatus()).body(response);
    }

}
