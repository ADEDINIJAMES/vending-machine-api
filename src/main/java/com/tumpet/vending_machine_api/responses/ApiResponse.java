package com.tumpet.vending_machine_api.responses;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ApiResponse<T> {
    private Integer status;
    private String message;
    private T data;
    private LocalDateTime timestamp = LocalDateTime.now();

}
