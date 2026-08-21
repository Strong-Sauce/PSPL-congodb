package com.postSale.amcProject.Repositories;

import org.springframework.data.neo4j.core.Neo4jClient;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Repository
public class SeedDataRepositoryImpl implements SeedDataRepository {

    private final Neo4jClient neo4jClient;

    public SeedDataRepositoryImpl(Neo4jClient neo4jClient) {
        this.neo4jClient = neo4jClient;
    }

    // ============================================================
    // SEED CHECK
    // ============================================================

    @Override
    public long countUsers() {

        return neo4jClient.query("""
                MATCH (u:User)
                RETURN count(u) AS count
                """)
                .fetchAs(Long.class)
                .one()
                .orElse(0L);
    }


    // ============================================================
    // AMC OFFERS
    // ============================================================

    @Override
    public long createOffer(
            String offerId,
            String offerType,
            int durationMonths,
            double price,
            String terms
    ) {

        return neo4jClient.query("""
                CREATE (o:AMCOffer {
                    offerId: $offerId,
                    offerType: $offerType,
                    offerDurationMonths: $durationMonths,
                    offerPrice: $price,
                    offerTerms: $terms
                })
                RETURN count(o) AS count
                """)
                .bind(offerId).to("offerId")
                .bind(offerType).to("offerType")
                .bind(durationMonths).to("durationMonths")
                .bind(price).to("price")
                .bind(terms).to("terms")
                .fetchAs(Long.class)
                .one()
                .orElse(0L);
    }


    // ============================================================
    // PRODUCT + WARRANTY
    // ============================================================

    @Override
    public long createProductWithWarranty(
            String serialNumber,
            String productName,
            LocalDate createdDate,
            String category,
            String warrantyId,
            LocalDate warrantyStartDate,
            LocalDate warrantyEndDate
    ) {

        return neo4jClient.query("""
                CREATE (p:Product {
                    productSerialNumber: $serialNumber,
                    productName: $productName,
                    productCreatedDate: $createdDate,
                    productCategory: $category
                })
                CREATE (w:Warranty {
                    warrantyId: $warrantyId,
                    warrantyStartDate: $warrantyStartDate,
                    warrantyEndDate: $warrantyEndDate
                })
                CREATE (p)-[:HAS_WARRANTY]->(w)
                RETURN count(p) AS count
                """)
                .bind(serialNumber).to("serialNumber")
                .bind(productName).to("productName")
                .bind(createdDate).to("createdDate")
                .bind(category).to("category")
                .bind(warrantyId).to("warrantyId")
                .bind(warrantyStartDate).to("warrantyStartDate")
                .bind(warrantyEndDate).to("warrantyEndDate")
                .fetchAs(Long.class)
                .one()
                .orElse(0L);
    }


    // ============================================================
    // AMC
    // ============================================================

    @Override
    public long createAMC(
            String amcId,
            LocalDate startDate,
            LocalDate endDate
    ) {

        return neo4jClient.query("""
                CREATE (a:AMC {
                    amcId: $amcId,
                    amcStartDate: $startDate,
                    amcEndDate: $endDate
                })
                RETURN count(a) AS count
                """)
                .bind(amcId).to("amcId")
                .bind(startDate).to("startDate")
                .bind(endDate).to("endDate")
                .fetchAs(Long.class)
                .one()
                .orElse(0L);
    }


    // ============================================================
    // AMC + OFFER
    // ============================================================

    @Override
    public long createAMCWithOffer(
            String amcId,
            LocalDate startDate,
            LocalDate endDate,
            String offerId
    ) {

        return neo4jClient.query("""
                CREATE (a:AMC {
                    amcId: $amcId,
                    amcStartDate: $startDate,
                    amcEndDate: $endDate
                })
                WITH a
                MATCH (o:AMCOffer {offerId: $offerId})
                CREATE (a)-[:BASED_ON]->(o)
                RETURN count(a) AS count
                """)
                .bind(amcId).to("amcId")
                .bind(startDate).to("startDate")
                .bind(endDate).to("endDate")
                .bind(offerId).to("offerId")
                .fetchAs(Long.class)
                .one()
                .orElse(0L);
    }


    // ============================================================
    // WARRANTY -> AMC
    // ============================================================

    @Override
    public long linkWarrantyAMC(
            String warrantyId,
            String amcId
    ) {

        return neo4jClient.query("""
                MATCH (w:Warranty {warrantyId: $warrantyId})
                MATCH (a:AMC {amcId: $amcId})
                MERGE (w)-[:EXTENDED_BY]->(a)
                RETURN count(*) AS count
                """)
                .bind(warrantyId).to("warrantyId")
                .bind(amcId).to("amcId")
                .fetchAs(Long.class)
                .one()
                .orElse(0L);
    }


    // ============================================================
    // AMC -> OFFER
    // ============================================================

    @Override
    public long linkAMCOffer(
            String amcId,
            String offerId
    ) {

        return neo4jClient.query("""
                MATCH (a:AMC {amcId: $amcId})
                MATCH (o:AMCOffer {offerId: $offerId})
                MERGE (a)-[:BASED_ON]->(o)
                RETURN count(*) AS count
                """)
                .bind(amcId).to("amcId")
                .bind(offerId).to("offerId")
                .fetchAs(Long.class)
                .one()
                .orElse(0L);
    }


    // ============================================================
    // USER + CUSTOMER
    // ============================================================

    @Override
    public long createUserWithCustomer(
            String userId,
            String name,
            String email,
            String password,
            LocalDateTime createdAt,
            LocalDateTime updatedAt,
            String customerId,
            String customerName
    ) {

        return neo4jClient.query("""
                CREATE (u:User {
                    id: $userId,
                    name: $name,
                    email: $email,
                    password: $password,
                    createdAt: $createdAt,
                    updatedAt: $updatedAt
                })
                CREATE (c:Customer {
                    custId: $customerId,
                    custName: $customerName
                })
                CREATE (u)-[:IS_CUSTOMER]->(c)
                RETURN count(u) AS count
                """)
                .bind(userId).to("userId")
                .bind(name).to("name")
                .bind(email).to("email")
                .bind(password).to("password")
                .bind(createdAt).to("createdAt")
                .bind(updatedAt).to("updatedAt")
                .bind(customerId).to("customerId")
                .bind(customerName).to("customerName")
                .fetchAs(Long.class)
                .one()
                .orElse(0L);
    }


    // ============================================================
    // USER -> CUSTOMER
    // ============================================================

    @Override
    public long linkUserCustomer(
            String userId,
            String customerId
    ) {

        return neo4jClient.query("""
                MATCH (u:User {id: $userId})
                MATCH (c:Customer {custId: $customerId})
                MERGE (u)-[:IS_CUSTOMER]->(c)
                RETURN count(*) AS count
                """)
                .bind(userId).to("userId")
                .bind(customerId).to("customerId")
                .fetchAs(Long.class)
                .one()
                .orElse(0L);
    }


    // ============================================================
    // SALE
    // ============================================================

    @Override
    public long createSale(
            String saleId,
            LocalDate saleDate
    ) {

        return neo4jClient.query("""
                CREATE (s:Sale {
                    saleId: $saleId,
                    saleDate: $saleDate
                })
                RETURN count(s) AS count
                """)
                .bind(saleId).to("saleId")
                .bind(saleDate).to("saleDate")
                .fetchAs(Long.class)
                .one()
                .orElse(0L);
    }


    // ============================================================
    // CUSTOMER -> SALE
    // ============================================================

    @Override
    public long linkCustomerSale(
            String customerId,
            String saleId
    ) {

        return neo4jClient.query("""
                MATCH (c:Customer {custId: $customerId})
                MATCH (s:Sale {saleId: $saleId})
                MERGE (c)-[:PURCHASED]->(s)
                RETURN count(*) AS count
                """)
                .bind(customerId).to("customerId")
                .bind(saleId).to("saleId")
                .fetchAs(Long.class)
                .one()
                .orElse(0L);
    }


    // ============================================================
    // SALE -> PRODUCT
    // ============================================================

    @Override
    public long linkSaleProduct(
            String saleId,
            String serialNumber
    ) {

        return neo4jClient.query("""
                MATCH (s:Sale {saleId: $saleId})
                MATCH (p:Product {
                    productSerialNumber: $serialNumber
                })
                MERGE (s)-[:OF_PRODUCT]->(p)
                RETURN count(*) AS count
                """)
                .bind(saleId).to("saleId")
                .bind(serialNumber).to("serialNumber")
                .fetchAs(Long.class)
                .one()
                .orElse(0L);
    }


    // ============================================================
    // SALE + CUSTOMER + PRODUCT
    // ============================================================

    @Override
    public long createSaleWithProduct(
            String saleId,
            LocalDate saleDate,
            String customerId,
            String serialNumber
    ) {

        return neo4jClient.query("""
                CREATE (s:Sale {
                    saleId: $saleId,
                    saleDate: $saleDate
                })
                WITH s
                MATCH (c:Customer {custId: $customerId})
                CREATE (c)-[:PURCHASED]->(s)
                WITH s
                MATCH (p:Product {
                    productSerialNumber: $serialNumber
                })
                CREATE (s)-[:OF_PRODUCT]->(p)
                RETURN count(s) AS count
                """)
                .bind(saleId).to("saleId")
                .bind(saleDate).to("saleDate")
                .bind(customerId).to("customerId")
                .bind(serialNumber).to("serialNumber")
                .fetchAs(Long.class)
                .one()
                .orElse(0L);
    }
}