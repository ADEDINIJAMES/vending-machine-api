package com.tumpet.vending_machine_api.service;

import com.tumpet.vending_machine_api.exceptions.ProductNotFoundException;
import com.tumpet.vending_machine_api.model.Users;
import com.tumpet.vending_machine_api.responses.ApiResponse;

import java.util.UUID;

public interface PurchaseService {
    ApiResponse<Object> buyProduct (UUID productId, int quantity, Users users) throws ProductNotFoundException;
}
