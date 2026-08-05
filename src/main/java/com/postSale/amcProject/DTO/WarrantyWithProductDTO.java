package com.postSale.amcProject.DTO;

import com.postSale.amcProject.Model.enums.WarrantyStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WarrantyWithProductDTO {

    private String warrantyId;

    private LocalDate warrantyStartDate;

    private LocalDate warrantyEndDate;

    private WarrantyStatus warrantyStatus;

    private String productName;

    private String productSerialNumber;
}