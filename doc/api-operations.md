# API Operations

This guide covers defining paths and operations in your OpenAPI specification, including all HTTP methods, operation metadata, and advanced configurations.

## Table of Contents
- [Path Definition](#path-definition)
- [HTTP Methods](#http-methods)
- [Operation Metadata](#operation-metadata)
- [Tags and Organization](#tags-and-organization)
- [Operation Parameters](#operation-parameters)
- [Request Bodies](#request-bodies)
- [Responses](#responses)
- [Deprecated Operations](#deprecated-operations)
- [External Documentation](#external-documentation)
- [Server Overrides](#server-overrides)
- [Complete Examples](#complete-examples)

## Path Definition

### Basic Path Structure

```kotlin
paths {
    // Simple path
    path("/users") {
        get { /* ... */ }
        post { /* ... */ }
    }
    
    // Path with parameter
    path("/users/{userId}") {
        get { /* ... */ }
        put { /* ... */ }
        delete { /* ... */ }
    }
    
    // Nested paths
    path("/users/{userId}/posts") {
        get { /* ... */ }
        post { /* ... */ }
    }
    
    path("/users/{userId}/posts/{postId}") {
        get { /* ... */ }
        patch { /* ... */ }
        delete { /* ... */ }
    }
}
```

### Path Parameters

```kotlin
path("/organizations/{orgId}/projects/{projectId}/tasks/{taskId}") {
    // Define path parameters at the path level
    parameter {
        name = "orgId"
        `in` = ParameterLocation.PATH
        required = true
        description = "Organization identifier"
        schema {
            type = SchemaType.STRING
            format = "uuid"
        }
    }
    
    parameter {
        name = "projectId"
        `in` = ParameterLocation.PATH
        required = true
        description = "Project identifier"
        schema {
            type = SchemaType.STRING
            pattern = "^PRJ-[0-9]{6}$"
        }
    }
    
    parameter {
        name = "taskId"
        `in` = ParameterLocation.PATH
        required = true
        description = "Task identifier"
        schema {
            type = SchemaType.INTEGER
            minimum = 1
        }
    }
    
    // These parameters are available to all operations
    get { /* ... */ }
    put { /* ... */ }
    delete { /* ... */ }
}
```

### Path-level Configuration

```kotlin
path("/api/v2/resources") {
    // Summary for the path
    summary = "Resource management endpoints"
    
    // Description for the path
    description = "Operations for managing system resources"
    
    // Servers specific to this path
    servers {
        server {
            url = "https://resources.api.example.com"
            description = "Dedicated resource server"
        }
    }
    
    // Parameters common to all operations
    parameter {
        name = "X-Tenant-ID"
        `in` = ParameterLocation.HEADER
        required = true
        description = "Tenant identifier"
        schema {
            type = SchemaType.STRING
        }
    }
    
    get { /* ... */ }
    post { /* ... */ }
}
```

## HTTP Methods

### GET Operations

```kotlin
get {
    summary = "List users"
    description = "Retrieve a paginated list of users with optional filtering"
    operationId = "listUsers"
    tags = listOf("Users")
    
    // Query parameters for filtering and pagination
    parameter {
        name = "page"
        `in` = ParameterLocation.QUERY
        description = "Page number"
        schema {
            type = SchemaType.INTEGER
            minimum = 1
            default = 1
        }
    }
    
    parameter {
        name = "pageSize"
        `in` = ParameterLocation.QUERY
        description = "Items per page"
        schema {
            type = SchemaType.INTEGER
            minimum = 1
            maximum = 100
            default = 20
        }
    }
    
    parameter {
        name = "filter"
        `in` = ParameterLocation.QUERY
        description = "Filter expression"
        schema {
            type = SchemaType.STRING
        }
        example = "status eq 'active'"
    }
    
    response("200", "Successful response") {
        jsonContent {
            schema {
                type = SchemaType.OBJECT
                properties {
                    "users" to schema {
                        type = SchemaType.ARRAY
                        items = schema {
                            ref = "#/components/schemas/User"
                        }
                    }
                    "pagination" to schema {
                        ref = "#/components/schemas/PaginationInfo"
                    }
                }
            }
        }
        
        headers {
            header("X-Total-Count") {
                description = "Total number of users"
                schema {
                    type = SchemaType.INTEGER
                }
            }
        }
    }
    
    response("400", "Bad request") {
        jsonContent(Error::class)
    }
}
```

### POST Operations

```kotlin
post {
    summary = "Create user"
    description = "Create a new user account"
    operationId = "createUser"
    tags = listOf("Users")
    
    requestBody("User data") {
        required = true
        description = "User information for account creation"
        
        jsonContent(CreateUserRequest::class)
        
        // Alternative content types
        content("application/x-www-form-urlencoded") {
            schema {
                type = SchemaType.OBJECT
                required = listOf("username", "email", "password")
                properties {
                    "username" to schema { type = SchemaType.STRING }
                    "email" to schema { 
                        type = SchemaType.STRING 
                        format = "email"
                    }
                    "password" to schema { 
                        type = SchemaType.STRING 
                        format = "password"
                    }
                }
            }
        }
    }
    
    response("201", "User created") {
        description = "User successfully created"
        jsonContent(User::class)
        
        headers {
            header("Location") {
                description = "URL of the created user"
                schema {
                    type = SchemaType.STRING
                    format = "uri"
                }
                example = "/users/123"
            }
        }
    }
    
    response("400", "Validation error") {
        jsonContent(ValidationError::class)
    }
    
    response("409", "Conflict") {
        description = "Username or email already exists"
        jsonContent(Error::class)
    }
}
```

### PUT Operations

```kotlin
put {
    summary = "Replace user"
    description = "Replace entire user data"
    operationId = "replaceUser"
    tags = listOf("Users")
    
    requestBody("Complete user data") {
        required = true
        jsonContent(User::class)
    }
    
    response("200", "User updated") {
        jsonContent(User::class)
    }
    
    response("404", "User not found")
    
    response("422", "Unprocessable entity") {
        description = "Invalid user data"
        jsonContent(ValidationError::class)
    }
}
```

### PATCH Operations

```kotlin
patch {
    summary = "Update user"
    description = "Partially update user data"
    operationId = "updateUser"
    tags = listOf("Users")
    
    requestBody("Fields to update") {
        required = true
        
        // JSON Patch format
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
                        }
                        "value" to schema {
                            // Any type
                        }
                        "from" to schema {
                            type = SchemaType.STRING
                        }
                    }
                }
            }
            
            example = listOf(
                mapOf("op" to "replace", "path" to "/email", "value" to "newemail@example.com"),
                mapOf("op" to "add", "path" to "/phone", "value" to "+1234567890")
            )
        }
        
        // JSON Merge Patch format
        jsonContent {
            schema {
                type = SchemaType.OBJECT
                properties {
                    "email" to schema { 
                        type = SchemaType.STRING 
                        format = "email"
                    }
                    "name" to schema { type = SchemaType.STRING }
                    "status" to schema { 
                        type = SchemaType.STRING 
                        enum = listOf("active", "inactive", "suspended")
                    }
                }
            }
        }
    }
    
    response("200", "User updated") {
        jsonContent(User::class)
    }
    
    response("404", "User not found")
}
```

### DELETE Operations

```kotlin
delete {
    summary = "Delete user"
    description = "Permanently delete a user account"
    operationId = "deleteUser"
    tags = listOf("Users")
    
    // Optional query parameter for soft delete
    parameter {
        name = "soft"
        `in` = ParameterLocation.QUERY
        description = "Perform soft delete instead of hard delete"
        schema {
            type = SchemaType.BOOLEAN
            default = false
        }
    }
    
    response("204", "User deleted") {
        description = "User successfully deleted"
    }
    
    response("404", "User not found")
    
    response("409", "Conflict") {
        description = "User has dependent resources"
        jsonContent {
            schema {
                type = SchemaType.OBJECT
                properties {
                    "error" to schema { type = SchemaType.STRING }
                    "dependencies" to schema {
                        type = SchemaType.ARRAY
                        items = schema { type = SchemaType.STRING }
                    }
                }
            }
        }
    }
}
```

### HEAD Operations

```kotlin
head {
    summary = "Check user exists"
    description = "Check if a user exists without retrieving data"
    operationId = "checkUserExists"
    tags = listOf("Users")
    
    response("200", "User exists") {
        headers {
            header("X-User-Status") {
                description = "User account status"
                schema {
                    type = SchemaType.STRING
                    enum = listOf("active", "inactive", "suspended")
                }
            }
        }
    }
    
    response("404", "User not found")
}
```

### OPTIONS Operations

```kotlin
options {
    summary = "Get allowed methods"
    description = "Get the allowed HTTP methods for this resource"
    operationId = "getUserOptions"
    tags = listOf("Users")
    
    response("200", "Allowed methods") {
        headers {
            header("Allow") {
                description = "Allowed HTTP methods"
                schema {
                    type = SchemaType.STRING
                }
                example = "GET, POST, PUT, PATCH, DELETE, HEAD, OPTIONS"
            }
            
            header("Access-Control-Allow-Methods") {
                description = "CORS allowed methods"
                schema {
                    type = SchemaType.STRING
                }
            }
            
            header("Access-Control-Allow-Headers") {
                description = "CORS allowed headers"
                schema {
                    type = SchemaType.STRING
                }
            }
        }
    }
}
```

### TRACE Operations

```kotlin
trace {
    summary = "Trace request"
    description = "Trace the request path through the system"
    operationId = "traceRequest"
    tags = listOf("Diagnostics")
    deprecated = true  // TRACE is often disabled for security
    
    response("200", "Trace information") {
        content("message/http") {
            schema {
                type = SchemaType.STRING
            }
        }
    }
}
```

## Operation Metadata

### Operation Identification

```kotlin
get {
    // Unique operation identifier
    operationId = "getUserById"  // Used by code generators
    
    // Short summary (should be < 120 chars)
    summary = "Get user by ID"
    
    // Detailed description (supports Markdown)
    description = """
        Retrieve detailed information about a specific user.
        
        This endpoint returns:
        - User profile information
        - Account settings
        - Preferences
        
        **Note**: Some fields may be hidden based on privacy settings.
    """.trimIndent()
    
    // Tags for grouping
    tags = listOf("Users", "Account Management")
}
```

### External Documentation

```kotlin
get {
    summary = "Complex search operation"
    
    // Link to external documentation
    externalDocs {
        description = "Advanced search syntax guide"
        url = "https://docs.example.com/search-syntax"
    }
    
    // Can also be at the operation level
    description = "See external docs for search syntax"
}
```

## Tags and Organization

### Global Tag Definitions

```kotlin
openApi {
    // Define tags globally
    tags {
        tag {
            name = "Users"
            description = "User management operations"
            externalDocs {
                description = "User API guide"
                url = "https://docs.example.com/users"
            }
        }
        
        tag {
            name = "Authentication"
            description = "Authentication and authorization"
        }
        
        tag {
            name = "Admin"
            description = "Administrative operations"
        }
    }
    
    paths {
        path("/users") {
            get {
                tags = listOf("Users")  // Reference defined tags
            }
        }
    }
}
```

### Tag Organization Patterns

```kotlin
// Pattern 1: Resource-based tags
paths {
    path("/users") {
        get { tags = listOf("Users") }
        post { tags = listOf("Users") }
    }
    
    path("/products") {
        get { tags = listOf("Products") }
        post { tags = listOf("Products") }
    }
}

// Pattern 2: Feature-based tags
paths {
    path("/auth/login") {
        post { tags = listOf("Authentication") }
    }
    
    path("/auth/logout") {
        post { tags = listOf("Authentication") }
    }
    
    path("/search") {
        get { tags = listOf("Search") }
    }
}

// Pattern 3: Multiple tags
paths {
    path("/admin/users") {
        get { 
            tags = listOf("Admin", "Users")  // Multiple tags
        }
    }
}
```

## Operation Parameters

### Parameter Types and Locations

```kotlin
get {
    summary = "Search with multiple parameter types"
    
    // Path parameter (always required)
    parameter {
        name = "category"
        `in` = ParameterLocation.PATH
        required = true
        schema {
            type = SchemaType.STRING
        }
    }
    
    // Query parameters
    parameter {
        name = "q"
        `in` = ParameterLocation.QUERY
        description = "Search query"
        required = true
        schema {
            type = SchemaType.STRING
            minLength = 1
        }
    }
    
    // Header parameters
    parameter {
        name = "X-Request-ID"
        `in` = ParameterLocation.HEADER
        description = "Request tracking ID"
        schema {
            type = SchemaType.STRING
            format = "uuid"
        }
    }
    
    // Cookie parameters
    parameter {
        name = "session_id"
        `in` = ParameterLocation.COOKIE
        description = "Session identifier"
        required = true
        schema {
            type = SchemaType.STRING
        }
    }
}
```

### Array and Object Parameters

```kotlin
get {
    // Array parameter with different styles
    parameter {
        name = "tags"
        `in` = ParameterLocation.QUERY
        description = "Filter by tags"
        schema {
            type = SchemaType.ARRAY
            items = schema {
                type = SchemaType.STRING
            }
        }
        style = "form"  // Default: ?tags=tag1&tags=tag2
        explode = true
    }
    
    parameter {
        name = "ids"
        `in` = ParameterLocation.QUERY
        schema {
            type = SchemaType.ARRAY
            items = schema {
                type = SchemaType.STRING
            }
        }
        style = "simple"  // ?ids=id1,id2,id3
        explode = false
    }
    
    // Object parameter
    parameter {
        name = "filter"
        `in` = ParameterLocation.QUERY
        description = "Complex filter object"
        schema {
            type = SchemaType.OBJECT
            properties {
                "status" to schema { type = SchemaType.STRING }
                "minPrice" to schema { type = SchemaType.NUMBER }
                "maxPrice" to schema { type = SchemaType.NUMBER }
            }
        }
        style = "deepObject"  // ?filter[status]=active&filter[minPrice]=10
        explode = true
    }
}
```

### Parameter Examples

```kotlin
parameter {
    name = "date"
    `in` = ParameterLocation.QUERY
    description = "Date filter"
    schema {
        type = SchemaType.STRING
        format = "date"
    }
    
    // Single example
    example = "2023-10-20"
    
    // Multiple examples
    examples {
        example("today") {
            summary = "Today's date"
            value = "2023-10-20"
        }
        example("yesterday") {
            summary = "Yesterday's date"
            value = "2023-10-19"
        }
        example("lastWeek") {
            summary = "Date from last week"
            value = "2023-10-13"
        }
    }
}
```

## Request Bodies

### Multiple Content Types

```kotlin
post {
    summary = "Upload data in multiple formats"
    
    requestBody("Data to upload") {
        required = true
        
        // JSON content
        jsonContent {
            schema {
                ref = "#/components/schemas/DataModel"
            }
        }
        
        // XML content
        content("application/xml") {
            schema {
                ref = "#/components/schemas/DataModel"
            }
        }
        
        // Form data
        content("application/x-www-form-urlencoded") {
            schema {
                type = SchemaType.OBJECT
                properties {
                    "field1" to schema { type = SchemaType.STRING }
                    "field2" to schema { type = SchemaType.INTEGER }
                }
            }
        }
        
        // Multipart form data
        content("multipart/form-data") {
            schema {
                type = SchemaType.OBJECT
                properties {
                    "file" to schema {
                        type = SchemaType.STRING
                        format = "binary"
                    }
                    "metadata" to schema {
                        type = SchemaType.OBJECT
                        properties {
                            "filename" to schema { type = SchemaType.STRING }
                            "description" to schema { type = SchemaType.STRING }
                        }
                    }
                }
            }
        }
        
        // Plain text
        content("text/plain") {
            schema {
                type = SchemaType.STRING
            }
            example = "Plain text content"
        }
        
        // Binary data
        content("application/octet-stream") {
            schema {
                type = SchemaType.STRING
                format = "binary"
            }
        }
    }
}
```

### Request Body Examples

```kotlin
requestBody("User registration") {
    jsonContent {
        schema {
            ref = "#/components/schemas/UserRegistration"
        }
        
        // Multiple examples
        examples {
            example("minimal") {
                summary = "Minimal registration"
                description = "Only required fields"
                value = mapOf(
                    "username" to "johndoe",
                    "email" to "john@example.com",
                    "password" to "SecurePass123!"
                )
            }
            
            example("complete") {
                summary = "Complete registration"
                description = "All fields provided"
                value = mapOf(
                    "username" to "johndoe",
                    "email" to "john@example.com",
                    "password" to "SecurePass123!",
                    "fullName" to "John Doe",
                    "phoneNumber" to "+1234567890",
                    "newsletter" to true,
                    "preferences" to mapOf(
                        "language" to "en",
                        "timezone" to "UTC"
                    )
                )
            }
            
            example("invalid") {
                summary = "Invalid example"
                description = "This will fail validation"
                value = mapOf(
                    "username" to "jd",  // Too short
                    "email" to "notanemail",  // Invalid format
                    "password" to "weak"  // Too short
                )
            }
        }
    }
}
```

## Responses

### Response Structure

```kotlin
response("200", "Success") {
    description = "Detailed success description"
    
    // Response headers
    headers {
        header("X-Rate-Limit-Limit") {
            description = "Rate limit ceiling"
            schema {
                type = SchemaType.INTEGER
            }
        }
        
        header("X-Rate-Limit-Remaining") {
            description = "Remaining requests"
            schema {
                type = SchemaType.INTEGER
            }
        }
        
        header("X-Rate-Limit-Reset") {
            description = "Reset timestamp"
            schema {
                type = SchemaType.INTEGER
                format = "timestamp"
            }
        }
    }
    
    // Response content
    jsonContent {
        schema {
            ref = "#/components/schemas/SuccessResponse"
        }
    }
    
    // Response links (OpenAPI 3.1)
    links {
        link("GetUserById") {
            operationId = "getUserById"
            parameters = mapOf(
                "userId" to "\$response.body#/id"
            )
            description = "Get full user details"
        }
    }
}
```

### Response Ranges

```kotlin
// Success responses (2XX)
response("200", "OK") { /* ... */ }
response("201", "Created") { /* ... */ }
response("202", "Accepted") { /* ... */ }
response("204", "No Content")  // No body

// Redirection (3XX)
response("301", "Moved Permanently") {
    headers {
        header("Location") {
            schema { type = SchemaType.STRING }
        }
    }
}
response("302", "Found") { /* ... */ }
response("304", "Not Modified")

// Client errors (4XX)
response("400", "Bad Request") { /* ... */ }
response("401", "Unauthorized") { /* ... */ }
response("403", "Forbidden") { /* ... */ }
response("404", "Not Found") { /* ... */ }
response("409", "Conflict") { /* ... */ }
response("422", "Unprocessable Entity") { /* ... */ }
response("429", "Too Many Requests") { /* ... */ }

// Server errors (5XX)
response("500", "Internal Server Error") { /* ... */ }
response("502", "Bad Gateway") { /* ... */ }
response("503", "Service Unavailable") { /* ... */ }
response("504", "Gateway Timeout") { /* ... */ }

// Default response
response("default", "Unexpected error") {
    jsonContent(Error::class)
}
```

## Deprecated Operations

```kotlin
path("/v1/users") {
    get {
        summary = "List users (deprecated)"
        description = "This endpoint is deprecated. Use /v2/users instead."
        deprecated = true  // Mark as deprecated
        
        // Add deprecation notice in description
        externalDocs {
            description = "Migration guide to v2"
            url = "https://docs.example.com/migration/v2"
        }
        
        response("200", "Success") {
            jsonContent(listOf<User>())
            
            headers {
                header("Sunset") {
                    description = "Deprecation date"
                    schema {
                        type = SchemaType.STRING
                    }
                    example = "Wed, 31 Dec 2024 23:59:59 GMT"
                }
                
                header("Link") {
                    description = "Link to new endpoint"
                    schema {
                        type = SchemaType.STRING
                    }
                    example = "</v2/users>; rel=\"successor-version\""
                }
            }
        }
    }
}
```

## Server Overrides

```kotlin
path("/special/endpoint") {
    // Override servers for this path
    servers {
        server {
            url = "https://special.api.example.com"
            description = "Special purpose server"
            variables {
                variable("environment") {
                    enum = listOf("production", "staging")
                    default = "production"
                }
            }
        }
    }
    
    get {
        summary = "Special operation"
        
        // Can also override at operation level
        servers {
            server {
                url = "https://read-replica.api.example.com"
                description = "Read-only replica"
            }
        }
        
        response("200", "Success") {
            jsonContent(Data::class)
        }
    }
}
```

## Complete Examples

### RESTful Resource API

```kotlin
paths {
    // Collection resource
    path("/api/v1/articles") {
        get {
            summary = "List articles"
            operationId = "listArticles"
            tags = listOf("Articles")
            
            parameter {
                name = "status"
                `in` = ParameterLocation.QUERY
                schema {
                    type = SchemaType.STRING
                    enum = listOf("draft", "published", "archived")
                }
            }
            
            parameter {
                name = "author"
                `in` = ParameterLocation.QUERY
                schema {
                    type = SchemaType.STRING
                }
            }
            
            parameter {
                name = "tags"
                `in` = ParameterLocation.QUERY
                schema {
                    type = SchemaType.ARRAY
                    items = schema { type = SchemaType.STRING }
                }
                style = "form"
                explode = true
            }
            
            parameter {
                name = "sort"
                `in` = ParameterLocation.QUERY
                schema {
                    type = SchemaType.STRING
                    enum = listOf("createdAt", "-createdAt", "title", "-title")
                    default = "-createdAt"
                }
            }
            
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
                name = "limit"
                `in` = ParameterLocation.QUERY
                schema {
                    type = SchemaType.INTEGER
                    minimum = 1
                    maximum = 100
                    default = 20
                }
            }
            
            response("200", "Article list") {
                jsonContent {
                    schema {
                        type = SchemaType.OBJECT
                        properties {
                            "articles" to schema {
                                type = SchemaType.ARRAY
                                items = schema {
                                    ref = "#/components/schemas/Article"
                                }
                            }
                            "pagination" to schema {
                                ref = "#/components/schemas/Pagination"
                            }
                        }
                    }
                }
                
                headers {
                    header("Link") {
                        description = "Pagination links"
                        schema { type = SchemaType.STRING }
                        example = "</articles?page=2>; rel=\"next\", </articles?page=5>; rel=\"last\""
                    }
                }
            }
        }
        
        post {
            summary = "Create article"
            operationId = "createArticle"
            tags = listOf("Articles")
            
            requestBody("Article data") {
                required = true
                jsonContent(CreateArticleRequest::class)
            }
            
            response("201", "Article created") {
                jsonContent(Article::class)
                headers {
                    header("Location") {
                        schema { 
                            type = SchemaType.STRING 
                            format = "uri"
                        }
                    }
                }
            }
            
            response("422", "Validation failed") {
                jsonContent(ValidationError::class)
            }
        }
    }
    
    // Individual resource
    path("/api/v1/articles/{articleId}") {
        parameter {
            name = "articleId"
            `in` = ParameterLocation.PATH
            required = true
            schema {
                type = SchemaType.STRING
                pattern = "^[a-z0-9-]+$"
            }
        }
        
        get {
            summary = "Get article"
            operationId = "getArticle"
            tags = listOf("Articles")
            
            parameter {
                name = "include"
                `in` = ParameterLocation.QUERY
                description = "Include related resources"
                schema {
                    type = SchemaType.ARRAY
                    items = schema {
                        type = SchemaType.STRING
                        enum = listOf("author", "comments", "tags")
                    }
                }
                style = "form"
                explode = false  // ?include=author,comments
            }
            
            response("200", "Article details") {
                jsonContent(Article::class)
                
                headers {
                    header("Cache-Control") {
                        schema { type = SchemaType.STRING }
                        example = "max-age=3600"
                    }
                    
                    header("ETag") {
                        schema { type = SchemaType.STRING }
                        example = "\"33a64df551425fcc55e4d42a148795d9f25f89d4\""
                    }
                }
            }
            
            response("404", "Article not found") {
                jsonContent(Error::class)
            }
        }
        
        put {
            summary = "Update article"
            operationId = "updateArticle"
            tags = listOf("Articles")
            
            parameter {
                name = "If-Match"
                `in` = ParameterLocation.HEADER
                description = "ETag for optimistic concurrency control"
                schema { type = SchemaType.STRING }
            }
            
            requestBody("Updated article") {
                required = true
                jsonContent(UpdateArticleRequest::class)
            }
            
            response("200", "Article updated") {
                jsonContent(Article::class)
            }
            
            response("409", "Conflict") {
                description = "ETag mismatch"
                jsonContent(Error::class)
            }
        }
        
        delete {
            summary = "Delete article"
            operationId = "deleteArticle"
            tags = listOf("Articles")
            
            response("204", "Article deleted")
            
            response("404", "Article not found")
        }
    }
    
    // Sub-resources
    path("/api/v1/articles/{articleId}/comments") {
        parameter {
            name = "articleId"
            `in` = ParameterLocation.PATH
            required = true
            schema { type = SchemaType.STRING }
        }
        
        get {
            summary = "List article comments"
            operationId = "listArticleComments"
            tags = listOf("Articles", "Comments")
            
            response("200", "Comments") {
                jsonContent(listOf<Comment>())
            }
        }
        
        post {
            summary = "Add comment"
            operationId = "addArticleComment"
            tags = listOf("Articles", "Comments")
            
            requestBody("Comment") {
                required = true
                jsonContent(CreateCommentRequest::class)
            }
            
            response("201", "Comment added") {
                jsonContent(Comment::class)
            }
        }
    }
}
```

### Batch Operations

```kotlin
path("/api/v1/batch") {
    post {
        summary = "Batch operations"
        description = "Execute multiple operations in a single request"
        operationId = "batchOperations"
        tags = listOf("Batch")
        
        requestBody("Batch request") {
            required = true
            jsonContent {
                schema {
                    type = SchemaType.OBJECT
                    required = listOf("operations")
                    properties {
                        "operations" to schema {
                            type = SchemaType.ARRAY
                            minItems = 1
                            maxItems = 100
                            items = schema {
                                type = SchemaType.OBJECT
                                required = listOf("method", "path")
                                properties {
                                    "id" to schema {
                                        type = SchemaType.STRING
                                        description = "Operation identifier"
                                    }
                                    "method" to schema {
                                        type = SchemaType.STRING
                                        enum = listOf("GET", "POST", "PUT", "PATCH", "DELETE")
                                    }
                                    "path" to schema {
                                        type = SchemaType.STRING
                                        pattern = "^/.*"
                                    }
                                    "body" to schema {
                                        description = "Request body for POST/PUT/PATCH"
                                    }
                                    "headers" to schema {
                                        type = SchemaType.OBJECT
                                        additionalProperties = schema {
                                            type = SchemaType.STRING
                                        }
                                    }
                                }
                            }
                        }
                        "sequential" to schema {
                            type = SchemaType.BOOLEAN
                            default = false
                            description = "Execute operations sequentially"
                        }
                    }
                }
                
                example = mapOf(
                    "operations" to listOf(
                        mapOf(
                            "id" to "op1",
                            "method" to "GET",
                            "path" to "/users/123"
                        ),
                        mapOf(
                            "id" to "op2",
                            "method" to "POST",
                            "path" to "/users",
                            "body" to mapOf(
                                "name" to "John Doe",
                                "email" to "john@example.com"
                            )
                        )
                    )
                )
            }
        }
        
        response("200", "Batch results") {
            jsonContent {
                schema {
                    type = SchemaType.OBJECT
                    properties {
                        "results" to schema {
                            type = SchemaType.ARRAY
                            items = schema {
                                type = SchemaType.OBJECT
                                properties {
                                    "id" to schema { type = SchemaType.STRING }
                                    "status" to schema { type = SchemaType.INTEGER }
                                    "headers" to schema {
                                        type = SchemaType.OBJECT
                                        additionalProperties = schema {
                                            type = SchemaType.STRING
                                        }
                                    }
                                    "body" to schema {
                                        description = "Response body"
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
```

## Best Practices

1. **Use Meaningful Operation IDs**: They're used by code generators
2. **Add Descriptions**: Explain what the operation does and any side effects
3. **Group with Tags**: Use consistent tags for better organization
4. **Version Your APIs**: Use path versioning (/v1/, /v2/) or headers
5. **Handle Errors Consistently**: Use standard error response schemas
6. **Document Edge Cases**: Explain special behaviors in descriptions
7. **Use Appropriate Status Codes**: Follow HTTP semantics
8. **Include Examples**: Provide request/response examples

## Next Steps

- [Request & Response](request-response.md) - Deep dive into parameters and responses
- [Security](security.md) - Adding authentication to operations
- [Advanced Features](advanced-features.md) - Callbacks, webhooks, and more