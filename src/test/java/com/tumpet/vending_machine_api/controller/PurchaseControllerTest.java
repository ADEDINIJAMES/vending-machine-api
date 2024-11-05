package com.tumpet.vending_machine_api.controller;

import com.tumpet.vending_machine_api.dto.ProductDto;
import com.tumpet.vending_machine_api.enums.Role;
import com.tumpet.vending_machine_api.exceptions.ProductNotFoundException;
import com.tumpet.vending_machine_api.model.Users;
import com.tumpet.vending_machine_api.responses.ApiResponse;
import com.tumpet.vending_machine_api.service.ProductService;
import com.tumpet.vending_machine_api.service.PurchaseService;
import com.tumpet.vending_machine_api.util.JwtAuthenticationFilter;
import com.tumpet.vending_machine_api.util.JwtUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import static org.mockito.Mockito.*;
import static org.springframework.http.HttpStatus.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.time.LocalDateTime;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class PurchaseControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private PurchaseService purchaseService;

    @InjectMocks
    private PurchaseController purchaseController;

    @MockBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Mock
    private ProductService productService;

    @InjectMocks
    private ProductController productController;
    @MockBean
    private JwtUtils jwtUtils;
    private UUID productId;
    private Users user;

    @BeforeEach
    void setUp() {
        productId = UUID.randomUUID();
        user = new Users();
        user.setBalance(BigDecimal.valueOf(500));
        user.setEmail("adedinijames28@gmail.com");
        user.setRole(Role.BUYER);
        user.setUsername("Busayomi");

        MockitoAnnotations.openMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(productController).build();


        UserDetails userDetails = User.withUsername(user.getUsername())
                .password("password")
                .roles("BUYER")
                .build();

        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }





    @Test
    public void testGetAProduct_Success() throws Exception {
        UUID productId = UUID.randomUUID();
        ProductDto productDto = new ProductDto();
        ApiResponse<Object> response = ApiResponse.builder()
                .status(200)
                .message("product details fetched successfully")
                .data(productDto)
                .timestamp(LocalDateTime.now())
                .build();
        when(productService.getProduct(productId)).thenReturn(response);

        mockMvc.perform(get("/products/{id}", productId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.message").value("product details fetched successfully"))
                .andExpect(jsonPath("$.data").isNotEmpty());
    }

    @Test
    public void testGetAProduct_ProductNotFound() throws Exception {
        // Arrange
        UUID productId = UUID.randomUUID();
        when(productService.getProduct(productId)).thenThrow(new ProductNotFoundException("The product was not found"));

        // Act & Assert
        mockMvc.perform(get("/products/{id}", productId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("The product was not found"));
    }



    @Test
    @WithMockUser(username = "Busayomi", roles = "BUYER")
    void buyProduct_SuccessfulPurchase() throws Exception {
        ApiResponse<Object> response = new ApiResponse<>(HttpStatus.OK.value(), "Purchase successful", null, LocalDateTime.now());
        when(purchaseService.buyProduct(eq(productId), eq(5), any(Users.class))).thenReturn(response);

        mockMvc.perform(post("/api/v1/purchase")
                        .param("productId", productId.toString())
                        .param("quantity", "5")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message", is("Purchase successful")));
    }

    @Test
    void buyProduct_ProductNotFoundException() throws Exception {
        when(purchaseService.buyProduct(eq(productId), eq(5), any(Users.class)))
                .thenThrow(new ProductNotFoundException("Product not found"));

        mockMvc.perform(post("/api/v1/purchase")
                        .param("productId", productId.toString())
                        .param("quantity", "5")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message", is("Product not found")));
    }

    @Test
    void buyProduct_InvalidQuantity() throws Exception {
        mockMvc.perform(post("/api/v1/purchase")
                        .param("productId", productId.toString())
                        .param("quantity", "-1")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }
}
