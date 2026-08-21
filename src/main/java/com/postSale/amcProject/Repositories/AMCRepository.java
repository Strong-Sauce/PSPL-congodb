package com.postSale.amcProject.Repositories;

import com.postSale.amcProject.Model.nodes.AMC;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.neo4j.repository.query.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface AMCRepository extends Neo4jRepository<AMC, String> {

    @Query("""
            MATCH (a:AMC)
            RETURN a
            ORDER BY a.amcStartDate ASC
            """)
    List<AMC> findAllAMCs();

    @Query("""
            MATCH (a:AMC {amcId: $amcId})
            RETURN a
            """)
    Optional<AMC> findAMCById(String amcId);

    @Query("""
            MATCH (a:AMC)
            WHERE a.amcId IN $amcIds
            RETURN a
            """)
    List<AMC> findAMCsByIds(Iterable<String> amcIds);

    @Query("""
            MATCH (a:AMC {amcId: $amcId})
            RETURN count(a) > 0
            """)
    boolean existsAMCById(String amcId);

    @Query("""
            CREATE (a:AMC)
            SET a.amcId = $amcId,
                a.amcStartDate = $amcStartDate,
                a.amcEndDate = $amcEndDate
            RETURN a.amcId
            """)
    String createAMC(String amcId, LocalDate amcStartDate, LocalDate amcEndDate);

    @Query("""
            MATCH (a:AMC {amcId: $amcId})
            SET a.amcStartDate = $amcStartDate,
                a.amcEndDate = $amcEndDate
            RETURN a.amcId
            """)
    String updateAMC(String amcId, LocalDate amcStartDate, LocalDate amcEndDate);

    @Query("""
            MATCH (a:AMC {amcId: $amcId})-[r:BASED_ON]->(:AMCOffer)
            DELETE r
            """)
    void clearOfferLinks(String amcId);

    @Query("""
            MATCH (a:AMC {amcId: $amcId})
            MATCH (o:AMCOffer {offerId: $offerId})
            MERGE (a)-[:BASED_ON]->(o)
            """)
    void linkOffer(String amcId, String offerId);

    @Query("""
            MATCH (a:AMC {amcId: $amcId})
            DETACH DELETE a
            """)
    void deleteAMCById(String amcId);

}
