package com.tumpet.vending_machine_api.serviceImp;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tumpet.vending_machine_api.dto.ProductNotification;
import com.tumpet.vending_machine_api.exceptions.ProductNotFoundException;
import com.tumpet.vending_machine_api.model.Product;
import com.tumpet.vending_machine_api.model.Users;
import com.tumpet.vending_machine_api.repository.ProductRepository;
import com.tumpet.vending_machine_api.repository.UserRepository;
import com.tumpet.vending_machine_api.responses.ApiResponse;
import com.tumpet.vending_machine_api.responses.PurchaseResponse;
import com.tumpet.vending_machine_api.service.ProductService;
import com.tumpet.vending_machine_api.service.PurchaseService;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PurchaseServiceImpl implements PurchaseService {
    private final UserRepository userRepository;
    private final ProductRepository productRepository;
    private final RabbitTemplate rabbitTemplate;
    private static final List<Integer> DENOMINATION = Arrays.asList(1000, 500, 200, 100, 50);

    @Value("${EXCHANGE_NAME}")
    private String exchangeName;

    @Value("${ROUTING_KEY}")
    private String routingKey;

    @Override
    public ApiResponse<Object> buyProduct(UUID productId, int quantity, Users user) throws ProductNotFoundException, JsonProcessingException {
        // Find the product
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ProductNotFoundException("Product Not Found"));

        // Calculate total price for the quantity
        BigDecimal totalPrice = BigDecimal.valueOf(product.getPrice())
                .multiply(BigDecimal.valueOf(quantity));

        if (quantity <= 0) {
            return ApiResponse.builder()
                    .status(400)
                    .message("Enter a valid quantity")
                    .timestamp(LocalDateTime.now())
                    .build();
        }

        if (product.getQuantity() < quantity) {
            return ApiResponse.builder()
                    .status(400)
                    .message("Insufficient product quantity available")
                    .timestamp(LocalDateTime.now())
                    .build();
        }

        if (user.getBalance().compareTo(totalPrice) < 0) {
            return ApiResponse.builder()
                    .status(400)
                    .message("You don't have sufficient balance for this purchase")
                    .timestamp(LocalDateTime.now())
                    .build();
        }

        // Deduct and update user's balance and product quantity
        user.setBalance(user.getBalance().subtract(totalPrice));
        product.setQuantity(product.getQuantity() - quantity);
        Users updatedUser = userRepository.save(user);
        Product updatedProduct = productRepository.save(product);

        // Calculate change
        int remainingBalance = updatedUser.getBalance().intValue();
        List<Integer> change = calculateChange(remainingBalance);

        // Create purchase response
        PurchaseResponse response = PurchaseResponse.builder()
                .productName(updatedProduct.getName())
                .totalSpent(totalPrice.intValue())
                .change(change)
                .build();

        // Send notification
        ProductNotification notification = ProductNotification.builder()
                .productName(updatedProduct.getName())
                .totalSpent(totalPrice.intValue())
                .quantity(quantity)
                .buyerEmail(user.getEmail())
                .build();
        ObjectMapper objectMapper = new ObjectMapper();
        String jsonPayload = objectMapper.writeValueAsString(notification);
        rabbitTemplate.convertAndSend(exchangeName, routingKey, jsonPayload);

        return ApiResponse.builder()
                .status(200)
                .message("Product purchased successfully")
                .data(response)
                .timestamp(LocalDateTime.now())
                .build();
    }

    private List<Integer> calculateChange(int remainingBalance) {
        List<Integer> change = new ArrayList<>();
        for (int denomination : DENOMINATION) {
            while (remainingBalance >= denomination) {
                change.add(denomination);
                remainingBalance -= denomination;
            }
        }
        return change;
    }
}
