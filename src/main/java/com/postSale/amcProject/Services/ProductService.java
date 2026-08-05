package com.postSale.amcProject.Services;

import com.postSale.amcProject.Exceptions.ResourceNotFoundException;
import com.postSale.amcProject.Model.enums.ProductCategory;
import com.postSale.amcProject.Model.nodes.Product;
import com.postSale.amcProject.Model.nodes.Warranty;
import com.postSale.amcProject.Repositories.ProductRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.*;

@Service
public class ProductService {

    private final ProductRepository productRepository;

    public ProductService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    // *******************************************
    // SERVICES
    // *******************************************
    @Transactional
    public Product createProduct(Product product) {

        validateProduct(product);

        // Set creation date
        product.setProductCreatedDate(LocalDate.now());

        // Generate unique serial number
        product.setProductSerialNumber(generateUniqueSerialNumber(product.getProductCategory()));

        // Create initial warranty
        Warranty warranty = createInitialWarranty(product);

        // Attach warranty to product
        product.setWarrantyList(new ArrayList<>(List.of(warranty)));

        // Save everything
        return productRepository.save(product);
    }

    @Transactional(readOnly = true)
    public List<Product> getAllProducts() {
        return productRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Optional<Product> getProduct(String serialNumber) {
        return productRepository.findByProductSerialNumber(serialNumber);
    }

    @Transactional
    public Product updateProd(Product product) {
        validateProduct(product);
        if (!productRepository.existsById(product.getProductSerialNumber())) {
            throw new ResourceNotFoundException("Product", product.getProductSerialNumber());
        }
        return productRepository.save(product);
    }

    @Transactional
    public boolean deleteProduct(String serialNumber) {
        if (serialNumber == null || serialNumber.trim().isEmpty()) {
            throw new IllegalArgumentException("Product Serial Number is required.");
        }
        if (!productRepository.existsById(serialNumber)) {
            throw new ResourceNotFoundException("Product", serialNumber);
        }
        productRepository.deleteProduct(serialNumber);
        return true;
    }

    // *******************************************
    // HELPER METHODS
    // *******************************************
    private void validateProduct(Product product) {

        if (product == null) {
            throw new IllegalArgumentException("Product cannot be null.");
        }

        if (product.getProductName() == null ||
                product.getProductName().trim().isEmpty()) {
            throw new IllegalArgumentException("Product name is required.");
        }

        if (product.getProductCategory() == null) {
            throw new IllegalArgumentException("Product category is required.");
        }
    }

    private String generateUniqueSerialNumber(ProductCategory category) {

        String serial;
        do {
            String random =
                    UUID.randomUUID()
                            .toString()
                            .replace("-", "")
                            .substring(0, 6)
                            .toUpperCase();

            serial = category.getSerialPrefix() + "-" + random;
        } while (productRepository.existsByProductSerialNumber(serial));

        return serial;
    }

    private Warranty createInitialWarranty(Product product) {

        Warranty warranty = new Warranty();
        String warrantyId = "WAR-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        warranty.setWarrantyId(warrantyId);
        LocalDate startDate = product.getProductCreatedDate();
        warranty.setWarrantyStartDate(startDate);
        warranty.setWarrantyEndDate(
                startDate.plusMonths(
                        product.getProductCategory().getDefaultWarrantyMonths()
                )
        );

        return warranty;
    }
}
