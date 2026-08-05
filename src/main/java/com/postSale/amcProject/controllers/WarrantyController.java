package com.postSale.amcProject.controllers;

import com.postSale.amcProject.DTO.WarrantyWithProductDTO;
import com.postSale.amcProject.Model.nodes.Warranty;
import com.postSale.amcProject.Services.WarrantyService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("api/warranty")
public class WarrantyController {

    private final WarrantyService warrantyService;

    public WarrantyController(WarrantyService warrantyService) {
        this.warrantyService = warrantyService;
    }

    @Deprecated(forRemoval=true)
//    @GetMapping
    public ResponseEntity<List<Warranty>> getAllSoonExpiring() {
        return ResponseEntity.ok(warrantyService.getExpiringWarranties());
    }

    @GetMapping
    public ResponseEntity<List<WarrantyWithProductDTO>> getAllWarranties() {
        return warrantyService.getAllWarranties()
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/{warrantyId}")
    public ResponseEntity<WarrantyWithProductDTO> getByWarrantyId(@PathVariable String warrantyId) {
        return warrantyService.getByWarrantyId(warrantyId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}
