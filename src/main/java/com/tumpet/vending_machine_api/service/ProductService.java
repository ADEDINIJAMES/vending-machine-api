package com.tumpet.vending_machine_api.service;

import com.tumpet.vending_machine_api.exceptions.ProductNotFoundException;
import com.tumpet.vending_machine_api.exceptions.UserNotFoundException;
import com.tumpet.vending_machine_api.request.ProductRequest;
import com.tumpet.vending_machine_api.request.ProductUpdateRequest;
import com.tumpet.vending_machine_api.responses.ApiResponse;

import java.util.UUID;

public interface ProductService {
    ApiResponse<Object> createProduct (ProductRequest request) throws UserNotFoundException;
    ApiResponse<Object> getProduct (UUID id) throws ProductNotFoundException;
    ApiResponse<Object> getAllProduct (int pageNumber, int pageSize, String sortBy, String sortDirection);
    public ApiResponse<Object> updateProduct(UUID id, ProductUpdateRequest productUpdate) ;
    ApiResponse<Object> deleteProduct (UUID id);
}
