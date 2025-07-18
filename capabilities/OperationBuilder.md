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
| `tags` | `List<String>?` | Tags for API documentation grouping |
| `parameters` | `MutableList<Parameter>` | Operation parameters |
| `requestBody` | `RequestBody?` | Request body configuration |
| `responses` | `MutableMap<String, Response>` | Response definitions by status code |
| `deprecated` | `Boolean` | Whether the operation is deprecated |
| `security` | `List<Map<String, List<String>>>?` | Security requirements |
| `externalDocs` | `ExternalDocumentation?` | External documentation link |
| `callbacks` | `Map<String, Callback>?` | Callback definitions |
| `servers` | `List<Server>?` | Operation-specific servers |

## Key Methods

### Parameter Methods

#### `parameter(name: String, location: ParameterLocation, block: ParameterBuilder.() -> Unit)`
Adds a parameter with full configuration:

```kotlin
parameter("limit", ParameterLocation.QUERY) {
    description = "Maximum number of results"
    schema {
        type = "integer"
        minimum = 1
        maximum = 100
        default = JsonPrimitive(20)
    }
}
```

#### `queryParameter(name: String, block: ParameterBuilder.() -> Unit)`
Adds a query parameter:

```kotlin
queryParameter("search") {
    description = "Search term"
    required = true
    schema { type = "string" }
}
```

#### `pathParameter(name: String, block: ParameterBuilder.() -> Unit)`
Adds a path parameter:

```kotlin
pathParameter("userId") {
    description = "User identifier"
    schema {
        type = "string"
        format = "uuid"
    }
}
```

#### `headerParameter(name: String, block: ParameterBuilder.() -> Unit)`
Adds a header parameter:

```kotlin
headerParameter("X-API-Key") {
    description = "API authentication key"
    required = true
    schema { type = "string" }
}
```

#### `cookieParameter(name: String, block: ParameterBuilder.() -> Unit)`
Adds a cookie parameter:

```kotlin
cookieParameter("session_id") {
    description = "Session identifier"
    schema { type = "string" }
}
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
    header("X-Rate-Limit-Remaining") {
        description = "Remaining requests in window"
        schema { type = "integer" }
    }
}
```

#### `jsonResponse(code: String, description: String, schemaRef: String)`
Quick method for JSON responses with schema reference:

```kotlin
jsonResponse("200", "User data", "User")
```

#### `jsonResponse(code: String, description: String, schemaClass: KClass<*>)`
Quick method for JSON responses with class reference:

```kotlin
jsonResponse("200", "User data", User::class)
```

#### `emptyResponse(code: String, description: String)`
Defines an empty response:

```kotlin
emptyResponse("204", "Resource deleted successfully")
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
    tags = listOf("Users")
    
    // Query parameters for filtering and pagination
    queryParameter("page") {
        description = "Page number (1-based)"
        schema {
            type = "integer"
            minimum = 1
            default = JsonPrimitive(1)
        }
    }
    
    queryParameter("limit") {
        description = "Results per page"
        schema {
            type = "integer"
            minimum = 1
            maximum = 100
            default = JsonPrimitive(20)
        }
    }
    
    queryParameter("status") {
        description = "Filter by user status"
        schema {
            type = "string"
            enum = listOf("active", "inactive", "pending")
        }
    }
    
    queryParameter("search") {
        description = "Search in name and email"
        schema {
            type = "string"
            minLength = 3
        }
    }
    
    // Responses
    response("200", "Successful response") {
        jsonContent {
            type = "object"
            properties {
                property("users") {
                    type = "array"
                    items { ref("User") }
                }
                property("pagination") {
                    ref("PaginationInfo")
                }
            }
        }
        header("X-Total-Count") {
            description = "Total number of users"
            schema { type = "integer" }
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
    tags = listOf("Users")
    
    requestBody {
        description = "User data for account creation"
        required = true
        jsonContent {
            ref("CreateUserRequest")
            example = jsonObjectOf(
                "username" to "johndoe",
                "email" to "john@example.com",
                "password" to "SecurePass123!",
                "profile" to jsonObjectOf(
                    "firstName" to "John",
                    "lastName" to "Doe"
                )
            )
        }
    }
    
    response("201", "User created successfully") {
        description = "Returns the created user with generated ID"
        jsonContent(User::class)
        header("Location") {
            description = "URL of the created resource"
            schema { type = "string" }
        }
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
    tags = listOf("Users")
    
    pathParameter("userId") {
        description = "User ID to update"
        schema {
            type = "string"
            format = "uuid"
        }
    }
    
    headerParameter("If-Match") {
        description = "ETag for optimistic concurrency control"
        required = true
        schema { type = "string" }
    }
    
    requestBody {
        required = true
        jsonContent(UpdateUserRequest::class)
    }
    
    response("200", "User updated") {
        jsonContent(User::class)
        header("ETag") {
            description = "New entity tag"
            schema { type = "string" }
        }
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
    description = "Update specific user fields using JSON Patch"
    tags = listOf("Users")
    
    pathParameter("userId") {
        description = "User ID to update"
        schema { type = "string" }
    }
    
    requestBody {
        required = true
        content("application/json-patch+json") {
            schema {
                type = "array"
                items {
                    type = "object"
                    properties {
                        property("op") {
                            type = "string"
                            enum = listOf("add", "remove", "replace", "move", "copy", "test")
                        }
                        property("path") {
                            type = "string"
                            description = "JSON Pointer"
                        }
                        property("value") {
                            description = "The value to apply"
                        }
                    }
                    required = listOf("op", "path")
                }
                example = jsonArrayOf(
                    jsonObjectOf(
                        "op" to "replace",
                        "path" to "/email",
                        "value" to "newemail@example.com"
                    )
                )
            }
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
    tags = listOf("Users")
    
    pathParameter("userId") {
        description = "User ID to delete"
        schema { type = "string" }
    }
    
    queryParameter("force") {
        description = "Force deletion even with existing data"
        schema {
            type = "boolean"
            default = JsonPrimitive(false)
        }
    }
    
    response("204", "User deleted successfully")
    
    response("404", "User not found")
    
    response("409", "Cannot delete - user has dependencies") {
        jsonContent {
            type = "object"
            properties {
                property("error") { type = "string" }
                property("dependencies") {
                    type = "array"
                    items { type = "string" }
                }
            }
        }
    }
}
```

### Advanced Operation Examples

#### File Upload Operation

```kotlin
post {
    operationId = "uploadFile"
    summary = "Upload a file"
    description = "Upload a file with metadata"
    tags = listOf("Files")
    
    requestBody {
        required = true
        multipartContent {
            property("file") {
                type = "string"
                format = "binary"
                description = "The file to upload"
            }
            property("metadata") {
                type = "object"
                properties {
                    property("description") { type = "string" }
                    property("tags") {
                        type = "array"
                        items { type = "string" }
                    }
                }
            }
        }
    }
    
    response("201", "File uploaded") {
        jsonContent(FileInfo::class)
    }
    
    response("413", "File too large") {
        jsonContent(Error::class)
    }
}
```

#### Streaming Response Operation

```kotlin
get {
    operationId = "streamEvents"
    summary = "Stream real-time events"
    description = "Server-sent events stream"
    tags = listOf("Events")
    
    queryParameter("types") {
        description = "Event types to include"
        style = ParameterStyle.FORM
        explode = true
        schema {
            type = "array"
            items { type = "string" }
        }
    }
    
    response("200", "Event stream") {
        content("text/event-stream") {
            schema {
                type = "string"
                description = "Server-sent events stream"
            }
        }
    }
}
```

#### Webhook Callback Operation

```kotlin
post {
    operationId = "createSubscription"
    summary = "Create webhook subscription"
    tags = listOf("Webhooks")
    
    requestBody {
        required = true
        jsonContent {
            type = "object"
            properties {
                property("url") {
                    type = "string"
                    format = "uri"
                }
                property("events") {
                    type = "array"
                    items { type = "string" }
                }
            }
        }
    }
    
    response("201", "Subscription created") {
        jsonContent(Subscription::class)
    }
    
    callbacks = mapOf(
        "statusUpdate" to mapOf(
            "{$request.body#/url}" to PathItem(
                post = Operation(
                    summary = "Webhook notification",
                    requestBody = RequestBody(
                        required = true,
                        content = mapOf(
                            "application/json" to MediaType(
                                schema = Schema(ref = "#/components/schemas/WebhookPayload")
                            )
                        )
                    ),
                    responses = mapOf(
                        "200" to Response(description = "Notification received")
                    )
                )
            )
        )
    )
}
```

### Security Configuration Examples

```kotlin
get {
    operationId = "getSecureData"
    summary = "Get secure data"
    tags = listOf("Secure")
    
    // Multiple security options (any one can be used)
    security = listOf(
        mapOf("bearerAuth" to emptyList()),
        mapOf("apiKey" to emptyList()),
        mapOf("oauth2" to listOf("read:data"))
    )
    
    response("200", "Secure data") {
        jsonContent(SecureData::class)
    }
    
    response("401", "Unauthorized")
    response("403", "Forbidden - insufficient permissions")
}
```

### Deprecated Operation

```kotlin
get {
    operationId = "getOldEndpoint"
    summary = "Get data (deprecated)"
    description = "This endpoint is deprecated. Use /v2/data instead."
    deprecated = true
    tags = listOf("Deprecated")
    
    response("200", "Data") {
        jsonContent(OldDataFormat::class)
        header("Sunset") {
            description = "Deprecation date"
            schema { type = "string" }
        }
        header("Link") {
            description = "Link to new endpoint"
            schema { type = "string" }
        }
    }
}
```

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
queryParameter("page") {
    description = "Page number"
    schema {
        type = "integer"
        minimum = 1
        default = JsonPrimitive(1)
    }
}
queryParameter("pageSize") {
    description = "Items per page"
    schema {
        type = "integer"
        minimum = 1
        maximum = 100
        default = JsonPrimitive(20)
    }
}
queryParameter("sort") {
    description = "Sort field and direction (e.g., 'name:asc')"
    schema { type = "string" }
}
```

### Error Response Pattern

```kotlin
response("400", "Bad Request") {
    jsonContent {
        type = "object"
        properties {
            property("error") {
                type = "object"
                properties {
                    property("code") { type = "string" }
                    property("message") { type = "string" }
                    property("details") {
                        type = "array"
                        items {
                            type = "object"
                            properties {
                                property("field") { type = "string" }
                                property("issue") { type = "string" }
                            }
                        }
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
            type = "object"
            properties {
                property("jobId") { type = "string" }
                property("status") { type = "string" }
                property("statusUrl") { type = "string" }
            }
        }
        header("Location") {
            description = "URL to check job status"
            schema { type = "string" }
        }
    }
}
```

## Related Builders

- [PathItemBuilder](PathItemBuilder.md) - Parent builder for operations
- [RequestBodyBuilder](RequestBodyBuilder.md) - For request body configuration
- [ResponseBuilder](ResponseBuilder.md) - For response configuration
- [ParameterBuilder](ParameterBuilder.md) - For parameter configuration