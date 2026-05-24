package dev.edsonmm.products;

import dev.edsonmm.products.data.repository.ProductRepository;
import dev.edsonmm.products.domain.entity.Product;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final ProductRepository productRepository;

    @Override
    public void run(String... args) {
        if (productRepository.count() > 0) {
            return;
        }
        productRepository.saveAll(List.of(
                product("Laptop Pro 14", "High-performance laptop with 14-inch IPS display and 16GB RAM", new BigDecimal("1299.99"), 15),
                product("Wireless Mouse", "Ergonomic wireless mouse with silent clicks and long battery life", new BigDecimal("29.99"), 50),
                product("Mechanical Keyboard", "Compact RGB mechanical keyboard with tactile brown switches", new BigDecimal("89.99"), 30),
                product("USB-C Dock", "7-in-1 USB-C docking station with HDMI, USB 3.0, and SD card reader", new BigDecimal("59.99"), 25),
                product("Monitor 27\"", "4K UHD 27-inch IPS monitor with 99% sRGB color coverage", new BigDecimal("399.99"), 10)
        ));
    }

    private Product product(String name, String description, BigDecimal price, int stock) {
        Product p = new Product();
        p.setName(name);
        p.setDescription(description);
        p.setPrice(price);
        p.setStock(stock);
        return p;
    }
}
