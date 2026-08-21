package com.postSale.amcProject.DTO.sale;

import com.postSale.amcProject.DTO.product.ProductCreateRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.util.List;

public record PurchaseRequest(

        @NotNull(message = "Sale date is required")
        LocalDate saleDate,

        @NotEmpty(message = "A purchase must contain at least one product")
        List<@Valid ProductCreateRequest> products

) {}