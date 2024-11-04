package com.tumpet.vending_machine_api.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;
@AllArgsConstructor
@NoArgsConstructor
@Getter
public class purchaseRequest {
    private UUID productId;
    private int quantity;

}
