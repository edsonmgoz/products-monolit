package dev.edsonmm.products.domain.service.implement;

import dev.edsonmm.products.data.repository.ProductRepository;
import dev.edsonmm.products.domain.entity.Product;
import dev.edsonmm.products.domain.mapper.ProductMapper;
import dev.edsonmm.products.domain.service.interfaces.ProductService;
import dev.edsonmm.products.exception.ProductNotFoundException;
import dev.edsonmm.products.presentation.request.ProductRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;
    private final ProductMapper productMapper;

    @Override
    @Transactional(readOnly = true)
    public List<Product> findAll() {
        return productRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public Product findById(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new ProductNotFoundException(id));
    }

    @Override
    @Transactional
    public Product create(ProductRequest request) {
        Product product = productMapper.toEntity(request);
        return productRepository.save(product);
    }

    @Override
    @Transactional
    public Product update(Long id, ProductRequest request) {
        Product product = findById(id);
        productMapper.updateEntity(request, product);
        return productRepository.save(product);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        Product product = findById(id);
        productRepository.delete(product);
    }
}
