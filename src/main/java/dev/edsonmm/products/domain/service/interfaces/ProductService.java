package dev.edsonmm.products.domain.service.interfaces;

import dev.edsonmm.products.domain.entity.Product;
import dev.edsonmm.products.presentation.request.ProductRequest;

import java.util.List;

public interface ProductService {

    List<Product> findAll();

    Product findById(Long id);

    Product create(ProductRequest request);

    Product update(Long id, ProductRequest request);

    void delete(Long id);
}
