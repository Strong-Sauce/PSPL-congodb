package com.postSale.amcProject.Repositories;

import com.postSale.amcProject.Model.nodes.Product;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ProductRepository extends Neo4jRepository<Product, String> {
    boolean existsByProductSerialNumber(String productSerialNumber);
    Optional<Product> findByProductSerialNumber(String productSerialNumber);
}
