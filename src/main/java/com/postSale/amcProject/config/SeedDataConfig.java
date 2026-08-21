package com.postSale.amcProject.config;

import com.postSale.amcProject.Model.enums.ProductCategory;
import com.postSale.amcProject.Model.nodes.*;
import com.postSale.amcProject.Repositories.*;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

@Configuration
@RequiredArgsConstructor
public class SeedDataConfig {

    private final UserRepository userRepository;
    private final CustomerRepository customerRepository;
    private final ProductRepository productRepository;
    private final SaleRepository saleRepository;
    private final WarrantyRepository warrantyRepository;
    private final AMCRepository amcRepository;
    private final AMCOfferRepository amcOfferRepository;
    private final PasswordEncoder passwordEncoder;
    private final Environment env;

    @Bean
    public ApplicationRunner seedDataRunner() {
        return args -> {
            // Only seed when property app.seed-data=true is set (safe default: disabled)
            boolean doSeed = Boolean.parseBoolean(env.getProperty("app.seed-data", "false"));
            if (!doSeed) {
                return;
            }

            // Avoid reseeding if users already exist
            if (userRepository.count() > 0) {
                System.out.println("Seed: Users already present - skipping seed to avoid duplicates.");
                return;
            }

            System.out.println("Seed: Starting deterministic seed data (app.seed-data=true)");

            // 1) AMC Offers (reusable)
            List<AMCOffer> offers = new ArrayList<>();
            offers.add(buildOffer("OFFER-SILVER", "Silver", 12, 99.0, "Basic coverage"));
            offers.add(buildOffer("OFFER-GOLD", "Gold", 24, 179.0, "Extended parts & labor"));
            offers.add(buildOffer("OFFER-PLAT", "Platinum", 36, 299.0, "Premium on-site support"));
            offers.add(buildOffer("OFFER-BASIC", "Basic", 6, 49.0, "Limited remote support"));
            offers.add(buildOffer("OFFER-PREMIUM", "Premium", 18, 199.0, "Priority SLA"));
            amcOfferRepository.saveAll(offers);

            // 2) Products
            List<Product> products = new ArrayList<>();
            AtomicInteger serialCounter = new AtomicInteger(1000);

            // Create a mix of product types with deterministic created dates
            for (ProductCategory cat : ProductCategory.values()) {
                // create 8 products per category approximately to hit 30-50 products
                for (int i = 0; i < 6; i++) {
                    Product p = new Product();
                    String serial = cat.getSerialPrefix() + "-" + serialCounter.getAndIncrement();
                    p.setProductSerialNumber(serial);
                    p.setProductName(cat.getDisplayName() + " Model " + (100 + i));
                    // product created date spread over the last 36 months deterministically
                    p.setProductCreatedDate(LocalDate.now().minusMonths((i * 3) + (cat.ordinal() * 2)));
                    p.setProductCategory(cat);
                    products.add(p);
                }
            }
            productRepository.saveAll(products);

            // 3) Warranties - one per product, using category default months
            List<Warranty> warranties = new ArrayList<>();
            AtomicInteger warrantyCounter = new AtomicInteger(2000);
            for (Product p : products) {
                Warranty w = new Warranty();
                w.setWarrantyId("W-" + warrantyCounter.getAndIncrement());
                LocalDate start = p.getProductCreatedDate().plusDays(7); // warranty starts 1 week after creation
                int months = p.getProductCategory().getDefaultWarrantyMonths();
                LocalDate end = start.plusMonths(months);
                w.setWarrantyStartDate(start);
                w.setWarrantyEndDate(end);
                warrantyRepository.save(w);

                // Link product -> warranty
                p.getWarrantyList().add(w);
                productRepository.save(p);
                warranties.add(w);
            }

            // 4) AMC sequences for many warranties (0..3 sequential AMCs)
            AtomicInteger amcCounter = new AtomicInteger(3000);
            for (int i = 0; i < warranties.size(); i++) {
                Warranty w = warranties.get(i);
                // Decide how many AMCs: deterministic based on index
                int numAmcs = (i % 5 == 0) ? 3 : (i % 3 == 0) ? 2 : (i % 7 == 0) ? 1 : 0;
                LocalDate prevEnd = w.getWarrantyEndDate();
                for (int a = 0; a < numAmcs; a++) {
                    AMC amc = new AMC();
                    amc.setAmcId("AMC-" + amcCounter.getAndIncrement());
                    // start the AMC the day after previous end
                    LocalDate start = prevEnd.plusDays(1);
                    // pick an offer to base on deterministically
                    AMCOffer offer = offers.get((i + a) % offers.size());
                    int months = offer.getOfferDurationMonths();
                    LocalDate end = start.plusMonths(months).minusDays(1);
                    amc.setAmcStartDate(start);
                    amc.setAmcEndDate(end);
                    // link to offer
                    amc.getAmcOfferList().add(offer);
                    amcRepository.save(amc);

                    // link warranty -> amc
                    w.getAmcList().add(amc);
                    warrantyRepository.save(w);

                    // set next previous end
                    prevEnd = end;
                }
            }

            // 5) Users + Customers + Sales linking products
            String[] names = new String[]{
                    "Alice Monroe", "Bob Patel", "Carol Nguyen", "David Chen",
                    "Esha Roy", "Franklin King", "Grace Lee", "Hassan Ali",
                    "Isha Kapoor", "Jorge Silva"
            };

            AtomicInteger userCounter = new AtomicInteger(4000);
            AtomicInteger custCounter = new AtomicInteger(5000);
            AtomicInteger saleCounter = new AtomicInteger(6000);

            int productIndex = 0;

            for (int i = 0; i < names.length; i++) {
                String name = names[i];
                String userId = "user-" + userCounter.getAndIncrement();
                String custId = "cust-" + custCounter.getAndIncrement();

                // create customer
                Customer c = new Customer();
                c.setCustId(custId);
                c.setCustName(name + " Co");
                customerRepository.save(c);

                // create user mapped to customer
                User u = new User();
                u.setId(userId);
                u.setName(name);
                u.setEmail(name.toLowerCase().replaceAll("[^a-z]", ".") + "@example.com");
                // All seeded users share the demo password - securely hashed using PasswordEncoder
                String demoPassword = "Password123!";
                u.setPassword(passwordEncoder.encode(demoPassword));
                u.setCreatedAt(LocalDateTime.now().minusDays(30 + i));
                u.setUpdatedAt(LocalDateTime.now().minusDays(1));
                u.setCustomer(c);
                userRepository.save(u);

                // number of sales per customer deterministic: some have multiple sales
                int salesForCustomer = 2 + (i % 4); // 2..5
                for (int s = 0; s < salesForCustomer; s++) {
                    Sale sale = new Sale();
                    sale.setSaleId("sale-" + saleCounter.getAndIncrement());
                    sale.setSaleDate(LocalDate.now().minusMonths(1 + (i + s)));

                    // each sale contains 1..3 products
                    int prodCount = 1 + ((i + s) % 3);
                    for (int p = 0; p < prodCount; p++) {
                        // pick product deterministically from products list
                        Product prod = products.get(productIndex % products.size());
                        sale.getProductList().add(prod);
                        productIndex++;
                    }

                    saleRepository.save(sale);

                    // link customer -> sale
                    c.getPurchases().add(sale);
                    customerRepository.save(c);
                }
            }

            System.out.println("Seed: Completed. Created users, customers, sales, products, warranties, AMCs, and offers.");
            System.out.println("Seed: Demo password for all seeded users = Password123! (stored as BCrypt hash)");
        };
    }

    private AMCOffer buildOffer(String id, String type, int months, double price, String terms) {
        AMCOffer o = new AMCOffer();
        o.setOfferId(id);
        o.setOfferType(type);
        o.setOfferDurationMonths(months);
        o.setOfferPrice(price);
        o.setOfferTerms(terms);
        return o;
    }
}
