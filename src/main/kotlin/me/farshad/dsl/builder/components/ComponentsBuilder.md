# ComponentsBuilder

**Package**: `me.farshad.dsl.builder.components`  
**File**: `ComponentsBuilder.kt`

## Overview

`ComponentsBuilder` is responsible for managing reusable components in an OpenAPI specification. It serves as a central registry for schemas, examples, security schemes, parameters, request bodies, responses, headers, and other elements that can be referenced throughout the API specification.

## Class Declaration

```kotlin
class ComponentsBuilder
```

## Properties

| Property | Type | Description |
|----------|------|-------------|
| `schemas` | `MutableMap<String, Schema>` | Reusable schema definitions |
| `examples` | `MutableMap<String, Example>` | Reusable example definitions |
| `securitySchemes` | `MutableMap<String, SecurityScheme>` | Security scheme definitions |
| `parameters` | `MutableMap<String, Parameter>` | Reusable parameter definitions |
| `requestBodies` | `MutableMap<String, RequestBody>` | Reusable request body definitions |
| `responses` | `MutableMap<String, Response>` | Reusable response definitions |
| `headers` | `MutableMap<String, Header>` | Reusable header definitions |
| `links` | `MutableMap<String, Link>` | Reusable link definitions |
| `callbacks` | `MutableMap<String, Callback>` | Reusable callback definitions |

## Key Methods

### Schema Methods

#### `schema(name: String, block: SchemaBuilder.() -> Unit)`
Defines a named schema:

```kotlin
schema("User") {
    type = "object"
    properties {
        property("id", "string") { format = "uuid" }
        property("username", "string")
        property("email", "string") { format = "email" }
    }
    required.addAll(listOf("id", "username", "email"))
}
```

#### `schema(name: String, type: String, block: SchemaBuilder.() -> Unit = {})`
Defines a typed schema:

```kotlin
schema("UserId", "string") {
    format = "uuid"
    description = "Unique user identifier"
}
```

#### `schema(dataClass: KClass<*>)`
Auto-generates schema from a Kotlin data class:

```kotlin
schema(User::class)  // Uses class name as schema name
```

#### `schema<T : Any>()`
Auto-generates schema from a reified type parameter:

```kotlin
schema<User>()  // More concise syntax using reified types
```

### Example Methods

#### `example(name: String, block: ExampleBuilder.() -> Unit)`
Defines a named example:

```kotlin
example("UserExample") {
    summary = "A typical user object"
    description = "Example showing all user fields"
    value = jsonObjectOf(
        "id" to "123e4567-e89b-12d3-a456-426614174000",
        "username" to "johndoe",
        "email" to "john@example.com"
    )
}
```

### Parameter Methods

#### `parameter(name: String, block: ParameterBuilder.() -> Unit)`
Defines a reusable parameter:

```kotlin
parameter("limitParam") {
    name = "limit"
    `in` = ParameterLocation.QUERY
    description = "Maximum number of items to return"
    schema {
        type = "integer"
        minimum = 1
        maximum = 100
        default = JsonPrimitive(20)
    }
}
```

### Response Methods

#### `response(name: String, description: String, block: ResponseBuilder.() -> Unit = {})`
Defines a reusable response:

```kotlin
response("NotFound", "Resource not found") {
    jsonContent {
        type = "object"
        properties {
            property("error", "string")
            property("message", "string")
        }
    }
}
```

### Security Scheme Methods

#### `securityScheme(name: String, block: SecuritySchemeBuilder.() -> Unit)`
Defines a security scheme:

```kotlin
securityScheme("bearerAuth") {
    type = "http"
    scheme = "bearer"
    bearerFormat = "JWT"
    description = "JWT authentication"
}
```

### Build Method

#### `build(): Components`
Builds the final `Components` object with all definitions.

## Usage Examples

### Complete Components Example

```kotlin
components {
    // Data model schemas
    schema<User>()
    schema<Product>()
    schema<Order>()
    
    // Common schemas
    schema("Error") {
        type = "object"
        properties {
            property("code", "string") {
                description = "Error code"
                example = JsonPrimitive("ERR_001")
            }
            property("message", "string") {
                description = "Error message"
            }
            property("details") {
                type = "array"
                items { type = "string" }
            }
            property("timestamp", "string") {
                format = "date-time"
            }
        }
        required.addAll(listOf("code", "message", "timestamp"))
    }
    
    schema("PaginationInfo") {
        type = "object"
        properties {
            property("page", "integer") { minimum = 1 }
            property("pageSize", "integer") { minimum = 1; maximum = 100 }
            property("totalPages", "integer")
            property("totalItems", "integer")
        }
        required.addAll(listOf("page", "pageSize", "totalPages", "totalItems"))
    }
    
    // Reusable parameters
    parameter("idParam") {
        name = "id"
        `in` = ParameterLocation.PATH
        description = "Resource identifier"
        required = true
        schema {
            type = "string"
            format = "uuid"
        }
    }
    
    parameter("pageParam") {
        name = "page"
        `in` = ParameterLocation.QUERY
        description = "Page number"
        schema {
            type = "integer"
            minimum = 1
            default = JsonPrimitive(1)
        }
    }
    
    // Reusable responses
    response("BadRequest", "Invalid request") {
        jsonContent("Error")
    }
    
    response("Unauthorized", "Authentication required") {
        jsonContent {
            type = "object"
            properties {
                property("error", "string") { const = JsonPrimitive("unauthorized") }
                property("message", "string")
            }
        }
        header("WWW-Authenticate") {
            description = "Authentication challenge"
            schema { type = "string" }
        }
    }
    
    // Security schemes
    securityScheme("apiKey") {
        type = "apiKey"
        name = "X-API-Key"
        `in` = "header"
        description = "API key authentication"
    }
    
    securityScheme("oauth2") {
        type = "oauth2"
        flows {
            authorizationCode {
                authorizationUrl = "https://example.com/oauth/authorize"
                tokenUrl = "https://example.com/oauth/token"
                scopes = mapOf(
                    "read" to "Read access",
                    "write" to "Write access",
                    "admin" to "Admin access"
                )
            }
        }
    }
}
```

### Schema Inheritance Pattern

```kotlin
components {
    // Base schemas
    schema("BaseEntity") {
        type = "object"
        properties {
            property("id", "string") { format = "uuid"; readOnly = true }
            property("createdAt", "string") { format = "date-time"; readOnly = true }
            property("updatedAt", "string") { format = "date-time"; readOnly = true }
        }
    }
    
    schema("AuditableEntity") {
        allOf = listOf(
            Schema(ref = "#/components/schemas/BaseEntity"),
            Schema(
                type = "object",
                properties = mapOf(
                    "createdBy" to Schema(type = "string"),
                    "updatedBy" to Schema(type = "string")
                )
            )
        )
    }
    
    // Domain schemas extending base
    schema("User") {
        allOf = listOf(
            Schema(ref = "#/components/schemas/AuditableEntity"),
            Schema(
                type = "object",
                properties = mapOf(
                    "username" to Schema(type = "string", minLength = 3),
                    "email" to Schema(type = "string", format = "email"),
                    "profile" to Schema(ref = "#/components/schemas/UserProfile")
                ),
                required = listOf("username", "email")
            )
        )
    }
}
```

### Polymorphic Schemas

```kotlin
components {
    // Base notification schema
    schema("Notification") {
        type = "object"
        properties {
            property("id", "string")
            property("type", "string") {
                description = "Notification type discriminator"
            }
            property("timestamp", "string") { format = "date-time" }
            property("read", "boolean") { default = JsonPrimitive(false) }
        }
        required.addAll(listOf("id", "type", "timestamp"))
        discriminator = Discriminator(propertyName = "type")
    }
    
    // Email notification
    schema("EmailNotification") {
        allOf = listOf(
            Schema(ref = "#/components/schemas/Notification"),
            Schema(
                type = "object",
                properties = mapOf(
                    "type" to Schema(type = "string", const = JsonPrimitive("email")),
                    "to" to Schema(type = "string", format = "email"),
                    "subject" to Schema(type = "string"),
                    "body" to Schema(type = "string")
                ),
                required = listOf("to", "subject", "body")
            )
        )
    }
    
    // SMS notification
    schema("SmsNotification") {
        allOf = listOf(
            Schema(ref = "#/components/schemas/Notification"),
            Schema(
                type = "object",
                properties = mapOf(
                    "type" to Schema(type = "string", const = JsonPrimitive("sms")),
                    "phoneNumber" to Schema(type = "string"),
                    "message" to Schema(type = "string", maxLength = 160)
                ),
                required = listOf("phoneNumber", "message")
            )
        )
    }
    
    // Union type for all notifications
    schema("AnyNotification") {
        oneOf = listOf(
            Schema(ref = "#/components/schemas/EmailNotification"),
            Schema(ref = "#/components/schemas/SmsNotification")
        )
        discriminator = Discriminator(
            propertyName = "type",
            mapping = mapOf(
                "email" to "#/components/schemas/EmailNotification",
                "sms" to "#/components/schemas/SmsNotification"
            )
        )
    }
}
```

### Request/Response Body Components

```kotlin
components {
    // Reusable request bodies
    requestBodies["CreateUserRequest"] = RequestBody(
        description = "User creation data",
        required = true,
        content = mapOf(
            "application/json" to MediaType(
                schema = Schema(ref = "#/components/schemas/CreateUserDto")
            )
        )
    )
    
    requestBodies["UpdateUserRequest"] = RequestBody(
        description = "User update data",
        required = true,
        content = mapOf(
            "application/json" to MediaType(
                schema = Schema(ref = "#/components/schemas/UpdateUserDto")
            ),
            "application/x-www-form-urlencoded" to MediaType(
                schema = Schema(ref = "#/components/schemas/UpdateUserDto")
            )
        )
    )
    
    // Reusable responses
    responses["UserResponse"] = Response(
        description = "Successful user response",
        content = mapOf(
            "application/json" to MediaType(
                schema = Schema(ref = "#/components/schemas/User"),
                examples = mapOf(
                    "default" to Example(ref = "#/components/examples/UserExample")
                )
            )
        )
    )
}
```

### Complex Security Schemes

```kotlin
components {
    // Basic authentication
    securityScheme("basicAuth") {
        type = "http"
        scheme = "basic"
        description = "Basic HTTP authentication"
    }
    
    // API key in multiple locations
    securityScheme("apiKeyHeader") {
        type = "apiKey"
        name = "X-API-Key"
        `in` = "header"
    }
    
    securityScheme("apiKeyQuery") {
        type = "apiKey"
        name = "api_key"
        `in` = "query"
    }
    
    // OAuth2 with multiple flows
    securityScheme("oauth2") {
        type = "oauth2"
        description = "OAuth2 authentication"
        flows {
            implicit {
                authorizationUrl = "https://api.example.com/oauth/authorize"
                scopes = mapOf(
                    "read:users" to "Read user information",
                    "write:users" to "Modify user information"
                )
            }
            
            password {
                tokenUrl = "https://api.example.com/oauth/token"
                scopes = mapOf(
                    "read" to "Read access",
                    "write" to "Write access"
                )
            }
            
            clientCredentials {
                tokenUrl = "https://api.example.com/oauth/token"
                scopes = mapOf(
                    "admin" to "Admin access"
                )
            }
        }
    }
    
    // OpenID Connect
    securityScheme("openIdConnect") {
        type = "openIdConnect"
        openIdConnectUrl = "https://api.example.com/.well-known/openid-configuration"
        description = "OpenID Connect authentication"
    }
}
```

### Shared Headers

```kotlin
components {
    // Rate limiting headers
    headers["X-Rate-Limit-Limit"] = Header(
        description = "Request limit per hour",
        schema = Schema(type = "integer", example = JsonPrimitive(1000))
    )
    
    headers["X-Rate-Limit-Remaining"] = Header(
        description = "Remaining requests in window",
        schema = Schema(type = "integer", minimum = 0)
    )
    
    headers["X-Rate-Limit-Reset"] = Header(
        description = "UTC epoch seconds when limit resets",
        schema = Schema(type = "integer")
    )
    
    // Pagination headers
    headers["X-Total-Count"] = Header(
        description = "Total number of items",
        schema = Schema(type = "integer", minimum = 0)
    )
    
    headers["Link"] = Header(
        description = "Pagination links (RFC 5988)",
        schema = Schema(
            type = "string",
            example = JsonPrimitive("<https://api.example.com/users?page=2>; rel=\"next\"")
        )
    )
}
```

### Examples with References

```kotlin
components {
    // Define examples
    example("ValidUser") {
        summary = "A valid user object"
        value = jsonObjectOf(
            "id" to "123e4567-e89b-12d3-a456-426614174000",
            "username" to "johndoe",
            "email" to "john.doe@example.com",
            "createdAt" to "2023-01-01T00:00:00Z"
        )
    }
    
    example("ValidationError") {
        summary = "Validation error response"
        value = jsonObjectOf(
            "error" to jsonObjectOf(
                "code" to "VALIDATION_ERROR",
                "message" to "Invalid input data",
                "details" to jsonArrayOf(
                    jsonObjectOf(
                        "field" to "email",
                        "issue" to "Invalid email format"
                    )
                )
            )
        )
    }
    
    // Use in operations
    paths {
        path("/users") {
            post {
                requestBody {
                    jsonContent("CreateUserDto") {
                        examples = mapOf(
                            "valid" to Example(ref = "#/components/examples/ValidUser")
                        )
                    }
                }
                response("400", "Validation error") {
                    jsonContent("Error") {
                        examples = mapOf(
                            "validation" to Example(ref = "#/components/examples/ValidationError")
                        )
                    }
                }
            }
        }
    }
}
```

## Best Practices

1. **Use meaningful names**: Choose clear, descriptive names for components.

2. **Group related schemas**: Organize schemas logically (e.g., DTOs, entities, enums).

3. **Leverage inheritance**: Use allOf for schema inheritance to reduce duplication.

4. **Define common patterns once**: Error responses, pagination, etc. should be defined once.

5. **Use schema generation**: For data classes, use automatic schema generation.

6. **Document thoroughly**: Add descriptions to all components.

7. **Version carefully**: Consider versioning strategies for schema evolution.

## Common Patterns

### DTO Pattern

```kotlin
components {
    // Entity schema
    schema("User") {
        // Full user entity
    }
    
    // Create DTO
    schema("CreateUserDto") {
        // Only fields needed for creation
    }
    
    // Update DTO  
    schema("UpdateUserDto") {
        // Only updatable fields
    }
    
    // Response DTO
    schema("UserResponseDto") {
        // Public fields only
    }
}
```

### Error Hierarchy Pattern

```kotlin
components {
    schema("BaseError") {
        type = "object"
        properties {
            property("code", "string")
            property("message", "string")
            property("timestamp", "string") { format = "date-time" }
        }
    }
    
    schema("ValidationError") {
        allOf = listOf(
            Schema(ref = "#/components/schemas/BaseError"),
            Schema(/* validation-specific fields */)
        )
    }
    
    schema("BusinessError") {
        allOf = listOf(
            Schema(ref = "#/components/schemas/BaseError"),
            Schema(/* business-specific fields */)
        )
    }
}
```

### Enum Schema Pattern

```kotlin
components {
    schema("Status", "string") {
        enum = listOf("active", "inactive", "pending", "deleted")
        description = "Entity status"
    }
    
    schema("Role", "string") {
        enum = listOf("admin", "user", "guest")
        description = "User role"
    }
}
```

## Related Builders

- [OpenApiBuilder](../../../../../../../../capabilities/OpenApiBuilder.md) - Parent builder that uses ComponentsBuilder
- [SchemaBuilder](../../../../../../../../capabilities/SchemaBuilder.md) - For building individual schemas
- [ExampleBuilder](../../../../../../../../capabilities/ExampleBuilder.md) - For building examples
- [SecuritySchemeBuilder](SecuritySchemeBuilder.md) - For security schemes