# PathItemBuilder

**Package**: `me.farshad.dsl.builder.paths`  
**File**: `PathItemBuilder.kt`

## Overview

`PathItemBuilder` is responsible for building individual path items in an OpenAPI specification. Each path item can contain multiple HTTP operations (GET, POST, PUT, etc.) and shared parameters that apply to all operations on that path.

## Class Declaration

```kotlin
class PathItemBuilder
```

## Properties

| Property | Type | Description |
|----------|------|-------------|
| `summary` | `String?` | Short summary of what the path offers |
| `description` | `String?` | Detailed description of the path |
| `parameters` | `MutableList<Parameter>` | Parameters shared by all operations |
| `get` | `Operation?` | GET operation |
| `post` | `Operation?` | POST operation |
| `put` | `Operation?` | PUT operation |
| `delete` | `Operation?` | DELETE operation |
| `patch` | `Operation?` | PATCH operation |
| `options` | `Operation?` | OPTIONS operation |
| `head` | `Operation?` | HEAD operation |
| `trace` | `Operation?` | TRACE operation |

## Key Methods

### HTTP Operation Methods

Each method configures an HTTP operation for the path:

#### `get(block: OperationBuilder.() -> Unit)`
```kotlin
get {
    summary = "Retrieve resource"
    description = "Get detailed information about the resource"
    // ... operation configuration
}
```

#### `post(block: OperationBuilder.() -> Unit)`
```kotlin
post {
    summary = "Create resource"
    requestBody {
        required = true
        jsonContent(CreateRequest::class)
    }
    // ... operation configuration
}
```

#### `put(block: OperationBuilder.() -> Unit)`
```kotlin
put {
    summary = "Update resource"
    description = "Replace the entire resource"
    // ... operation configuration
}
```

#### `delete(block: OperationBuilder.() -> Unit)`
```kotlin
delete {
    summary = "Delete resource"
    response("204", "Resource deleted")
}
```

#### `patch(block: OperationBuilder.() -> Unit)`
```kotlin
patch {
    summary = "Partial update"
    description = "Update specific fields of the resource"
    // ... operation configuration
}
```

#### `options(block: OperationBuilder.() -> Unit)`
```kotlin
options {
    summary = "Get allowed operations"
    response("200", "Allowed methods") {
        header("Allow") {
            description = "Comma-separated list of allowed methods"
            schema { type = "string" }
        }
    }
}
```

#### `head(block: OperationBuilder.() -> Unit)`
```kotlin
head {
    summary = "Check resource existence"
    description = "Same as GET but returns only headers"
}
```

#### `trace(block: OperationBuilder.() -> Unit)`
```kotlin
trace {
    summary = "Trace request"
    description = "Echoes back the received request"
}
```

### Parameter Methods

#### `parameters(block: ParametersBuilder.() -> Unit)`
Define parameters shared across all operations:

```kotlin
parameters {
    pathParameter("id") {
        description = "Resource identifier"
        schema { 
            type = "string"
            format = "uuid"
        }
    }
    headerParameter("X-Request-ID") {
        description = "Request tracking ID"
        schema { type = "string" }
    }
}
```

#### `pathParameter(name: String, block: ParameterBuilder.() -> Unit)`
Shorthand for adding a path parameter:

```kotlin
pathParameter("userId") {
    description = "User identifier"
    schema { type = "string" }
}
```

### `build(): PathItem`
Builds the final `PathItem` object with all configured operations and parameters.

## Usage Examples

### Complete CRUD Path Item

```kotlin
path("/products/{productId}") {
    summary = "Product operations"
    description = "Endpoints for managing individual products"
    
    // Shared parameter for all operations
    pathParameter("productId") {
        description = "Unique product identifier"
        schema {
            type = "string"
            pattern = "^[A-Z]{3}-[0-9]{6}$"
        }
    }
    
    get {
        summary = "Get product details"
        tags = listOf("Products")
        response("200", "Product found") {
            jsonContent(Product::class)
        }
        response("404", "Product not found") {
            jsonContent(Error::class)
        }
    }
    
    put {
        summary = "Update product"
        tags = listOf("Products")
        requestBody {
            required = true
            jsonContent(Product::class)
        }
        response("200", "Product updated") {
            jsonContent(Product::class)
        }
        response("404", "Product not found")
        response("422", "Invalid product data")
    }
    
    patch {
        summary = "Partial product update"
        tags = listOf("Products")
        requestBody {
            required = true
            jsonContent {
                description = "Fields to update"
                properties {
                    property("name") { type = "string" }
                    property("price") { type = "number" }
                    property("description") { type = "string" }
                }
            }
        }
        response("200", "Product updated") {
            jsonContent(Product::class)
        }
    }
    
    delete {
        summary = "Delete product"
        tags = listOf("Products")
        response("204", "Product deleted")
        response("404", "Product not found")
        response("409", "Product has dependencies")
    }
}
```

### Resource with Shared Security

```kotlin
path("/admin/users/{userId}") {
    description = "Administrative user management"
    
    // Shared parameters
    parameters {
        pathParameter("userId") {
            description = "User ID"
            schema { type = "integer" }
        }
        headerParameter("X-Admin-Token") {
            description = "Admin authentication token"
            required = true
            schema { type = "string" }
        }
    }
    
    get {
        summary = "Get user details (admin)"
        security = listOf(
            mapOf("adminAuth" to emptyList())
        )
        response("200", "User details") {
            jsonContent(UserDetails::class)
        }
    }
    
    put {
        summary = "Update user (admin)"
        security = listOf(
            mapOf("adminAuth" to emptyList())
        )
        requestBody {
            required = true
            jsonContent(UpdateUserRequest::class)
        }
        response("200", "User updated")
    }
    
    delete {
        summary = "Delete user (admin)"
        security = listOf(
            mapOf("adminAuth" to listOf("users:delete"))
        )
        response("204", "User deleted")
    }
}
```

### File Operations Path

```kotlin
path("/files/{fileId}") {
    pathParameter("fileId") {
        description = "File identifier"
        schema { 
            type = "string"
            format = "uuid"
        }
    }
    
    get {
        summary = "Download file"
        description = "Retrieve file content"
        response("200", "File content") {
            content("application/octet-stream") {
                schema {
                    type = "string"
                    format = "binary"
                }
            }
            header("Content-Disposition") {
                description = "Attachment filename"
                schema { type = "string" }
            }
            header("Content-Type") {
                description = "File MIME type"
                schema { type = "string" }
            }
        }
        response("404", "File not found")
    }
    
    head {
        summary = "Get file metadata"
        description = "Check file existence and get metadata without downloading"
        response("200", "File exists") {
            header("Content-Length") {
                description = "File size in bytes"
                schema { type = "integer" }
            }
            header("Last-Modified") {
                description = "Last modification date"
                schema { type = "string" }
            }
        }
        response("404", "File not found")
    }
    
    delete {
        summary = "Delete file"
        response("204", "File deleted")
        response("404", "File not found")
    }
}
```

### Conditional Operations

```kotlin
path("/resources/{id}") {
    pathParameter("id") {
        description = "Resource ID"
        schema { type = "string" }
    }
    
    get {
        summary = "Get resource"
        headerParameter("If-None-Match") {
            description = "ETag for conditional requests"
            schema { type = "string" }
        }
        response("200", "Resource data") {
            jsonContent(Resource::class)
            header("ETag") {
                description = "Resource version identifier"
                schema { type = "string" }
            }
        }
        response("304", "Not modified")
    }
    
    put {
        summary = "Update resource"
        headerParameter("If-Match") {
            description = "ETag for optimistic concurrency"
            required = true
            schema { type = "string" }
        }
        requestBody {
            required = true
            jsonContent(Resource::class)
        }
        response("200", "Resource updated") {
            jsonContent(Resource::class)
            header("ETag") {
                description = "New resource version"
                schema { type = "string" }
            }
        }
        response("412", "Precondition failed")
    }
}
```

## Best Practices

1. **Use shared parameters wisely**: Only include parameters that truly apply to all operations.

2. **Provide path-level descriptions**: Use summary and description to explain the resource, not individual operations.

3. **Group related operations**: All operations on a specific resource should be in the same path item.

4. **Consider operation availability**: Not all paths need all HTTP methods - only define what makes sense.

5. **Maintain consistency**: Use consistent parameter names and response structures across operations.

## Common Patterns

### Read-Only Resource

```kotlin
path("/system/status") {
    description = "System status information (read-only)"
    
    get {
        summary = "Get current system status"
        response("200", "System status") {
            jsonContent(SystemStatus::class)
        }
    }
    
    // No POST, PUT, DELETE operations - this is read-only
}
```

### Collection vs Item Operations

```kotlin
// Collection operations
path("/items") {
    get {
        summary = "List items"
        queryParameter("page") { /* pagination */ }
        queryParameter("limit") { /* pagination */ }
    }
    post {
        summary = "Create new item"
        requestBody { /* ... */ }
    }
}

// Item operations  
path("/items/{id}") {
    pathParameter("id") { /* ... */ }
    
    get { summary = "Get specific item" }
    put { summary = "Replace item" }
    patch { summary = "Update item fields" }
    delete { summary = "Remove item" }
}
```

### Batch Operations

```kotlin
path("/users/batch") {
    post {
        summary = "Batch create users"
        requestBody {
            required = true
            jsonContent {
                type = "array"
                items { ref("CreateUserRequest") }
            }
        }
        response("207", "Multi-status response") {
            jsonContent {
                type = "array"
                items { ref("BatchOperationResult") }
            }
        }
    }
    
    patch {
        summary = "Batch update users"
        requestBody {
            required = true
            jsonContent(BatchUpdateRequest::class)
        }
        response("207", "Multi-status response")
    }
    
    delete {
        summary = "Batch delete users"
        requestBody {
            required = true
            jsonContent {
                type = "object"
                properties {
                    property("userIds") {
                        type = "array"
                        items { type = "string" }
                    }
                }
            }
        }
        response("207", "Multi-status response")
    }
}
```

## Related Builders

- [PathsBuilder](PathsBuilder.md) - Parent builder that manages path items
- [OperationBuilder](OperationBuilder.md) - For configuring individual operations
- [ParameterBuilder](ParameterBuilder.md) - For defining parameters