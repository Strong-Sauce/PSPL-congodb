# Project Architecture

## 1. High Level Architecture

This project is a full-stack web application for post-sale product lifecycle and AMC (Annual Maintenance Contract) management. The implementation is a modular monolith composed of:

- A Spring Boot 4 backend serving REST APIs and handling authentication/session management.
- An Angular 21 single-page frontend built with standalone components and signals.
- A Neo4j graph database that stores customers, products, sales, warranties, AMC offers, AMCs, and user accounts as graph nodes with typed relationships.

### Overall architecture style

The project follows a layered, modular-monolith architecture:

- Presentation layer: Angular UI
- Application layer: Spring controllers and services
- Persistence layer: Spring Data Neo4j repositories
- Cross-cutting concerns: Spring Security, validation, CORS, exception handling, sessions

### Design philosophy

The codebase is designed around a simple, explicit domain model focused on graph relationships between product lifecycle entities. The architecture favors:

- Clear separation between web/API concerns and business logic
- Direct use of Spring Data Neo4j for persistence with minimal ceremony
- Session-based authentication over JWT
- Client-side state driven by Angular signals for a lightweight SPA experience

### Major layers

- Frontend layer: routes, pages, services, shared layout components, auth guards, interceptors
- Backend layer: REST controllers, service classes, repositories, domain models, DTOs, exceptions
- Data layer: Neo4j nodes and relationships

### Technology stack

| Layer | Technology |
| --- | --- |
| Backend | Java 25, Spring Boot 4.0.2, Spring Web MVC, Spring Security, Spring Data Neo4j, Validation, Mail |
| Frontend | Angular 21, TypeScript, RxJS, Angular SSR |
| Database | Neo4j |
| Build | Maven for backend, npm for frontend |
| Authentication | Server-side HTTP sessions + Spring Security + BCrypt |

### Request flow from frontend to database

1. The Angular app sends an HTTP request to the Spring Boot API.
2. The Spring MVC controller receives the request.
3. The controller delegates to a service class.
4. The service uses a Neo4j repository to read or write graph data.
5. The repository executes Cypher through Spring Data Neo4j.
6. The result is returned to the controller and serialized as JSON to the Angular app.

## 2. Folder Structure

The project is rooted at `PSPLProject/` and contains both backend and frontend source trees.

### Root-level structure

- `src/` — backend source code and configuration
- `frontend/` — Angular frontend source code
- `docs/` — documentation assets
- `mvnw`, `mvnw.cmd` — Maven wrapper scripts
- `pom.xml` — Maven build configuration
- `README.md`, `HELP.md`, `TECHNICAL_EXPLAINER.md` — project documentation

### Backend package structure

`src/main/java/com/postSale/amcProject/`

- `AmcProjectApplication.java` — Spring Boot entry point.
- `config/` — security, CORS, SPA routing, authentication entry point configuration.
- `controllers/` — REST controllers for auth, products, customers, sales, warranties, AMCs, AMC offers.
- `Services/` — business logic layer.
- `Repositories/` — Spring Data Neo4j repositories.
- `Model/` — domain model and DTOs.
  - `Model/nodes/` — Neo4j node entities.
  - `Model/Relationships/` — relationship classes placeholder/unused implementation.
  - `Model/dto/auth/` — auth request/response DTO records.
- `Exceptions/` — custom exception and global exception handling.

### Frontend package structure

`frontend/src/app/`

- `app.ts` — root application component.
- `app.routes.ts` — route definitions.
- `app.config.ts` — Angular app configuration and HTTP interceptors.
- `guards/` — auth and guest route guards.
- `interceptors/` — HTTP interceptor for session cookies.
- `layout/` — navbar and sidebar components used across authenticated screens.
- `models/` — TypeScript interfaces for domain entities and auth payloads.
- `pages/` — feature pages such as login, signup, home, profile, product create/detail, about, contact, forgot/reset password.
- `services/` — Angular services for each domain resource and auth state.
- `environments/` — environment configuration.

## 3. Backend Architecture

### Entry point

The backend starts in `AmcProjectApplication`.

- It is annotated with `@SpringBootApplication`.
- It enables Neo4j repositories with `@EnableNeo4jRepositories`.

### Package organization

The backend is organized as a classic Spring layered architecture:

- `controllers` for HTTP entry points
- `Services` for business logic
- `Repositories` for persistence access
- `Model` for domain entities and DTOs
- `config` for infrastructure concerns
- `Exceptions` for error handling

### Controller layer

Controllers are thin HTTP adapters. They receive incoming requests, delegate to services, and return DTOs or domain entities as JSON.

Implemented controllers:

- `AuthController` — `/api/auth/*`
- `ProductController` — `/api/products`
- `CustomerController` — `/api/customers`
- `SaleController` — `/api/sales`
- `WarrantyController` — `/api/warranty`
- `AMCController` — `/api/amcs`
- `AMCOfferController` — `/api/amc-offers`

These controllers are annotated with `@RestController` and use Spring `@RequestMapping` for URL organization.

### Service layer

Services contain business logic and transaction boundaries.

Key responsibilities:

- `AuthService` handles signup, login, session creation, current-user lookup, password reset flow, logout.
- `ProductService`, `CustomerService`, `AMCService`, `AMCOfferService` handle CRUD-style operations.
- `SaleService` handles sale lookup by customer.
- `WarrantyService` handles warranty queries for expiring warranties.
- `EmailService` sends password reset emails or logs the URL when mail is not configured.

Services are annotated with `@Service` and use `@Transactional` where appropriate.

### Repository layer

Repositories are interfaces extending `Neo4jRepository<T, String>`.

Examples:

- `ProductRepository`
- `CustomerRepository`
- `SaleRepository`
- `WarrantyRepository`
- `AMCRepository`
- `AMCOfferRepository`
- `UserRepository`

`SaleRepository` and `WarrantyRepository` implement custom Cypher queries with `@Query` to traverse graph relationships.

### DTO layer

The backend uses DTOs mainly for authentication endpoints.

Implemented DTOs:

- `LoginRequest`, `SignupRequest`, `ResetPasswordRequest`, `ForgotPasswordRequest`
- `AuthResponse`, `AuthUserResponse`, `MessageResponse`

These are immutable Java records with Bean Validation annotations.

### Model layer

The domain model is graph-oriented and stored in Neo4j.

Node types:

- `User`
- `Customer`
- `Product`
- `Sale`
- `Warranty`
- `AMC`
- `AMCOffer`

Each node is annotated with `@Node` and uses Spring Data Neo4j annotations such as `@Id`, `@GeneratedValue`, and `@Relationship`.

### Utility classes

- `EmailService` for email delivery and password reset flow
- `RestAuthenticationEntryPoint` for JSON 401 responses
- `SpaController` to forward client-side routes to `index.html`

### Configuration classes

- `SecurityConfig` defines authorization rules, password hashing, session handling, and CSRF/CORS setup.
- `WebConfig` configures CORS for browser-based requests from the Angular app.
- `SpaController` ensures SPA route refreshes work correctly.

### Exception handling

The backend uses a global exception handler:

- `GlobalExceptionHandler` is annotated with `@RestControllerAdvice`.
- It converts `ResourceNotFoundException`, `IllegalArgumentException`, validation failures, and unexpected exceptions into consistent JSON error responses.

### Validation

Validation is implemented with Bean Validation annotations on auth DTO records:

- `@NotBlank`
- `@Email`
- `@Size`

Failures surface as `400 Bad Request` responses with a single message.

### Dependency Injection

The backend relies heavily on Spring’s constructor injection.

Examples:

- Controllers receive services through their constructors.
- Services receive repositories through their constructors.
- `AuthService` receives `UserRepository`, `PasswordEncoder`, and `EmailService`.

### Bean usage

The Spring container creates the following important beans:

- `PasswordEncoder` as a BCrypt encoder bean
- `SecurityFilterChain` for HTTP authorization rules
- `RestAuthenticationEntryPoint` for auth failures
- `JavaMailSender` is optional and injected if configured via properties

### Data flow through layers

A typical request flows like this:

1. HTTP request arrives at a controller.
2. Controller delegates to a service.
3. Service performs business logic and transaction handling.
4. Repository executes Neo4j queries.
5. Data is mapped to Java entity objects.
6. Controller returns the result as JSON.

## 4. Frontend Architecture

### Angular structure

The frontend is an Angular application using the standalone component architecture introduced by Angular 17+.

Important characteristics:

- No `NgModule`-based feature modules are used.
- Components are standalone and import their dependencies directly.
- Routing is configured through `app.routes.ts`.
- The app uses the `App` component as the root shell.

### Modules

No traditional Angular modules are used. The application uses standalone components instead.

### Components

The UI is split into feature pages and reusable layout components.

Key components:

- `App` — root shell with navbar and sidebar
- `NavbarComponent` — top navigation and logout action
- `SidebarComponent` — collapsible product/warranty navigation panel
- `LoginComponent`, `SignupComponent`, `ForgotPasswordComponent`, `ResetPasswordComponent` — auth UI
- `HomeComponent` — dashboard for products and expiring warranties
- `ProductCreateComponent` — create product form
- `ProductDetailComponent` — display/edit/delete a product
- `ProfileComponent` — current-user profile page
- `AboutComponent`, `ContactComponent` — static content pages

### Services

The frontend uses service classes as the main abstraction layer for API communication.

Implemented services:

- `AuthService` — authentication state and API calls
- `ProductService`
- `CustomerService`
- `SaleService`
- `WarrantyService`
- `AMCService`
- `AMCOfferService`

These services use `HttpClient` and are provided at the root via `providedIn: 'root'`.

### Routing

Routes are declared in `app.routes.ts`.

Protected routes are gated by `authGuard`, while auth pages are gated by `guestGuard`.

Routes include:

- `/login`
- `/signup`
- `/forgot-password`
- `/reset-password`
- `/`
- `/products/new`
- `/products/:id`
- `/about`
- `/contact`
- `/profile`

### Signals

Angular signals are used extensively for local UI state and derived state.

Examples:

- `AuthService.currentUser = signal<User | null>(null)`
- `HomeComponent.allProducts`, `productSearch`, `productPage`
- `LoginComponent.email`, `password`, `submitting`, `errorMessage`
- `ProductDetailComponent.product`, `editing`, `loading`

Signals are used for reactive UI updates without a heavyweight state management library.

### Forms

The frontend uses template-driven forms via `FormsModule` rather than reactive forms.

Examples:

- `LoginComponent`
- `SignupComponent`
- `ProductCreateComponent`
- `ProductDetailComponent`

### Material components

Not implemented. The project does not include Angular Material or any component library.

### Shared components

The layout folder contains reusable shell components:

- `navbar`
- `sidebar`

These are included in the root app shell and reused by pages.

### HTTP communication

HTTP requests are performed through Angular `HttpClient`.

The app uses a custom interceptor:

- `credentials.interceptor.ts` adds `withCredentials: true` to every request so session cookies are sent correctly.

### State management

There is no dedicated state library such as NgRx or Akita. State is handled with:

- Angular signals in services and components
- Local component-level state
- HTTP response data stored in service state

### Folder organization

The frontend is organized by feature area rather than by module. Pages and components are grouped by domain and route purpose.

## 5. Database Architecture

### Database type

The application uses Neo4j, a native graph database.

### Graph model

The data model is a graph of connected business entities. Rather than storing everything in tables, the application models relationships directly in the graph.

### Node types

- `(:User)` — authentication account data
- `(:Customer)` — customer entity
- `(:Product)` — product entity
- `(:Sale)` — sale record
- `(:Warranty)` — warranty record
- `(:AMC)` — annual maintenance contract
- `(:AMCOffer)` — AMC offer template or plan

### Relationships

The graph includes these relationships:

- `(:Customer)-[:PURCHASED]->(:Sale)`
- `(:Sale)-[:OF_PRODUCT]->(:Product)`
- `(:Product)-[:HAS_WARRANTY]->(:Warranty)`
- `(:Warranty)-[:EXTENDED_BY]->(:AMC)`
- `(:AMC)-[:BASED_ON]->(:AMCOffer)`

### Constraints

No explicit Neo4j constraints or unique constraints are defined in the codebase. The application relies on Spring Data Neo4j-generated IDs and repository methods.

### Indexes

No explicit index definitions or schema migration files are present.

### Repository usage

Repositories are thin abstractions that let Spring Data Neo4j map Java entities to Neo4j nodes and relationships.

The persistence style is object-graph mapping plus custom Cypher queries for more complex traversals.

### Query style

The backend uses:

- standard repository methods such as `findAll()`, `findById()`, `save()`, `deleteById()`
- custom Cypher via `@Query` for graph traversal operations

Examples:

- `SaleRepository.findAllSalesByCustomerId()`
- `WarrantyRepository.findWarrantiesExpiringSoon()`
- `WarrantyRepository.findWarrantiesByCustomerId()`

### Data model

The data is stored as graph nodes with relationships, not as a relational schema. This is appropriate for connected business data such as customer purchases, product warranties, and service contracts.

## 6. API Architecture

### Base URL

The backend is configured to listen on port `8080` by default, with environment override support through `PORT`.

The frontend uses:

- `environment.apiBaseUrl = 'http://localhost:8080/api'`

### API versioning

No API versioning strategy is implemented. Endpoints are exposed directly under the root API path.

### Endpoint organization

The API is organized by resource:

| Resource | Base path |
| --- | --- |
| Auth | `/api/auth` |
| Products | `/api/products` |
| Customers | `/api/customers` |
| Sales | `/api/sales` |
| Warranty | `/api/warranty` |
| AMCs | `/api/amcs` |
| AMC Offers | `/api/amc-offers` |

### Request flow

1. Frontend issues an HTTP request.
2. CORS and cookies are handled via `WebConfig` and the Angular interceptor.
3. Spring Security checks authorization.
4. Controller parses input and delegates to service.
5. Service uses repository.
6. Response is returned as JSON.

### Response format

Most endpoints return JSON objects or arrays. For auth operations the response is a DTO record such as `AuthResponse` or `MessageResponse`.

### Error responses

The project returns JSON errors through `GlobalExceptionHandler`.

Typical shape:

```json
{
  "timestamp": "2026-07-24T10:00:00",
  "status": 400,
  "error": "Bad Request",
  "message": "Validation failed"
}
```

### Validation

Validation is implemented on auth DTOs and enforced by `@Valid` in controllers.

### Pagination

Not implemented. The API returns full lists for resources such as products and customers.

### Search

No backend search endpoints are implemented. Search is done client-side in the Angular home page using string matching against the loaded data.

### Sorting

No backend sorting is implemented.

## 7. Authentication & Security

The project implements session-based authentication using Spring Security.

### Security mechanisms

- `SecurityConfig` defines the authorization rules.
- `BCryptPasswordEncoder` is used for password hashing.
- Authentication state is stored in the HTTP session.
- `RestAuthenticationEntryPoint` returns JSON for unauthorized access.
- CORS is enabled for the Angular frontend.
- Cookies are used to send the session identifier to the backend.

### Authentication flow

1. User signs up or logs in via `/api/auth/signup` or `/api/auth/login`.
2. `AuthService` validates credentials.
3. Passwords are compared using BCrypt.
4. A Spring Security authentication object is stored in the session.
5. Subsequent requests use the session cookie to identify the user.

### Authorization rules

Public endpoints:

- `/api/auth/signup`
- `/api/auth/login`
- `/api/auth/forgot-password`
- `/api/auth/reset-password`

Protected endpoints:

- `/products/**`
- `/customers/**`
- `/sales/**`
- `/warranty/**`
- `/amcs/**`
- `/amc-offers/**`

The security configuration is notable because the controllers are mapped under `/api/...`, while the matcher rules use `/products/**` and similar paths. This mismatch is a potential weakness in the current implementation.

### Password reset flow

- A reset token is generated and stored on the `User` node.
- The token expires after 30 minutes.
- The frontend can use `/reset-password?token=...` to complete the reset.
- If mail is not configured, the reset URL is logged to the console.

### Security strengths

- Passwords are never stored in plain text.
- Session-based auth is simple and fits the current monolithic deployment.
- CSRF is disabled for API-style usage.

### Security limitations

- No JWT support
- No role-based authorization beyond `ROLE_USER`
- No refresh token mechanism
- No fine-grained permission model for specific resources

## 8. Dependency Graph

The dependency chain is straightforward and consistent:

```text
Angular Component
↓
Angular Service
↓
HttpClient / Interceptor
↓
REST API
↓
Controller
↓
Service
↓
Repository
↓
Neo4j Database
```

For authentication:

```text
Login/Signup Page
↓
AuthService
↓
AuthController
↓
AuthService
↓
UserRepository
↓
Neo4j User node
```

For graph-based business data:

```text
Home Page / Sidebar
↓
ProductService / WarrantyService
↓
Controller
↓
Service
↓
Repository
↓
Neo4j graph traversal
```

## 9. Request Lifecycle

### Create Product

1. The user navigates to `/products/new`.
2. `ProductCreateComponent` collects the product name and serial number.
3. The component calls `ProductService.create(product)`.
4. The Angular service sends `POST /api/products` with the payload.
5. `ProductController` receives the request.
6. `ProductService.createProduct()` delegates to `ProductRepository.save(product)`.
7. Spring Data Neo4j creates a `Product` node in Neo4j.
8. The created node is returned to the frontend.
9. The Angular router navigates to `/products/{id}`.

### Login flow

1. The user submits the login form.
2. `LoginComponent` calls `AuthService.login()`.
3. The frontend posts to `POST /api/auth/login`.
4. `AuthController` delegates to `AuthService.login()`.
5. `AuthService` validates the credentials against the `User` node in Neo4j.
6. A Spring Security session is created and stored in the HTTP session.
7. The frontend stores the authenticated user in the Angular signal-based auth state.
8. The user is redirected to the home page.

### Browse products and warranties

1. `HomeComponent` loads products and expiring warranties on initialization.
2. It calls `ProductService.getAll()` and `WarrantyService.getExpiringSoon()`.
3. The backend returns full lists.
4. The UI filters and paginates those lists in the browser using signals.

## 10. Design Patterns Used

### MVC

The backend follows a traditional MVC-style split:

- Controllers handle requests
- Services implement business logic
- Models represent graph entities

### Repository Pattern

Used in the persistence layer through `Neo4jRepository` and custom repository interfaces.

### Dependency Injection

A central Spring feature used throughout the backend and Angular services.

### DTO Pattern

Used for authentication request and response payloads. The app avoids exposing the raw `User` entity directly to clients.

### Singleton / Service Bean Pattern

Spring services are singleton beans by default.

### Signal-based State Management

The Angular frontend uses signals as a lightweight reactive state mechanism.

### Interceptor Pattern

The Angular app uses an HTTP interceptor to attach credentials to all requests.

### Notable absences

- Builder pattern is not used.
- Factory pattern is not used.
- Strategy pattern is not used.
- Domain event or CQRS pattern is not present.

## 11. Important Configurations

### `pom.xml`

The Maven build file defines:

- Spring Boot parent version `4.0.2`
- Java 25 as the target runtime
- Spring Data Neo4j, Spring Web MVC, Spring Security, Validation, Mail, Lombok dependencies
- Test dependencies for Spring Boot testing

### `application.properties`

This file contains:

- application name
- server port (`8080` or `PORT` env var)
- Neo4j URI and credentials
- CORS allowed origins
- base URL used for password reset links
- optional mail configuration
- logging levels

### Angular configuration

`frontend/angular.json` configures:

- Angular application build
- SSR build output
- asset handling
- dev-server proxy support
- production budgets

### CORS

CORS is configured in `WebConfig` via `addCorsMappings()` and uses the `app.cors.allowed-origins` property.

### Maven

Maven is the build tool for the backend. The project uses the Maven wrapper (`mvnw`) for cross-platform builds.

### Gradle

Not used.

### Environment configuration

The frontend uses `frontend/src/environments/environment.ts` for the API base URL. The Angular dev server uses `proxy.conf.json` to forward API calls to the backend during development.

## 12. Error Handling Architecture

### Exception hierarchy

The codebase has a simple exception hierarchy:

- `ResourceNotFoundException` extends `RuntimeException`

There are no broader custom exception categories such as `BadRequestException`, `ConflictException`, or `UnauthorizedException`.

### Global exception handling

`GlobalExceptionHandler` centrally handles:

- missing resources
- illegal argument failures
- validation errors
- unexpected exceptions

### Validation handling

Validation errors are produced using Bean Validation annotations and converted into `400 Bad Request` responses.

### API error responses

Errors are returned as JSON with a consistent envelope:

- `timestamp`
- `status`
- `error`
- `message`

## 13. Performance Considerations

### Pagination

Pagination is not implemented in the backend API. The frontend implements client-side pagination for products and warranties after loading the full lists.

### Search optimization

Search is currently client-side and simple. It is efficient for small datasets but scales poorly for large data volumes.

### Lazy loading

Not implemented. The app loads data eagerly in the UI and sidebar.

### Caching

No explicit caching layer exists.

### Query optimization

Custom Cypher queries are used for graph traversal, but there are no advanced optimization patterns such as query planning hints, indexes, or graph-specific tuning.

### Large dataset handling

The current architecture is adequate for a small-to-medium dataset and for demonstration or internal use, but it will not scale gracefully without server-side pagination, filtering, query tuning, and possibly a more explicit API contract.

## 14. Current Project Structure Summary

The current project is a compact, instructional full-stack architecture with the following main responsibilities:

| Package / Folder | Responsibility |
| --- | --- |
| `controllers` | Expose REST endpoints |
| `Services` | Enforce business rules and transactions |
| `Repositories` | Abstract Neo4j persistence |
| `Model/nodes` | Define graph nodes |
| `Model/dto/auth` | Define request/response DTOs |
| `config` | Security, routing, CORS |
| `Exceptions` | Central error responses |
| `frontend/src/app/pages` | Feature UIs |
| `frontend/src/app/services` | API communication |
| `frontend/src/app/layout` | Shared shell UI |
| `frontend/src/app/models` | TypeScript data contracts |
| `frontend/src/app/guards` | Route protection |

The packages interact in a straightforward chain:

- Angular pages call services.
- Services call backend REST endpoints.
- Controllers delegate to services.
- Services use repositories.
- Repositories persist and query Neo4j.

## 15. Architecture Diagram (Text)

### End-to-end application diagram

```text
Frontend (Angular SPA)
    │
    ▼
REST API (Spring Boot)
    │
    ▼
Controller
    │
    ▼
Service
    │
    ▼
Repository
    │
    ▼
Neo4j Graph Database
```

### Authentication flow diagram

```text
Browser
  │
  ▼
Angular Login Page
  │
  ▼
POST /api/auth/login
  │
  ▼
AuthController
  │
  ▼
AuthService
  │
  ▼
UserRepository + BCrypt
  │
  ▼
Spring Security Session
  │
  ▼
Authenticated API requests
```

### Graph domain diagram

```text
Customer
  └─ PURCHASED → Sale
        └─ OF_PRODUCT → Product
              └─ HAS_WARRANTY → Warranty
                    └─ EXTENDED_BY → AMC
                          └─ BASED_ON → AMCOffer
```

## 16. Strengths

- Clear separation of frontend, backend, and persistence concerns.
- The graph model is well suited to connected post-sale and AMC data.
- Spring Data Neo4j keeps persistence code concise.
- Session-based authentication is simple and easy to understand.
- The Angular frontend uses signals and standalone components, keeping the UI modern and lightweight.
- CORS and SPA routing are handled deliberately rather than implicitly.

## 17. Weaknesses

- No API versioning strategy is implemented.
- The backend exposes domain entities directly rather than using richer DTOs for most resources.
- Pagination and server-side filtering are missing.
- The security configuration appears to mismatch the actual `/api/...` controller paths for protected endpoints.
- There are no explicit Neo4j indexes or constraints.
- The frontend uses template-driven forms instead of reactive forms, which may be less scalable for large forms.
- The project does not use Angular Material or a component system, which limits UI consistency.
- Relationship classes under `Model/Relationships` are effectively unused placeholders.
- Test coverage is minimal; only a context load test exists.

## 18. Scalability

The architecture is suitable for a small-to-medium internal application and for a demonstration or prototype environment.

### What scales well

- The monolithic structure is straightforward to deploy and operate.
- Neo4j is a strong database choice for highly connected data.
- Angular standalone components and signals keep the frontend maintainable.

### What does not scale well yet

- Full-list API responses will become expensive as the graph grows.
- Client-side search and pagination will struggle at larger data volumes.
- Session-based auth is fine for a single instance but less ideal for distributed deployments without shared session storage.
- Lack of API versioning and DTO separation may make evolution harder.

## 19. Future Improvements

To move this architecture toward production readiness without changing the overall structure too drastically, the following improvements would be valuable:

- Introduce API versioning, for example `/api/v1/`.
- Add dedicated DTOs for all resource endpoints, not just auth.
- Implement server-side pagination, filtering, and sorting.
- Add Neo4j constraints and indexes for important properties.
- Replace the current path-based security matcher mismatch with correct protected-path patterns.
- Introduce integration tests for controllers, services, and repository interactions.
- Add structured logging, metrics, and health endpoints.
- Consider a more explicit service contract for graph relationships and relationship-specific business logic.
- Add caching for frequently read data and dashboard summaries.
- Consider a deployment strategy that supports distributed sessions if the app is scaled horizontally.

## 20. Application Startup Lifecycle

The startup lifecycle begins in `AmcProjectApplication.main()`.

```text
main()
↓
SpringApplication.run()
↓
ApplicationContext creation
↓
Component Scan
↓
Bean Definition Registration
↓
Dependency Injection
↓
Auto Configuration
↓
Embedded Tomcat startup
↓
DispatcherServlet initialization
↓
Security Filter Chain initialization
↓
Application Ready
```

### main()

`AmcProjectApplication.main(String[] args)` is the entry point. It calls `SpringApplication.run(AmcProjectApplication.class, args)`.

### SpringApplication.run()

Spring Boot bootstraps the application runtime and creates an `ApplicationContext`.

### ApplicationContext creation

The application context is the container that hosts all Spring-managed beans. In this project it contains controllers, services, repositories, configuration classes, and the security infrastructure.

### Component Scan

Because the application class is annotated with `@SpringBootApplication`, Spring scans the package `com.postSale.amcProject` for components. This discovers:

- `@Controller` classes in `controllers/`
- `@Service` classes in `Services/`
- `@Repository` interfaces in `Repositories/`
- `@Configuration` classes in `config/`
- `@Component` classes such as `RestAuthenticationEntryPoint`

### Bean Definition Registration

Spring registers beans for:

- `AuthController`, `ProductController`, `CustomerController`, `SaleController`, `WarrantyController`, `AMCController`, `AMCOfferController`
- `AuthService`, `ProductService`, `CustomerService`, `SaleService`, `WarrantyService`, `AMCService`, `AMCOfferService`, `EmailService`
- `UserRepository`, `ProductRepository`, `CustomerRepository`, `SaleRepository`, `WarrantyRepository`, `AMCRepository`, `AMCOfferRepository`
- `SecurityConfig`, `WebConfig`, `RestAuthenticationEntryPoint`
- `PasswordEncoder` and `SecurityFilterChain`

### Dependency Injection

The code uses constructor injection throughout the backend. For example, `AuthController` receives `AuthService`, and `AuthService` receives `UserRepository`, `PasswordEncoder`, and `EmailService`. Spring resolves these dependencies from the container before the beans are used.

### Auto Configuration

Spring Boot auto-configuration is active because the project includes starters such as `spring-boot-starter-webmvc`, `spring-boot-starter-security`, `spring-boot-starter-data-neo4j`, `spring-boot-starter-validation`, and `spring-boot-starter-mail`. The application properties in `application.properties` guide the auto-configured components, especially Neo4j, MVC, security, mail, and server settings.

### Embedded Tomcat startup

Spring Boot starts an embedded Tomcat server on the configured port. In this project the default is `8080`, but `server.port=${PORT:8080}` allows deployment environments like Render to override it.

### DispatcherServlet initialization

Spring Boot creates and initializes `DispatcherServlet` so the application can route incoming HTTP requests to controllers. This is the bridge from HTTP to Spring MVC controller methods.

### Security Filter Chain initialization

`SecurityConfig` creates the `SecurityFilterChain` bean. It registers the authorization rules and the authentication entry point, and it becomes active for incoming requests before controllers are invoked.

### Application Ready

Once the server is listening and the filters and servlet are initialized, the application is ready to serve requests. The project does not define a custom `ApplicationRunner` or `CommandLineRunner`, so readiness is implicit rather than explicitly implemented.

## 21. Detailed Request Execution Flow

The following flow is the normal path for a request such as `GET /api/products` or `POST /api/auth/login`.

```text
Browser
↓
Angular Component
↓
Signal update
↓
Angular Service
↓
HttpClient
↓
HTTP Interceptor
↓
Spring Security Filter Chain
↓
DispatcherServlet
↓
Handler Mapping
↓
Controller
↓
Validation
↓
Service
↓
Repository
↓
Spring Data Neo4j
↓
Cypher
↓
Neo4j
↓
Object Mapping
↓
JSON Serialization
↓
Angular receives response
↓
Signal updates
↓
DOM rerender
```

### Browser

The user action occurs in the browser, usually via a component such as `LoginComponent`, `HomeComponent`, or `ProductCreateComponent`.

### Angular Component

The component updates its local state using Angular signals and calls a service method. Example: `ProductCreateComponent` calls `ProductService.create(product)`.

### Signal update

Signals are updated immediately for local UI state. For example, `LoginComponent` updates `submitting` and `errorMessage` while the request is in flight.

### Angular Service

The service wraps the HTTP call and centralizes API knowledge. Example: `ProductService` constructs the endpoint `http://localhost:8080/api/products` from `environment.apiBaseUrl`.

### HttpClient

`HttpClient` sends the request to the backend over HTTP.

### HTTP Interceptor

`credentials.interceptor.ts` adds `withCredentials: true` so the browser includes the session cookie. This is critical for authentication because the backend relies on server-side sessions.

### Spring Security Filter Chain

The request passes through Spring Security before any controller logic is reached. The chain checks whether the route is public or protected, and it may reject the request with a JSON 401 if authentication is missing.

### DispatcherServlet

`DispatcherServlet` receives the request and uses the Spring MVC infrastructure to route it to the correct handler.

### Handler Mapping

The request is matched to a controller method based on the path and HTTP method. For example, `GET /api/products/{id}` maps to `ProductController.getProduct()`.

### Controller

The controller method receives the request, extracts parameters, and delegates to a service. Example: `AuthController.login()` delegates to `AuthService.login()`.

### Validation

If the request body is validated, Spring checks the DTO annotations such as `@Valid`, `@NotBlank`, `@Email`, and `@Size` before the service layer is invoked.

### Service

The service implements business logic and transaction boundaries. Example: `AuthService.login()` verifies credentials and creates a session.

### Repository

The service calls a repository interface such as `ProductRepository` or `UserRepository`.

### Spring Data Neo4j

Spring Data Neo4j translates the repository call into Neo4j operations. It can use repository methods like `findAll()` or `findById()`, and it can execute custom Cypher for relation-based queries.

### Cypher

Custom repository methods such as `findAllSalesByCustomerId()` or `findWarrantiesExpiringSoon()` execute Cypher generated from the `@Query` annotations.

### Neo4j

Neo4j returns the matching node or relationship data. The graph database is the source of truth for the current application state.

### Object Mapping

Spring Data Neo4j maps the database result back to Java objects such as `Product`, `Sale`, `Warranty`, or `User`.

### JSON Serialization

The controller returns Java objects or DTOs. Spring MVC serializes them to JSON and sends them back over HTTP.

### Angular receives response

The Angular service receives the JSON payload and returns an `Observable` to the component.

### Signal updates

The component updates its signals based on the result. For example, a successful product creation updates the route or the local product state.

### DOM rerender

Angular re-renders the affected UI based on the new signal values.

## 22. Package Dependency Architecture

The package relationships are simple and explicit.

### Backend dependency diagram

```text
com.postSale.amcProject.controllers
    ↓
com.postSale.amcProject.Services
    ↓
com.postSale.amcProject.Repositories
    ↓
Neo4j
```

```text
com.postSale.amcProject.config
    ├─ configures security rules
    ├─ configures CORS
    └─ depends on auth beans such as RestAuthenticationEntryPoint and PasswordEncoder
```

```text
com.postSale.amcProject.Services
    ├─ depends on com.postSale.amcProject.Repositories
    ├─ depends on com.postSale.amcProject.Model.nodes
    ├─ depends on com.postSale.amcProject.Model.dto.auth
    └─ throws com.postSale.amcProject.Exceptions.ResourceNotFoundException
```

```text
com.postSale.amcProject.Repositories
    └─ depends on com.postSale.amcProject.Model.nodes
```

### Frontend dependency diagram

```text
frontend/src/app/pages
    ↓
frontend/src/app/services
    ↓
backend REST API
```

```text
frontend/src/app/pages
    ├─ depends on frontend/src/app/services
    ├─ depends on frontend/src/app/models
    └─ depends on frontend/src/app/guards
```

```text
frontend/src/app/services
    ├─ depends on frontend/src/app/models
    └─ calls backend endpoints through HttpClient
```

### Dependency direction rules inferred from the code

- Controllers depend on services.
- Services depend on repositories.
- Repositories depend on graph entities.
- Frontend pages depend on services, not repositories.
- Configuration classes are infrastructure-oriented and are used by Spring, not by business logic classes directly.

## 23. Bean & Configuration Architecture

### Component Scan

The component scan begins at `com.postSale.amcProject` because `AmcProjectApplication` is in that package. It discovers every Spring-managed class under that tree.

### Auto Configuration

Spring Boot auto-configures the following areas based on the classpath and configuration:

- MVC web layer via `spring-boot-starter-webmvc`
- Security via `spring-boot-starter-security`
- Neo4j persistence via `spring-boot-starter-data-neo4j`
- Validation via `spring-boot-starter-validation`
- Mail via `spring-boot-starter-mail`

### Bean creation

Beans are created in three ways in this project:

1. Component scan creation for `@Service`, `@Controller`, `@Repository`, `@Component`
2. `@Bean` methods in `SecurityConfig`
3. Spring Boot framework internals such as `DispatcherServlet`, `TomcatServletWebServerFactory`, and the security filter chain infrastructure

### Bean lifecycle

The code does not define custom `@PostConstruct`, `@PreDestroy`, or lifecycle hooks. Beans are created as Spring singleton beans by default. The transaction boundaries are controlled by annotations such as `@Transactional` rather than lifecycle hooks.

### Constructor Injection

Constructor injection is the dominant pattern. Examples:

- `ProductController(ProductService productService)`
- `ProductService(ProductRepository productRepository)`
- `AuthService(UserRepository, PasswordEncoder, EmailService)`

This makes dependencies explicit and easier to test.

### SecurityConfig

`SecurityConfig` is the central security bean configuration class. It creates:

- `PasswordEncoder` as a BCrypt bean
- `SecurityFilterChain` that controls authorization rules and session behavior

It depends on `RestAuthenticationEntryPoint` for JSON 401 responses.

### WebConfig

`WebConfig` implements `WebMvcConfigurer`. It is responsible for CORS mapping so the Angular frontend can call the backend. It reads the `app.cors.allowed-origins` property and configures allowed methods and headers.

### RestAuthenticationEntryPoint

`RestAuthenticationEntryPoint` implements `AuthenticationEntryPoint`. It is instantiated by Spring as a component and used by the security configuration when authentication is missing.

### PasswordEncoder Bean

The `PasswordEncoder` bean is created by `SecurityConfig.passwordEncoder()` and returns `BCryptPasswordEncoder`. It is used by `AuthService` during signup and login, and it is also part of the Spring Security authentication flow.

### DispatcherServlet

The `DispatcherServlet` is created by Spring Boot’s web MVC auto-configuration. It is the central dispatcher for incoming requests and is responsible for routing requests to the correct controller methods.

## 24. Frontend Internal Architecture

The frontend is built around a simple internal chain:

```text
Component
↓
Signal
↓
Service
↓
HttpClient
↓
Interceptor
↓
Backend
```

### Component

Components are standalone and own their localized UI state. Examples:

- `LoginComponent` owns the email/password form state via signals.
- `ProductCreateComponent` owns the create-form state.
- `ProductDetailComponent` owns edit mode and the currently displayed product.

### Signal

Signals are the primary reactive primitive. They are used both for simple local state and derived values. Example: `HomeComponent` uses `productSearch`, `productPage`, and computed values such as `filteredProducts` and `pagedProducts`.

### Service

Services abstract backend communication. Example:

- `AuthService` manages authentication state and network calls.
- `ProductService` wraps `GET /api/products`, `POST /api/products`, `PUT /api/products`, and `DELETE /api/products`.
- `WarrantyService` wraps warranty queries.

### HttpClient

`HttpClient` is the transport layer. It sends HTTP requests and receives JSON responses.

### Interceptor

The `credentialsInterceptor` attaches `withCredentials: true` to every request. This ensures that the session cookie created by the backend is sent back on subsequent calls.

### Backend

The backend responds with JSON. The UI updates signals based on the result and rerenders the DOM.

### Actual examples from the codebase

- `App` uses `AuthService.isAuthenticated` and `AuthService.currentUser` to show or hide the sidebar.
- `NavbarComponent` calls `AuthService.logout()` when the user clicks logout.
- `HomeComponent` fetches products and warranties on initialization and derives client-side pagination states from signals.
- `ProductDetailComponent` uses route parameters to load a single product, edit it in place, and navigate away on delete.

## 25. Feature Architecture

### Authentication

#### Purpose

Provide signup, login, logout, current-user lookup, and password reset for the application.

#### Classes involved

- `AuthController`
- `AuthService`
- `UserRepository`
- `EmailService`
- `User`
- `LoginRequest`, `SignupRequest`, `ForgotPasswordRequest`, `ResetPasswordRequest`
- `AuthResponse`, `AuthUserResponse`, `MessageResponse`
- `SecurityConfig`
- `RestAuthenticationEntryPoint`

#### REST endpoints

- `POST /api/auth/signup`
- `POST /api/auth/login`
- `GET /api/auth/me`
- `POST /api/auth/forgot-password`
- `POST /api/auth/reset-password`
- `POST /api/auth/logout`

#### Database nodes

- `(:User)`

#### Relationships

- None in the current implementation.

#### Frontend pages

- `login`
- `signup`
- `forgot-password`
- `reset-password`
- `profile`

#### Services

- `AuthService`

#### Execution flow

1. The user submits credentials in a page component.
2. The Angular service posts to `/api/auth/login` or `/api/auth/signup`.
3. `AuthService` validates the DTO and creates a Spring Security session.
4. The frontend stores the authenticated user in the `currentUser` signal.

#### ASCII diagram

```text
Login/Signup Page
↓
AuthService
↓
AuthController
↓
AuthService
↓
UserRepository
↓
User node in Neo4j
```

### Product

#### Purpose

Create, read, update, and delete products.

#### Classes involved

- `ProductController`
- `ProductService`
- `ProductRepository`
- `Product`

#### REST endpoints

- `POST /api/products`
- `GET /api/products`
- `GET /api/products/{id}`
- `PUT /api/products`
- `DELETE /api/products/{id}`

#### Database nodes

- `(:Product)`

#### Relationships

- `(:Product)-[:HAS_WARRANTY]->(:Warranty)`

#### Frontend pages

- `home`
- `product-create`
- `product-detail`

#### Services

- `ProductService`

#### Execution flow

1. The user enters product details in `ProductCreateComponent`.
2. `ProductService.create()` sends `POST /api/products`.
3. `ProductController` delegates to `ProductService.createProduct()`.
4. `ProductRepository.save(product)` persists the product node.
5. The user is redirected to the detail page.

#### ASCII diagram

```text
ProductCreateComponent
↓
ProductService
↓
ProductController
↓
ProductService
↓
ProductRepository
↓
Product node
```

### Customer

#### Purpose

Represent customers and their purchase relationships.

#### Classes involved

- `CustomerController`
- `CustomerService`
- `CustomerRepository`
- `Customer`

#### REST endpoints

- `POST /api/customers`
- `GET /api/customers`
- `GET /api/customers/{id}`
- `PUT /api/customers`
- `DELETE /api/customers/{id}`

#### Database nodes

- `(:Customer)`

#### Relationships

- `(:Customer)-[:PURCHASED]->(:Sale)`

#### Frontend pages

- No dedicated Angular page currently uses this feature.

#### Services

- `CustomerService`

#### Execution flow

1. A client calls the customer endpoint.
2. The controller delegates to the service.
3. The repository persists or retrieves the `Customer` node.
4. The effect is visible in the graph relationship to sales.

#### ASCII diagram

```text
CustomerController
↓
CustomerService
↓
CustomerRepository
↓
Customer node
```

### Sale

#### Purpose

Track purchases and link them to products.

#### Classes involved

- `SaleController`
- `SaleService`
- `SaleRepository`
- `Sale`

#### REST endpoints

- `GET /api/sales/{id}`
- `POST /api/sales`
- `GET /api/sales` (implemented with a request body)

#### Database nodes

- `(:Sale)`
- `(:Customer)`
- `(:Product)`

#### Relationships

- `(:Customer)-[:PURCHASED]->(:Sale)`
- `(:Sale)-[:OF_PRODUCT]->(:Product)`

#### Frontend pages

- No dedicated Angular page currently uses this feature.

#### Services

- `SaleService`

#### Execution flow

1. The controller receives a sale request.
2. `SaleService` delegates to `SaleRepository`.
3. The repository uses a custom Cypher query to find sales for a customer when requested.
4. The data is returned as `Sale` entities.

#### ASCII diagram

```text
SaleController
↓
SaleService
↓
SaleRepository
↓
Cypher query
↓
Customer/Sale/Product graph
```

### Warranty

#### Purpose

Expose warranties and detect those expiring soon.

#### Classes involved

- `WarrantyController`
- `WarrantyService`
- `WarrantyRepository`
- `Warranty`

#### REST endpoints

- `GET /api/warranty`
- `GET /api/warranty/{id}`

#### Database nodes

- `(:Warranty)`
- `(:Product)`
- `(:Customer)`

#### Relationships

- `(:Product)-[:HAS_WARRANTY]->(:Warranty)`
- `(:Customer)-[:PURCHASED]->(:Sale)-[:OF_PRODUCT]->(:Product)`

#### Frontend pages

- `home`
- `sidebar`

#### Services

- `WarrantyService`

#### Execution flow

1. `HomeComponent` and `SidebarComponent` request warranty data from the backend.
2. `WarrantyController` delegates to `WarrantyService`.
3. `WarrantyRepository` executes Cypher to find expiring warranties or those linked to a customer.
4. The results are displayed in the UI.

#### ASCII diagram

```text
HomeComponent / SidebarComponent
↓
WarrantyService
↓
WarrantyController
↓
WarrantyRepository
↓
Cypher traversal
↓
Warranty nodes
```

### AMC

#### Purpose

Manage annual maintenance contracts.

#### Classes involved

- `AMCController`
- `AMCService`
- `AMCRepository`
- `AMC`

#### REST endpoints

- `POST /api/amcs`
- `GET /api/amcs`
- `GET /api/amcs/{id}`
- `PUT /api/amcs`
- `DELETE /api/amcs/{id}`

#### Database nodes

- `(:AMC)`

#### Relationships

- `(:Warranty)-[:EXTENDED_BY]->(:AMC)`

#### Frontend pages

- No dedicated Angular page currently uses this feature.

#### Services

- `AMCService`

#### Execution flow

1. The client submits AMC data.
2. The controller delegates to the service.
3. The repository saves the `AMC` node.
4. The node can later be connected to a warranty via the graph relationship.

#### ASCII diagram

```text
AMCController
↓
AMCService
↓
AMCRepository
↓
AMC node
```

### AMC Offer

#### Purpose

Represent AMC offer templates such as Silver or Gold plans.

#### Classes involved

- `AMCOfferController`
- `AMCOfferService`
- `AMCOfferRepository`
- `AMCOffer`

#### REST endpoints

- `POST /api/amc-offers`
- `GET /api/amc-offers`
- `GET /api/amc-offers/{id}`
- `PUT /api/amc-offers`
- `DELETE /api/amc-offers/{id}`

#### Database nodes

- `(:AMCOffer)`

#### Relationships

- `(:AMC)-[:BASED_ON]->(:AMCOffer)`

#### Frontend pages

- No dedicated Angular page currently uses this feature.

#### Services

- `AMCOfferService`

#### Execution flow

1. An AMC offer is created through the REST API.
2. The service persists the offer node.
3. AMCs can later reference the offer through the `BASED_ON` relationship.

#### ASCII diagram

```text
AMCOfferController
↓
AMCOfferService
↓
AMCOfferRepository
↓
AMCOffer node
```

## 26. Neo4j Graph Architecture

The project uses Neo4j as a graph database and stores the business domain as a connected graph of nodes and relationships.

### Graph overview

```text
(:Customer)
      |
  :PURCHASED
      |
    (:Sale)
      |
  :OF_PRODUCT
      |
    (:Product)
      |
  :HAS_WARRANTY
      |
    (:Warranty)
      |
  :EXTENDED_BY
      |
    (:AMC)
      |
  :BASED_ON
      |
    (:AMCOffer)
```

### Ownership of each relationship

The relationship ownership is expressed in the Java domain model through the node fields that hold outgoing relationships:

- `Customer.purchases` owns `PURCHASED`
- `Sale.productList` owns `OF_PRODUCT`
- `Product.warrantyList` owns `HAS_WARRANTY`
- `Warranty.amcList` owns `EXTENDED_BY`
- `AMC.amcOfferList` owns `BASED_ON`

### Notes on graph semantics

- `User` is a separate node type and is not connected to the product lifecycle graph in the current code.
- The relationship classes under `Model/Relationships` are present but not actively used by the current implementation.
- The graph is modeled as a simple directed graph with one outgoing relationship list per owning node.

## 27. Layer Responsibilities

| Layer | Responsibility | Depends On | Used By | Examples |
| --- | --- | --- | --- | --- |
| Angular component | Render UI and manage local UI state | Signals, services, router | User | `LoginComponent`, `HomeComponent`, `ProductDetailComponent` |
| Angular signal/state | Hold reactive UI state and derived values | Component | Component | `currentUser`, `filteredProducts`, `productPage` |
| Angular service | Coordinate frontend state and HTTP calls | HttpClient, models | Components | `AuthService`, `ProductService`, `WarrantyService` |
| HTTP interceptor | Attach credentials and session cookies | HttpClient | All frontend services | `credentialsInterceptor` |
| Controller | Receive HTTP requests and return JSON responses | Service layer | Browser | `AuthController`, `ProductController` |
| Service | Implement business rules and transaction boundaries | Repository layer, models, exceptions | Controllers | `AuthService`, `ProductService`, `WarrantyService` |
| Repository | Encapsulate persistence operations and Cypher queries | Neo4j entities | Services | `UserRepository`, `WarrantyRepository` |
| Neo4j data layer | Store and query graph nodes and relationships | Neo4j engine | Repositories | `(:Product)`, `(:Warranty)`, `(:AMC)` |
| Configuration layer | Manage infrastructure concerns such as security and CORS | Spring framework | Spring container | `SecurityConfig`, `WebConfig` |
| Exception layer | Convert failures into consistent API responses | Spring MVC | Controllers and services | `GlobalExceptionHandler` |

## 28. Package/Class Inventory

The following inventory is derived from the actual files present in the source tree.

| Item | Count |
| --- | ---: |
| Controllers | 7 |
| Services | 8 |
| Repositories | 7 |
| Entities | 7 |
| Relationship classes | 5 |
| DTOs | 7 |
| Configuration classes | 4 |
| Exception classes | 2 |
| Angular pages | 10 |
| Angular component files | 12 |
| Angular services | 8 |
| Guards | 2 |
| Interceptors | 1 |
| Route files | 1 |
| Route entries | 10 |
| REST API endpoint methods | 31 |
| Node labels | 7 |
| Relationship types | 5 |

## 29. Production Readiness Analysis

### Performance bottlenecks

- The API returns full lists for several resources, which can become expensive as the graph grows.
- Pagination is implemented client-side only, not server-side.
- Search and filtering are done in the browser after data has already been loaded.
- There are no explicit Neo4j indexes or constraints for common lookup properties.
- The `WarrantyRepository` and `SaleRepository` queries are simple but not tuned for large datasets.

### Scalability bottlenecks

- The architecture is a single monolithic backend and frontend deployment, which is fine for a small application but not ideal for high-scale distributed systems.
- Session authentication is simple but not ideal for horizontally scaled deployments without shared session storage.
- No API versioning, no server-side pagination, and no caching make future growth harder.

### Security limitations

- Authentication is session-based and does not implement JWT, refresh tokens, or external identity providers.
- Authorization is coarse-grained; there is only a single `ROLE_USER` role in the current code.
- The security matcher patterns for protected endpoints do not line up with the actual `/api/...` controller paths, which is a correctness risk.
- There is no explicit CSRF protection strategy beyond disabling it for API-style usage.

### Code organization strengths

- Layering is clear and consistent.
- The package structure is easy to navigate.
- Most business logic is isolated in services rather than controllers.
- Spring dependency injection keeps the code relatively decoupled.

### Code organization weaknesses

- Direct entity serialization is used for many resource endpoints rather than dedicated DTOs.
- The project mixes UI state management and HTTP concerns across components without a larger state layer.
- Some relationship classes exist but are not actively used, which can confuse future maintainers.
- The frontend does not yet expose all backend capabilities, especially for customers, AMCs, and AMC offers.

### Extension points

- New features can be added by creating a new controller, service, repository, and UI service pair.
- Existing auth and CRUD flows already provide a consistent pattern to follow.
- The graph model can be extended by adding more node types and relationship annotations.

### Maintainability concerns

- The project relies heavily on convention and naming rather than explicit interfaces or abstraction layers.
- The authentication and domain flows are straightforward, but they are not yet separated into richer command/query or feature-specific modules.
- Tests are minimal; the current suite only verifies that the Spring context loads.

### Potential refactoring opportunities

- Introduce DTOs for all resource endpoints instead of returning node entities directly.
- Add pagination and filtering to the backend API rather than relying on client-side logic.
- Add indexes and constraints to the graph model for important lookups.
- Add more focused integration tests around controllers, services, and repositories.

## 30. Architecture Decision Record

The architecture appears to have been chosen for clarity and simplicity rather than for maximum enterprise complexity.

### Why Neo4j

Neo4j was chosen because the business domain is inherently graph-like: customers buy products, products have warranties, warranties can be extended by AMCs, and AMCs are based on offer templates. The data model is easier to understand when expressed as nodes and relationships rather than as wide tables.

### Why layered architecture

The project uses a layered structure because it keeps responsibilities clear and makes the code easier to follow for a small-to-medium application. Controllers manage HTTP, services implement business logic, and repositories manage persistence.

### Why session authentication

Session-based authentication was chosen because the application is a traditional browser-based SPA with a server-side backend. It avoids the need to manage JWT tokens in the frontend and works well with the current cookie-based flow and Spring Security integration.

### Why Angular standalone

Angular standalone components were chosen to keep the frontend lightweight and modern. The project avoids `NgModule` boilerplate and uses direct imports for services, forms, and router features.

### Why Signals

Signals were chosen because the application is relatively small and does not yet need a heavy state-management library such as NgRx. Signals provide a direct, reactive way to manage local UI state and derived state.

### Advantages

- Clear separation of concerns
- Low implementation complexity
- Good fit for graph-based relationships
- Easy to reason about for a small team or educational project

### Tradeoffs

- Less scalable than a more explicitly partitioned enterprise architecture
- Less expressive than a richer DTO and API design for larger systems
- Session authentication is simple but not ideal for distributed deployments

## 31. Development Guidelines

The codebase reveals several conventions that future developers can follow.

### Naming conventions

- Java classes use PascalCase: `AuthController`, `ProductService`, `CustomerRepository`.
- Java methods and fields use camelCase: `createProduct`, `getAllProducts`, `productName`.
- Package names use lowercase segments: `com.postSale.amcProject.controllers`, `com.postSale.amcProject.Services`.
- Angular files follow framework conventions such as `*.component.ts`, `*.service.ts`, `*.guard.ts`, and `*.interceptor.ts`.

### Package conventions

- HTTP entry points live under `controllers`.
- Business logic lives under `Services`.
- Persistence abstractions live under `Repositories`.
- Domain entities live under `Model/nodes`.
- DTOs for auth live under `Model/dto/auth`.
- Cross-cutting infrastructure lives under `config` and `Exceptions`.

### Dependency rules

- Controllers should depend on services, not on repositories.
- Services should depend on repositories, not on controllers.
- Frontend pages should depend on services and models, not on backend repositories.
- Configuration classes should remain infrastructure-focused and should not contain business logic.

### Layering rules

- Keep HTTP concerns in controllers.
- Keep business rules in services.
- Keep database access in repositories.
- Keep validation close to the request DTOs.
- Keep exception translation in the global exception handler.

### Coding patterns

- Use constructor injection for dependencies.
- Use `@Transactional` on service methods that mutate or read domain state.
- Use `@RestController` for JSON endpoints.
- Use `@Service` for business logic beans.
- Use Angular signals for responsive local UI state.
- Use an interceptor for cross-cutting request concerns such as credentials.

### Validation approach

- Use Bean Validation annotations on DTOs such as `@NotBlank`, `@Email`, and `@Size`.
- Apply `@Valid` at the controller boundary.
- Perform simple client-side validation in the Angular components before submitting data.

### Exception handling approach

- Throw `ResourceNotFoundException` for missing entities.
- Let `GlobalExceptionHandler` convert exceptions into consistent JSON responses.
- Avoid ad-hoc error handling inside controllers where possible.

### DTO usage

- DTOs are used for authentication endpoints and are the main form of input/output contract in the current code.
- Most CRUD endpoints still return the domain entities directly rather than using dedicated resource DTOs.

### Repository usage

- Repositories should extend `Neo4jRepository<T, String>` for standard CRUD operations.
- Custom graph-specific queries should be expressed with `@Query` and Cypher where relationship traversal is required.
- Repository methods should remain persistence-oriented and should not contain business rules.
