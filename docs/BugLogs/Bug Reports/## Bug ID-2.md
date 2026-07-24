## Bug ID
BUG-001

### Date
2026-07-24

### Version
Unknown

### Category
Backend / Spring Boot / Neo4j Domain Relationship

### Severity
High

### Status
Resolved

### Detection Method
Runtime Exception (HTTP 500 Internal Server Error)

### Symptoms
Creating a new Product failed with an HTTP 500 response.

Error message:

```
Cannot invoke "java.util.List.add(Object)"
because the return value of
"Product.getWarrantyList()" is null
```

As a result, automatic warranty generation during product creation failed and the Product could not be persisted.

### Immediate Cause
The application attempted to execute:

```java
product.getWarrantyList().add(warranty);
```

while `warrantyList` had never been initialized, resulting in a `NullPointerException`.

### Root Cause
The newly introduced automatic warranty generation feature assumed that every `Product` instance already contained an initialized `warrantyList`.

However, products created from incoming API requests do not receive a `warrantyList` from the client because warranty creation is now fully backend-managed. Consequently, the collection remained `null` until explicitly initialized.

### Investigation Summary
- Reproduced the issue during Product creation.
- Verified that Product creation reached the automatic warranty generation logic.
- Examined the stack trace indicating `Product.getWarrantyList()` returned `null`.
- Confirmed that the backend attempted to append a generated Warranty to an uninitialized collection.
- Verified that initializing the collection before assignment resolved the issue.

### Fix Implemented
Replaced the list append operation with explicit initialization of the warranty collection during Product creation:

```java
product.setWarrantyList(new ArrayList<>(List.of(warranty)));
```

This guarantees that every newly created Product has a non-null `warrantyList` containing the automatically generated initial Warranty before persistence.

### Files Changed
- `Services/ProductService.java`

### Verification
Verified successfully by creating Products across multiple Product Categories.

Confirmed:
- Product created successfully.
- Warranty generated automatically.
- Warranty attached correctly.
- No HTTP 500 error.
- Product persisted successfully in Neo4j.

### Regression Risk
Low

Reason:
The change only affects initialization of the warranty relationship during Product creation and does not alter existing business logic or persistence behavior.

### Prevention
- Initialize collection fields either:
  - during entity declaration (`new ArrayList<>()`), or
  - through constructors/builders.
- Never assume collection relationships are initialized when objects originate from client requests.
- Add unit tests covering automatic relationship creation.
- Add integration tests verifying Product creation with generated Warranty.

### Lessons Learned
Backend-managed relationship collections should always be initialized before mutation. When business logic creates child entities automatically, the owning collection cannot be assumed to exist unless explicitly initialized.

### Related Bugs
None