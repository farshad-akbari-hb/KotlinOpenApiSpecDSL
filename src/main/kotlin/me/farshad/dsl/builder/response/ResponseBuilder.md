# ResponseBuilder

**Package**: `me.farshad.dsl.builder.response`  
**File**: `ResponseBuilder.kt`

## Overview

`ResponseBuilder` is responsible for building API response configurations in OpenAPI operations. It defines what data an API endpoint returns, including status codes, headers, content types, and response schemas.

## Class Declaration

```kotlin
class ResponseBuilder
```

## Properties

| Property | Type | Description |
|----------|------|-------------|
| `description` | `String?` | Description of the response (required) |
| `headers` | `MutableMap<String, Header>` | Response headers |
| `content` | `MutableMap<String, MediaType>` | Response content by media type |
| `links` | `MutableMap<String, Link>` | Links to related operations |

## Key Methods

### Header Methods

#### `header(name: String, block: HeaderBuilder.() -> Unit)`
Adds a response header:

```kotlin
header("X-Rate-Limit-Remaining") {
    description = "Number of requests remaining"
    schema {
        type = "integer"
        minimum = 0
    }
}
```

### Content Configuration Methods

#### `content(mediaType: String, block: MediaTypeBuilder.() -> Unit)`
Adds content for a specific media type:

```kotlin
content("application/json") {
    schema {
        ref("User")
    }
    example = jsonObjectOf(
        "id" to "123",
        "name" to "John Doe"
    )
}
```

### Convenience Methods for Common Content Types

#### `jsonContent(block: SchemaBuilder.() -> Unit)`
Adds JSON content with inline schema:

```kotlin
jsonContent {
    type = "object"
    properties {
        property("id", "string")
        property("name", "string")
        property("email", "string")
    }
}
```

#### `jsonContent(schemaRef: String)`
Adds JSON content with schema reference:

```kotlin
jsonContent("UserResponse")  // References #/components/schemas/UserResponse
```

#### `jsonContent(schemaClass: KClass<*>)`
Adds JSON content using a Kotlin class:

```kotlin
jsonContent(UserResponse::class)
```

#### `xmlContent(block: SchemaBuilder.() -> Unit)`
Adds XML content:

```kotlin
xmlContent {
    type = "object"
    xml = Xml(name = "UserResponse")
    properties {
        property("id", "string")
        property("name", "string")
    }
}
```

#### `textContent(block: SchemaBuilder.() -> Unit)`
Adds plain text content:

```kotlin
textContent {
    type = "string"
    example = JsonPrimitive("Operation completed successfully")
}
```

#### `htmlContent(block: SchemaBuilder.() -> Unit)`
Adds HTML content:

```kotlin
htmlContent {
    type = "string"
    example = JsonPrimitive("<h1>Welcome</h1><p>Operation successful</p>")
}
```

### Build Method

#### `build(): Response`
Builds the final `Response` object. Validates that description is not null.

## Usage Examples

### Success Response with Headers

```kotlin
response("200", "User retrieved successfully") {
    jsonContent(User::class)
    
    header("X-Rate-Limit-Limit") {
        description = "Request limit per hour"
        schema {
            type = "integer"
            example = JsonPrimitive(1000)
        }
    }
    
    header("X-Rate-Limit-Remaining") {
        description = "Remaining requests in current window"
        schema {
            type = "integer"
            minimum = 0
        }
    }
    
    header("X-Rate-Limit-Reset") {
        description = "UTC epoch seconds when limit resets"
        schema {
            type = "integer"
        }
    }
}
```

### Paginated Response

```kotlin
response("200", "List of users") {
    jsonContent {
        type = "object"
        properties {
            property("data") {
                type = "array"
                items {
                    ref("User")
                }
            }
            property("pagination") {
                type = "object"
                properties {
                    property("page", "integer") {
                        description = "Current page number"
                        minimum = 1
                    }
                    property("pageSize", "integer") {
                        description = "Items per page"
                        minimum = 1
                        maximum = 100
                    }
                    property("totalPages", "integer") {
                        description = "Total number of pages"
                        minimum = 0
                    }
                    property("totalItems", "integer") {
                        description = "Total number of items"
                        minimum = 0
                    }
                }
                required.addAll(listOf("page", "pageSize", "totalPages", "totalItems"))
            }
            property("links") {
                type = "object"
                properties {
                    property("self", "string") { format = "uri" }
                    property("first", "string") { format = "uri" }
                    property("last", "string") { format = "uri" }
                    property("next", "string") { format = "uri"; nullable = true }
                    property("prev", "string") { format = "uri"; nullable = true }
                }
            }
        }
        required.addAll(listOf("data", "pagination", "links"))
    }
    
    header("X-Total-Count") {
        description = "Total number of items across all pages"
        schema { type = "integer" }
    }
}
```

### Error Response

```kotlin
response("400", "Bad Request") {
    jsonContent {
        type = "object"
        properties {
            property("error") {
                type = "object"
                properties {
                    property("code", "string") {
                        description = "Error code"
                        example = JsonPrimitive("VALIDATION_ERROR")
                    }
                    property("message", "string") {
                        description = "Human-readable error message"
                    }
                    property("timestamp", "string") {
                        format = "date-time"
                        description = "When the error occurred"
                    }
                    property("path", "string") {
                        description = "API path where error occurred"
                    }
                    property("details") {
                        type = "array"
                        description = "Detailed error information"
                        items {
                            type = "object"
                            properties {
                                property("field", "string") {
                                    description = "Field that caused the error"
                                }
                                property("value") {
                                    description = "Invalid value provided"
                                }
                                property("issue", "string") {
                                    description = "Description of the issue"
                                }
                            }
                        }
                    }
                }
                required.addAll(listOf("code", "message", "timestamp"))
            }
            property("traceId", "string") {
                description = "Request trace ID for debugging"
                format = "uuid"
            }
        }
        required.addAll(listOf("error", "traceId"))
        
        example = jsonObjectOf(
            "error" to jsonObjectOf(
                "code" to "VALIDATION_ERROR",
                "message" to "Invalid request data",
                "timestamp" to "2023-10-01T12:00:00Z",
                "path" to "/api/users",
                "details" to jsonArrayOf(
                    jsonObjectOf(
                        "field" to "email",
                        "value" to "not-an-email",
                        "issue" to "Invalid email format"
                    )
                )
            ),
            "traceId" to "550e8400-e29b-41d4-a716-446655440000"
        )
    }
}
```

### Multiple Content Types Response

```kotlin
response("200", "User data in requested format") {
    // JSON format
    jsonContent {
        ref("User")
    }
    
    // XML format
    content("application/xml") {
        schema {
            ref("User")
        }
        example = """
            <User>
                <id>123</id>
                <name>John Doe</name>
                <email>john@example.com</email>
            </User>
        """.trimIndent()
    }
    
    // CSV format
    content("text/csv") {
        schema {
            type = "string"
        }
        example = """
            id,name,email
            123,"John Doe",john@example.com
        """.trimIndent()
    }
    
    // Content negotiation header
    header("Content-Type") {
        description = "Response content type based on Accept header"
        schema {
            type = "string"
            enum = listOf("application/json", "application/xml", "text/csv")
        }
    }
}
```

### File Download Response

```kotlin
response("200", "File download") {
    content("application/octet-stream") {
        schema {
            type = "string"
            format = "binary"
        }
    }
    
    header("Content-Disposition") {
        description = "Attachment with filename"
        schema {
            type = "string"
            example = JsonPrimitive("attachment; filename=\"report.pdf\"")
        }
    }
    
    header("Content-Type") {
        description = "MIME type of the file"
        schema {
            type = "string"
            example = JsonPrimitive("application/pdf")
        }
    }
    
    header("Content-Length") {
        description = "File size in bytes"
        schema {
            type = "integer"
            minimum = 0
        }
    }
}
```

### Streaming Response

```kotlin
response("200", "Server-sent event stream") {
    content("text/event-stream") {
        schema {
            type = "string"
            description = "Server-sent events following SSE protocol"
        }
        example = """
            event: user-update
            data: {"id": "123", "status": "online"}
            
            event: message
            data: {"from": "user123", "text": "Hello!"}
        """.trimIndent()
    }
    
    header("Cache-Control") {
        description = "Disable caching for live stream"
        schema {
            type = "string"
            const = JsonPrimitive("no-cache")
        }
    }
    
    header("Connection") {
        description = "Keep connection alive"
        schema {
            type = "string"
            const = JsonPrimitive("keep-alive")
        }
    }
}
```

### Response with Links (HATEOAS)

```kotlin
response("201", "Resource created") {
    jsonContent {
        type = "object"
        properties {
            property("id", "string") { format = "uuid" }
            property("name", "string")
            property("createdAt", "string") { format = "date-time" }
            property("_links") {
                type = "object"
                properties {
                    property("self") {
                        type = "object"
                        properties {
                            property("href", "string") { format = "uri" }
                        }
                    }
                    property("update") {
                        type = "object"
                        properties {
                            property("href", "string") { format = "uri" }
                            property("method", "string") { const = JsonPrimitive("PUT") }
                        }
                    }
                    property("delete") {
                        type = "object"
                        properties {
                            property("href", "string") { format = "uri" }
                            property("method", "string") { const = JsonPrimitive("DELETE") }
                        }
                    }
                }
            }
        }
    }
    
    // OpenAPI links
    links = mapOf(
        "GetUserById" to Link(
            operationId = "getUserById",
            parameters = mapOf(
                "userId" to "\$response.body#/id"
            ),
            description = "Get this user by ID"
        ),
        "UpdateUser" to Link(
            operationId = "updateUser",
            parameters = mapOf(
                "userId" to "\$response.body#/id"
            ),
            description = "Update this user"
        )
    )
    
    header("Location") {
        description = "URL of the created resource"
        schema {
            type = "string"
            format = "uri"
        }
    }
}
```

### Conditional Response

```kotlin
// 304 Not Modified response
response("304", "Not Modified") {
    description = "Resource has not been modified"
    
    header("ETag") {
        description = "Entity tag for cache validation"
        schema { type = "string" }
    }
    
    header("Cache-Control") {
        description = "Caching directives"
        schema {
            type = "string"
            example = JsonPrimitive("public, max-age=3600")
        }
    }
    
    header("Last-Modified") {
        description = "Last modification timestamp"
        schema {
            type = "string"
            format = "date-time"
        }
    }
    
    // No content for 304 responses
}
```

### Async Operation Response

```kotlin
response("202", "Accepted for processing") {
    jsonContent {
        type = "object"
        properties {
            property("jobId", "string") {
                format = "uuid"
                description = "Unique job identifier"
            }
            property("status", "string") {
                enum = listOf("pending", "processing", "completed", "failed")
                default = JsonPrimitive("pending")
            }
            property("message", "string") {
                description = "Status message"
            }
            property("createdAt", "string") {
                format = "date-time"
            }
            property("estimatedCompletionTime", "string") {
                format = "date-time"
                description = "Estimated completion time"
                nullable = true
            }
            property("_links") {
                type = "object"
                properties {
                    property("status") {
                        type = "object"
                        properties {
                            property("href", "string") {
                                format = "uri"
                                description = "URL to check job status"
                            }
                        }
                    }
                    property("cancel") {
                        type = "object"
                        properties {
                            property("href", "string") { format = "uri" }
                            property("method", "string") { const = JsonPrimitive("DELETE") }
                        }
                    }
                }
            }
        }
        required.addAll(listOf("jobId", "status", "createdAt", "_links"))
    }
    
    header("Location") {
        description = "URL to check job status"
        schema {
            type = "string"
            format = "uri"
        }
    }
}
```

## Best Practices

1. **Always provide descriptions**: Every response must have a meaningful description.

2. **Include all status codes**: Document both success and error responses.

3. **Use appropriate content types**: Match content types to what your API actually returns.

4. **Document headers**: Include all custom headers your API returns.

5. **Provide examples**: Include realistic response examples.

6. **Use schema references**: For complex responses, define schemas in components.

7. **Be consistent**: Use consistent response structures across your API.

## Common Response Patterns

### Standard Success Response

```kotlin
response("200", "Success") {
    jsonContent {
        type = "object"
        properties {
            property("success", "boolean") { const = JsonPrimitive(true) }
            property("data") { ref("ResponseData") }
            property("meta") {
                type = "object"
                additionalProperties { type = "string" }
            }
        }
        required.addAll(listOf("success", "data"))
    }
}
```

### Standard Error Response

```kotlin
response("500", "Internal Server Error") {
    jsonContent {
        type = "object"
        properties {
            property("success", "boolean") { const = JsonPrimitive(false) }
            property("error") {
                type = "object"
                properties {
                    property("code", "string") { example = JsonPrimitive("INTERNAL_ERROR") }
                    property("message", "string")
                    property("details", "string") { nullable = true }
                }
                required.addAll(listOf("code", "message"))
            }
            property("traceId", "string") { format = "uuid" }
        }
        required.addAll(listOf("success", "error", "traceId"))
    }
}
```

### Empty Success Response

```kotlin
response("204", "No Content") {
    // No content body for 204 responses
    
    header("X-Resource-Count") {
        description = "Number of resources affected"
        schema { type = "integer" }
    }
}
```

### Redirect Response

```kotlin
response("301", "Moved Permanently") {
    header("Location") {
        description = "New permanent URL"
        schema {
            type = "string"
            format = "uri"
        }
        required = true
    }
}
```

## Response Status Code Guidelines

- **2xx Success**:
  - 200: OK - General success
  - 201: Created - Resource created
  - 202: Accepted - Async operation started
  - 204: No Content - Success with no body

- **3xx Redirection**:
  - 301: Moved Permanently
  - 302: Found (Temporary redirect)
  - 304: Not Modified

- **4xx Client Error**:
  - 400: Bad Request
  - 401: Unauthorized
  - 403: Forbidden
  - 404: Not Found
  - 409: Conflict
  - 422: Unprocessable Entity

- **5xx Server Error**:
  - 500: Internal Server Error
  - 502: Bad Gateway
  - 503: Service Unavailable

## Related Builders

- [OperationBuilder](../paths/OperationBuilder.md) - Parent builder that uses ResponseBuilder
- [SchemaBuilder](../../../../../../../../capabilities/SchemaBuilder.md) - For defining response schemas
- [RequestBodyBuilder](../../../../../../../../capabilities/RequestBodyBuilder.md) - For defining requests
- [HeaderBuilder](HeaderBuilder.md) - For configuring response headers