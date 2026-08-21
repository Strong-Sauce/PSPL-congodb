package com.postSale.amcProject.Services;

import com.postSale.amcProject.DTO.product.ProductCreateRequest;
import com.postSale.amcProject.DTO.sale.PurchaseRequest;
import com.postSale.amcProject.Exceptions.ResourceNotFoundException;
import com.postSale.amcProject.Model.enums.ProductCategory;
import com.postSale.amcProject.Model.nodes.Product;
import com.postSale.amcProject.Model.nodes.Sale;
import com.postSale.amcProject.Model.nodes.Warranty;
import com.postSale.amcProject.Repositories.ProductRepository;
import com.postSale.amcProject.Repositories.SaleRepository;
import com.postSale.amcProject.Repositories.UserRepository;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class SaleService {

    private final SaleRepository saleRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;

    public SaleService(
            SaleRepository saleRepository,
            ProductRepository productRepository,
            UserRepository userRepository
    ) {
        this.saleRepository = saleRepository;
        this.productRepository = productRepository;
        this.userRepository = userRepository;
    }

    /**
     * Creates a complete purchase for the currently authenticated user.
     * User ownership is resolved server-side:
     * User -> Customer -> Sale -> Products -> Warranties
     */
    @Transactional
    public Sale createPurchase(PurchaseRequest request, Authentication authentication) {

        if (authentication == null || !authentication.isAuthenticated()) {
            throw new IllegalArgumentException("User must be authenticated.");
        }

        String email = authentication.getName();

        // Resolve the authenticated user's Customer.
        String customerId = userRepository.findCustomerIdByEmail(email)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Customer", email)
                );

        // Create Sale ID server-side.
        String saleId = UUID.randomUUID().toString();

        String createdSaleId = saleRepository.createSaleForCustomer( customerId, saleId, request.saleDate() );

        if (createdSaleId == null) { throw new ResourceNotFoundException( "Customer", customerId ); }

        List<Product> createdProducts = new ArrayList<>();

        for (ProductCreateRequest productRequest : request.products()) {

            ProductCategory category = productRequest.productCategory();

            String productSerialNumber = generateUniqueSerialNumber(category);

            Warranty warranty = createInitialWarranty(productSerialNumber, request.saleDate(), category);

            productRepository.createProductWithWarranty(
                    productSerialNumber,
                    productRequest.productName().trim(),
                    request.saleDate(),
                    category.name(),
                    warranty.getWarrantyId(),
                    warranty.getWarrantyStartDate(),
                    warranty.getWarrantyEndDate()
            );

            saleRepository.linkProduct(saleId, productSerialNumber);

            Product product = new Product();
            product.setProductSerialNumber(productSerialNumber);
            product.setProductName(productRequest.productName().trim());
            product.setProductCreatedDate(request.saleDate());
            product.setProductCategory(category);
            product.setWarrantyList(new ArrayList<>(List.of(warranty)));

            createdProducts.add(product);
        }

        Sale sale = new Sale();
        sale.setSaleId(saleId);
        sale.setSaleDate(request.saleDate());
        sale.setProductList(createdProducts);

        return sale;
    }

    @Transactional(readOnly = true)
    public List<Sale> getSalesForCustomer(String customerId) {
        return saleRepository.findAllSalesByCustomerId(customerId);
    }

    private String generateUniqueSerialNumber(ProductCategory category) {

        String serial;
        do {
            String random = UUID.randomUUID()
                            .toString()
                            .replace("-", "")
                            .substring(0, 6)
                            .toUpperCase();

            serial = category.getSerialPrefix() + "-" + random;

        } while (productRepository.existsByProductSerialNumber(serial));

        return serial;
    }

    private Warranty createInitialWarranty(String productSerialNumber, LocalDate saleDate, ProductCategory category) {

        Warranty warranty = new Warranty();

        String warrantyId = "WAR-" + UUID.randomUUID()
                                        .toString()
                                        .substring(0, 8)
                                        .toUpperCase();

        warranty.setWarrantyId(warrantyId);
        warranty.setWarrantyStartDate(saleDate);

        warranty.setWarrantyEndDate( saleDate.plusMonths( category.getDefaultWarrantyMonths() ) );

        return warranty;
    }
}