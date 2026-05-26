package dev.edsonmm.products.presentation.controller;

import dev.edsonmm.products.domain.entity.Product;
import dev.edsonmm.products.domain.service.interfaces.ProductService;
import dev.edsonmm.products.exception.ProductNotFoundException;
import dev.edsonmm.products.presentation.request.ProductRequest;
import tools.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willDoNothing;
import static org.mockito.BDDMockito.willThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ProductController.class)
class ProductControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private ProductService productService;

    private Product product;
    private ProductRequest request;

    @BeforeEach
    void setUp() {
        product = new Product();
        product.setId(1L);
        product.setName("Laptop");
        product.setDescription("Laptop gamer");
        product.setPrice(new BigDecimal("1500.00"));
        product.setStock(10);
        product.setActive(true);

        request = new ProductRequest();
        request.setName("Laptop");
        request.setDescription("Laptop gamer");
        request.setPrice(new BigDecimal("1500.00"));
        request.setStock(10);
        request.setActive(true);
    }

    @Test
    void getAll_shouldReturn200WithProductList() throws Exception {
        given(productService.findAll()).willReturn(List.of(product));

        mockMvc.perform(get("/api/products").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].name").value("Laptop"))
                .andExpect(jsonPath("$[0].stock").value(10));
    }

    @Test
    void getAll_shouldReturn200WithEmptyList() throws Exception {
        given(productService.findAll()).willReturn(List.of());

        mockMvc.perform(get("/api/products").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$").isEmpty());
    }

    @Test
    void getById_shouldReturn200WhenExists() throws Exception {
        given(productService.findById(1L)).willReturn(product);

        mockMvc.perform(get("/api/products/1").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Laptop"))
                .andExpect(jsonPath("$.price").value(1500.00));
    }

    @Test
    void getById_shouldReturn404WhenNotFound() throws Exception {
        given(productService.findById(99L)).willThrow(new ProductNotFoundException(99L));

        mockMvc.perform(get("/api/products/99").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").exists())
                .andExpect(jsonPath("$.path").value("/api/products/99"));
    }

    @Test
    void create_shouldReturn201WhenValid() throws Exception {
        given(productService.create(any())).willReturn(product);

        mockMvc.perform(post("/api/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Laptop"));
    }

    @Test
    void create_shouldReturn400WhenNameIsBlank() throws Exception {
        request.setName("   ");

        mockMvc.perform(post("/api/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.errors.name").exists());
    }

    @Test
    void create_shouldReturn400WhenPriceIsNull() throws Exception {
        request.setPrice(null);

        mockMvc.perform(post("/api/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.price").exists());
    }

    @Test
    void create_shouldReturn400WhenStockIsNull() throws Exception {
        request.setStock(null);

        mockMvc.perform(post("/api/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.stock").exists());
    }

    @Test
    void update_shouldReturn200WhenValid() throws Exception {
        given(productService.update(eq(1L), any())).willReturn(product);

        mockMvc.perform(put("/api/products/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Laptop"));
    }

    @Test
    void update_shouldReturn404WhenNotFound() throws Exception {
        given(productService.update(eq(99L), any())).willThrow(new ProductNotFoundException(99L));

        mockMvc.perform(put("/api/products/99")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    void update_shouldReturn400WhenNameIsInvalid() throws Exception {
        request.setName("X");

        mockMvc.perform(put("/api/products/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.name").exists());
    }

    @Test
    void delete_shouldReturn204WhenExists() throws Exception {
        willDoNothing().given(productService).delete(1L);

        mockMvc.perform(delete("/api/products/1"))
                .andExpect(status().isNoContent());
    }

    @Test
    void delete_shouldReturn404WhenNotFound() throws Exception {
        willThrow(new ProductNotFoundException(99L)).given(productService).delete(99L);

        mockMvc.perform(delete("/api/products/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").exists());
    }
}
