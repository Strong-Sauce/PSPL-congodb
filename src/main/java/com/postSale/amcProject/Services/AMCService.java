package com.postSale.amcProject.Services;

import com.postSale.amcProject.Exceptions.ResourceNotFoundException;
import com.postSale.amcProject.Model.nodes.AMC;
import com.postSale.amcProject.Repositories.AMCRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class AMCService {

    private final AMCRepository amcRepository;

    public AMCService(AMCRepository amcRepository) {
        this.amcRepository = amcRepository;
    }

    @Transactional
    public AMC createAMC(AMC amc) {
        amc.setAmcId(UUID.randomUUID().toString());
        amcRepository.createAMC(amc.getAmcId(), amc.getAmcStartDate(), amc.getAmcEndDate());
        if (amc.getAmcOfferList() != null) {
            amc.getAmcOfferList().stream()
                    .filter(offer -> offer != null && offer.getOfferId() != null)
                    .forEach(offer -> amcRepository.linkOffer(amc.getAmcId(), offer.getOfferId()));
        }
        return amc;
    }

    @Transactional(readOnly = true)
    public List<AMC> getAllAMCs() {
        return amcRepository.findAllAMCs();
    }

    @Transactional(readOnly = true)
    public Optional<AMC> getAMCById(String id) {
        return amcRepository.findAMCById(id);
    }

    @Transactional
    public AMC updateAMC(AMC amc) {
        if (!amcRepository.existsAMCById(amc.getAmcId())) {
            throw new ResourceNotFoundException("AMC", amc.getAmcId());
        }
        amcRepository.updateAMC(amc.getAmcId(), amc.getAmcStartDate(), amc.getAmcEndDate());
        if (amc.getAmcOfferList() != null) {
            amcRepository.clearOfferLinks(amc.getAmcId());
            amc.getAmcOfferList().stream()
                    .filter(offer -> offer != null && offer.getOfferId() != null)
                    .forEach(offer -> amcRepository.linkOffer(amc.getAmcId(), offer.getOfferId()));
        }
        return amc;
    }

    @Transactional
    public void deleteAMC(String id) {
        if (!amcRepository.existsAMCById(id)) {
            throw new ResourceNotFoundException("AMC", id);
        }
        amcRepository.deleteAMCById(id);
    }
}
