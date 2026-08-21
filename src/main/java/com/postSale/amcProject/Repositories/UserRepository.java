package com.postSale.amcProject.Repositories;

import com.postSale.amcProject.Model.nodes.Customer;
import com.postSale.amcProject.Model.nodes.User;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.neo4j.repository.query.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

/**
 * UserRepository handles all database operations for the User node.
 * Spring Data Neo4j auto-generates the Cypher queries for us.
 */
@Repository
public interface UserRepository extends Neo4jRepository<User, String> {

    // Find a user by their email address (used during login)
    @Query("""
        MATCH (u:User)
        WHERE u.email = $email
        RETURN u
        """)
    Optional<User> findByEmail(String email);

    @Query("""
            MATCH (u:User)-[:IS_CUSTOMER]->(c:Customer)
            WHERE u.email = $email
            RETURN c
            """)
    Optional<Customer> findCustomerByUserEmail(String email);

    // Check if an email is already registered (used during signup to prevent duplicates)
    boolean existsByEmail(String email);

    /**
     * Creates a User, its corresponding Customer, and their IS_CUSTOMER
     * relationship in a single database operation.
     */
    @Query("""
    CREATE (u:User)
    SET u.id = $id,
        u.name = $name,
        u.email = $email,
        u.password = $password,
        u.createdAt = $createdAt,
        u.updatedAt = $updatedAt

    CREATE (c:Customer)
    SET c.custId = $customerId,
        c.custName = $name

    CREATE (u)-[:IS_CUSTOMER]->(c)

    RETURN u.id
    """)
    String createUser(
            String id,
            String name,
            String email,
            String password,
            LocalDateTime createdAt,
            LocalDateTime updatedAt,
            String customerId
    );

    @Query("""
    MATCH (u:User {email: $email})-[:IS_CUSTOMER]->(c:Customer)
    RETURN c.custId
    """)
    Optional<String> findCustomerIdByEmail(String email);
}