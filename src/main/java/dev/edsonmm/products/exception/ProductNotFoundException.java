package dev.edsonmm.products.exception;

public class ProductNotFoundException extends RuntimeException {

    public ProductNotFoundException(Long id) {
        super(String.format("No se encontró el producto con id %d", id));
    }
}
