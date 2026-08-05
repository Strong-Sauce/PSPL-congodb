package com.postSale.amcProject.DTO.query_records;

import java.time.LocalDate;

public record WarrantyQueryResult(

        String warrantyId,

        LocalDate warrantyStartDate,

        LocalDate warrantyEndDate,

        String productName,

        String productSerialNumber

) {}