package com.postSale.amcProject.Services;

import com.postSale.amcProject.DTO.query_records.WarrantyQueryResult;
import com.postSale.amcProject.DTO.WarrantyWithProductDTO;
import com.postSale.amcProject.Model.enums.WarrantyStatus;
import com.postSale.amcProject.Model.nodes.Warranty;
import com.postSale.amcProject.Repositories.WarrantyRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
public class WarrantyService {

    private final WarrantyRepository warrantyRepository;

    public WarrantyService(WarrantyRepository warrantyRepository) {
        this.warrantyRepository = warrantyRepository;
    }

    //Depricated
    @Transactional(readOnly = true)
    public List<Warranty> getExpiringWarranties() {

        return warrantyRepository.findWarrantiesExpiringSoon();
    }

    // *******************************************
    // SERVICES
    // *******************************************
    @Transactional(readOnly = true)
    public Optional<WarrantyWithProductDTO> getByWarrantyId(String warrantyId) {

        return warrantyRepository.findWarrantyById(warrantyId)
                .map(this::toDTO);
    }

    @Transactional(readOnly = true)
    public Optional<List<WarrantyWithProductDTO>> getAllWarranties() {

        return warrantyRepository.findAllProductsWithWarranty()
                .map(results ->
                        results.stream()
                                .map(this::toDTO)
                                .toList()
                );
    }

    // *******************************************
    // HELPER METHODS
    // *******************************************
    private WarrantyWithProductDTO toDTO(WarrantyQueryResult result) {

        return WarrantyWithProductDTO.builder()
                .warrantyId( result.warrantyId() )
                .warrantyStartDate( result.warrantyStartDate() )
                .warrantyEndDate( result.warrantyEndDate() )
                .productName( result.productName() )
                .productSerialNumber( result.productSerialNumber() )
                .warrantyStatus( getWarrantyStatus(result.warrantyEndDate()) )
                .build();
    }

    private WarrantyStatus getWarrantyStatus(LocalDate endDate) {

        LocalDate today = LocalDate.now();

        if (endDate.isBefore(today)) {
            return WarrantyStatus.EXPIRED;
        }

        if (!endDate.isAfter(today.plusDays(30))) {
            return WarrantyStatus.EXPIRING_SOON;
        }

        return WarrantyStatus.ACTIVE;
    }
}
