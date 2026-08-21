package com.postSale.amcProject.Repositories;

import com.postSale.amcProject.Model.nodes.AMCOffer;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.neo4j.repository.query.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AMCOfferRepository extends Neo4jRepository<AMCOffer, String> {

    @Query("""
            MATCH (o:AMCOffer)
            RETURN o
            ORDER BY o.offerType ASC
            """)
    List<AMCOffer> findAllOffers();

    @Query("""
            MATCH (o:AMCOffer {offerId: $offerId})
            RETURN o
            """)
    Optional<AMCOffer> findOfferById(String offerId);

    @Query("""
            MATCH (o:AMCOffer {offerId: $offerId})
            RETURN count(o) > 0
            """)
    boolean existsOfferById(String offerId);

    @Query("""
            CREATE (o:AMCOffer)
            SET o.offerId = $offerId,
                o.offerType = $offerType,
                o.offerDurationMonths = $offerDurationMonths,
                o.offerPrice = $offerPrice,
                o.offerTerms = $offerTerms
            RETURN o.offerId
            """)
    String createOffer(
            String offerId,
            String offerType,
            Integer offerDurationMonths,
            Double offerPrice,
            String offerTerms
    );

    @Query("""
            MATCH (o:AMCOffer {offerId: $offerId})
            SET o.offerType = $offerType,
                o.offerDurationMonths = $offerDurationMonths,
                o.offerPrice = $offerPrice,
                o.offerTerms = $offerTerms
            RETURN o.offerId
            """)
    String updateOffer(
            String offerId,
            String offerType,
            Integer offerDurationMonths,
            Double offerPrice,
            String offerTerms
    );

    @Query("""
            MATCH (o:AMCOffer {offerId: $offerId})
            DETACH DELETE o
            """)
    void deleteOfferById(String offerId);
}