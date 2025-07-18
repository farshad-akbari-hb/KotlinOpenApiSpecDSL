# Best Practices

This guide provides best practices and design patterns for creating high-quality OpenAPI specifications using the Kotlin OpenAPI Spec DSL.

## Table of Contents
- [API Design Principles](#api-design-principles)
- [Schema Design](#schema-design)
- [Naming Conventions](#naming-conventions)
- [Versioning Strategies](#versioning-strategies)
- [Error Handling](#error-handling)
- [Security Best Practices](#security-best-practices)
- [Documentation Guidelines](#documentation-guidelines)
- [Performance Considerations](#performance-considerations)
- [Testing Your API Specs](#testing-your-api-specs)
- [Code Organization](#code-organization)

## API Design Principles

### 1. RESTful Design

```kotlin
// Good: RESTful resource design
paths {
    // Collection resource
    path("/users") {
        get {   // List users
            operationId = "listUsers"
        }
        post {  // Create user
            operationId = "createUser"
        }
    }
    
    // Individual resource
    path("/users/{userId}") {
        get {    // Get specific user
            operationId = "getUser"
        }
        put {    // Replace user
            operationId = "replaceUser"
        }
        patch {  // Update user partially
            operationId = "updateUser"
        }
        delete { // Delete user
            operationId = "deleteUser"
        }
    }
    
    // Sub-resources
    path("/users/{userId}/posts") {
        get {    // Get user's posts
            operationId = "getUserPosts"
        }
    }
}

// Avoid: Non-RESTful design
paths {
    // Bad: Verbs in URLs
    path("/getUser") { }
    path("/deleteUser") { }
    
    // Bad: Non-standard methods
    path("/users/{userId}") {
        post {  // Should use PUT or PATCH for updates
            summary = "Update user"
        }
    }
}
```

### 2. Consistent Response Structure

```kotlin
// Define consistent response wrapper
@Serializable
data class ApiResponse<T>(
    val success: Boolean,
    val data: T? = null,
    val error: ErrorInfo? = null,
    val metadata: ResponseMetadata
)

@Serializable
data class ResponseMetadata(
    val timestamp: String,
    val requestId: String,
    val version: String
)

// Use consistently across endpoints
response("200", "Success") {
    jsonContent {
        schema {
            allOf = listOf(
                ref("#/components/schemas/ApiResponse"),
                SchemaReference.Schema(Schema(
                    type = SchemaType.OBJECT,
                    properties = mapOf(
                        "data" to Schema(ref = "#/components/schemas/User")
                    )
                ))
            )
        }
    }
}
```

### 3. Idempotency

```kotlin
// Support idempotency for non-safe methods
post {
    summary = "Create payment"
    
    parameter {
        name = "Idempotency-Key"
        `in` = ParameterLocation.HEADER
        required = true
        description = "Unique key to ensure idempotent requests"
        schema {
            type = SchemaType.STRING
            format = "uuid"
        }
    }
    
    response("201", "Payment created")
    response("200", "Payment already exists (idempotent response)") {
        headers {
            header("X-Idempotent-Replay") {
                description = "Indicates this is a replay of a previous request"
                schema {
                    type = SchemaType.BOOLEAN
                }
            }
        }
    }
}
```

## Schema Design

### 1. Use Composition Over Inheritance

```kotlin
// Good: Composition with allOf
components {
    schema("Timestamped") {
        type = SchemaType.OBJECT
        properties {
            "createdAt" to schema {
                type = SchemaType.STRING
                format = "date-time"
                readOnly = true
            }
            "updatedAt" to schema {
                type = SchemaType.STRING
                format = "date-time"
                readOnly = true
            }
        }
    }
    
    schema("Identifiable") {
        type = SchemaType.OBJECT
        required = listOf("id")
        properties {
            "id" to schema {
                type = SchemaType.STRING
                format = "uuid"
                readOnly = true
            }
        }
    }
    
    schema("User") {
        allOf = listOf(
            ref("#/components/schemas/Identifiable"),
            ref("#/components/schemas/Timestamped"),
            SchemaReference.Schema(Schema(
                type = SchemaType.OBJECT,
                required = listOf("email", "name"),
                properties = mapOf(
                    "email" to Schema(type = SchemaType.STRING, format = "email"),
                    "name" to Schema(type = SchemaType.STRING)
                )
            ))
        )
    }
}
```

### 2. Separate Read and Write Schemas

```kotlin
// Write schema (for requests)
@Serializable
data class CreateUserRequest(
    val email: String,
    val name: String,
    val password: String
)

// Read schema (for responses)
@Serializable
data class User(
    val id: String,
    val email: String,
    val name: String,
    val createdAt: String,
    val updatedAt: String
    // Note: No password in response
)

// Update schema (for partial updates)
@Serializable
data class UpdateUserRequest(
    val email: String? = null,
    val name: String? = null,
    val password: String? = null
)
```

### 3. Use Enums Judiciously

```kotlin
// Good: Stable, well-defined enums
@Serializable
enum class OrderStatus {
    PENDING, PROCESSING, SHIPPED, DELIVERED, CANCELLED
}

// Consider: Extensible string with examples
schema("PaymentMethod") {
    type = SchemaType.STRING
    description = "Payment method type"
    examples = listOf("credit_card", "paypal", "bank_transfer", "crypto")
    // Allows for future payment methods without breaking changes
}

// For truly dynamic values, use pattern validation
schema("CustomStatus") {
    type = SchemaType.STRING
    pattern = "^[A-Z][A-Z0-9_]*$"
    description = "Custom status in UPPER_SNAKE_CASE"
}
```

### 4. Nullable vs Optional

```kotlin
@Serializable
data class UserProfile(
    // Required, cannot be null
    val userId: String,
    
    // Required, can be null (user explicitly set no bio)
    val bio: String?,
    
    // Optional, defaults to null if not provided
    val website: String? = null,
    
    // Optional with default value
    val isPublic: Boolean = true
)

// In OpenAPI schema
schema("UserProfile") {
    type = SchemaType.OBJECT
    required = listOf("userId", "bio") // Note: bio is required but nullable
    properties {
        "userId" to schema {
            type = SchemaType.STRING
        }
        "bio" to schema {
            type = SchemaType.STRING
            nullable = true // Can be null
        }
        "website" to schema {
            type = SchemaType.STRING
            format = "uri"
            nullable = true
        }
        "isPublic" to schema {
            type = SchemaType.BOOLEAN
            default = true
        }
    }
}
```

## Naming Conventions

### 1. URL Paths

```kotlin
// Good: Lowercase, hyphenated, plural for collections
paths {
    path("/users") { }
    path("/user-profiles") { }
    path("/shopping-carts") { }
    path("/users/{userId}/billing-addresses") { }
}

// Avoid
paths {
    path("/Users") { }              // Don't use PascalCase
    path("/user_profiles") { }       // Don't use underscores
    path("/shoppingCart") { }        // Don't use camelCase
    path("/user/{id}/address") { }   // Be consistent with parameter names
}
```

### 2. Operation IDs

```kotlin
// Pattern: verbResource[ByQualifier]
get {
    operationId = "listUsers"        // GET /users
}
post {
    operationId = "createUser"       // POST /users
}
get {
    operationId = "getUserById"      // GET /users/{userId}
}
get {
    operationId = "listUserPosts"    // GET /users/{userId}/posts
}
```

### 3. Schema Names

```kotlin
components {
    // Entities: Singular, PascalCase
    schema("User") { }
    schema("OrderItem") { }
    
    // Requests: [Action][Resource]Request
    schema("CreateUserRequest") { }
    schema("UpdateOrderRequest") { }
    
    // Responses: [Resource]Response or specific name
    schema("UserResponse") { }
    schema("PaginatedUsersResponse") { }
    schema("ErrorResponse") { }
    
    // Value objects: Descriptive names
    schema("Money") { }
    schema("Address") { }
    schema("DateRange") { }
}
```

## Versioning Strategies

### 1. URL Path Versioning

```kotlin
servers {
    server {
        url = "https://api.example.com/v1"
        description = "API version 1"
    }
    server {
        url = "https://api.example.com/v2"
        description = "API version 2"
    }
}

// Or with variable
server {
    url = "https://api.example.com/{version}"
    variables {
        variable("version") {
            default = "v2"
            enum = listOf("v1", "v2")
            description = "API version"
        }
    }
}
```

### 2. Header Versioning

```kotlin
paths {
    path("/users") {
        get {
            parameter {
                name = "API-Version"
                `in` = ParameterLocation.HEADER
                required = true
                schema {
                    type = SchemaType.STRING
                    enum = listOf("1.0", "1.1", "2.0")
                    default = "2.0"
                }
            }
        }
    }
}
```

### 3. Content Type Versioning

```kotlin
get {
    response("200", "Success") {
        // Version 1
        content("application/vnd.api.v1+json") {
            schema {
                ref = "#/components/schemas/UserV1"
            }
        }
        
        // Version 2
        content("application/vnd.api.v2+json") {
            schema {
                ref = "#/components/schemas/UserV2"
            }
        }
    }
}
```

## Error Handling

### 1. Consistent Error Schema

```kotlin
@Serializable
data class ErrorResponse(
    val error: ErrorDetails,
    val traceId: String? = null,
    val timestamp: String
)

@Serializable
data class ErrorDetails(
    val code: String,           // Machine-readable error code
    val message: String,        // Human-readable message
    val target: String? = null, // Which field/parameter caused the error
    val details: List<ErrorDetail>? = null // Nested errors
)

@Serializable
data class ErrorDetail(
    val code: String,
    val message: String,
    val target: String
)

// Usage
response("400", "Validation error") {
    jsonContent {
        schema {
            ref = "#/components/schemas/ErrorResponse"
        }
        example = mapOf(
            "error" to mapOf(
                "code" to "VALIDATION_ERROR",
                "message" to "Request validation failed",
                "details" to listOf(
                    mapOf(
                        "code" to "REQUIRED_FIELD",
                        "message" to "Email is required",
                        "target" to "email"
                    ),
                    mapOf(
                        "code" to "INVALID_FORMAT",
                        "message" to "Invalid email format",
                        "target" to "email"
                    )
                )
            ),
            "traceId" to "550e8400-e29b-41d4-a716-446655440000",
            "timestamp" to "2023-10-20T15:30:00Z"
        )
    }
}
```

### 2. HTTP Status Codes

```kotlin
// Use appropriate status codes
paths {
    path("/users") {
        post {
            response("201", "Created") {        // Resource created
                headers {
                    header("Location") {        // Include location of new resource
                        schema { type = SchemaType.STRING }
                    }
                }
            }
            response("400", "Bad Request")      // Client error - invalid data
            response("401", "Unauthorized")     // Authentication required
            response("403", "Forbidden")        // Authenticated but not authorized
            response("409", "Conflict")         // Resource already exists
            response("422", "Unprocessable")    // Validation failed
        }
        
        get {
            response("200", "OK")               // Success with content
            response("204", "No Content")       // Success without content
            response("304", "Not Modified")     // Caching
        }
        
        delete {
            response("204", "No Content")       // Successful deletion
            response("404", "Not Found")        // Resource doesn't exist
            response("410", "Gone")             // Resource permanently deleted
        }
    }
}
```

## Security Best Practices

### 1. Least Privilege Principle

```kotlin
components {
    securityScheme("oauth2") {
        type = SecuritySchemeType.OAUTH2
        flows {
            authorizationCode {
                authorizationUrl = "https://auth.example.com/authorize"
                tokenUrl = "https://auth.example.com/token"
                scopes {
                    // Fine-grained scopes
                    scope("users:read", "Read user information")
                    scope("users:write", "Create and update users")
                    scope("users:delete", "Delete users")
                    scope("profile:read", "Read own profile")
                    scope("profile:write", "Update own profile")
                    scope("admin:all", "Full administrative access")
                }
            }
        }
    }
}

// Apply minimal required scopes
paths {
    path("/profile") {
        get {
            security {
                requirement("oauth2") {
                    scopes = listOf("profile:read") // Only read scope needed
                }
            }
        }
        
        patch {
            security {
                requirement("oauth2") {
                    scopes = listOf("profile:write") // Write scope for updates
                }
            }
        }
    }
    
    path("/admin/users") {
        delete {
            security {
                requirement("oauth2") {
                    scopes = listOf("admin:all", "users:delete") // Either scope works
                }
            }
        }
    }
}
```

### 2. Sensitive Data Handling

```kotlin
// Never expose sensitive data in responses
@Serializable
data class User(
    val id: String,
    val email: String,
    val name: String
    // Don't include: password, ssn, credit cards, etc.
)

// Mark sensitive parameters
parameter {
    name = "password"
    `in` = ParameterLocation.QUERY  // Avoid! Use body instead
    schema {
        type = SchemaType.STRING
        format = "password"  // Indicates UI should hide input
    }
}

// Use write-only for sensitive fields
schema("UserInput") {
    type = SchemaType.OBJECT
    properties {
        "password" to schema {
            type = SchemaType.STRING
            format = "password"
            writeOnly = true  // Never appears in responses
            minLength = 12
        }
    }
}
```

### 3. API Key Security

```kotlin
components {
    // Don't use API keys in URLs
    securityScheme("apiKey") {
        type = SecuritySchemeType.API_KEY
        name = "X-API-Key"
        `in` = ApiKeyLocation.HEADER  // Not QUERY
        description = """
            API key for authentication.
            
            Best practices:
            - Rotate keys regularly
            - Use different keys for different environments
            - Implement key expiration
            - Log key usage for auditing
        """.trimIndent()
    }
}
```

## Documentation Guidelines

### 1. Comprehensive Descriptions

```kotlin
openApi {
    info {
        description = """
            # Book Store API
            
            Welcome to the Book Store API documentation. This API allows you to:
            
            * Browse and search our catalog
            * Manage your shopping cart
            * Place and track orders
            * Manage your user profile
            
            ## Getting Started
            
            1. Sign up for an account at [https://bookstore.example.com](https://bookstore.example.com)
            2. Generate an API key from your dashboard
            3. Include the API key in all requests using the `X-API-Key` header
            
            ## Rate Limiting
            
            - 1000 requests per hour for authenticated requests
            - 100 requests per hour for unauthenticated requests
            
            ## Support
            
            Contact api-support@bookstore.example.com for help
        """.trimIndent()
    }
}

// Operation descriptions
get {
    summary = "List books"  // Short, action-oriented summary
    description = """
        Retrieve a paginated list of books from the catalog.
        
        ## Filtering
        
        You can filter results using the following parameters:
        - `genre`: Filter by book genre
        - `author`: Filter by author name (partial match)
        - `publishedAfter`: Filter by publication date
        
        ## Sorting
        
        Use the `sort` parameter with the following values:
        - `title`: Sort by title (default)
        - `-price`: Sort by price descending
        - `publishedDate`: Sort by publication date
        
        ## Response
        
        Returns a paginated list with a maximum of 100 items per page.
        
        The `Link` header contains pagination URLs following RFC 5988.
    """.trimIndent()
}
```

### 2. Examples

```kotlin
// Provide realistic examples
requestBody("User registration") {
    jsonContent(CreateUserRequest::class)
    
    examples {
        example("minimal") {
            summary = "Minimal required fields"
            description = "Only required fields provided"
            value = mapOf(
                "email" to "user@example.com",
                "password" to "SecurePass123!",
                "name" to "John Doe"
            )
        }
        
        example("complete") {
            summary = "All fields"
            description = "Complete user registration with all optional fields"
            value = mapOf(
                "email" to "user@example.com",
                "password" to "SecurePass123!",
                "name" to "John Doe",
                "phone" to "+1234567890",
                "dateOfBirth" to "1990-01-01",
                "preferences" to mapOf(
                    "newsletter" to true,
                    "language" to "en"
                )
            )
        }
        
        example("invalid") {
            summary = "Invalid example"
            description = "This will fail validation - for testing error responses"
            value = mapOf(
                "email" to "not-an-email",
                "password" to "weak",
                "name" to ""
            )
        }
    }
}
```

## Performance Considerations

### 1. Pagination

```kotlin
// Always paginate list endpoints
get {
    summary = "List resources"
    
    parameter {
        name = "page"
        `in` = ParameterLocation.QUERY
        schema {
            type = SchemaType.INTEGER
            minimum = 1
            default = 1
        }
    }
    
    parameter {
        name = "pageSize"
        `in` = ParameterLocation.QUERY
        schema {
            type = SchemaType.INTEGER
            minimum = 1
            maximum = 100  // Set reasonable limits
            default = 20
        }
    }
    
    response("200", "Success") {
        headers {
            header("X-Total-Count") {
                description = "Total number of items"
                schema { type = SchemaType.INTEGER }
            }
            header("Link") {
                description = "Pagination links (RFC 5988)"
                schema { type = SchemaType.STRING }
            }
        }
    }
}
```

### 2. Field Selection

```kotlin
// Allow clients to request only needed fields
parameter {
    name = "fields"
    `in` = ParameterLocation.QUERY
    description = "Comma-separated list of fields to include"
    schema {
        type = SchemaType.STRING
        example = "id,name,email"
    }
}

parameter {
    name = "expand"
    `in` = ParameterLocation.QUERY
    description = "Related resources to include"
    schema {
        type = SchemaType.ARRAY
        items = schema {
            type = SchemaType.STRING
            enum = listOf("profile", "orders", "addresses")
        }
    }
    style = "form"
    explode = false
}
```

### 3. Caching Headers

```kotlin
response("200", "Success") {
    headers {
        header("Cache-Control") {
            schema { type = SchemaType.STRING }
            example = "public, max-age=3600"
        }
        header("ETag") {
            description = "Entity tag for cache validation"
            schema { type = SchemaType.STRING }
        }
        header("Last-Modified") {
            schema { type = SchemaType.STRING }
        }
    }
}

// Support conditional requests
get {
    parameter {
        name = "If-None-Match"
        `in` = ParameterLocation.HEADER
        description = "ETag from previous response"
        schema { type = SchemaType.STRING }
    }
    
    parameter {
        name = "If-Modified-Since"
        `in` = ParameterLocation.HEADER
        schema { type = SchemaType.STRING }
    }
    
    response("304", "Not Modified") {
        description = "Resource hasn't changed"
    }
}
```

## Testing Your API Specs

### 1. Validation Testing

```kotlin
import io.swagger.v3.parser.OpenAPIV3Parser

@Test
fun `should generate valid OpenAPI spec`() {
    val spec = createApiSpec()
    val json = spec.toJson()
    
    // Parse and validate
    val parseResult = OpenAPIV3Parser().readContents(json)
    
    assertThat(parseResult.messages).isEmpty()
    assertThat(parseResult.openAPI).isNotNull()
}

@Test
fun `should include all required fields`() {
    val spec = createApiSpec()
    
    assertThat(spec.openapi).isEqualTo("3.1.0")
    assertThat(spec.info.title).isNotEmpty()
    assertThat(spec.info.version).matches("\\d+\\.\\d+\\.\\d+")
    assertThat(spec.paths).isNotEmpty()
}
```

### 2. Contract Testing

```kotlin
// Test schema compatibility
@Test
fun `user schema should match data class`() {
    val spec = createApiSpec()
    val userSchema = spec.components?.schemas?.get("User")
    
    val user = User(
        id = "123",
        name = "Test User",
        email = "test@example.com"
    )
    
    val json = Json.encodeToJsonElement(user)
    
    // Validate JSON against schema
    val validator = JsonSchemaValidator(userSchema)
    assertThat(validator.validate(json)).isTrue()
}
```

## Code Organization

### 1. Modular Structure

```kotlin
// api/common/Schemas.kt
object CommonSchemas {
    fun ComponentsBuilder.addCommonSchemas() {
        schema("Error") { /* ... */ }
        schema("Money") { /* ... */ }
        schema("Address") { /* ... */ }
    }
}

// api/users/UserSchemas.kt
object UserSchemas {
    fun ComponentsBuilder.addUserSchemas() {
        schema("User") { /* ... */ }
        schema("CreateUserRequest") { /* ... */ }
    }
}

// api/users/UserPaths.kt
object UserPaths {
    fun PathsBuilder.addUserPaths() {
        path("/users") {
            get { /* ... */ }
            post { /* ... */ }
        }
    }
}

// api/ApiSpec.kt
fun createApiSpec() = openApi {
    openapi = "3.1.0"
    
    info { /* ... */ }
    
    components {
        addCommonSchemas()
        addUserSchemas()
        addOrderSchemas()
    }
    
    paths {
        addUserPaths()
        addOrderPaths()
    }
}
```

### 2. Configuration Management

```kotlin
// Config object for API settings
object ApiConfig {
    const val VERSION = "2.0.0"
    const val TITLE = "My API"
    
    object Limits {
        const val MAX_PAGE_SIZE = 100
        const val DEFAULT_PAGE_SIZE = 20
        const val MAX_UPLOAD_SIZE = 10_485_760 // 10MB
    }
    
    object Timeouts {
        const val DEFAULT = 30
        const val LONG_RUNNING = 300
    }
}

// Use throughout spec
info {
    title = ApiConfig.TITLE
    version = ApiConfig.VERSION
}

parameter {
    name = "pageSize"
    schema {
        type = SchemaType.INTEGER
        maximum = ApiConfig.Limits.MAX_PAGE_SIZE
        default = ApiConfig.Limits.DEFAULT_PAGE_SIZE
    }
}
```

### 3. Extension Functions

```kotlin
// Create extension functions for common patterns
fun OperationBuilder.addPaginationParameters() {
    parameter {
        name = "page"
        `in` = ParameterLocation.QUERY
        schema {
            type = SchemaType.INTEGER
            minimum = 1
            default = 1
        }
    }
    
    parameter {
        name = "pageSize"
        `in` = ParameterLocation.QUERY
        schema {
            type = SchemaType.INTEGER
            minimum = 1
            maximum = ApiConfig.Limits.MAX_PAGE_SIZE
            default = ApiConfig.Limits.DEFAULT_PAGE_SIZE
        }
    }
}

fun OperationBuilder.addStandardResponses() {
    response { ref = "#/components/responses/BadRequest" }
    response { ref = "#/components/responses/Unauthorized" }
    response { ref = "#/components/responses/Forbidden" }
    response { ref = "#/components/responses/NotFound" }
    response { ref = "#/components/responses/InternalServerError" }
}

// Usage
get {
    summary = "List users"
    addPaginationParameters()
    response("200", "Success") {
        jsonContent(listOf<User>())
    }
    addStandardResponses()
}
```

## Summary

Following these best practices will help you create:
- **Consistent** APIs that are predictable and easy to use
- **Well-documented** specifications that serve as living documentation
- **Maintainable** code that's easy to evolve
- **Secure** APIs that protect user data
- **Performant** APIs that scale well
- **Testable** specifications that can be validated

Remember: The OpenAPI specification is not just documentation—it's a contract between your API and its consumers. Treat it with the same care as your production code.