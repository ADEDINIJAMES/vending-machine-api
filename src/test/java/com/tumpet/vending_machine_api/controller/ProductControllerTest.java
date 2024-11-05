package com.tumpet.vending_machine_api.controller;

import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.tumpet.vending_machine_api.dto.ProductDto;
import com.tumpet.vending_machine_api.request.ProductRequest;
import com.tumpet.vending_machine_api.responses.ApiResponse;
import com.tumpet.vending_machine_api.service.ProductService;
import com.tumpet.vending_machine_api.service.UserService;
import com.tumpet.vending_machine_api.util.JwtUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.security.web.csrf.HttpSessionCsrfTokenRepository;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.security.test.context.support.WithMockUser;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.times;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ProductController.class)
class ProductControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ProductService productService;

    @MockBean
    private UserService authService;

    @MockBean
    private JwtUtils jwtUtils;

    @Autowired
    private ObjectMapper objectMapper;

    private ProductRequest productRequest;
    private ApiResponse<Object> successResponse;

    @MockBean
    private HttpSessionCsrfTokenRepository csrfTokenRepository;


    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        productRequest = new ProductRequest();
        productRequest.setName("Test Product");
        productRequest.setPrice(50);
        productRequest.setQuantity(10);

        successResponse = ApiResponse.builder()
                .status(201)
                .message("Product saved !!")
                .data(new ProductDto(UUID.randomUUID(), "Test Product", 50, 10, UUID.randomUUID()))
                .build();
    }

    @Test
    @WithMockUser(username = "sellerUser", roles = {"SELLER"})
    void testCreateProduct_Success() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(SerializationFeature.WRAP_ROOT_VALUE,false);
        ObjectWriter ow = mapper.writer().withDefaultPrettyPrinter();
        String requestJson=ow.writeValueAsString(productRequest);
      when(productService.createProduct(productRequest)).thenReturn(successResponse);
      this.mockMvc.perform(post("/")
              .contentType(MediaType.APPLICATION_JSON)
              .content(requestJson))
              .andDo( print()).andExpect(status().isOk());

    }


    @Test
    @WithMockUser(username = "buyerUser", roles = {"BUYER"})
    void testCreateProduct_Forbidden() throws Exception {
        mockMvc.perform(post("/")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(productRequest)))
                .andExpect(status().isForbidden());

        verify(productService, times(0)).createProduct(any(ProductRequest.class));  // Ensure service method is not called
    }
}