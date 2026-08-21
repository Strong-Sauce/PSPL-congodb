package com.postSale.amcProject.controllers;

import com.postSale.amcProject.Model.nodes.Product;
import com.postSale.amcProject.Services.ProductService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("api/products")
public class ProductController {

    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    // POST REQS
    @Deprecated
    @PostMapping
    public Product createProduct(
            @RequestBody Product product,
            Authentication authentication
    ) {
        return productService.createProduct(product, authentication);
    }

    // PUT REQS
    @PutMapping
    public ResponseEntity<Product> updateProduct(
            @RequestBody Product product,
            Authentication authentication
    ) {
        Product updatedProduct =
                productService.updateProduct(product, authentication);

        return ResponseEntity.ok(updatedProduct);
    }

    // GET REQS

    /**
     * Returns only products belonging to the currently authenticated user.
     */
    @GetMapping
    public List<Product> getAllProducts(Authentication authentication) {
        return productService.getAllProducts(authentication);
    }

    /**
     * Returns a product only if it belongs to the authenticated user.
     */
    @GetMapping("/{serialNumber}")
    public ResponseEntity<Product> getProduct(@PathVariable String serialNumber, Authentication authentication) {
        return productService.getProduct(serialNumber, authentication)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // DELETE REQS

    /**
     * Deletes a product only if it belongs to the authenticated user.
     */
    @DeleteMapping("/{serialNumber}")
    public ResponseEntity<Void> deleteProduct(
            @PathVariable String serialNumber,
            Authentication authentication
    ) {
        boolean deletedProduct =
                productService.deleteProduct(serialNumber, authentication);

        if (!deletedProduct) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.noContent().build();
    }
}