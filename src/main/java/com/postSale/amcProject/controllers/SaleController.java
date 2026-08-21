package com.postSale.amcProject.controllers;

import com.postSale.amcProject.DTO.sale.PurchaseRequest;
import com.postSale.amcProject.Model.nodes.Sale;
import com.postSale.amcProject.Services.SaleService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("api/sales")
public class SaleController {

    private final SaleService saleService;

    public SaleController(SaleService saleService) {
        this.saleService = saleService;
    }

    @PostMapping
    public ResponseEntity<Sale> createPurchase(@Valid @RequestBody PurchaseRequest request, Authentication authentication) {
        Sale createdSale = saleService.createPurchase( request, authentication );
        return ResponseEntity.ok(createdSale);
    }
}