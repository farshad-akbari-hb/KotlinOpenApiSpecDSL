# PathsBuilder

**Package**: `me.farshad.dsl.builder.paths`  
**File**: `PathsBuilder.kt`

## Overview

`PathsBuilder` manages the paths section of an OpenAPI specification. It provides methods to define API endpoints and their operations in a structured way.

## Class Declaration

```kotlin
class PathsBuilder
```

## Properties

| Property | Type | Description |
|----------|------|-------------|
| `paths` | `MutableMap<String, PathItem>` | Map of path strings to PathItem objects |

## Key Methods

### `path(path: String, block: PathItemBuilder.() -> Unit)`
Defines a new path with its operations.

```kotlin
path("/users/{id}") {
    get {
        summary = "Get user by ID"
        // ... operation details
    }
    put {
        summary = "Update user"
        // ... operation details
    }
    delete {
        summary = "Delete user"
        // ... operation details
    }
}
```

### `build(): Paths`
Builds the final `Paths` object containing all defined paths.

## Direct Access

The builder also supports direct map-like access to paths:

```kotlin
paths {
    // Using the path method
    path("/users") {
        get { /* ... */ }
    }
    
    // Direct access (less common)
    this["/products"] = PathItem(
        get = Operation(/* ... */)
    )
}
```

## Usage Examples

### Basic CRUD Endpoints

```kotlin
paths {
    path("/users") {
        get {
            summary = "List all users"
            description = "Returns a paginated list of users"
            response("200", "Successful response") {
                jsonContent {
                    type = "array"
                    items { ref("User") }
                }
            }
        }
        
        post {
            summary = "Create a new user"
            requestBody {
                required = true
                jsonContent(User::class)
            }
            response("201", "User created") {
                jsonContent(User::class)
            }
        }
    }
    
    path("/users/{id}") {
        pathParameter("id") {
            description = "User ID"
            schema { 
                type = "string"
                format = "uuid"
            }
        }
        
        get {
            summary = "Get user by ID"
            response("200", "User found") {
                jsonContent(User::class)
            }
            response("404", "User not found") {
                jsonContent(Error::class)
            }
        }
        
        put {
            summary = "Update user"
            requestBody {
                required = true
                jsonContent(User::class)
            }
            response("200", "User updated") {
                jsonContent(User::class)
            }
        }
        
        delete {
            summary = "Delete user"
            response("204", "User deleted")
            response("404", "User not found")
        }
    }
}
```

### Nested Resources

```kotlin
paths {
    path("/projects/{projectId}/tasks") {
        pathParameter("projectId") {
            description = "Project identifier"
            schema { type = "string" }
        }
        
        get {
            summary = "List project tasks"
            queryParameter("status") {
                description = "Filter by task status"
                schema {
                    type = "string"
                    enum = listOf("pending", "in_progress", "completed")
                }
            }
            response("200", "Task list") {
                jsonContent {
                    type = "array"
                    items { ref("Task") }
                }
            }
        }
        
        post {
            summary = "Create task in project"
            requestBody {
                required = true
                jsonContent(CreateTaskRequest::class)
            }
            response("201", "Task created") {
                jsonContent(Task::class)
            }
        }
    }
    
    path("/projects/{projectId}/tasks/{taskId}") {
        parameters {
            pathParameter("projectId") {
                description = "Project identifier"
                schema { type = "string" }
            }
            pathParameter("taskId") {
                description = "Task identifier"
                schema { type = "string" }
            }
        }
        
        get {
            summary = "Get specific task"
            response("200", "Task details") {
                jsonContent(Task::class)
            }
        }
        
        patch {
            summary = "Update task"
            requestBody {
                jsonContent(UpdateTaskRequest::class)
            }
            response("200", "Task updated") {
                jsonContent(Task::class)
            }
        }
    }
}
```

### API Versioning in Paths

```kotlin
paths {
    // Version 1 endpoints
    path("/v1/products") {
        get {
            summary = "List products (v1)"
            deprecated = true
            response("200", "Product list") {
                jsonContent {
                    type = "array"
                    items { ref("ProductV1") }
                }
            }
        }
    }
    
    // Version 2 endpoints
    path("/v2/products") {
        get {
            summary = "List products (v2)"
            description = "Enhanced product listing with filtering"
            queryParameter("category") {
                description = "Filter by category"
                schema { type = "string" }
            }
            queryParameter("minPrice") {
                description = "Minimum price filter"
                schema { 
                    type = "number"
                    minimum = 0
                }
            }
            response("200", "Product list") {
                jsonContent {
                    type = "array"
                    items { ref("ProductV2") }
                }
            }
        }
    }
}
```

### File Upload Endpoints

```kotlin
paths {
    path("/files/upload") {
        post {
            summary = "Upload a file"
            requestBody {
                required = true
                multipartContent {
                    property("file") {
                        type = "string"
                        format = "binary"
                        description = "File to upload"
                    }
                    property("description") {
                        type = "string"
                        description = "File description"
                    }
                }
            }
            response("201", "File uploaded") {
                jsonContent(FileMetadata::class)
            }
        }
    }
    
    path("/files/{fileId}/download") {
        get {
            summary = "Download a file"
            pathParameter("fileId") {
                description = "File identifier"
                schema { type = "string" }
            }
            response("200", "File content") {
                content("application/octet-stream") {
                    schema {
                        type = "string"
                        format = "binary"
                    }
                }
            }
            response("404", "File not found")
        }
    }
}
```

### Webhook Endpoints

```kotlin
paths {
    path("/webhooks/github") {
        post {
            summary = "GitHub webhook receiver"
            description = "Receives webhook events from GitHub"
            headerParameter("X-GitHub-Event") {
                description = "GitHub event type"
                required = true
                schema { type = "string" }
            }
            headerParameter("X-Hub-Signature-256") {
                description = "HMAC signature for validation"
                required = true
                schema { type = "string" }
            }
            requestBody {
                required = true
                jsonContent {
                    description = "GitHub webhook payload"
                }
            }
            response("200", "Webhook processed")
            response("401", "Invalid signature")
        }
    }
}
```

## Best Practices

1. **Use consistent path naming**: Follow REST conventions for resource paths.
   - Collection: `/users`
   - Individual resource: `/users/{id}`
   - Nested resources: `/users/{userId}/orders`

2. **Group related operations**: All operations on a resource should be defined in the same path block.

3. **Define shared parameters**: Use the path-level parameters for params shared across operations.

4. **Use meaningful path parameters**: Choose descriptive names like `userId` instead of just `id`.

5. **Follow HTTP method semantics**:
   - GET: Retrieve resources
   - POST: Create new resources
   - PUT: Full update/replace
   - PATCH: Partial update
   - DELETE: Remove resources

## Path Patterns

### RESTful Resource Pattern

```kotlin
paths {
    // Collection operations
    path("/resources") {
        get { /* List resources */ }
        post { /* Create resource */ }
    }
    
    // Individual resource operations
    path("/resources/{id}") {
        get { /* Get resource */ }
        put { /* Update resource */ }
        patch { /* Partial update */ }
        delete { /* Delete resource */ }
    }
    
    // Sub-resources
    path("/resources/{id}/sub-resources") {
        get { /* List sub-resources */ }
        post { /* Create sub-resource */ }
    }
}
```

### Action-Based Endpoints

```kotlin
paths {
    path("/users/{id}/activate") {
        post {
            summary = "Activate user account"
            pathParameter("id") {
                description = "User ID to activate"
                schema { type = "string" }
            }
            response("200", "User activated")
            response("404", "User not found")
            response("409", "User already active")
        }
    }
    
    path("/orders/{id}/cancel") {
        post {
            summary = "Cancel an order"
            requestBody {
                jsonContent(CancelOrderRequest::class)
            }
            response("200", "Order cancelled")
            response("409", "Order cannot be cancelled")
        }
    }
}
```

### Search and Filter Endpoints

```kotlin
paths {
    path("/products/search") {
        get {
            summary = "Search products"
            queryParameter("q") {
                description = "Search query"
                required = true
                schema { type = "string" }
            }
            queryParameter("category") {
                description = "Filter by category"
                schema { type = "string" }
            }
            queryParameter("minPrice") {
                description = "Minimum price"
                schema { 
                    type = "number"
                    minimum = 0
                }
            }
            queryParameter("maxPrice") {
                description = "Maximum price"
                schema { type = "number" }
            }
            queryParameter("page") {
                description = "Page number"
                schema { 
                    type = "integer"
                    default = JsonPrimitive(1)
                }
            }
            queryParameter("limit") {
                description = "Results per page"
                schema {
                    type = "integer"
                    default = JsonPrimitive(20)
                    maximum = 100
                }
            }
            response("200", "Search results") {
                jsonContent(SearchResults::class)
            }
        }
    }
}
```

## Related Builders

- [PathItemBuilder](PathItemBuilder.md) - For defining operations on a path
- [OperationBuilder](OperationBuilder.md) - For configuring individual operations
- [OpenApiBuilder](../../../../../../../../capabilities/OpenApiBuilder.md) - Parent builder that uses PathsBuilder