package com.postSale.amcProject.Repositories;

import com.postSale.amcProject.DTO.query_records.WarrantyQueryResult;
import com.postSale.amcProject.Model.nodes.Warranty;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.neo4j.repository.query.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface WarrantyRepository extends Neo4jRepository<Warranty, String> {

    @Query("""
    MATCH (w:Warranty)
    RETURN w
    ORDER BY w.warrantyEndDate ASC
    LIMIT 5
    """)
    List<Warranty> findWarrantiesExpiringSoon();

    @Query("""
    MATCH (p:Product)-[:HAS_WARRANTY]->(w:Warranty {warrantyId: $warrantyId})
    RETURN
        w.warrantyId AS warrantyId,
        w.warrantyStartDate AS warrantyStartDate,
        w.warrantyEndDate AS warrantyEndDate,
        p.productName AS productName,
        p.productSerialNumber AS productSerialNumber
    """)
    Optional<WarrantyQueryResult> findWarrantyById(String warrantyId);

    @Query("""
    MATCH (p:Product)-[:HAS_WARRANTY]->(w:Warranty)
    RETURN
        w.warrantyId AS warrantyId,
        w.warrantyStartDate AS warrantyStartDate,
        w.warrantyEndDate AS warrantyEndDate,
        p.productName AS productName,
        p.productSerialNumber AS productSerialNumber
    ORDER BY w.warrantyEndDate ASC
    """)
    Optional<List<WarrantyQueryResult>> findAllProductsWithWarranty();

}

