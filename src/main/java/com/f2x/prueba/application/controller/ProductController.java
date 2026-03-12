package com.f2x.prueba.application.controller;

import java.net.URI;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.f2x.prueba.application.dto.product.CreateProductRequestDto;
import com.f2x.prueba.application.dto.product.ProductResponseDto;
import com.f2x.prueba.application.dto.product.UpdateProductRequestDto;
import com.f2x.prueba.domain.model.Product;
import com.f2x.prueba.domain.service.ProductService;

import jakarta.validation.Valid;


@RestController
@RequestMapping("/api/v1/products")
public class ProductController {
    
    private final ProductService productService;
    
    public ProductController(ProductService productService) {
        this.productService = productService;
    }
    
    @PostMapping
    public ResponseEntity<ProductResponseDto> createProduct(
            @Valid @RequestBody CreateProductRequestDto request) {
        
        Product product = toDomain(request);
        
        Product createdProduct = productService.createProduct(product);
        
        ProductResponseDto response = ProductResponseDto.fromDomain(createdProduct);
        
        return ResponseEntity
            .created(URI.create("/api/v1/products/" + createdProduct.getId()))
            .body(response);
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<ProductResponseDto> getProduct(@PathVariable UUID id) {
        return productService.getProductById(id)
            .map(product -> ResponseEntity.ok(ProductResponseDto.fromDomain(product)))
            .orElse(ResponseEntity.notFound().build());
    }
    
    @GetMapping("/account/{accountNumber}")
    public ResponseEntity<ProductResponseDto> getProductByAccountNumber(
            @PathVariable String accountNumber) {
        return productService.getProductByAccountNumber(accountNumber)
            .map(product -> ResponseEntity.ok(ProductResponseDto.fromDomain(product)))
            .orElse(ResponseEntity.notFound().build());
    }
    
    @GetMapping("/client/{clientId}")
    public ResponseEntity<List<ProductResponseDto>> getProductsByClientId(
            @PathVariable UUID clientId) {
        List<ProductResponseDto> products = productService.getProductsByClientId(clientId).stream()
            .map(ProductResponseDto::fromDomain)
            .collect(Collectors.toList());
        
        return ResponseEntity.ok(products);
    }
    
    @GetMapping
    public ResponseEntity<List<ProductResponseDto>> getAllProducts() {
        List<ProductResponseDto> products = productService.getAllProducts().stream()
            .map(ProductResponseDto::fromDomain)
            .collect(Collectors.toList());
        
        return ResponseEntity.ok(products);
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<ProductResponseDto> updateProduct(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateProductRequestDto request) {
        
        Product productData = toDomain(request);
        
        Product updatedProduct = productService.updateProduct(id, productData);
        
        ProductResponseDto response = ProductResponseDto.fromDomain(updatedProduct);
        
        return ResponseEntity.ok(response);
    }
    
    @PatchMapping("/{id}/status")
    public ResponseEntity<ProductResponseDto> changeProductStatus(
            @PathVariable UUID id,
            @RequestBody UpdateProductRequestDto request) {
        
        if (request.getStatus() == null) {
            return ResponseEntity.badRequest().build();
        }
        
        Product updatedProduct = productService.changeProductStatus(id, request.getStatus());
        ProductResponseDto response = ProductResponseDto.fromDomain(updatedProduct);
        
        return ResponseEntity.ok(response);
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProduct(@PathVariable UUID id) {
        productService.deleteProduct(id);
        return ResponseEntity.noContent().build();
    }
    
    private Product toDomain(CreateProductRequestDto request) {
        return Product.builder()
            .type(request.getType())
            .balance(request.getBalance())
            .gmfExempt(request.getGmfExempt())
            .clientId(request.getClientId())
            .build();
    }
    
    private Product toDomain(UpdateProductRequestDto request) {
        return Product.builder()
            .status(request.getStatus())
            .balance(request.getBalance())
            .gmfExempt(request.getGmfExempt())
            .build();
    }
}
