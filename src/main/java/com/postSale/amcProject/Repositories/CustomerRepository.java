package com.postSale.amcProject.Repositories;

import com.postSale.amcProject.Model.nodes.Customer;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.neo4j.repository.query.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CustomerRepository extends Neo4jRepository<Customer, String> {

    @Query("""
            MATCH (c:Customer)
            RETURN c
            ORDER BY c.custName ASC
            """)
    List<Customer> findAllCustomers();

    @Query("""
            MATCH (c:Customer {custId: $custId})
            RETURN c
            """)
    Optional<Customer> findCustomerById(String custId);

    @Query("""
            MATCH (c:Customer)
            WHERE c.custId IN $custIds
            RETURN c
            """)
    List<Customer> findCustomersByIds(Iterable<String> custIds);

    @Query("""
            MATCH (c:Customer {custId: $custId})
            RETURN count(c) > 0
            """)
    boolean existsCustomerById(String custId);

    @Query("""
            CREATE (c:Customer)
            SET c.custId = $custId,
                c.custName = $custName
            RETURN c.custId
            """)
    String createCustomer(String custId, String custName);

    @Query("""
            MATCH (c:Customer {custId: $custId})
            SET c.custName = $custName
            RETURN c.custId
            """)
    String updateCustomer(String custId, String custName);

    @Query("""
            MATCH (c:Customer {custId: $custId})
            DETACH DELETE c
            """)
    void deleteCustomerByAppId(String custId);


    @Query("""
    MATCH (c:Customer {custId: $customerId}),
          (s:Sale {saleId: $saleId})
    MERGE (c)-[:PURCHASED]->(s)
    """)
    void linkSale(String customerId, String saleId);

}
