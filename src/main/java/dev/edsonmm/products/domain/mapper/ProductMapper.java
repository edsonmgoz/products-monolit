package dev.edsonmm.products.domain.mapper;

import dev.edsonmm.products.domain.entity.Product;
import dev.edsonmm.products.presentation.request.ProductRequest;
import org.springframework.stereotype.Component;

@Component
public class ProductMapper {

    public Product toEntity(ProductRequest request) {
        return apply(request, new Product());
    }

    public void updateEntity(ProductRequest request, Product product) {
        apply(request, product);
    }

    private Product apply(ProductRequest request, Product product) {
        product.setName(request.getName());
        product.setDescription(request.getDescription());
        product.setPrice(request.getPrice());
        product.setStock(request.getStock());
        product.setActive(request.isActive());
        return product;
    }
}
