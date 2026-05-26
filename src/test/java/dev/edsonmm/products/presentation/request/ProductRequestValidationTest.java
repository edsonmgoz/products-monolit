package dev.edsonmm.products.presentation.request;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class ProductRequestValidationTest {

    private Validator validator;

    @BeforeEach
    void setUp() {
        validator = Validation.buildDefaultValidatorFactory().getValidator();
    }

    @Test
    void shouldPassWithValidRequest() {
        Set<ConstraintViolation<ProductRequest>> violations = validator.validate(validRequest());
        assertThat(violations).isEmpty();
    }

    @Test
    void shouldPassWithNullDescription() {
        ProductRequest request = validRequest();
        request.setDescription(null);

        Set<ConstraintViolation<ProductRequest>> violations = validator.validate(request);

        assertThat(violations).isEmpty();
    }

    @Test
    void shouldPassWhenPriceIsZero() {
        ProductRequest request = validRequest();
        request.setPrice(BigDecimal.ZERO);

        Set<ConstraintViolation<ProductRequest>> violations = validator.validate(request);

        assertThat(violations).isEmpty();
    }

    @Test
    void shouldPassWhenStockIsZero() {
        ProductRequest request = validRequest();
        request.setStock(0);

        Set<ConstraintViolation<ProductRequest>> violations = validator.validate(request);

        assertThat(violations).isEmpty();
    }

    @Test
    void shouldFailWhenNameIsBlank() {
        ProductRequest request = validRequest();
        request.setName("   ");

        assertFieldViolation(request, "name");
    }

    @Test
    void shouldFailWhenNameIsTooShort() {
        ProductRequest request = validRequest();
        request.setName("A");

        assertFieldViolation(request, "name");
    }

    @Test
    void shouldFailWhenNameIsTooLong() {
        ProductRequest request = validRequest();
        request.setName("A".repeat(101));

        assertFieldViolation(request, "name");
    }

    @Test
    void shouldFailWhenDescriptionExceedsMaxLength() {
        ProductRequest request = validRequest();
        request.setDescription("D".repeat(501));

        assertFieldViolation(request, "description");
    }

    @Test
    void shouldFailWhenPriceIsNull() {
        ProductRequest request = validRequest();
        request.setPrice(null);

        assertFieldViolation(request, "price");
    }

    @Test
    void shouldFailWhenPriceIsNegative() {
        ProductRequest request = validRequest();
        request.setPrice(new BigDecimal("-0.01"));

        assertFieldViolation(request, "price");
    }

    @Test
    void shouldFailWhenStockIsNull() {
        ProductRequest request = validRequest();
        request.setStock(null);

        assertFieldViolation(request, "stock");
    }

    @Test
    void shouldFailWhenStockIsNegative() {
        ProductRequest request = validRequest();
        request.setStock(-1);

        assertFieldViolation(request, "stock");
    }

    private void assertFieldViolation(ProductRequest request, String field) {
        Set<ConstraintViolation<ProductRequest>> violations = validator.validate(request);
        assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals(field));
    }

    private ProductRequest validRequest() {
        ProductRequest r = new ProductRequest();
        r.setName("Producto válido");
        r.setDescription("Descripción opcional");
        r.setPrice(new BigDecimal("99.99"));
        r.setStock(10);
        r.setActive(true);
        return r;
    }
}
