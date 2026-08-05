package com.postSale.amcProject.controllers;

import com.postSale.amcProject.Model.nodes.Product;
import com.postSale.amcProject.Services.ProductService;
import org.springframework.http.ResponseEntity;
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
    @PostMapping
    public Product createProduct(@RequestBody Product product) {
        return productService.createProduct(product);
    }

    // PUT REQS
    @PutMapping()
    public ResponseEntity<Product> updateCustomer(@RequestBody Product product){
        Product updatedProduct = productService.updateProd(product);
        return ResponseEntity.ok(updatedProduct);
    }


    // GET REQS
    @GetMapping
    public List<Product> getAllProducts(){
        return productService.getAllProducts();
    }

    @GetMapping("/{serialNumber}")
    public ResponseEntity<Product> getProduct(@PathVariable String serialNumber){
        return productService.getProduct(serialNumber)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }


    // DELETE REQS
    @DeleteMapping("/{serialNumber}")
    public ResponseEntity<Product> deleteProduct(@PathVariable String serialNumber) {
        boolean deletedProduct = productService.deleteProduct(serialNumber);
        if(!deletedProduct)
            return ResponseEntity.notFound().build();
        return ResponseEntity.noContent().build();
    }
}
