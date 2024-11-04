package com.tumpet.vending_machine_api.serviceImp;

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
    private final ProductServiceImplementation productServiceImplementation;
    private static final List<Integer> DENOMINATION = Arrays.asList(50,100,200,500,1000);



    @Override
    public ApiResponse<Object> buyProduct(UUID productId, int quantity, Users user) throws ProductNotFoundException {
        //find the product
        // product must be present
        //quantity must be present
        //quanttiy the user wants to buy must be greater than 0
        //does the user have the balance
        // deduct and update from users balance
        // deduct and update from quantity

        //Edge : if more than one user is purchasing

Product product=productRepository.findById(productId).orElseThrow(()-> new ProductNotFoundException("Product Not Found"));
BigDecimal totalPrice = BigDecimal.valueOf(product.getPrice()).multiply(BigDecimal.valueOf(quantity));  //Price is an Integer
if(quantity<=0){
    return ApiResponse.builder()
            .status(400)
            .message("Enter number of Quantity")
            .timestamp(LocalDateTime.now())
            .build();
}


    if(product.getQuantity()< quantity){
        return ApiResponse.builder()
                .status(400)
                .message("Insufficient Product Quantity Available")
                .timestamp(LocalDateTime.now())
                .build();
    }

    if(user.getBalance().compareTo( BigDecimal.valueOf(product.getPrice()))<0){
return ApiResponse.builder()
        .status(400)
        .message("You don't have sufficient balance for this purchase")
        .timestamp(LocalDateTime.now())
        .build();
        }
        int remainingBalance = user.getBalance().subtract(totalPrice)
                .setScale(0, RoundingMode.HALF_UP)
                .intValue();
        user.setBalance(user.getBalance().subtract(totalPrice));
        product.setQuantity(product.getQuantity()- quantity);
      Users updatedUser =  userRepository.save(user);
       Product updatedProduct = productRepository.save(product);

        List<Integer> change = calculateChange(remainingBalance);

        PurchaseResponse response = PurchaseResponse.builder()
                .productName(product.getName())
                .totalSpent(totalPrice.intValue())
                .change(change)
                .build();


        return ApiResponse.builder()
                .status(200)
                .message("Product Purchased successfully")
                .data(response)
                .timestamp(LocalDateTime.now())
                .build();
    }

    private List<Integer> calculateChange(int remainingBalance){
        List<Integer> change = new ArrayList<>();
        for(int denomination : DENOMINATION){
while (remainingBalance>= denomination){
    change.add(denomination);
    remainingBalance-=denomination;
}
        }
        return change;
    }
}
