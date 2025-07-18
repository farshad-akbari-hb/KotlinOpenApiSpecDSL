# OperationBuilder

**Package**: `me.farshad.dsl.builder.paths`  
**File**: `OperationBuilder.kt`

## Overview

`OperationBuilder` is responsible for building individual API operations (GET, POST, PUT, etc.) within a path. It provides comprehensive configuration options for requests, responses, parameters, security, and more.

## Class Declaration

```kotlin
class OperationBuilder
```

## Properties

| Property | Type | Description |
|----------|------|-------------|
| `operationId` | `String?` | Unique identifier for the operation |
| `summary` | `String?` | Short summary of the operation |
| `description` | `String?` | Detailed description of the operation |
| `tags` | `MutableList<String>` | Tags for API documentation grouping |
| `parameters` | `MutableList<Parameter>` | Operation parameters |
| `requestBody` | `RequestBody?` | Request body configuration |
| `responses` | `MutableMap<String, Response>` | Response definitions by status code |
| `security` | `MutableList<Map<String, List<String>>>` | Security requirements |

## Key Methods

### Parameter Methods

#### `parameter(name: String, location: ParameterLocation, type: PropertyType, required: Boolean = false, description: String? = null, format: SchemaFormat? = null)`
Adds a parameter directly:

```kotlin
parameter(
    name = "limit",
    location = ParameterLocation.QUERY,
    type = PropertyType.INTEGER,
    required = false,
    description = "Maximum number of results",
    format = SchemaFormat.INT32
)

parameter(
    name = "userId",
    location = ParameterLocation.PATH,
    type = PropertyType.STRING,
    required = true,
    description = "User identifier"
)
```

### Tag Methods

#### `tags(vararg tagNames: String)`
Adds tags to the operation:

```kotlin
tags("Users", "Admin")
```

### Request Body Methods

#### `requestBody(block: RequestBodyBuilder.() -> Unit)`
Configures the request body:

```kotlin
requestBody {
    description = "User data for creation"
    required = true
    jsonContent(CreateUserRequest::class)
}
```

### Response Methods

#### `response(code: String, description: String, block: ResponseBuilder.() -> Unit = {})`
Defines a response:

```kotlin
response("200", "Successful operation") {
    jsonContent(User::class)
}

response("404", "User not found") {
    jsonContent {
        type = SchemaType.OBJECT
        properties {
            "error" to schema {
                type = SchemaType.STRING
            }
            "message" to schema {
                type = SchemaType.STRING
            }
        }
    }
}
```

### Security Methods

#### `security(scheme: String, vararg scopes: String)`
Adds security requirements to the operation:

```kotlin
security("bearerAuth")
security("oauth2", "read:users", "write:users")
```

### Other Methods

#### `build(): Operation`
Builds the final `Operation` object with all configurations.

## Usage Examples

### Complete CRUD Operation Examples

#### GET Operation - List Resources

```kotlin
get {
    operationId = "listUsers"
    summary = "List all users"
    description = "Retrieve a paginated list of users with optional filtering"
    tags("Users")
    
    // Query parameters for filtering and pagination
    parameter(
        name = "page",
        location = ParameterLocation.QUERY,
        type = PropertyType.INTEGER,
        description = "Page number (1-based)"
    )
    
    parameter(
        name = "limit",
        location = ParameterLocation.QUERY,
        type = PropertyType.INTEGER,
        description = "Results per page"
    )
    
    parameter(
        name = "status",
        location = ParameterLocation.QUERY,
        type = PropertyType.STRING,
        description = "Filter by user status"
    )
    
    parameter(
        name = "search",
        location = ParameterLocation.QUERY,
        type = PropertyType.STRING,
        description = "Search in name and email"
    )
    
    // Responses
    response("200", "Successful response") {
        jsonContent {
            type = SchemaType.OBJECT
            properties {
                "users" to schema {
                    type = SchemaType.ARRAY
                    items = Schema(ref = "#/components/schemas/User")
                }
                "pagination" to schema {
                    ref = "#/components/schemas/PaginationInfo"
                }
            }
        }
    }
    
    response("400", "Invalid parameters") {
        jsonContent(ErrorResponse::class)
    }
}
```

#### POST Operation - Create Resource

```kotlin
post {
    operationId = "createUser"
    summary = "Create a new user"
    description = "Create a new user account with the provided information"
    tags("Users")
    
    requestBody {
        description = "User data for account creation"
        required = true
        jsonContent("CreateUserRequest")
    }
    
    response("201", "User created successfully") {
        jsonContent(User::class)
    }
    
    response("400", "Invalid user data") {
        jsonContent(ValidationError::class)
    }
    
    response("409", "Username or email already exists") {
        jsonContent(ConflictError::class)
    }
}
```

#### PUT Operation - Full Update

```kotlin
put {
    operationId = "updateUser"
    summary = "Update user"
    description = "Replace entire user data"
    tags("Users")
    
    parameter(
        name = "userId",
        location = ParameterLocation.PATH,
        type = PropertyType.STRING,
        required = true,
        description = "User ID to update"
    )
    
    parameter(
        name = "If-Match",
        location = ParameterLocation.HEADER,
        type = PropertyType.STRING,
        required = true,
        description = "ETag for optimistic concurrency control"
    )
    
    requestBody {
        required = true
        jsonContent(UpdateUserRequest::class)
    }
    
    response("200", "User updated") {
        jsonContent(User::class)
    }
    
    response("404", "User not found")
    response("412", "Precondition failed - ETag mismatch")
    response("422", "Invalid user data")
}
```

#### PATCH Operation - Partial Update

```kotlin
patch {
    operationId = "patchUser"
    summary = "Partially update user"
    description = "Update specific user fields"
    tags("Users")
    
    parameter(
        name = "userId",
        location = ParameterLocation.PATH,
        type = PropertyType.STRING,
        required = true,
        description = "User ID to update"
    )
    
    requestBody {
        required = true
        jsonContent {
            type = SchemaType.OBJECT
            additionalProperties = Schema(type = SchemaType.STRING)
        }
    }
    
    response("200", "User updated") {
        jsonContent(User::class)
    }
    
    response("400", "Invalid patch document")
    response("404", "User not found")
}
```

#### DELETE Operation

```kotlin
delete {
    operationId = "deleteUser"
    summary = "Delete user"
    description = "Permanently delete a user account"
    tags("Users")
    
    parameter(
        name = "userId",
        location = ParameterLocation.PATH,
        type = PropertyType.STRING,
        required = true,
        description = "User ID to delete"
    )
    
    parameter(
        name = "force",
        location = ParameterLocation.QUERY,
        type = PropertyType.BOOLEAN,
        required = false,
        description = "Force deletion even with existing data"
    )
    
    response("204", "User deleted successfully")
    
    response("404", "User not found")
    
    response("409", "Cannot delete - user has dependencies") {
        jsonContent {
            type = SchemaType.OBJECT
            properties {
                "error" to schema { type = SchemaType.STRING }
                "dependencies" to schema {
                    type = SchemaType.ARRAY
                    items = Schema(type = SchemaType.STRING)
                }
            }
        }
    }
}
```

### Advanced Operation Examples

#### Operation with Multiple Parameters

```kotlin
get {
    operationId = "searchProducts"
    summary = "Search products"
    description = "Search products with multiple filters"
    tags("Products")
    
    // Multiple query parameters
    parameter(
        name = "q",
        location = ParameterLocation.QUERY,
        type = PropertyType.STRING,
        required = true,
        description = "Search query"
    )
    
    parameter(
        name = "category",
        location = ParameterLocation.QUERY,
        type = PropertyType.STRING,
        description = "Product category"
    )
    
    parameter(
        name = "minPrice",
        location = ParameterLocation.QUERY,
        type = PropertyType.NUMBER,
        description = "Minimum price"
    )
    
    parameter(
        name = "maxPrice",
        location = ParameterLocation.QUERY,
        type = PropertyType.NUMBER,
        description = "Maximum price"
    )
    
    parameter(
        name = "inStock",
        location = ParameterLocation.QUERY,
        type = PropertyType.BOOLEAN,
        description = "Only show in-stock items"
    )
    
    response("200", "Search results") {
        jsonContent {
            type = SchemaType.OBJECT
            properties {
                "results" to schema {
                    type = SchemaType.ARRAY
                    items = Schema(ref = "#/components/schemas/Product")
                }
                "totalCount" to schema {
                    type = SchemaType.INTEGER
                }
            }
        }
    }
}
```

### Security Configuration Examples

```kotlin
get {
    operationId = "getSecureData"
    summary = "Get secure data"
    tags("Secure")
    
    // Add security requirements
    security("bearerAuth")
    // Or with scopes
    security("oauth2", "read:data", "read:profile")
    
    response("200", "Secure data") {
        jsonContent(SecureData::class)
    }
    
    response("401", "Unauthorized")
    response("403", "Forbidden - insufficient permissions")
}
```

## Current Limitations

1. **No ParameterBuilder**: Parameters are created directly with limited configuration options. Complex parameter schemas require manual construction.

2. **Limited Parameter Configuration**: The current `parameter` method doesn't support all OpenAPI parameter features like `style`, `explode`, `allowEmptyValue`, etc.

3. **No Convenience Methods**: Helper methods like `queryParameter`, `pathParameter`, etc. are not available.

4. **String References in Security**: Security requirements still use string references to security scheme names.

## Best Practices

1. **Always set operationId**: Use unique, descriptive operation IDs for code generation.

2. **Provide comprehensive descriptions**: Include both summary and description for clarity.

3. **Define all possible responses**: Document success and error responses.

4. **Use consistent tags**: Group related operations with consistent tag names.

5. **Validate parameters**: Use schema constraints to validate input parameters.

6. **Include examples**: Provide request/response examples for better understanding.

7. **Document security requirements**: Clearly specify required authentication/authorization.

## Common Patterns

### Pagination Pattern

```kotlin
parameter(
    name = "page",
    location = ParameterLocation.QUERY,
    type = PropertyType.INTEGER,
    description = "Page number (1-based)"
)

parameter(
    name = "pageSize",
    location = ParameterLocation.QUERY,
    type = PropertyType.INTEGER,
    description = "Items per page"
)

parameter(
    name = "sort",
    location = ParameterLocation.QUERY,
    type = PropertyType.STRING,
    description = "Sort field and direction (e.g., 'name:asc')"
)
```

### Error Response Pattern

```kotlin
response("400", "Bad Request") {
    jsonContent {
        type = SchemaType.OBJECT
        properties {
            "error" to schema {
                type = SchemaType.OBJECT
                properties {
                    "code" to schema { type = SchemaType.STRING }
                    "message" to schema { type = SchemaType.STRING }
                    "details" to schema {
                        type = SchemaType.ARRAY
                        items = Schema(
                            type = SchemaType.OBJECT,
                            properties = mapOf(
                                "field" to Schema(type = SchemaType.STRING),
                                "issue" to Schema(type = SchemaType.STRING)
                            )
                        )
                    }
                }
            }
        }
    }
}
```

### Async Operation Pattern

```kotlin
post {
    operationId = "startAsyncJob"
    summary = "Start async processing"
    
    requestBody {
        required = true
        jsonContent(JobRequest::class)
    }
    
    response("202", "Job accepted") {
        jsonContent {
            type = SchemaType.OBJECT
            properties {
                "jobId" to schema { type = SchemaType.STRING }
                "status" to schema { type = SchemaType.STRING }
                "statusUrl" to schema { type = SchemaType.STRING }
            }
        }
    }
}
```

## Related Builders

- [PathItemBuilder](PathItemBuilder.md) - Parent builder for operations
- [RequestBodyBuilder](../request/RequestBodyBuilder.md) - For request body configuration
- [ResponseBuilder](../response/ResponseBuilder.md) - For response configuration
- [SchemaBuilder](../schema/SchemaBuilder.md) - For defining schemas