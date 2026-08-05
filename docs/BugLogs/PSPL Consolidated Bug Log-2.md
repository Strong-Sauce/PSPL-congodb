# BUG-004: Spring Data Neo4j Custom Query Returned Null-Mapped Product Entity

## Date
2026-07-28

## Version
Unknown

## Category
Backend / Spring Data Neo4j / Custom Cypher Mapping

## Severity
High

## Status
Resolved

## Detection Method
Runtime Validation / API Response Inspection

---

## Symptoms

The endpoint:

```
GET /api/warranty/{warrantyId}
```

returned:

```json
{
    "warrantyId": null,
    "warrantyStartDate": null,
    "warrantyEndDate": null,
    "productName": null,
    "productSerialNumber": null
}
```

despite the Warranty node, Product node, and `HAS_WARRANTY` relationship existing correctly in Neo4j.

Spring logs also produced mapping warnings similar to:

```
Cannot retrieve a value for property ...
DtoInstantiatingConverter
```

---

## Immediate Cause

The repository method attempted to return a `Product` entity from a custom Cypher query:

```java
@Query("""
MATCH (p:Product)-[:HAS_WARRANTY]->(w:Warranty {warrantyId: $warrantyId})
RETURN p
""")
Product findWarrantyById(String warrantyId);
```

Although the query returned the correct node, Spring Data Neo4j failed to hydrate the entity correctly, resulting in a Product object with all fields set to `null`.

---

## Root Cause

The service layer unnecessarily loaded an entire `Product` entity only to extract a few fields required by the API response.

Spring Data Neo4j interpreted the custom query using its projection/DTO mapping mechanism (`DtoInstantiatingConverter`), but the returned graph data did not match the expected entity structure for full hydration. As a result, the Product entity was instantiated with null properties.

---

## Investigation Summary

- Verified the Warranty node existed.
- Verified the Product node existed.
- Verified the `HAS_WARRANTY` relationship.
- Executed the Cypher query directly in Neo4j Browser and confirmed correct results.
- Reviewed Spring logs and identified `DtoInstantiatingConverter` mapping warnings.
- Confirmed the repository returned a Product instance whose properties were all null.
- Determined the issue was entity mapping rather than query execution.

---

## Fix Implemented

Redesigned the repository to return the required DTO projection directly from Cypher instead of returning a Product entity.

```cypher
MATCH (p:Product)-[:HAS_WARRANTY]->(w:Warranty)
WHERE w.warrantyId = $warrantyId

RETURN
    w.warrantyId AS warrantyId,
    w.warrantyStartDate AS warrantyStartDate,
    w.warrantyEndDate AS warrantyEndDate,
    p.productName AS productName,
    p.productSerialNumber AS productSerialNumber
```

The repository now returns the DTO projection directly, eliminating unnecessary entity mapping and relationship traversal.

---

## Files Changed

- WarrantyRepository.java
- WarrantyService.java
- Warranty DTO projection

---

## Verification

Confirmed:

- Warranty lookup endpoint returns correct data.
- Product name and serial number populate correctly.
- Warranty dates populate correctly.
- No Spring Data Neo4j mapping warnings.
- API response matches expected DTO.

---

## Regression Risk

Low

The change only affects the warranty lookup implementation and simplifies the data retrieval path without modifying the underlying graph model.

---

## Prevention

- Prefer DTO projections for read-only APIs.
- Return only the fields required by the endpoint.
- Avoid loading aggregate roots when only a subset of data is needed.
- Validate custom Cypher queries independently from entity mapping.
- Align repository return types with API response models.

---

## Lessons Learned

Spring Data Neo4j entity mapping is not always the optimal choice for read-only APIs. When an endpoint ultimately returns a DTO, querying directly into that DTO is simpler, more efficient, and avoids unnecessary graph entity hydration and mapping issues.

---

## Related Bugs

None.