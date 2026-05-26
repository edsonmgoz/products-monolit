package dev.edsonmm.products.domain.service;

import dev.edsonmm.products.data.repository.ProductRepository;
import dev.edsonmm.products.domain.entity.Product;
import dev.edsonmm.products.domain.service.interfaces.ProductService;
import dev.edsonmm.products.exception.ProductNotFoundException;
import dev.edsonmm.products.presentation.request.ProductRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@Transactional
class ProductServiceImplTest {

    @Autowired
    private ProductService productService;

    @Autowired
    private ProductRepository productRepository;

    @BeforeEach
    void setUp() {
        productRepository.deleteAll();
    }

    @Test
    void findAll_shouldReturnAllProducts() {
        productRepository.save(buildProduct("Laptop", new BigDecimal("1500.00"), 5));
        productRepository.save(buildProduct("Mouse", new BigDecimal("25.00"), 100));

        List<Product> products = productService.findAll();

        assertThat(products).hasSize(2);
        assertThat(products).extracting(Product::getName).containsExactlyInAnyOrder("Laptop", "Mouse");
    }

    @Test
    void findAll_shouldReturnEmptyListWhenNoProducts() {
        List<Product> products = productService.findAll();

        assertThat(products).isEmpty();
    }

    @Test
    void findById_shouldReturnProductWhenExists() {
        Product saved = productRepository.save(buildProduct("Teclado", new BigDecimal("75.00"), 20));

        Product found = productService.findById(saved.getId());

        assertThat(found.getName()).isEqualTo("Teclado");
        assertThat(found.getPrice()).isEqualByComparingTo("75.00");
        assertThat(found.getStock()).isEqualTo(20);
    }

    @Test
    void findById_shouldThrowWhenNotFound() {
        assertThatThrownBy(() -> productService.findById(999L))
                .isInstanceOf(ProductNotFoundException.class)
                .hasMessageContaining("999");
    }

    @Test
    void create_shouldPersistAndReturnProduct() {
        ProductRequest request = buildRequest("Monitor", "Monitor 4K", new BigDecimal("800.00"), 3);

        Product created = productService.create(request);

        assertThat(created.getId()).isNotNull();
        assertThat(created.getName()).isEqualTo("Monitor");
        assertThat(created.getDescription()).isEqualTo("Monitor 4K");
        assertThat(created.getPrice()).isEqualByComparingTo("800.00");
        assertThat(created.getStock()).isEqualTo(3);
        assertThat(created.isActive()).isTrue();
    }

    @Test
    void create_shouldSetTimestampsOnCreation() {
        ProductRequest request = buildRequest("Audífonos", null, new BigDecimal("50.00"), 15);

        Product created = productService.create(request);

        assertThat(created.getCreatedAt()).isNotNull();
        assertThat(created.getUpdatedAt()).isNotNull();
    }

    @Test
    void update_shouldModifyAndReturnProduct() {
        Product saved = productRepository.save(buildProduct("Viejo", new BigDecimal("100.00"), 1));
        ProductRequest request = buildRequest("Nuevo", "Actualizado", new BigDecimal("200.00"), 5);

        Product updated = productService.update(saved.getId(), request);

        assertThat(updated.getName()).isEqualTo("Nuevo");
        assertThat(updated.getDescription()).isEqualTo("Actualizado");
        assertThat(updated.getPrice()).isEqualByComparingTo("200.00");
        assertThat(updated.getStock()).isEqualTo(5);
    }

    @Test
    void update_shouldThrowWhenProductNotFound() {
        ProductRequest request = buildRequest("Producto", null, new BigDecimal("10.00"), 1);

        assertThatThrownBy(() -> productService.update(999L, request))
                .isInstanceOf(ProductNotFoundException.class)
                .hasMessageContaining("999");
    }

    @Test
    void delete_shouldRemoveProduct() {
        Product saved = productRepository.save(buildProduct("A eliminar", new BigDecimal("30.00"), 2));
        Long id = saved.getId();

        productService.delete(id);

        assertThat(productRepository.findById(id)).isEmpty();
    }

    @Test
    void delete_shouldThrowWhenNotFound() {
        assertThatThrownBy(() -> productService.delete(999L))
                .isInstanceOf(ProductNotFoundException.class)
                .hasMessageContaining("999");
    }

    private Product buildProduct(String name, BigDecimal price, int stock) {
        Product p = new Product();
        p.setName(name);
        p.setPrice(price);
        p.setStock(stock);
        p.setActive(true);
        return p;
    }

    private ProductRequest buildRequest(String name, String description, BigDecimal price, int stock) {
        ProductRequest r = new ProductRequest();
        r.setName(name);
        r.setDescription(description);
        r.setPrice(price);
        r.setStock(stock);
        r.setActive(true);
        return r;
    }
}
