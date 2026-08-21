package com.postSale.amcProject.Services;

import com.postSale.amcProject.Exceptions.ResourceNotFoundException;
import com.postSale.amcProject.Model.enums.ProductCategory;
import com.postSale.amcProject.Model.nodes.Customer;
import com.postSale.amcProject.Model.nodes.Product;
import com.postSale.amcProject.Model.nodes.Warranty;
import com.postSale.amcProject.Repositories.ProductRepository;
import com.postSale.amcProject.Repositories.SaleRepository;
import com.postSale.amcProject.Repositories.UserRepository;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.*;

@Service
public class ProductService {

    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final SaleRepository saleRepository;

    public ProductService(
            ProductRepository productRepository,
            UserRepository userRepository,
            SaleRepository saleRepository
    ) {
        this.productRepository = productRepository;
        this.userRepository = userRepository;
        this.saleRepository = saleRepository;
    }
    // *******************************************
    // SERVICES
    // *******************************************
    @Transactional
    public Product createProduct(Product product, Authentication authentication) {

        validateProduct(product);

        if (authentication == null || !authentication.isAuthenticated()) {
            throw new IllegalStateException("Authenticated user is required to create a product.");
        }

        String email = authentication.getName();

        Customer customer = userRepository.findCustomerByUserEmail(email)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Customer",
                                "for authenticated user"
                        )
                );

        // Set creation date
        product.setProductCreatedDate(LocalDate.now());

        // Generate unique serial number
        product.setProductSerialNumber(generateUniqueSerialNumber(product.getProductCategory()));

        // Create initial warranty
        Warranty warranty = createInitialWarranty(product);

        // Attach warranty to product
        product.setWarrantyList(new ArrayList<>(List.of(warranty)));

        // Create the Product + Warranty graph
        productRepository.createProductWithWarranty(
                product.getProductSerialNumber(),
                product.getProductName(),
                product.getProductCreatedDate(),
                product.getProductCategory().name(),
                warranty.getWarrantyId(),
                warranty.getWarrantyStartDate(),
                warranty.getWarrantyEndDate()
        );

        // Create the Sale representing this product purchase
        String saleId = UUID.randomUUID().toString();

        saleRepository.createSale(saleId, product.getProductCreatedDate());

        // Connect Customer -> Sale
        saleRepository.linkCustomer(customer.getCustId(), saleId);

        // Connect Sale -> Product
        saleRepository.linkProduct(saleId, product.getProductSerialNumber());

        return product;
    }

    @Transactional(readOnly = true)
    public List<Product> getAllProducts(Authentication authentication) {
        String customerId = getCustomerId(authentication);
        System.out.println("PRODUCT ACCESS -> email=" + authentication.getName() + ", customerId=" + customerId);

        return productRepository.findAllProductsByCustomerId(customerId);
    }

    @Transactional(readOnly = true)
    public Optional<Product> getProduct(String serialNumber, Authentication authentication) {
        String customerId = getCustomerId(authentication);

        return productRepository.findProductByCustomerId(customerId, serialNumber)
                .map(product -> {List<Warranty> warranties = productRepository.findWarrantiesByProductSerialNumber(serialNumber);
            product.setWarrantyList(warranties);
            return product;
        });
    }

    @Transactional
    public Product updateProduct( Product product, Authentication authentication ) {

        validateProduct(product);

        String customerId = getCustomerId(authentication);

        if (!productRepository.existsProductByCustomerId( customerId, product.getProductSerialNumber() )) {
            throw new ResourceNotFoundException( "Product", product.getProductSerialNumber() );
        }

        productRepository.updateProduct(
                customerId,
                product.getProductSerialNumber(),
                product.getProductName(),
                product.getProductCreatedDate(),
                product.getProductCategory().name()
        );
        return product;
    }

    @Transactional
    public boolean deleteProduct( String serialNumber, Authentication authentication ) {

        if (serialNumber == null || serialNumber.trim().isEmpty()) {
            throw new IllegalArgumentException( "Product Serial Number is required." );
        }

        String customerId = getCustomerId(authentication);

        if (!productRepository.existsProductByCustomerId( customerId, serialNumber )) {
            throw new ResourceNotFoundException( "Product", serialNumber );
        }

        productRepository.deleteLinkedAMCs(serialNumber);
        productRepository.deleteLinkedWarranties(serialNumber);
        productRepository.deleteProductNode(serialNumber);

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

    private String getCustomerId(Authentication authentication) {

        if (authentication == null || !authentication.isAuthenticated()) {
            throw new IllegalArgumentException( "User must be authenticated." );
        }

        return userRepository.findCustomerIdByEmail(authentication.getName())
                .orElseThrow(() -> new ResourceNotFoundException( "Customer", authentication.getName()));
    }
}
