package com.postSale.amcProject.Repositories;

import com.postSale.amcProject.Model.nodes.Product;
import com.postSale.amcProject.Model.nodes.Warranty;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.neo4j.repository.query.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface ProductRepository extends Neo4jRepository<Product, String> {

    @Query("""
            MATCH (c:Customer)-[:PURCHASED]->(s:Sale)-[:OF_PRODUCT]->(p:Product)
            WHERE c.custId = $customerId
            RETURN DISTINCT p
            ORDER BY p.productName ASC
            """)
    List<Product> findAllProductsByCustomerId(String customerId);

    @Query("""
            MATCH (c:Customer)-[:PURCHASED]->(s:Sale)-[:OF_PRODUCT]->(p:Product)
            WHERE c.custId = $customerId AND p.productSerialNumber = $productSerialNumber
            RETURN p
            """)
    Optional<Product> findProductByCustomerId(String customerId, String productSerialNumber);

    @Query("""
        MATCH (c:Customer)-[:PURCHASED]->(s:Sale)-[:OF_PRODUCT]->(p:Product)
        WHERE c.custId = $customerId
          AND p.productSerialNumber = $productSerialNumber
        SET p.productName = $productName,
            p.productCreatedDate = $productCreatedDate,
            p.productCategory = $productCategory
        RETURN p.productSerialNumber
        """)
    String updateProduct(
            String customerId,
            String productSerialNumber,
            String productName,
            LocalDate productCreatedDate,
            String productCategory
    );

    @Query("""
            MATCH (c:Customer)-[:PURCHASED]->(s:Sale)-[:OF_PRODUCT]->(p:Product)
            WHERE c.custId = $customerId
              AND p.productSerialNumber = $productSerialNumber
            RETURN count(p) > 0
            """)
    boolean existsProductByCustomerId(
            String customerId,
            String productSerialNumber
    );
    boolean existsByProductSerialNumber(String productSerialNumber);

    @Query("""
            MATCH (p:Product {productSerialNumber: $serialNumber})-[:HAS_WARRANTY]->(w:Warranty)
            RETURN
                w.warrantyId AS warrantyId,
                w.warrantyStartDate AS warrantyStartDate,
                w.warrantyEndDate AS warrantyEndDate
            ORDER BY w.warrantyStartDate ASC
            """)
    List<Warranty> findWarrantiesByProductSerialNumber(String serialNumber);

    @Query("""
            CREATE (p:Product)
            SET p.productSerialNumber = $productSerialNumber,
                p.productName = $productName,
                p.productCreatedDate = $productCreatedDate,
                p.productCategory = $productCategory
            CREATE (w:Warranty)
            SET w.warrantyId = $warrantyId,
                w.warrantyStartDate = $warrantyStartDate,
                w.warrantyEndDate = $warrantyEndDate
            CREATE (p)-[:HAS_WARRANTY]->(w)
            RETURN p.productSerialNumber
            """)
    String createProductWithWarranty(
            String productSerialNumber,
            String productName,
            LocalDate productCreatedDate,
            String productCategory,
            String warrantyId,
            LocalDate warrantyStartDate,
            LocalDate warrantyEndDate
    );


    @Query("""
            MATCH (p:Product {productSerialNumber: $serialNumber})
                  -[:HAS_WARRANTY]->(w:Warranty)
                  -[:EXTENDED_BY]->(a:AMC)
            WITH collect(a) AS amcs
            FOREACH (a IN amcs | DETACH DELETE a)
            RETURN size(amcs) AS deletedCount
            """)
    long deleteLinkedAMCs(String serialNumber);


    @Query("""
            MATCH (p:Product {productSerialNumber: $serialNumber})
                  -[:HAS_WARRANTY]->(w:Warranty)
            WITH collect(w) AS warranties
            FOREACH (w IN warranties | DETACH DELETE w)
            RETURN size(warranties) AS deletedCount
            """)
    long deleteLinkedWarranties(String serialNumber);


    @Query("""
            MATCH (p:Product {productSerialNumber: $serialNumber})
            WITH collect(p) AS products
            FOREACH (p IN products | DETACH DELETE p)
            RETURN size(products) AS deletedCount
            """)
    long deleteProductNode(String serialNumber);


    @Query("""
    MATCH (p:Product {productSerialNumber: $productSerialNumber}),
          (w:Warranty {warrantyId: $warrantyId})
    MERGE (p)-[:HAS_WARRANTY]->(w)
    """)
    void linkWarranty(String productSerialNumber, String warrantyId);

}
