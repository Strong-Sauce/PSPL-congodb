Seed data for PSPL (Neo4j / CognoDB)

Overview
--------
This project includes a deterministic seeder that populates a Neo4j (CognoDB) instance with realistic, interconnected PSPL AMC data.

What the seeder creates (approximate counts)
- Users: 10
- Customers: 10 (one per user)
- Sales: ~20-40 (varies per customer)
- Products: ~36
- Warranties: one per product (~36)
- AMCs: multiple sequential AMCs for many warranties (0..3 each)
- AMCOffers: 5 reusable offers

Design notes
------------
- The seeder is implemented as a Spring ApplicationRunner so it uses the application's PasswordEncoder (BCrypt) and repositories to create nodes and relationships in a way consistent with the domain model.
- Seeding is disabled by default to avoid accidental runs in production. Enable it explicitly as described below.

Prerequisites
-------------
- A running Neo4j / CognoDB instance
- Application configured to connect to Neo4j (see src/main/resources/application.properties)

Running the seed
-----------------
1. Ensure Neo4j is running and application.properties (or environment variables) point to your Neo4j instance and valid credentials.

2. Start the Spring Boot application with the seeder enabled. Two options:

   - Environment variable (Windows PowerShell):
     $env:APP_SEED_DATA = 'true'
     mvn spring-boot:run

   - JVM system property (any OS):
     mvn spring-boot:run -Dspring-boot.run.jvmArguments="-Dapp.seed-data=true"

   - Or pass via application.properties / environment: set app.seed-data=true

3. The application will print seed progress to the console. When done it will show the demo password used for seeded accounts.

Credentials (demo)
------------------
- All seeded users use the same demo password: Password123!
- Passwords are stored only as BCrypt hashes via the application's PasswordEncoder.
- Each user's email is deterministic (name-based) and can be observed in the database or console logs.

Determinism & re-runs
---------------------
- The seeder checks whether users already exist; if users exist it will skip seeding to avoid duplicates.
- To force reseed on a clean database, drop the database (or remove nodes) and start the application with app.seed-data=true.

Verification queries (run in Neo4j Browser / CognoDB query console)
-------------------------------------------------------------------
-- 1) Count nodes by label
MATCH (n) RETURN labels(n)[0] AS label, count(*) AS count ORDER BY count DESC

-- 2) Count relationships by type
MATCH ()-[r]-() RETURN type(r) AS rel, count(*) AS count ORDER BY count DESC

-- 3) Show lifecycle paths (example limited to 25)
MATCH (u:User)-[:IS_CUSTOMER]->(c:Customer)-[:PURCHASED]->(s:Sale)-[:OF_PRODUCT]->(p:Product)-[:HAS_WARRANTY]->(w:Warranty)-[:EXTENDED_BY]->(a:AMC)-[:BASED_ON]->(o:AMCOffer)
RETURN u.email AS user, c.custName AS customer, s.saleId AS sale, p.productSerialNumber AS product, w.warrantyId AS warranty, a.amcId AS amc, o.offerType AS offer
LIMIT 25

-- 4) Customers with multiple purchases
MATCH (c:Customer)-[:PURCHASED]->(s:Sale)
WITH c, count(s) AS salesCount
WHERE salesCount > 1
RETURN c.custId, c.custName, salesCount
ORDER BY salesCount DESC

-- 5) Products with multiple AMCs
MATCH (p:Product)-[:HAS_WARRANTY]->(w:Warranty)-[:EXTENDED_BY]->(a:AMC)
WITH p, count(a) AS amcCount
WHERE amcCount > 1
RETURN p.productSerialNumber, p.productName, amcCount
ORDER BY amcCount DESC

-- 6) Warranties with no AMC
MATCH (w:Warranty)
WHERE NOT (w)-[:EXTENDED_BY]->(:AMC)
RETURN w.warrantyId, w.warrantyStartDate, w.warrantyEndDate
LIMIT 50

-- 7) Warranties with multiple AMC extensions
MATCH (w:Warranty)-[:EXTENDED_BY]->(a:AMC)
WITH w, count(a) AS numAmc
WHERE numAmc > 1
RETURN w.warrantyId, numAmc
ORDER BY numAmc DESC

-- 8) Find a customer and all their products
MATCH (u:User)-[:IS_CUSTOMER]->(c:Customer)-[:PURCHASED]->(s:Sale)-[:OF_PRODUCT]->(p:Product)
WHERE u.email CONTAINS "example.com"
RETURN u.email, c.custName, collect(DISTINCT p.productSerialNumber) AS products
LIMIT 25

-- 9) AMC usage by offer type
MATCH (a:AMC)-[:BASED_ON]->(o:AMCOffer)
RETURN o.offerType, count(a) AS uses
ORDER BY uses DESC

Notes
-----
- The seeder aims to be minimal and fit the existing code; it relies on the same domain classes and repositories used by the application.
- If you prefer a standalone Cypher script, the code in this seeder can be used as a reference to build one; however this version avoids duplicating password hashing logic by reusing the app's PasswordEncoder.
