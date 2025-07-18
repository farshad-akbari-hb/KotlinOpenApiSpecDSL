# Request & Response

This guide provides an in-depth look at defining parameters, request bodies, and responses in your OpenAPI specification.

## Table of Contents
- [Parameters](#parameters)
  - [Parameter Locations](#parameter-locations)
  - [Parameter Serialization](#parameter-serialization)
  - [Parameter Validation](#parameter-validation)
  - [Reusable Parameters](#reusable-parameters)
- [Request Bodies](#request-bodies)
  - [Content Negotiation](#content-negotiation)
  - [File Uploads](#file-uploads)
  - [Request Validation](#request-validation)
  - [Request Examples](#request-examples)
- [Responses](#responses)
  - [Response Structure](#response-structure)
  - [Response Headers](#response-headers)
  - [Response Links](#response-links)
  - [Response Examples](#response-examples)
- [Advanced Patterns](#advanced-patterns)

## Parameters

### Parameter Locations

#### Path Parameters

```kotlin
path("/users/{userId}/posts/{postId}") {
    parameter {
        name = "userId"
        `in` = ParameterLocation.PATH
        required = true  // Always required for path parameters
        description = "User identifier"
        schema {
            type = SchemaType.STRING
            format = "uuid"
        }
        example = "550e8400-e29b-41d4-a716-446655440000"
    }
    
    parameter {
        name = "postId"
        `in` = ParameterLocation.PATH
        required = true
        description = "Post identifier"
        schema {
            type = SchemaType.INTEGER
            minimum = 1
        }
        examples {
            example("first-post") {
                value = 1
                summary = "The first post"
            }
            example("recent-post") {
                value = 12345
                summary = "A recent post ID"
            }
        }
    }
}
```

#### Query Parameters

```kotlin
get {
    // Simple query parameter
    parameter {
        name = "search"
        `in` = ParameterLocation.QUERY
        description = "Search term"
        schema {
            type = SchemaType.STRING
            minLength = 3
        }
        allowEmptyValue = false
    }
    
    // Query parameter with default
    parameter {
        name = "limit"
        `in` = ParameterLocation.QUERY
        description = "Number of items to return"
        schema {
            type = SchemaType.INTEGER
            minimum = 1
            maximum = 100
            default = 20
        }
    }
    
    // Deprecated parameter
    parameter {
        name = "old_format"
        `in` = ParameterLocation.QUERY
        deprecated = true
        description = "Use 'format' parameter instead"
        schema {
            type = SchemaType.BOOLEAN
            default = false
        }
    }
    
    // Required query parameter
    parameter {
        name = "api_version"
        `in` = ParameterLocation.QUERY
        required = true
        description = "API version to use"
        schema {
            type = SchemaType.STRING
            enum = listOf("v1", "v2", "v3")
        }
    }
}
```

#### Header Parameters

```kotlin
get {
    // Standard header
    parameter {
        name = "Authorization"
        `in` = ParameterLocation.HEADER
        required = true
        description = "Bearer token"
        schema {
            type = SchemaType.STRING
            pattern = "^Bearer .+$"
        }
        example = "Bearer eyJhbGciOiJIUzI1NiIs..."
    }
    
    // Custom headers
    parameter {
        name = "X-API-Key"
        `in` = ParameterLocation.HEADER
        required = true
        description = "API key for authentication"
        schema {
            type = SchemaType.STRING
            minLength = 32
            maxLength = 32
        }
    }
    
    parameter {
        name = "X-Request-ID"
        `in` = ParameterLocation.HEADER
        description = "Unique request identifier for tracing"
        schema {
            type = SchemaType.STRING
            format = "uuid"
        }
    }
    
    // Content negotiation headers
    parameter {
        name = "Accept-Language"
        `in` = ParameterLocation.HEADER
        description = "Preferred response language"
        schema {
            type = SchemaType.STRING
            default = "en-US"
        }
        examples {
            example("english") { value = "en-US" }
            example("spanish") { value = "es-ES" }
            example("japanese") { value = "ja-JP" }
        }
    }
}
```

#### Cookie Parameters

```kotlin
get {
    parameter {
        name = "session_id"
        `in` = ParameterLocation.COOKIE
        required = true
        description = "Session identifier"
        schema {
            type = SchemaType.STRING
            pattern = "^[a-zA-Z0-9]{32}$"
        }
    }
    
    parameter {
        name = "preferences"
        `in` = ParameterLocation.COOKIE
        description = "User preferences"
        schema {
            type = SchemaType.STRING
            format = "base64"
        }
    }
    
    parameter {
        name = "tracking_consent"
        `in` = ParameterLocation.COOKIE
        description = "User consent for tracking"
        schema {
            type = SchemaType.BOOLEAN
            default = false
        }
    }
}
```

### Parameter Serialization

#### Array Serialization Styles

```kotlin
// Form style (default for query)
parameter {
    name = "tags"
    `in` = ParameterLocation.QUERY
    description = "Filter by tags"
    schema {
        type = SchemaType.ARRAY
        items = schema { type = SchemaType.STRING }
    }
    style = "form"
    explode = true  // ?tags=tag1&tags=tag2
}

// Simple style (comma-separated)
parameter {
    name = "ids"
    `in` = ParameterLocation.QUERY
    schema {
        type = SchemaType.ARRAY
        items = schema { type = SchemaType.INTEGER }
    }
    style = "simple"
    explode = false  // ?ids=1,2,3
}

// Space delimited
parameter {
    name = "coordinates"
    `in` = ParameterLocation.QUERY
    schema {
        type = SchemaType.ARRAY
        items = schema { type = SchemaType.NUMBER }
    }
    style = "spaceDelimited"
    explode = false  // ?coordinates=1.5%202.3%203.7
}

// Pipe delimited
parameter {
    name = "filters"
    `in` = ParameterLocation.QUERY
    schema {
        type = SchemaType.ARRAY
        items = schema { type = SchemaType.STRING }
    }
    style = "pipeDelimited"
    explode = false  // ?filters=active|verified|premium
}
```

#### Object Serialization Styles

```kotlin
// Deep object style (for query parameters)
parameter {
    name = "filter"
    `in` = ParameterLocation.QUERY
    description = "Complex filter object"
    schema {
        type = SchemaType.OBJECT
        properties {
            "status" to schema { 
                type = SchemaType.STRING 
                enum = listOf("active", "inactive")
            }
            "category" to schema { type = SchemaType.STRING }
            "price" to schema {
                type = SchemaType.OBJECT
                properties {
                    "min" to schema { type = SchemaType.NUMBER }
                    "max" to schema { type = SchemaType.NUMBER }
                }
            }
        }
    }
    style = "deepObject"
    explode = true  // ?filter[status]=active&filter[price][min]=10
}

// Form style for objects
parameter {
    name = "options"
    `in` = ParameterLocation.QUERY
    schema {
        type = SchemaType.OBJECT
        properties {
            "includeArchived" to schema { type = SchemaType.BOOLEAN }
            "sortBy" to schema { type = SchemaType.STRING }
        }
    }
    style = "form"
    explode = true  // ?includeArchived=true&sortBy=date
}
```

### Parameter Validation

#### Complex Validation Rules

```kotlin
// Date range validation
parameter {
    name = "startDate"
    `in` = ParameterLocation.QUERY
    required = true
    description = "Start date (must be before endDate)"
    schema {
        type = SchemaType.STRING
        format = "date"
        // Can't be in the future
        maximum = "2024-12-31"
    }
}

parameter {
    name = "endDate"
    `in` = ParameterLocation.QUERY
    required = true
    description = "End date (must be after startDate)"
    schema {
        type = SchemaType.STRING
        format = "date"
        minimum = "2020-01-01"
    }
}

// Enum with validation
parameter {
    name = "status"
    `in` = ParameterLocation.QUERY
    description = "Filter by status"
    schema {
        type = SchemaType.ARRAY
        items = schema {
            type = SchemaType.STRING
            enum = listOf("pending", "approved", "rejected", "cancelled")
        }
        minItems = 1
        maxItems = 3
        uniqueItems = true
    }
}

// Pattern validation
parameter {
    name = "phone"
    `in` = ParameterLocation.QUERY
    description = "Phone number in E.164 format"
    schema {
        type = SchemaType.STRING
        pattern = "^\\+[1-9]\\d{1,14}$"
    }
    example = "+1234567890"
}
```

### Reusable Parameters

```kotlin
components {
    // Define reusable parameters
    parameter("pageParam") {
        name = "page"
        `in` = ParameterLocation.QUERY
        description = "Page number for pagination"
        schema {
            type = SchemaType.INTEGER
            minimum = 1
            default = 1
        }
    }
    
    parameter("limitParam") {
        name = "limit"
        `in` = ParameterLocation.QUERY
        description = "Number of items per page"
        schema {
            type = SchemaType.INTEGER
            minimum = 1
            maximum = 100
            default = 20
        }
    }
    
    parameter("apiKeyHeader") {
        name = "X-API-Key"
        `in` = ParameterLocation.HEADER
        required = true
        description = "API key for authentication"
        schema {
            type = SchemaType.STRING
        }
    }
    
    parameter("includeParam") {
        name = "include"
        `in` = ParameterLocation.QUERY
        description = "Related resources to include"
        schema {
            type = SchemaType.ARRAY
            items = schema {
                type = SchemaType.STRING
            }
        }
        style = "form"
        explode = false
    }
}

// Use in operations
paths {
    path("/users") {
        get {
            // Reference reusable parameters
            parameter { ref = "#/components/parameters/pageParam" }
            parameter { ref = "#/components/parameters/limitParam" }
            parameter { ref = "#/components/parameters/apiKeyHeader" }
            
            // Add operation-specific parameters
            parameter {
                name = "role"
                `in` = ParameterLocation.QUERY
                schema {
                    type = SchemaType.STRING
                    enum = listOf("admin", "user", "guest")
                }
            }
        }
    }
}
```

## Request Bodies

### Content Negotiation

```kotlin
post {
    requestBody("Resource data") {
        required = true
        description = "Resource to create in various formats"
        
        // JSON (most common)
        jsonContent {
            schema {
                ref = "#/components/schemas/Resource"
            }
            examples {
                example("minimal") {
                    summary = "Minimal valid resource"
                    value = mapOf(
                        "name" to "Resource Name",
                        "type" to "basic"
                    )
                }
                example("complete") {
                    summary = "Complete resource with all fields"
                    externalValue = "https://example.com/examples/complete-resource.json"
                }
            }
        }
        
        // XML
        content("application/xml") {
            schema {
                ref = "#/components/schemas/Resource"
            }
            example = """
                <resource>
                    <name>Resource Name</name>
                    <type>basic</type>
                </resource>
            """.trimIndent()
        }
        
        // YAML
        content("application/yaml") {
            schema {
                ref = "#/components/schemas/Resource"
            }
            example = """
                name: Resource Name
                type: basic
            """.trimIndent()
        }
        
        // Form URL-encoded
        content("application/x-www-form-urlencoded") {
            schema {
                type = SchemaType.OBJECT
                required = listOf("name", "type")
                properties {
                    "name" to schema {
                        type = SchemaType.STRING
                        minLength = 1
                        maxLength = 100
                    }
                    "type" to schema {
                        type = SchemaType.STRING
                        enum = listOf("basic", "advanced")
                    }
                    "description" to schema {
                        type = SchemaType.STRING
                        maxLength = 500
                    }
                }
            }
            encoding {
                property("description") {
                    contentType = "text/plain"
                    style = "form"
                }
            }
        }
        
        // Plain text
        content("text/plain") {
            schema {
                type = SchemaType.STRING
                maxLength = 1000
            }
            example = "Plain text content for simple resources"
        }
        
        // Binary
        content("application/octet-stream") {
            schema {
                type = SchemaType.STRING
                format = "binary"
            }
        }
    }
}
```

### File Uploads

#### Single File Upload

```kotlin
post {
    summary = "Upload single file"
    
    requestBody("File to upload") {
        required = true
        
        content("multipart/form-data") {
            schema {
                type = SchemaType.OBJECT
                required = listOf("file")
                properties {
                    "file" to schema {
                        type = SchemaType.STRING
                        format = "binary"
                        description = "File to upload"
                    }
                    "description" to schema {
                        type = SchemaType.STRING
                        maxLength = 200
                        description = "File description"
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
            
            encoding {
                property("file") {
                    contentType = "image/png, image/jpeg"
                    headers {
                        header("X-Custom-Header") {
                            schema {
                                type = SchemaType.STRING
                            }
                        }
                    }
                }
            }
        }
    }
}
```

#### Multiple File Upload

```kotlin
post {
    summary = "Upload multiple files"
    
    requestBody("Files to upload") {
        required = true
        
        content("multipart/form-data") {
            schema {
                type = SchemaType.OBJECT
                required = listOf("files")
                properties {
                    "files" to schema {
                        type = SchemaType.ARRAY
                        items = schema {
                            type = SchemaType.STRING
                            format = "binary"
                        }
                        minItems = 1
                        maxItems = 10
                        description = "Files to upload (max 10)"
                    }
                    "category" to schema {
                        type = SchemaType.STRING
                        enum = listOf("documents", "images", "videos", "other")
                    }
                    "metadata" to schema {
                        type = SchemaType.OBJECT
                        properties {
                            "author" to schema { type = SchemaType.STRING }
                            "created" to schema { 
                                type = SchemaType.STRING 
                                format = "date"
                            }
                        }
                    }
                }
            }
        }
    }
}
```

#### Mixed Content Upload

```kotlin
post {
    summary = "Upload with mixed content"
    
    requestBody("Mixed content") {
        content("multipart/mixed") {
            schema {
                type = SchemaType.OBJECT
                properties {
                    "profile" to schema {
                        type = SchemaType.OBJECT
                        properties {
                            "name" to schema { type = SchemaType.STRING }
                            "email" to schema { 
                                type = SchemaType.STRING 
                                format = "email"
                            }
                        }
                    }
                    "avatar" to schema {
                        type = SchemaType.STRING
                        format = "binary"
                    }
                    "documents" to schema {
                        type = SchemaType.ARRAY
                        items = schema {
                            type = SchemaType.STRING
                            format = "binary"
                        }
                    }
                }
            }
            
            encoding {
                property("profile") {
                    contentType = "application/json"
                }
                property("avatar") {
                    contentType = "image/*"
                    headers {
                        header("Content-Disposition") {
                            schema {
                                type = SchemaType.STRING
                            }
                        }
                    }
                }
                property("documents") {
                    contentType = "application/pdf"
                }
            }
        }
    }
}
```

### Request Validation

```kotlin
// Complex validation example
requestBody("User registration") {
    required = true
    
    jsonContent {
        schema {
            type = SchemaType.OBJECT
            required = listOf("username", "email", "password", "termsAccepted")
            properties {
                "username" to schema {
                    type = SchemaType.STRING
                    pattern = "^[a-zA-Z0-9_]{3,20}$"
                    description = "Alphanumeric username (3-20 chars)"
                }
                "email" to schema {
                    type = SchemaType.STRING
                    format = "email"
                    // Additional pattern for stricter validation
                    pattern = "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$"
                }
                "password" to schema {
                    type = SchemaType.STRING
                    format = "password"
                    minLength = 8
                    maxLength = 128
                    // Pattern for strong password
                    pattern = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]+$"
                    description = "Must contain uppercase, lowercase, number, and special character"
                }
                "confirmPassword" to schema {
                    type = SchemaType.STRING
                    format = "password"
                    description = "Must match password"
                }
                "age" to schema {
                    type = SchemaType.INTEGER
                    minimum = 18
                    maximum = 120
                }
                "termsAccepted" to schema {
                    type = SchemaType.BOOLEAN
                    const = true  // Must be true
                }
                "referralCode" to schema {
                    type = SchemaType.STRING
                    pattern = "^REF-[A-Z0-9]{6}$"
                    nullable = true
                }
                "preferences" to schema {
                    type = SchemaType.OBJECT
                    properties {
                        "newsletter" to schema {
                            type = SchemaType.BOOLEAN
                            default = false
                        }
                        "language" to schema {
                            type = SchemaType.STRING
                            enum = listOf("en", "es", "fr", "de", "ja")
                            default = "en"
                        }
                        "timezone" to schema {
                            type = SchemaType.STRING
                            pattern = "^[A-Za-z]+/[A-Za-z_]+$"
                            example = "America/New_York"
                        }
                    }
                }
            }
            // Additional validation
            dependencies = mapOf(
                "confirmPassword" to listOf("password")
            )
        }
    }
}
```

### Request Examples

```kotlin
requestBody("Complex request") {
    jsonContent {
        schema {
            ref = "#/components/schemas/ComplexRequest"
        }
        
        examples {
            example("minimal") {
                summary = "Minimal valid request"
                description = "Only required fields"
                value = mapOf(
                    "id" to "123",
                    "type" to "basic"
                )
            }
            
            example("typical") {
                summary = "Typical request"
                description = "Common use case"
                value = mapOf(
                    "id" to "123",
                    "type" to "standard",
                    "name" to "Example Item",
                    "tags" to listOf("important", "reviewed"),
                    "metadata" to mapOf(
                        "created" to "2023-10-20",
                        "author" to "John Doe"
                    )
                )
            }
            
            example("complete") {
                summary = "Complete request"
                description = "All possible fields"
                // Reference external file
                externalValue = "https://api.example.com/examples/complete-request.json"
            }
            
            example("invalid") {
                summary = "Invalid request"
                description = "This will fail validation (for testing)"
                value = mapOf(
                    "id" to "",  // Empty ID
                    "type" to "unknown",  // Invalid type
                    "tags" to listOf("tag1", "tag1")  // Duplicates
                )
            }
        }
    }
}
```

## Responses

### Response Structure

```kotlin
// Comprehensive response definition
response("200", "Successful operation") {
    description = """
        The operation completed successfully.
        
        The response includes:
        - Resource data
        - Metadata
        - HATEOAS links
    """.trimIndent()
    
    // Headers
    headers {
        header("X-RateLimit-Limit") {
            description = "Rate limit ceiling for the client"
            required = true
            schema {
                type = SchemaType.INTEGER
                minimum = 0
            }
        }
        
        header("X-RateLimit-Remaining") {
            description = "Number of requests left for the time window"
            required = true
            schema {
                type = SchemaType.INTEGER
                minimum = 0
            }
        }
        
        header("X-RateLimit-Reset") {
            description = "UTC epoch seconds when the rate limit resets"
            schema {
                type = SchemaType.INTEGER
            }
        }
        
        header("Cache-Control") {
            description = "Caching directives"
            schema {
                type = SchemaType.STRING
            }
            example = "max-age=3600, must-revalidate"
        }
        
        header("ETag") {
            description = "Entity tag for cache validation"
            schema {
                type = SchemaType.STRING
            }
        }
        
        header("Link") {
            description = "Related resources (RFC 5988)"
            schema {
                type = SchemaType.STRING
            }
            example = "</users?page=2>; rel=\"next\", </users?page=10>; rel=\"last\""
        }
    }
    
    // Content
    jsonContent {
        schema {
            type = SchemaType.OBJECT
            properties {
                "data" to schema {
                    ref = "#/components/schemas/Resource"
                }
                "metadata" to schema {
                    type = SchemaType.OBJECT
                    properties {
                        "version" to schema { type = SchemaType.STRING }
                        "timestamp" to schema { 
                            type = SchemaType.STRING 
                            format = "date-time"
                        }
                        "requestId" to schema { 
                            type = SchemaType.STRING 
                            format = "uuid"
                        }
                    }
                }
                "links" to schema {
                    type = SchemaType.OBJECT
                    properties {
                        "self" to schema { 
                            type = SchemaType.STRING 
                            format = "uri"
                        }
                        "related" to schema {
                            type = SchemaType.OBJECT
                            additionalProperties = schema {
                                type = SchemaType.STRING
                                format = "uri"
                            }
                        }
                    }
                }
            }
        }
    }
}
```

### Response Headers

#### Standard Headers

```kotlin
// Pagination headers
headers {
    header("X-Total-Count") {
        description = "Total number of items"
        schema {
            type = SchemaType.INTEGER
        }
    }
    
    header("X-Page-Count") {
        description = "Total number of pages"
        schema {
            type = SchemaType.INTEGER
        }
    }
    
    header("Link") {
        description = "Pagination links"
        schema {
            type = SchemaType.STRING
        }
        examples {
            example("with-pagination") {
                value = "</items?page=1>; rel=\"first\", </items?page=2>; rel=\"next\", </items?page=5>; rel=\"last\""
            }
        }
    }
}

// Security headers
headers {
    header("X-Content-Type-Options") {
        description = "Prevents MIME type sniffing"
        schema {
            type = SchemaType.STRING
            const = "nosniff"
        }
    }
    
    header("X-Frame-Options") {
        description = "Clickjacking protection"
        schema {
            type = SchemaType.STRING
            enum = listOf("DENY", "SAMEORIGIN")
        }
    }
    
    header("Strict-Transport-Security") {
        description = "HSTS header"
        schema {
            type = SchemaType.STRING
        }
        example = "max-age=31536000; includeSubDomains"
    }
}

// Custom headers
headers {
    header("X-Request-Cost") {
        description = "Request cost in API credits"
        schema {
            type = SchemaType.NUMBER
            minimum = 0
        }
    }
    
    header("X-Response-Time") {
        description = "Server processing time in milliseconds"
        schema {
            type = SchemaType.INTEGER
        }
    }
}
```

### Response Links

```kotlin
response("201", "Resource created") {
    description = "Resource successfully created"
    
    jsonContent(Resource::class)
    
    headers {
        header("Location") {
            description = "URL of created resource"
            required = true
            schema {
                type = SchemaType.STRING
                format = "uri"
            }
        }
    }
    
    // OpenAPI 3.1 links
    links {
        link("GetResource") {
            operationId = "getResourceById"
            parameters = mapOf(
                "resourceId" to "\$response.body#/id"
            )
            description = "Get the created resource"
        }
        
        link("UpdateResource") {
            operationId = "updateResourceById"
            parameters = mapOf(
                "resourceId" to "\$response.body#/id"
            )
            description = "Update the created resource"
        }
        
        link("DeleteResource") {
            operationId = "deleteResourceById"
            parameters = mapOf(
                "resourceId" to "\$response.body#/id"
            )
            description = "Delete the created resource"
        }
        
        link("ListRelated") {
            operationRef = "#/paths/~1resources~1{resourceId}~1related/get"
            parameters = mapOf(
                "resourceId" to "\$response.body#/id"
            )
        }
    }
}
```

### Response Examples

#### Multiple Response Examples

```kotlin
response("200", "Search results") {
    jsonContent {
        schema {
            type = SchemaType.OBJECT
            properties {
                "results" to schema {
                    type = SchemaType.ARRAY
                    items = schema {
                        ref = "#/components/schemas/SearchResult"
                    }
                }
                "total" to schema {
                    type = SchemaType.INTEGER
                }
                "facets" to schema {
                    type = SchemaType.OBJECT
                    additionalProperties = schema {
                        type = SchemaType.ARRAY
                        items = schema {
                            type = SchemaType.OBJECT
                            properties {
                                "value" to schema { type = SchemaType.STRING }
                                "count" to schema { type = SchemaType.INTEGER }
                            }
                        }
                    }
                }
            }
        }
        
        examples {
            example("no-results") {
                summary = "No results found"
                value = mapOf(
                    "results" to emptyList<Any>(),
                    "total" to 0,
                    "facets" to emptyMap<String, Any>()
                )
            }
            
            example("single-result") {
                summary = "Single result"
                value = mapOf(
                    "results" to listOf(
                        mapOf(
                            "id" to "123",
                            "title" to "Example Result",
                            "score" to 0.95
                        )
                    ),
                    "total" to 1,
                    "facets" to mapOf(
                        "category" to listOf(
                            mapOf("value" to "documents", "count" to 1)
                        )
                    )
                )
            }
            
            example("multiple-results") {
                summary = "Multiple results with facets"
                externalValue = "https://api.example.com/examples/search-results.json"
            }
        }
    }
}
```

#### Error Response Examples

```kotlin
// Define reusable error schemas
components {
    schema("Error") {
        type = SchemaType.OBJECT
        required = listOf("code", "message")
        properties {
            "code" to schema {
                type = SchemaType.STRING
                description = "Error code"
            }
            "message" to schema {
                type = SchemaType.STRING
                description = "Human-readable error message"
            }
            "details" to schema {
                type = SchemaType.OBJECT
                additionalProperties = true
                description = "Additional error details"
            }
            "timestamp" to schema {
                type = SchemaType.STRING
                format = "date-time"
            }
            "path" to schema {
                type = SchemaType.STRING
                description = "Request path that caused the error"
            }
        }
    }
    
    schema("ValidationError") {
        allOf = listOf(
            SchemaReference.ReferenceTo("#/components/schemas/Error"),
            SchemaReference.Schema(Schema(
                type = SchemaType.OBJECT,
                properties = mapOf(
                    "violations" to Schema(
                        type = SchemaType.ARRAY,
                        items = Schema(
                            type = SchemaType.OBJECT,
                            properties = mapOf(
                                "field" to Schema(type = SchemaType.STRING),
                                "message" to Schema(type = SchemaType.STRING),
                                "rejectedValue" to Schema()
                            )
                        )
                    )
                )
            ))
        )
    }
}

// Use in responses
response("400", "Bad Request") {
    jsonContent {
        schema {
            ref = "#/components/schemas/ValidationError"
        }
        
        examples {
            example("missing-field") {
                summary = "Required field missing"
                value = mapOf(
                    "code" to "VALIDATION_ERROR",
                    "message" to "Validation failed",
                    "violations" to listOf(
                        mapOf(
                            "field" to "email",
                            "message" to "Field is required"
                        )
                    ),
                    "timestamp" to "2023-10-20T15:30:00Z",
                    "path" to "/api/users"
                )
            }
            
            example("invalid-format") {
                summary = "Invalid field format"
                value = mapOf(
                    "code" to "VALIDATION_ERROR",
                    "message" to "Validation failed",
                    "violations" to listOf(
                        mapOf(
                            "field" to "email",
                            "message" to "Invalid email format",
                            "rejectedValue" to "not-an-email"
                        )
                    ),
                    "timestamp" to "2023-10-20T15:30:00Z",
                    "path" to "/api/users"
                )
            }
        }
    }
}
```

## Advanced Patterns

### Conditional Responses

```kotlin
post {
    summary = "Process payment"
    
    requestBody("Payment details") {
        jsonContent(PaymentRequest::class)
    }
    
    // Success responses based on payment method
    response("200", "Payment processed (synchronous)") {
        description = "Used for credit card and PayPal payments"
        jsonContent {
            schema {
                type = SchemaType.OBJECT
                properties {
                    "transactionId" to schema { type = SchemaType.STRING }
                    "status" to schema { 
                        type = SchemaType.STRING 
                        const = "completed"
                    }
                    "receipt" to schema {
                        ref = "#/components/schemas/Receipt"
                    }
                }
            }
        }
    }
    
    response("202", "Payment accepted (asynchronous)") {
        description = "Used for bank transfers"
        jsonContent {
            schema {
                type = SchemaType.OBJECT
                properties {
                    "transactionId" to schema { type = SchemaType.STRING }
                    "status" to schema { 
                        type = SchemaType.STRING 
                        const = "pending"
                    }
                    "pollUrl" to schema { 
                        type = SchemaType.STRING 
                        format = "uri"
                    }
                    "estimatedCompletion" to schema {
                        type = SchemaType.STRING
                        format = "date-time"
                    }
                }
            }
        }
        
        headers {
            header("Location") {
                description = "URL to check payment status"
                schema {
                    type = SchemaType.STRING
                    format = "uri"
                }
            }
        }
    }
}
```

### Streaming Responses

```kotlin
get {
    summary = "Stream events"
    
    response("200", "Event stream") {
        description = "Server-sent events stream"
        
        content("text/event-stream") {
            schema {
                type = SchemaType.STRING
                description = "Server-sent events"
            }
            
            example = """
                event: user-connected
                data: {"userId": "123", "timestamp": "2023-10-20T15:30:00Z"}
                
                event: message
                data: {"from": "123", "text": "Hello!", "timestamp": "2023-10-20T15:30:01Z"}
                
                event: user-disconnected
                data: {"userId": "123", "timestamp": "2023-10-20T15:35:00Z"}
            """.trimIndent()
        }
        
        headers {
            header("Cache-Control") {
                schema {
                    type = SchemaType.STRING
                    const = "no-cache"
                }
            }
            header("Connection") {
                schema {
                    type = SchemaType.STRING
                    const = "keep-alive"
                }
            }
        }
    }
}
```

### Batch Response

```kotlin
response("207", "Multi-status") {
    description = "Batch operation results"
    
    jsonContent {
        schema {
            type = SchemaType.OBJECT
            properties {
                "results" to schema {
                    type = SchemaType.ARRAY
                    items = schema {
                        type = SchemaType.OBJECT
                        properties {
                            "id" to schema { 
                                type = SchemaType.STRING 
                                description = "Operation ID from request"
                            }
                            "status" to schema { 
                                type = SchemaType.INTEGER 
                                description = "HTTP status code"
                            }
                            "headers" to schema {
                                type = SchemaType.OBJECT
                                additionalProperties = schema {
                                    type = SchemaType.STRING
                                }
                            }
                            "body" to schema {
                                description = "Response body (any type)"
                            }
                            "error" to schema {
                                ref = "#/components/schemas/Error"
                                nullable = true
                            }
                        }
                        required = listOf("id", "status")
                    }
                }
                "summary" to schema {
                    type = SchemaType.OBJECT
                    properties {
                        "total" to schema { type = SchemaType.INTEGER }
                        "successful" to schema { type = SchemaType.INTEGER }
                        "failed" to schema { type = SchemaType.INTEGER }
                    }
                }
            }
        }
        
        example = mapOf(
            "results" to listOf(
                mapOf(
                    "id" to "op1",
                    "status" to 200,
                    "body" to mapOf("id" to "123", "name" to "Created Item")
                ),
                mapOf(
                    "id" to "op2",
                    "status" to 400,
                    "error" to mapOf(
                        "code" to "VALIDATION_ERROR",
                        "message" to "Invalid data"
                    )
                ),
                mapOf(
                    "id" to "op3",
                    "status" to 201,
                    "headers" to mapOf("Location" to "/items/456"),
                    "body" to mapOf("id" to "456", "name" to "Another Item")
                )
            ),
            "summary" to mapOf(
                "total" to 3,
                "successful" to 2,
                "failed" to 1
            )
        )
    }
}
```

## Best Practices

1. **Use Consistent Parameter Names**: Maintain consistency across your API
2. **Provide Examples**: Include realistic examples for complex parameters
3. **Document Constraints**: Clearly specify validation rules and limits
4. **Use Standard Headers**: Follow HTTP standards for headers
5. **Handle Errors Gracefully**: Provide detailed error information
6. **Version Carefully**: Consider how changes affect existing clients
7. **Optimize for Client Generation**: Design with SDK generation in mind
8. **Include Links**: Use HATEOAS principles where appropriate

## Next Steps

- [Security](security.md) - Authentication and authorization
- [Reusable Components](reusable-components.md) - Building component libraries
- [Advanced Features](advanced-features.md) - Webhooks, callbacks, and more