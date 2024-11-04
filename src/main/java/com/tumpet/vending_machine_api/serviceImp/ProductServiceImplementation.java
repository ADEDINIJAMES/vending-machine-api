package com.tumpet.vending_machine_api.serviceImp;

import com.tumpet.vending_machine_api.dto.ProductDto;
import com.tumpet.vending_machine_api.enums.Role;
import com.tumpet.vending_machine_api.exceptions.ProductNotFoundException;
import com.tumpet.vending_machine_api.exceptions.UserNotFoundException;
import com.tumpet.vending_machine_api.model.Product;
import com.tumpet.vending_machine_api.model.Users;
import com.tumpet.vending_machine_api.repository.ProductRepository;
import com.tumpet.vending_machine_api.repository.UserRepository;
import com.tumpet.vending_machine_api.request.ProductRequest;
import com.tumpet.vending_machine_api.request.ProductUpdateRequest;
import com.tumpet.vending_machine_api.responses.ApiResponse;
import com.tumpet.vending_machine_api.service.ProductService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.data.domain.*;
import org.springframework.data.mapping.PropertyReferenceException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
@Service
@RequiredArgsConstructor
@Slf4j
public class ProductServiceImplementation implements ProductService {
    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    @Override
    public ApiResponse<Object> createProduct(ProductRequest request) throws UserNotFoundException {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
      UserDetails userDetails= (UserDetails) authentication.getPrincipal();
     Optional<Users> users = userRepository.findByUsername(userDetails.getUsername());

     if(users.isEmpty()) {
         throw new UserNotFoundException("user not found");
     }
     if(users.get().getRole()!= Role.SELLER){
         log.error("The User are not Permitted !!!");
        return ApiResponse.builder()
                 .status(403)
                 .message("You are not Permitted to Add Product ")
                 .timestamp(LocalDateTime.now())
                 .build();
     }
         Product product = Product.builder()
                 .id(UUID.randomUUID())
                 .price(request.getPrice())
                 .quantity(request.getQuantity())
                 .name(request.getName())
                 .sellerId(users.get().getId())
                 .build();
       Product savedProduct = productRepository.save(product);
       return ApiResponse
               .builder()
               .status(201)
               .message("Product saved !!")
               .data(mapToProductDto(savedProduct))
               .timestamp(LocalDateTime.now())
               .build();

    }

    @Override
    public ApiResponse<Object> getProduct(UUID id) throws ProductNotFoundException {
        Product product = productRepository.findById(id).orElseThrow(()-> new ProductNotFoundException("The product was not found"));
       ProductDto productDto = mapToProductDto(product);
        return ApiResponse.builder()
                .status(200)
                .message("product details fetched successfully")
                .data(productDto)
                .timestamp(LocalDateTime.now())
                .build();
    }

    @Override
    public ApiResponse<Object> getAllProduct(int pageNumber, int pageSize, String sortBy, String sortDirection) {
        try {
            Sort sort = sortDirection.equalsIgnoreCase(Sort.Direction.ASC.name()) ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
            Pageable pageable = PageRequest.of(pageNumber, pageSize, sort);
            Page<Product> page = productRepository.findAll(pageable);
            Page<ProductDto>productDtoPage = page.map(this::mapToProductDto);
            return ApiResponse.builder()
                    .status(200)
                    .message("Products Fetched successfully")
                    .data(productDtoPage)
                    .timestamp(LocalDateTime.now())
                    .build();
        } catch (IllegalArgumentException | NullPointerException | PropertyReferenceException e) {
            e.printStackTrace();
            log.error(e.getMessage());
            return ApiResponse.builder()
                    .message("Invalid request parameters: " + e.getMessage())
                    .status(400)
                    .data(null)
                    .timestamp(LocalDateTime.now())
                    .build();

        } catch (DataAccessException e) {
            e.printStackTrace();
            log.error(e.getMessage());
            return ApiResponse.builder()
                    .message("Database error: " + e.getMessage())
                    .status(500)
                    .data(null)
                    .timestamp(LocalDateTime.now())
                    .build();
        } catch (Exception e) {
            e.printStackTrace();
            log.error(e.getMessage());
            return ApiResponse.builder()
                    .message("An unexpected error occurred: " + e.getMessage())
                    .status(500)
                    .data(null)
                    .timestamp(LocalDateTime.now())
                    .build();
        }
    }

    @Override
    public ApiResponse<Object> updateProduct(UUID id, ProductUpdateRequest productUpdate) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            UserDetails userDetails = (UserDetails) authentication.getPrincipal();
            Optional<Product> product = productRepository.findById(id);
            if (product.isPresent()) {
                Users users = userRepository.findByUsername(userDetails.getUsername()).orElseThrow(()-> new UserNotFoundException("User not found "));
                if(product.get().getSellerId().equals(users.getId())){
                product.get().setName(productUpdate.getName());
                product.get().setQuantity(productUpdate.getQuantity());
                product.get().setPrice(productUpdate.getPrice());
                Product updatedProduct = productRepository.save(product.get());
                return ApiResponse.builder()
                        .status(201)
                        .message("Product Updated Successfully")
                        .data(mapToProductDto(updatedProduct))
                        .timestamp(LocalDateTime.now())
                        .build();

                }
                log.error("Not permitted to make this change");
                log.info(String.valueOf(users.getId()));
                log.info(String.valueOf(product.get().getId()));
                return ApiResponse.builder()
                        .status(403)
                        .message("Not permitted to make this change")
                        .timestamp(LocalDateTime.now())
                        .build();
            }
            // Will still throw exception here
            log.error("Product not Found");
            return ApiResponse.builder()
                    .status(404)
                    .message("Product Not Found")
                    .data(null)
                    .timestamp(LocalDateTime.now())
                    .build();
        } catch (UserNotFoundException e) {
            e.printStackTrace();
            log.error(e.getMessage());
            return ApiResponse.builder()
                    .status(401)
                    .message("An Error occurred")
                    .timestamp(LocalDateTime.now())
                    .build();

         } catch (Exception e) {
        e.printStackTrace();
        log.error(e.getMessage());
        return ApiResponse.builder()
                .status(500)
                .message("An Error occurred !!!")
                .timestamp(LocalDateTime.now())
                .build();
    }
    }

    @Override
    public ApiResponse<Object> deleteProduct(UUID id) {
        try{
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        Optional<Product> product = productRepository.findById(id);
        if(product.isPresent()){
            Users users = userRepository.findByUsername(userDetails.getUsername()).orElseThrow(()-> new UserNotFoundException("User not found "));
            if(product.get().getSellerId().equals(users.getId())) {
                productRepository.delete(product.get());
                return ApiResponse.builder()
                        .status(200)
                        .message("Product Deleted Successfully !!")
                        .data(null)
                        .timestamp(LocalDateTime.now())
                        .build();
            }
            log.error("Not permitted to make this change");
            return ApiResponse.builder()
                    .status(403)
                    .message("Not permitted to make this change")
                    .timestamp(LocalDateTime.now())
                    .build();

        }
        return ApiResponse.builder()
                .status(404)
                .message("no Product found")
                .data(null)
                .timestamp(LocalDateTime.now())
                .build();

    }catch (Exception e){
            e.printStackTrace();
            log.error(e.getMessage());
            return ApiResponse.builder()
                    .status(500)
                    .message("An Error occurred !!!")
                    .timestamp(LocalDateTime.now())
                    .build();

        }
    }

    public ProductDto mapToProductDto (Product product){
        return ProductDto .builder()
                .id(product.getId())
                .name(product.getName())
                .price(product.getPrice())
                .quantity(product.getQuantity())
                .sellerId(product.getSellerId())
                .build();
    }
}
