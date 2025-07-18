# Reusable Components

This guide covers creating and managing reusable components in your OpenAPI specification, promoting consistency and reducing duplication across your API definition.

## Table of Contents
- [Components Overview](#components-overview)
- [Reusable Schemas](#reusable-schemas)
- [Reusable Parameters](#reusable-parameters)
- [Reusable Request Bodies](#reusable-request-bodies)
- [Reusable Responses](#reusable-responses)
- [Reusable Examples](#reusable-examples)
- [Reusable Headers](#reusable-headers)
- [Reusable Security Schemes](#reusable-security-schemes)
- [Reusable Links](#reusable-links)
- [Component Libraries](#component-libraries)
- [Best Practices](#best-practices)

## Components Overview

The `components` section allows you to define reusable elements that can be referenced throughout your API specification:

```kotlin
openApi {
    components {
        // Schemas - Data models
        schema("User") { /* ... */ }
        
        // Parameters - Query, path, header, cookie parameters
        parameter("limitParam") { /* ... */ }
        
        // Request bodies - Common request formats
        requestBody("UserInput") { /* ... */ }
        
        // Responses - Standard responses
        response("NotFound") { /* ... */ }
        
        // Examples - Reusable examples
        example("validUser") { /* ... */ }
        
        // Headers - Common headers
        header("X-Rate-Limit") { /* ... */ }
        
        // Security schemes - Authentication methods
        securityScheme("bearerAuth") { /* ... */ }
        
        // Links - Hypermedia links
        link("GetUserById") { /* ... */ }
    }
}
```

## Reusable Schemas

### Basic Schema Definitions

```kotlin
components {
    // Simple schema
    schema("UserId") {
        type = SchemaType.STRING
        format = "uuid"
        description = "Unique user identifier"
        example = "550e8400-e29b-41d4-a716-446655440000"
    }
    
    // Object schema
    schema("Address") {
        type = SchemaType.OBJECT
        required = listOf("street", "city", "country")
        properties {
            "street" to schema {
                type = SchemaType.STRING
                minLength = 1
                maxLength = 100
            }
            "city" to schema {
                type = SchemaType.STRING
                minLength = 1
                maxLength = 50
            }
            "state" to schema {
                type = SchemaType.STRING
                maxLength = 50
                nullable = true
            }
            "postalCode" to schema {
                type = SchemaType.STRING
                pattern = "^[0-9]{5}(-[0-9]{4})?$"
            }
            "country" to schema {
                type = SchemaType.STRING
                pattern = "^[A-Z]{2}$"
                description = "ISO 3166-1 alpha-2 country code"
            }
        }
    }
    
    // Enum schema
    schema("Status") {
        type = SchemaType.STRING
        enum = listOf("pending", "active", "inactive", "deleted")
        default = "pending"
    }
}
```

### Schema Composition

```kotlin
components {
    // Base schemas
    schema("Timestamps") {
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
                ref = "#/components/schemas/UserId"
            }
        }
    }
    
    // Composed schema
    schema("User") {
        allOf = listOf(
            SchemaReference.ReferenceTo("#/components/schemas/Identifiable"),
            SchemaReference.ReferenceTo("#/components/schemas/Timestamps"),
            SchemaReference.Schema(Schema(
                type = SchemaType.OBJECT,
                required = listOf("email", "username"),
                properties = mapOf(
                    "username" to Schema(
                        type = SchemaType.STRING,
                        pattern = "^[a-zA-Z0-9_]{3,20}$"
                    ),
                    "email" to Schema(
                        type = SchemaType.STRING,
                        format = "email"
                    ),
                    "profile" to Schema(
                        ref = "#/components/schemas/UserProfile"
                    ),
                    "status" to Schema(
                        ref = "#/components/schemas/Status"
                    )
                )
            ))
        )
    }
}
```

### Generic Schemas

```kotlin
components {
    // Paginated response wrapper
    schema("PaginatedResponse") {
        type = SchemaType.OBJECT
        properties {
            "items" to schema {
                type = SchemaType.ARRAY
                items = schema {
                    description = "Array of items"
                }
            }
            "pagination" to schema {
                ref = "#/components/schemas/PaginationInfo"
            }
        }
    }
    
    schema("PaginationInfo") {
        type = SchemaType.OBJECT
        properties {
            "page" to schema {
                type = SchemaType.INTEGER
                minimum = 1
            }
            "pageSize" to schema {
                type = SchemaType.INTEGER
                minimum = 1
                maximum = 100
            }
            "totalPages" to schema {
                type = SchemaType.INTEGER
                readOnly = true
            }
            "totalItems" to schema {
                type = SchemaType.INTEGER
                readOnly = true
            }
            "hasNext" to schema {
                type = SchemaType.BOOLEAN
                readOnly = true
            }
            "hasPrevious" to schema {
                type = SchemaType.BOOLEAN
                readOnly = true
            }
        }
    }
    
    // API response wrapper
    schema("ApiResponse") {
        type = SchemaType.OBJECT
        properties {
            "success" to schema {
                type = SchemaType.BOOLEAN
            }
            "data" to schema {
                description = "Response data"
                nullable = true
            }
            "error" to schema {
                ref = "#/components/schemas/ErrorInfo"
                nullable = true
            }
            "metadata" to schema {
                ref = "#/components/schemas/ResponseMetadata"
            }
        }
    }
}
```

### Schema Library

```kotlin
// Common field schemas
components {
    schema("Email") {
        type = SchemaType.STRING
        format = "email"
        pattern = "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$"
        maxLength = 254
    }
    
    schema("PhoneNumber") {
        type = SchemaType.STRING
        pattern = "^\\+?[1-9]\\d{1,14}$"
        description = "E.164 format"
        example = "+1234567890"
    }
    
    schema("Money") {
        type = SchemaType.OBJECT
        required = listOf("amount", "currency")
        properties {
            "amount" to schema {
                type = SchemaType.NUMBER
                multipleOf = 0.01
                minimum = 0
            }
            "currency" to schema {
                type = SchemaType.STRING
                pattern = "^[A-Z]{3}$"
                description = "ISO 4217 currency code"
                example = "USD"
            }
        }
    }
    
    schema("Percentage") {
        type = SchemaType.NUMBER
        minimum = 0
        maximum = 100
        description = "Percentage value (0-100)"
    }
    
    schema("Url") {
        type = SchemaType.STRING
        format = "uri"
        pattern = "^https?://.+"
        maxLength = 2048
    }
    
    schema("Uuid") {
        type = SchemaType.STRING
        format = "uuid"
        pattern = "^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$"
    }
}
```

## Reusable Parameters

### Common Query Parameters

```kotlin
components {
    // Pagination parameters
    parameter("pageParam") {
        name = "page"
        `in` = ParameterLocation.QUERY
        description = "Page number (1-based)"
        schema {
            type = SchemaType.INTEGER
            minimum = 1
            default = 1
        }
    }
    
    parameter("pageSizeParam") {
        name = "pageSize"
        `in` = ParameterLocation.QUERY
        description = "Number of items per page"
        schema {
            type = SchemaType.INTEGER
            minimum = 1
            maximum = 100
            default = 20
        }
    }
    
    // Sorting parameters
    parameter("sortParam") {
        name = "sort"
        `in` = ParameterLocation.QUERY
        description = "Sort field and order (e.g., 'name', '-created')"
        schema {
            type = SchemaType.STRING
            pattern = "^-?[a-zA-Z_]+$"
        }
        example = "-createdAt"
    }
    
    parameter("sortOrderParam") {
        name = "order"
        `in` = ParameterLocation.QUERY
        description = "Sort order"
        schema {
            type = SchemaType.STRING
            enum = listOf("asc", "desc")
            default = "asc"
        }
    }
    
    // Filtering parameters
    parameter("searchParam") {
        name = "q"
        `in` = ParameterLocation.QUERY
        description = "Search query"
        schema {
            type = SchemaType.STRING
            minLength = 1
            maxLength = 100
        }
    }
    
    parameter("filterParam") {
        name = "filter"
        `in` = ParameterLocation.QUERY
        description = "Filter expression"
        schema {
            type = SchemaType.STRING
        }
        examples {
            example("status-filter") {
                value = "status eq 'active'"
                summary = "Filter by status"
            }
            example("date-filter") {
                value = "createdAt gt '2023-01-01'"
                summary = "Filter by date"
            }
        }
    }
    
    // Field selection
    parameter("fieldsParam") {
        name = "fields"
        `in` = ParameterLocation.QUERY
        description = "Comma-separated list of fields to include"
        schema {
            type = SchemaType.STRING
            pattern = "^[a-zA-Z_]+(,[a-zA-Z_]+)*$"
        }
        example = "id,name,email"
    }
    
    parameter("includeParam") {
        name = "include"
        `in` = ParameterLocation.QUERY
        description = "Related resources to include"
        schema {
            type = SchemaType.ARRAY
            items = schema {
                type = SchemaType.STRING
                enum = listOf("profile", "posts", "comments", "followers")
            }
        }
        style = "form"
        explode = false
    }
}
```

### Common Header Parameters

```kotlin
components {
    parameter("apiVersionHeader") {
        name = "X-API-Version"
        `in` = ParameterLocation.HEADER
        description = "API version"
        required = true
        schema {
            type = SchemaType.STRING
            enum = listOf("v1", "v2", "v3")
        }
    }
    
    parameter("requestIdHeader") {
        name = "X-Request-ID"
        `in` = ParameterLocation.HEADER
        description = "Unique request identifier for tracing"
        schema {
            type = SchemaType.STRING
            format = "uuid"
        }
    }
    
    parameter("tenantIdHeader") {
        name = "X-Tenant-ID"
        `in` = ParameterLocation.HEADER
        description = "Tenant identifier for multi-tenant systems"
        required = true
        schema {
            type = SchemaType.STRING
        }
    }
    
    parameter("acceptLanguageHeader") {
        name = "Accept-Language"
        `in` = ParameterLocation.HEADER
        description = "Preferred response language"
        schema {
            type = SchemaType.STRING
            default = "en"
        }
        examples {
            example("english") { value = "en" }
            example("spanish") { value = "es" }
            example("multiple") { value = "en-US,en;q=0.9,es;q=0.8" }
        }
    }
    
    parameter("idempotencyKeyHeader") {
        name = "Idempotency-Key"
        `in` = ParameterLocation.HEADER
        description = "Idempotency key for safe retries"
        schema {
            type = SchemaType.STRING
            format = "uuid"
        }
    }
}
```

### Path Parameters

```kotlin
components {
    parameter("idPathParam") {
        name = "id"
        `in` = ParameterLocation.PATH
        required = true
        description = "Resource identifier"
        schema {
            ref = "#/components/schemas/Uuid"
        }
    }
    
    parameter("slugPathParam") {
        name = "slug"
        `in` = ParameterLocation.PATH
        required = true
        description = "URL-friendly resource identifier"
        schema {
            type = SchemaType.STRING
            pattern = "^[a-z0-9]+(?:-[a-z0-9]+)*$"
        }
        example = "my-blog-post"
    }
}
```

## Reusable Request Bodies

```kotlin
components {
    // User creation request
    requestBody("CreateUserRequest") {
        description = "User registration data"
        required = true
        
        jsonContent {
            schema {
                ref = "#/components/schemas/UserInput"
            }
            
            examples {
                example("minimal") {
                    summary = "Minimal user data"
                    value = mapOf(
                        "username" to "johndoe",
                        "email" to "john@example.com",
                        "password" to "SecurePass123!"
                    )
                }
                example("complete") {
                    summary = "Complete user data"
                    externalValue = "https://api.example.com/examples/user-complete.json"
                }
            }
        }
    }
    
    // Generic update request
    requestBody("PatchRequest") {
        description = "JSON Patch document"
        required = true
        
        content("application/json-patch+json") {
            schema {
                type = SchemaType.ARRAY
                items = schema {
                    type = SchemaType.OBJECT
                    required = listOf("op", "path")
                    properties {
                        "op" to schema {
                            type = SchemaType.STRING
                            enum = listOf("add", "remove", "replace", "move", "copy", "test")
                        }
                        "path" to schema {
                            type = SchemaType.STRING
                            pattern = "^/.+"
                        }
                        "value" to schema {
                            description = "The value to apply"
                        }
                        "from" to schema {
                            type = SchemaType.STRING
                            pattern = "^/.+"
                        }
                    }
                }
            }
        }
    }
    
    // File upload request
    requestBody("FileUploadRequest") {
        description = "File upload with metadata"
        required = true
        
        content("multipart/form-data") {
            schema {
                type = SchemaType.OBJECT
                required = listOf("file")
                properties {
                    "file" to schema {
                        type = SchemaType.STRING
                        format = "binary"
                    }
                    "description" to schema {
                        type = SchemaType.STRING
                        maxLength = 500
                    }
                    "tags" to schema {
                        type = SchemaType.ARRAY
                        items = schema {
                            type = SchemaType.STRING
                        }
                        maxItems = 10
                    }
                }
            }
        }
    }
}
```

## Reusable Responses

### Standard Error Responses

```kotlin
components {
    // 400 Bad Request
    response("BadRequest") {
        description = "Invalid request"
        
        jsonContent {
            schema {
                ref = "#/components/schemas/ErrorResponse"
            }
            
            examples {
                example("validation-error") {
                    summary = "Validation error"
                    value = mapOf(
                        "error" to mapOf(
                            "code" to "VALIDATION_ERROR",
                            "message" to "Request validation failed",
                            "details" to listOf(
                                mapOf(
                                    "field" to "email",
                                    "message" to "Invalid email format"
                                )
                            )
                        )
                    )
                }
            }
        }
    }
    
    // 401 Unauthorized
    response("Unauthorized") {
        description = "Authentication required"
        
        jsonContent {
            schema {
                ref = "#/components/schemas/ErrorResponse"
            }
        }
        
        headers {
            header("WWW-Authenticate") {
                schema {
                    type = SchemaType.STRING
                }
            }
        }
    }
    
    // 403 Forbidden
    response("Forbidden") {
        description = "Insufficient permissions"
        
        jsonContent {
            schema {
                ref = "#/components/schemas/ErrorResponse"
            }
            
            example = mapOf(
                "error" to mapOf(
                    "code" to "FORBIDDEN",
                    "message" to "You don't have permission to access this resource"
                )
            )
        }
    }
    
    // 404 Not Found
    response("NotFound") {
        description = "Resource not found"
        
        jsonContent {
            schema {
                ref = "#/components/schemas/ErrorResponse"
            }
            
            example = mapOf(
                "error" to mapOf(
                    "code" to "NOT_FOUND",
                    "message" to "The requested resource was not found"
                )
            )
        }
    }
    
    // 409 Conflict
    response("Conflict") {
        description = "Resource conflict"
        
        jsonContent {
            schema {
                ref = "#/components/schemas/ErrorResponse"
            }
        }
    }
    
    // 429 Too Many Requests
    response("TooManyRequests") {
        description = "Rate limit exceeded"
        
        jsonContent {
            schema {
                ref = "#/components/schemas/ErrorResponse"
            }
        }
        
        headers {
            header("Retry-After") {
                description = "Seconds until rate limit resets"
                schema {
                    type = SchemaType.INTEGER
                }
            }
            header("X-RateLimit-Limit") {
                schema { type = SchemaType.INTEGER }
            }
            header("X-RateLimit-Remaining") {
                schema { type = SchemaType.INTEGER }
            }
            header("X-RateLimit-Reset") {
                schema { type = SchemaType.INTEGER }
            }
        }
    }
    
    // 500 Internal Server Error
    response("InternalServerError") {
        description = "Internal server error"
        
        jsonContent {
            schema {
                ref = "#/components/schemas/ErrorResponse"
            }
            
            example = mapOf(
                "error" to mapOf(
                    "code" to "INTERNAL_ERROR",
                    "message" to "An unexpected error occurred",
                    "requestId" to "550e8400-e29b-41d4-a716-446655440000"
                )
            )
        }
    }
}
```

### Success Responses

```kotlin
components {
    // 201 Created
    response("Created") {
        description = "Resource created successfully"
        
        headers {
            header("Location") {
                description = "URL of the created resource"
                required = true
                schema {
                    type = SchemaType.STRING
                    format = "uri"
                }
            }
        }
    }
    
    // 204 No Content
    response("NoContent") {
        description = "Request successful, no content to return"
    }
    
    // Paginated response
    response("PaginatedResponse") {
        description = "Paginated list response"
        
        jsonContent {
            schema {
                ref = "#/components/schemas/PaginatedResponse"
            }
        }
        
        headers {
            header("X-Total-Count") {
                description = "Total number of items"
                schema {
                    type = SchemaType.INTEGER
                }
            }
            header("Link") {
                description = "Pagination links (RFC 5988)"
                schema {
                    type = SchemaType.STRING
                }
            }
        }
    }
}
```

## Reusable Examples

```kotlin
components {
    // User examples
    example("validUser") {
        summary = "Valid user object"
        description = "A complete user object with all fields"
        value = mapOf(
            "id" to "550e8400-e29b-41d4-a716-446655440000",
            "username" to "johndoe",
            "email" to "john.doe@example.com",
            "profile" to mapOf(
                "firstName" to "John",
                "lastName" to "Doe",
                "avatar" to "https://example.com/avatars/johndoe.jpg"
            ),
            "status" to "active",
            "createdAt" to "2023-10-20T15:30:00Z",
            "updatedAt" to "2023-10-20T15:30:00Z"
        )
    }
    
    example("minimalUser") {
        summary = "Minimal user object"
        value = mapOf(
            "username" to "janedoe",
            "email" to "jane@example.com"
        )
    }
    
    // Error examples
    example("validationError") {
        summary = "Validation error response"
        value = mapOf(
            "error" to mapOf(
                "code" to "VALIDATION_ERROR",
                "message" to "Validation failed",
                "details" to listOf(
                    mapOf(
                        "field" to "email",
                        "message" to "Invalid email format",
                        "value" to "not-an-email"
                    ),
                    mapOf(
                        "field" to "age",
                        "message" to "Must be at least 18",
                        "value" to 16
                    )
                )
            )
        )
    }
    
    // Pagination examples
    example("firstPage") {
        summary = "First page of results"
        value = mapOf(
            "items" to listOf("..."),
            "pagination" to mapOf(
                "page" to 1,
                "pageSize" to 20,
                "totalPages" to 5,
                "totalItems" to 97,
                "hasNext" to true,
                "hasPrevious" to false
            )
        )
    }
}
```

## Reusable Headers

```kotlin
components {
    // Rate limiting headers
    header("X-RateLimit-Limit") {
        description = "Request limit per hour"
        schema {
            type = SchemaType.INTEGER
        }
        example = 1000
    }
    
    header("X-RateLimit-Remaining") {
        description = "Remaining requests in current window"
        schema {
            type = SchemaType.INTEGER
            minimum = 0
        }
    }
    
    header("X-RateLimit-Reset") {
        description = "Unix timestamp when rate limit resets"
        schema {
            type = SchemaType.INTEGER
        }
    }
    
    // Caching headers
    header("ETag") {
        description = "Entity tag for cache validation"
        schema {
            type = SchemaType.STRING
        }
        example = "\"33a64df551425fcc55e4d42a148795d9f25f89d4\""
    }
    
    header("Last-Modified") {
        description = "Last modification timestamp"
        schema {
            type = SchemaType.STRING
        }
        example = "Wed, 21 Oct 2023 07:28:00 GMT"
    }
}
```

## Reusable Security Schemes

```kotlin
components {
    // API Key variations
    securityScheme("apiKey") {
        type = SecuritySchemeType.API_KEY
        name = "X-API-Key"
        `in` = ApiKeyLocation.HEADER
        description = "API key authentication"
    }
    
    securityScheme("appId") {
        type = SecuritySchemeType.API_KEY
        name = "X-App-ID"
        `in` = ApiKeyLocation.HEADER
        description = "Application ID"
    }
    
    securityScheme("appSecret") {
        type = SecuritySchemeType.API_KEY
        name = "X-App-Secret"
        `in` = ApiKeyLocation.HEADER
        description = "Application secret"
    }
    
    // OAuth2 configurations
    securityScheme("oauth2") {
        type = SecuritySchemeType.OAUTH2
        flows {
            authorizationCode {
                authorizationUrl = "https://auth.example.com/authorize"
                tokenUrl = "https://auth.example.com/token"
                refreshUrl = "https://auth.example.com/refresh"
                scopes {
                    scope("read", "Read access")
                    scope("write", "Write access")
                    scope("admin", "Admin access")
                }
            }
        }
    }
}
```

## Reusable Links

```kotlin
components {
    // HATEOAS links
    link("GetUserById") {
        operationId = "getUserById"
        parameters = mapOf(
            "userId" to "\$response.body#/id"
        )
        description = "Get user details by ID from response"
    }
    
    link("GetUserPosts") {
        operationId = "getUserPosts"
        parameters = mapOf(
            "userId" to "\$response.body#/id"
        )
        description = "Get posts by this user"
    }
    
    link("UpdateUser") {
        operationId = "updateUser"
        parameters = mapOf(
            "userId" to "\$response.body#/id"
        )
        description = "Update this user"
    }
    
    link("NextPage") {
        operationRef = "#/paths/~1users/get"
        parameters = mapOf(
            "page" to "\$response.body#/pagination/page + 1"
        )
        description = "Get next page of results"
    }
}
```

## Component Libraries

### Creating a Component Library

```kotlin
// commons.kt - Shared components library
object ApiCommons {
    fun addCommonComponents(components: ComponentsBuilder) {
        components.apply {
            // Common schemas
            schema("Id") {
                type = SchemaType.STRING
                format = "uuid"
            }
            
            schema("Timestamp") {
                type = SchemaType.STRING
                format = "date-time"
            }
            
            schema("ErrorResponse") {
                type = SchemaType.OBJECT
                required = listOf("error")
                properties {
                    "error" to schema {
                        type = SchemaType.OBJECT
                        required = listOf("code", "message")
                        properties {
                            "code" to schema { type = SchemaType.STRING }
                            "message" to schema { type = SchemaType.STRING }
                            "details" to schema { 
                                type = SchemaType.OBJECT
                                additionalProperties = true
                            }
                        }
                    }
                }
            }
            
            // Common parameters
            parameter("pageParam") {
                name = "page"
                `in` = ParameterLocation.QUERY
                schema {
                    type = SchemaType.INTEGER
                    minimum = 1
                    default = 1
                }
            }
            
            // Common responses
            response("NotFound") {
                description = "Resource not found"
                jsonContent {
                    schema {
                        ref = "#/components/schemas/ErrorResponse"
                    }
                }
            }
        }
    }
}

// Usage in API spec
openApi {
    components {
        // Add common components
        ApiCommons.addCommonComponents(this)
        
        // Add API-specific components
        schema("Product") {
            // ...
        }
    }
}
```

### Modular Component Organization

```kotlin
// schemas/user.kt
fun ComponentsBuilder.addUserSchemas() {
    schema("User") {
        // User schema definition
    }
    
    schema("UserProfile") {
        // Profile schema
    }
    
    schema("UserPreferences") {
        // Preferences schema
    }
}

// schemas/product.kt
fun ComponentsBuilder.addProductSchemas() {
    schema("Product") {
        // Product schema
    }
    
    schema("ProductCategory") {
        // Category schema
    }
}

// parameters/common.kt
fun ComponentsBuilder.addCommonParameters() {
    parameter("limitParam") {
        // Limit parameter
    }
    
    parameter("offsetParam") {
        // Offset parameter
    }
}

// Main API spec
openApi {
    components {
        // Add all component modules
        addUserSchemas()
        addProductSchemas()
        addCommonParameters()
    }
}
```

## Best Practices

### 1. Naming Conventions

```kotlin
components {
    // Schemas: PascalCase, singular
    schema("User") { }
    schema("OrderItem") { }
    
    // Parameters: camelCase with 'Param' suffix
    parameter("limitParam") { }
    parameter("apiKeyHeader") { }
    
    // Responses: PascalCase describing the response
    response("NotFound") { }
    response("ValidationError") { }
    
    // Request bodies: PascalCase with 'Request' suffix
    requestBody("CreateUserRequest") { }
    requestBody("UpdateOrderRequest") { }
}
```

### 2. Schema Organization

```kotlin
components {
    // Group related schemas
    
    // Entity schemas
    schema("User") { }
    schema("Product") { }
    schema("Order") { }
    
    // Request/Response schemas
    schema("UserInput") { }
    schema("UserResponse") { }
    
    // Common/Utility schemas
    schema("Money") { }
    schema("Address") { }
    schema("Pagination") { }
    
    // Error schemas
    schema("Error") { }
    schema("ValidationError") { }
}
```

### 3. Reusability Guidelines

1. **Extract Common Patterns**: If you use something more than twice, make it reusable
2. **Keep It Simple**: Don't over-abstract; some duplication is okay
3. **Document Everything**: Add descriptions to all components
4. **Version Carefully**: Consider backward compatibility when changing components
5. **Use References**: Always reference components instead of duplicating

### 4. Component Documentation

```kotlin
components {
    schema("Money") {
        description = """
            Represents a monetary amount with currency.
            
            The amount is stored as a decimal number with up to 2 decimal places.
            Currency must be a valid ISO 4217 code.
            
            Example: {"amount": 99.99, "currency": "USD"}
        """.trimIndent()
        
        type = SchemaType.OBJECT
        // ...
    }
}
```

## Next Steps

- [Advanced Features](advanced-features.md) - Webhooks, callbacks, and extensions
- [Examples](examples/) - Complete API examples using components
- [Best Practices](best-practices.md) - Design patterns and guidelines