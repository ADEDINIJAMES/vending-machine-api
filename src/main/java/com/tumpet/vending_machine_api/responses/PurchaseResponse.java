package com.tumpet.vending_machine_api.responses;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class PurchaseResponse {
    private int totalSpent;
    private String productName;
    private List<Integer> change;
}
