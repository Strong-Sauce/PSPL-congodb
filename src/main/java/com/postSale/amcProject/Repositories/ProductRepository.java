package com.postSale.amcProject.Repositories;

import com.postSale.amcProject.Model.nodes.Product;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.neo4j.repository.query.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ProductRepository extends Neo4jRepository<Product, String> {
    boolean existsByProductSerialNumber(String productSerialNumber);
    Optional<Product> findByProductSerialNumber(String productSerialNumber);

    @Query("""
            MATCH (p:Product {productSerialNumber: $serialNumber})
            OPTIONAL MATCH (p)-[:HAS_WARRANTY]->(w:Warranty)
            OPTIONAL MATCH (w)-[:EXTENDED_BY]->(a:AMC)
            DETACH DELETE p, w, a
            """)
    void deleteProduct(String serialNumber);

}
