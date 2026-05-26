package dev.edsonmm.products.domain.mapper;

import dev.edsonmm.products.domain.entity.Product;
import dev.edsonmm.products.presentation.request.ProductRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

class ProductMapperTest {

    private ProductMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = new ProductMapper();
    }

    @Test
    void toEntity_shouldMapAllFields() {
        ProductRequest request = buildRequest("Laptop", "Laptop gamer", new BigDecimal("1500.00"), 10, true);

        Product product = mapper.toEntity(request);

        assertThat(product.getName()).isEqualTo("Laptop");
        assertThat(product.getDescription()).isEqualTo("Laptop gamer");
        assertThat(product.getPrice()).isEqualByComparingTo("1500.00");
        assertThat(product.getStock()).isEqualTo(10);
        assertThat(product.isActive()).isTrue();
    }

    @Test
    void toEntity_shouldMapWithNullDescription() {
        ProductRequest request = buildRequest("Mouse", null, new BigDecimal("25.00"), 50, true);

        Product product = mapper.toEntity(request);

        assertThat(product.getDescription()).isNull();
    }

    @Test
    void toEntity_shouldMapActiveAsFalse() {
        ProductRequest request = buildRequest("Teclado", null, new BigDecimal("75.00"), 5, false);

        Product product = mapper.toEntity(request);

        assertThat(product.isActive()).isFalse();
    }

    @Test
    void updateEntity_shouldOverwriteAllFields() {
        Product existing = new Product();
        existing.setName("Viejo");
        existing.setPrice(new BigDecimal("100.00"));
        existing.setStock(1);
        existing.setActive(true);

        ProductRequest request = buildRequest("Nuevo", "Desc nueva", new BigDecimal("200.00"), 20, false);
        mapper.updateEntity(request, existing);

        assertThat(existing.getName()).isEqualTo("Nuevo");
        assertThat(existing.getDescription()).isEqualTo("Desc nueva");
        assertThat(existing.getPrice()).isEqualByComparingTo("200.00");
        assertThat(existing.getStock()).isEqualTo(20);
        assertThat(existing.isActive()).isFalse();
    }

    @Test
    void updateEntity_shouldOverwriteDescriptionWithNull() {
        Product existing = new Product();
        existing.setDescription("Descripción anterior");

        ProductRequest request = buildRequest("Producto", null, new BigDecimal("10.00"), 1, true);
        mapper.updateEntity(request, existing);

        assertThat(existing.getDescription()).isNull();
    }

    private ProductRequest buildRequest(String name, String description, BigDecimal price, int stock, boolean active) {
        ProductRequest r = new ProductRequest();
        r.setName(name);
        r.setDescription(description);
        r.setPrice(price);
        r.setStock(stock);
        r.setActive(active);
        return r;
    }
}
