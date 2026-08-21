package com.postSale.amcProject.Repositories;

import java.time.LocalDate;
import java.time.LocalDateTime;

public interface SeedDataRepository {

    long countUsers();

    long createOffer(
            String offerId,
            String offerType,
            int durationMonths,
            double price,
            String terms
    );

    long createProductWithWarranty(
            String serialNumber,
            String productName,
            LocalDate createdDate,
            String category,
            String warrantyId,
            LocalDate warrantyStartDate,
            LocalDate warrantyEndDate
    );

    long createAMC(
            String amcId,
            LocalDate startDate,
            LocalDate endDate
    );

    long createAMCWithOffer(
            String amcId,
            LocalDate startDate,
            LocalDate endDate,
            String offerId
    );

    long linkWarrantyAMC(
            String warrantyId,
            String amcId
    );

    long linkAMCOffer(
            String amcId,
            String offerId
    );

    long createUserWithCustomer(
            String userId,
            String name,
            String email,
            String password,
            LocalDateTime createdAt,
            LocalDateTime updatedAt,
            String customerId,
            String customerName
    );

    long linkUserCustomer(
            String userId,
            String customerId
    );

    long createSale(
            String saleId,
            LocalDate saleDate
    );

    long linkCustomerSale(
            String customerId,
            String saleId
    );

    long linkSaleProduct(
            String saleId,
            String serialNumber
    );

    long createSaleWithProduct(
            String saleId,
            LocalDate saleDate,
            String customerId,
            String serialNumber
    );
}