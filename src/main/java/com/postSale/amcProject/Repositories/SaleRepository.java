package com.postSale.amcProject.Repositories;

import com.postSale.amcProject.Model.nodes.Sale;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.neo4j.repository.query.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface SaleRepository extends Neo4jRepository<Sale, String> {

    @Query("""
            MATCH (s:Sale)
            RETURN s
            ORDER BY s.saleDate DESC
            """)
    List<Sale> findAllSales();

    @Query("""
            MATCH (s:Sale {saleId: $saleId})
            RETURN s
            """)
    Sale findSaleById(String saleId);

    @Query("""
            MATCH (s:Sale)
            WHERE s.saleId IN $saleIds
            RETURN s
            """)
    List<Sale> findSalesByIds(Iterable<String> saleIds);

    @Query("""
            CREATE (s:Sale)
            SET s.saleId = $saleId,
                s.saleDate = $saleDate
            RETURN s.saleId
            """)
    String createSale(String saleId, LocalDate saleDate);

    @Query("""
            MATCH (c:Customer {custId: $customerId})
            MATCH (s:Sale {saleId: $saleId})
            MERGE (c)-[:PURCHASED]->(s)
            """)
    void linkCustomer(String customerId, String saleId);

    @Query("""
            MATCH (s:Sale {saleId: $saleId})
            MATCH (p:Product {productSerialNumber: $productSerialNumber})
            MERGE (s)-[:OF_PRODUCT]->(p)
            RETURN count(p)
            """)
    long linkProduct(String saleId, String productSerialNumber);

    @Query("""
            MATCH (c:Customer {custId: $customerId})
            CREATE (s:Sale)
            SET s.saleId = $saleId,
                s.saleDate = $saleDate
            CREATE (c)-[:PURCHASED]->(s)
            RETURN s.saleId
            """)
    String createSaleForCustomer(String customerId, String saleId, LocalDate saleDate);

    @Query("""
            MATCH (c:Customer)-[:PURCHASED]->(s:Sale)
            WHERE c.custId = $customerId
            RETURN s
            """)
    List<Sale> findAllSalesByCustomerId(String customerId);



}
