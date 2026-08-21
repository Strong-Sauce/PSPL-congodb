package com.postSale.amcProject.config;

import com.postSale.amcProject.Model.enums.ProductCategory;
import com.postSale.amcProject.Repositories.SeedDataRepository;
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

    private final SeedDataRepository seedDataRepository;
    private final PasswordEncoder passwordEncoder;
    private final Environment env;

    @Bean
    public ApplicationRunner seedDataRunner() {

        return args -> {

            // =========================================================
            // 0. CHECK WHETHER SEEDING IS ENABLED
            // =========================================================

            boolean doSeed = Boolean.parseBoolean(
                    env.getProperty("app.seed-data", "false")
            );

            if (!doSeed) {
                return;
            }

            // =========================================================
            // 1. PREVENT DUPLICATE SEEDING
            // =========================================================

            if (seedDataRepository.countUsers() > 0) {
                System.out.println(
                        "Seed: Users already exist. Skipping seed to avoid duplicates."
                );
                return;
            }

            System.out.println("=================================================");
            System.out.println("Seed: Starting database seed...");
            System.out.println("=================================================");


            // =========================================================
            // 2. CREATE AMC OFFERS
            // =========================================================

            seedOffers();

            System.out.println("Seed: AMC offers created.");


            // =========================================================
            // 3. CREATE PRODUCTS + WARRANTIES
            // =========================================================

            List<SeedProduct> products = seedProducts();

            System.out.println(
                    "Seed: Products + warranties created: "
                            + products.size()
            );


            // =========================================================
            // 4. CREATE AMC CHAINS
            // =========================================================

            seedAMCs(products);

            System.out.println("Seed: AMC chains created.");


            // =========================================================
            // 5. CREATE USERS + CUSTOMERS + SALES
            // =========================================================

            seedUsersAndSales(products);

            System.out.println("=================================================");
            System.out.println("Seed: Database seeding completed successfully.");
            System.out.println("=================================================");

            System.out.println(
                    "Seed: Demo password for all users = Password123!"
            );
        };
    }


    // =============================================================
    // AMC OFFERS
    // =============================================================

    private void seedOffers() {

        seedDataRepository.createOffer(
                "OFFER-BASIC",
                "Basic",
                6,
                49.0,
                "Limited remote support"
        );

        seedDataRepository.createOffer(
                "OFFER-SILVER",
                "Silver",
                12,
                99.0,
                "Basic coverage"
        );

        seedDataRepository.createOffer(
                "OFFER-GOLD",
                "Gold",
                24,
                179.0,
                "Extended parts and labor"
        );

        seedDataRepository.createOffer(
                "OFFER-PREMIUM",
                "Premium",
                18,
                199.0,
                "Priority SLA"
        );

        seedDataRepository.createOffer(
                "OFFER-PLAT",
                "Platinum",
                36,
                299.0,
                "Premium on-site support"
        );
    }


    // =============================================================
    // PRODUCTS + WARRANTIES
    // =============================================================

    private List<SeedProduct> seedProducts() {

        List<SeedProduct> products = new ArrayList<>();

        AtomicInteger serialCounter =
                new AtomicInteger(1000);

        AtomicInteger warrantyCounter =
                new AtomicInteger(2000);

        for (ProductCategory category : ProductCategory.values()) {

            for (int i = 0; i < 6; i++) {

                String serialNumber =
                        category.getSerialPrefix()
                                + "-"
                                + serialCounter.getAndIncrement();

                String productName =
                        category.getDisplayName()
                                + " Model "
                                + (100 + i);

                LocalDate createdDate =
                        LocalDate.now()
                                .minusMonths(
                                        (long) i * 3
                                                + (long) category.ordinal() * 2
                                );

                String warrantyId =
                        "W-" + warrantyCounter.getAndIncrement();

                LocalDate warrantyStartDate =
                        createdDate.plusDays(7);

                LocalDate warrantyEndDate =
                        warrantyStartDate.plusMonths(
                                category.getDefaultWarrantyMonths()
                        );

                /*
                 * Product
                 *    |
                 *    | HAS_WARRANTY
                 *    v
                 * Warranty
                 */
                seedDataRepository.createProductWithWarranty(
                        serialNumber,
                        productName,
                        createdDate,
                        category.name(),
                        warrantyId,
                        warrantyStartDate,
                        warrantyEndDate
                );

                products.add(
                        new SeedProduct(
                                serialNumber,
                                warrantyId,
                                warrantyEndDate
                        )
                );
            }
        }

        return products;
    }


    // =============================================================
    // AMC CHAINS
    // =============================================================

    private void seedAMCs(List<SeedProduct> products) {

        AtomicInteger amcCounter =
                new AtomicInteger(3000);

        String[] offerIds = {
                "OFFER-BASIC",
                "OFFER-SILVER",
                "OFFER-GOLD",
                "OFFER-PREMIUM",
                "OFFER-PLAT"
        };

        for (int i = 0; i < products.size(); i++) {

            SeedProduct product = products.get(i);

            int numberOfAMCs;

            if (i % 5 == 0) {
                numberOfAMCs = 3;
            } else if (i % 3 == 0) {
                numberOfAMCs = 2;
            } else if (i % 7 == 0) {
                numberOfAMCs = 1;
            } else {
                numberOfAMCs = 0;
            }

            LocalDate previousEnd =
                    product.warrantyEndDate();

            for (int a = 0; a < numberOfAMCs; a++) {

                String amcId =
                        "AMC-" + amcCounter.getAndIncrement();

                LocalDate startDate =
                        previousEnd.plusDays(1);

                String offerId =
                        offerIds[(i + a) % offerIds.length];

                int durationMonths =
                        getOfferDurationMonths(offerId);

                LocalDate endDate =
                        startDate
                                .plusMonths(durationMonths)
                                .minusDays(1);

                /*
                 * AMC -> AMCOffer
                 */
                seedDataRepository.createAMCWithOffer(
                        amcId,
                        startDate,
                        endDate,
                        offerId
                );

                /*
                 * Warranty -> AMC
                 */
                seedDataRepository.linkWarrantyAMC(
                        product.warrantyId(),
                        amcId
                );

                previousEnd = endDate;
            }
        }
    }


    // =============================================================
    // USERS + CUSTOMERS + SALES
    // =============================================================

    private void seedUsersAndSales(
            List<SeedProduct> products
    ) {

        String[] names = {
                "Alice Monroe",
                "Bob Patel",
                "Carol Nguyen",
                "David Chen",
                "Esha Roy",
                "Franklin King",
                "Grace Lee",
                "Hassan Ali",
                "Isha Kapoor",
                "Jorge Silva"
        };

        AtomicInteger userCounter =
                new AtomicInteger(4000);

        AtomicInteger customerCounter =
                new AtomicInteger(5000);

        AtomicInteger saleCounter =
                new AtomicInteger(6000);

        int productIndex = 0;

        String encodedPassword =
                passwordEncoder.encode("Password123!");

        for (int i = 0; i < names.length; i++) {

            String name = names[i];

            String userId =
                    "user-" + userCounter.getAndIncrement();

            String customerId =
                    "cust-" + customerCounter.getAndIncrement();

            String email =
                    buildEmail(name);

            LocalDateTime createdAt =
                    LocalDateTime.now()
                            .minusDays(30L + i);

            LocalDateTime updatedAt =
                    LocalDateTime.now()
                            .minusDays(1);

            /*
             * User
             *   |
             *   | IS_CUSTOMER
             *   v
             * Customer
             */
            seedDataRepository.createUserWithCustomer(
                    userId,
                    name,
                    email,
                    encodedPassword,
                    createdAt,
                    updatedAt,
                    customerId,
                    name + " Co"
            );

            /*
             * Each customer gets 2-5 sales.
             */
            int salesForCustomer =
                    2 + (i % 4);

            for (int s = 0;
                 s < salesForCustomer;
                 s++) {

                String saleId =
                        "sale-" + saleCounter.getAndIncrement();

                LocalDate saleDate =
                        LocalDate.now()
                                .minusMonths(1L + i + s);

                /*
                 * Create the Sale and connect it to Customer.
                 */
                seedDataRepository.createSale(
                        saleId,
                        saleDate
                );

                seedDataRepository.linkCustomerSale(
                        customerId,
                        saleId
                );

                /*
                 * Give each sale 1-3 products.
                 *
                 * This is important because the intended graph is:
                 *
                 * Customer
                 *    |
                 *    | PURCHASED
                 *    v
                 *  Sale
                 *    |
                 *    | OF_PRODUCT
                 *    +------> Product
                 *    |
                 *    +------> Product
                 *    |
                 *    +------> Product
                 */
                int productsInSale =
                        1 + ((i + s) % 3);

                for (int p = 0;
                     p < productsInSale;
                     p++) {

                    SeedProduct product =
                            products.get(
                                    productIndex
                                            % products.size()
                            );

                    seedDataRepository.linkSaleProduct(
                            saleId,
                            product.serialNumber()
                    );

                    productIndex++;
                }
            }
        }
    }


    // =============================================================
    // HELPERS
    // =============================================================

    private String buildEmail(String name) {

        return name
                .toLowerCase()
                .replaceAll("[^a-z]+", ".")
                + "@example.com";
    }


    private int getOfferDurationMonths(
            String offerId
    ) {

        return switch (offerId) {

            case "OFFER-BASIC" ->
                    6;

            case "OFFER-SILVER" ->
                    12;

            case "OFFER-GOLD" ->
                    24;

            case "OFFER-PREMIUM" ->
                    18;

            case "OFFER-PLAT" ->
                    36;

            default ->
                    throw new IllegalArgumentException(
                            "Unknown AMC offer: " + offerId
                    );
        };
    }


    // =============================================================
    // SEED PRODUCT RECORD
    // =============================================================

    private record SeedProduct(
            String serialNumber,
            String warrantyId,
            LocalDate warrantyEndDate
    ) {
    }
}