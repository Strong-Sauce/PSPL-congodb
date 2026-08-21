package com.postSale.amcProject.Services;

import com.postSale.amcProject.DTO.query_records.WarrantyQueryResult;
import com.postSale.amcProject.DTO.WarrantyWithProductDTO;
import com.postSale.amcProject.Exceptions.ResourceNotFoundException;
import com.postSale.amcProject.Model.enums.WarrantyStatus;
import com.postSale.amcProject.Model.nodes.Warranty;
import com.postSale.amcProject.Repositories.UserRepository;
import com.postSale.amcProject.Repositories.WarrantyRepository;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
public class WarrantyService {

    private final WarrantyRepository warrantyRepository;
    private final UserRepository userRepository;

    public WarrantyService(WarrantyRepository warrantyRepository, UserRepository userRepository) {
        this.warrantyRepository = warrantyRepository;
        this.userRepository = userRepository;
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
    public Optional<WarrantyWithProductDTO> getByWarrantyId(String warrantyId, Authentication authentication) {
        String customerId = getCustomerId(authentication);

        return warrantyRepository.findWarrantyById(customerId, warrantyId).map(this::toDTO);
    }

    @Transactional(readOnly = true)
    public Optional<List<WarrantyWithProductDTO>> getAllWarranties(Authentication authentication) {

        String customerId = getCustomerId(authentication);

        return warrantyRepository.findAllProductsWithWarranty(customerId)
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

    private String getCustomerId(Authentication authentication) {

        if (authentication == null || !authentication.isAuthenticated()) {
            throw new IllegalArgumentException("User must be authenticated.");
        }

        return userRepository.findCustomerIdByEmail(authentication.getName())
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Customer",
                                authentication.getName()
                        )
                );
    }
}
