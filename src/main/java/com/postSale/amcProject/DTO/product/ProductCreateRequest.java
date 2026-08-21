package com.postSale.amcProject.DTO.product;

import com.postSale.amcProject.Model.enums.ProductCategory;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record ProductCreateRequest(

        @NotBlank(message = "Product name is required")
        String productName,

        @NotNull(message = "Product category is required")
        ProductCategory productCategory

) {}