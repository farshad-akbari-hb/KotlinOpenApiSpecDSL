# ResponseBuilder

**Package**: `me.farshad.dsl.builder.response`  
**File**: `ResponseBuilder.kt`

## Overview

`ResponseBuilder` is responsible for building API response configurations in OpenAPI operations. Currently, it only supports JSON content type (`application/json`) and focuses on defining response schemas and examples.

## Class Declaration

```kotlin
class ResponseBuilder(private val description: String)
```

## Constructor Parameters

| Parameter | Type | Description |
|-----------|------|-------------|
| `description` | `String` | Required description of the response |

## Properties

| Property | Type | Description |
|----------|------|-------------|
| `content` | `MutableMap<String, MediaType>` | Response content by media type (internal, currently JSON only) |

## Key Methods

### JSON Content Methods

#### `jsonContent(schemaRef: String? = null, block: SchemaBuilder.() -> Unit = {})`
Adds JSON content with optional schema reference or inline schema:

```kotlin
// With schema reference
jsonContent("UserResponse")

// With inline schema
jsonContent {
    type = SchemaType.OBJECT
    properties {
        "id" to schema {
            type = SchemaType.STRING
        }
        "name" to schema {
            type = SchemaType.STRING
        }
    }
}
```

#### `jsonContent(schemaClass: KClass<*>)`
Adds JSON content using a Kotlin class reference:

```kotlin
jsonContent(UserResponse::class)  // References #/components/schemas/UserResponse
```

#### `jsonContent(schemaClass: KClass<*>, example: Any)`
Adds JSON content with a Kotlin class reference and an example:

```kotlin
jsonContent(UserResponse::class, UserResponse("123", "John Doe"))
```

#### `jsonContent(schemaRef: String? = null, example: Any, block: SchemaBuilder.() -> Unit = {})`
Adds JSON content with schema and example:

```kotlin
jsonContent("UserResponse", mapOf("id" to "123", "name" to "John"))
```

### Example Methods

#### `example(value: Any)`
Sets an example for the JSON content:

```kotlin
jsonContent(UserResponse::class)
example(mapOf(
    "id" to "123",
    "name" to "John Doe",
    "email" to "john@example.com"
))
```

#### `examples(block: ExamplesBuilder.() -> Unit)`
Sets multiple examples for the JSON content:

```kotlin
jsonContent(UserResponse::class)
examples {
    example("success") {
        summary = "Successful response"
        value = mapOf("id" to "123", "name" to "John", "status" to "active")
    }
    example("pending") {
        summary = "Pending user"
        value = mapOf("id" to "456", "name" to "Jane", "status" to "pending")
    }
}
```

### Build Method

#### `build(): Response`
Builds the final `Response` object with the provided description and content.

## Usage Examples

### Basic Success Response

```kotlin
response("200", "User retrieved successfully") {
    jsonContent(User::class)
}
```

### Response with Inline Schema

```kotlin
response("200", "Operation successful") {
    jsonContent {
        type = SchemaType.OBJECT
        properties {
            "message" to schema {
                type = SchemaType.STRING
            }
            "timestamp" to schema {
                type = SchemaType.STRING
                format = SchemaFormat.DATE_TIME
            }
        }
        required = listOf("message", "timestamp")
    }
}
```

### Response with Example

```kotlin
response("201", "User created") {
    jsonContent(User::class, User(
        id = "123",
        name = "John Doe",
        email = "john@example.com"
    ))
}
```

### Paginated Response

```kotlin
response("200", "List of users") {
    jsonContent {
        type = SchemaType.OBJECT
        properties {
            "data" to schema {
                type = SchemaType.ARRAY
                items = Schema(ref = "#/components/schemas/User")
            }
            "pagination" to schema {
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
                        minimum = 0
                    }
                    "totalItems" to schema {
                        type = SchemaType.INTEGER
                        minimum = 0
                    }
                }
                required = listOf("page", "pageSize", "totalPages", "totalItems")
            }
        }
        required = listOf("data", "pagination")
    }
}
```

### Error Response

```kotlin
response("400", "Bad Request") {
    jsonContent {
        type = SchemaType.OBJECT
        properties {
            "error" to schema {
                type = SchemaType.OBJECT
                properties {
                    "code" to schema {
                        type = SchemaType.STRING
                    }
                    "message" to schema {
                        type = SchemaType.STRING
                    }
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
                required = listOf("code", "message")
            }
        }
        required = listOf("error")
    }
}
```

### Response with Multiple Examples

```kotlin
response("200", "Search results") {
    jsonContent {
        type = SchemaType.OBJECT
        properties {
            "results" to schema {
                type = SchemaType.ARRAY
                items = Schema(ref = "#/components/schemas/SearchResult")
            }
            "totalCount" to schema {
                type = SchemaType.INTEGER
            }
        }
    }
    
    examples {
        example("empty") {
            summary = "No results found"
            value = mapOf(
                "results" to emptyList<Any>(),
                "totalCount" to 0
            )
        }
        example("withResults") {
            summary = "Results found"
            value = mapOf(
                "results" to listOf(
                    mapOf("id" to "1", "title" to "First Result"),
                    mapOf("id" to "2", "title" to "Second Result")
                ),
                "totalCount" to 2
            )
        }
    }
}
```

### Empty Response

```kotlin
response("204", "Resource deleted successfully") {
    // No content for 204 responses - constructor description is sufficient
}
```

### Async Operation Response

```kotlin
response("202", "Accepted for processing") {
    jsonContent {
        type = SchemaType.OBJECT
        properties {
            "jobId" to schema {
                type = SchemaType.STRING
                format = SchemaFormat.UUID
            }
            "status" to schema {
                type = SchemaType.STRING
                // Note: enumValues would need JsonPrimitive import
                // enumValues = listOf("pending", "processing", "completed", "failed").map { JsonPrimitive(it) }
            }
            "createdAt" to schema {
                type = SchemaType.STRING
                format = SchemaFormat.DATE_TIME
            }
        }
        required = listOf("jobId", "status", "createdAt")
    }
}
```

## Current Limitations

1. **Only JSON Support**: The current implementation only supports `application/json` content type. Other content types like XML, HTML, plain text, or binary data are not yet implemented.

2. **No Headers Support**: Response headers cannot be defined through the builder, even though they're a common part of API responses.

3. **No Links Support**: OpenAPI links for HATEOAS-style APIs cannot be defined.

4. **String References**: The `jsonContent(schemaRef: String?)` method still uses string references, which doesn't provide compile-time safety.

5. **Limited Content Negotiation**: Cannot specify multiple content types for the same response.

## Best Practices

1. **Always Provide Meaningful Descriptions**: The description parameter is required and should clearly explain what the response represents.

2. **Use Class References When Possible**: Prefer `jsonContent(MyClass::class)` over string references for better type safety.

3. **Include Examples**: Provide realistic examples to help API consumers understand the response format.

4. **Use Schema References for Reusable Types**: Define complex response types in components and reference them.

5. **Document All Response Codes**: Include responses for all possible status codes an operation might return.

## Common Response Patterns

### Standard Success Response

```kotlin
response("200", "Success") {
    jsonContent {
        type = SchemaType.OBJECT
        properties {
            "success" to schema {
                type = SchemaType.BOOLEAN
            }
            "data" to schema {
                ref = "#/components/schemas/ResponseData"
            }
        }
        required = listOf("success", "data")
    }
}
```

### Standard Error Response

```kotlin
response("500", "Internal Server Error") {
    jsonContent {
        type = SchemaType.OBJECT
        properties {
            "success" to schema {
                type = SchemaType.BOOLEAN
            }
            "error" to schema {
                type = SchemaType.OBJECT
                properties {
                    "code" to schema { type = SchemaType.STRING }
                    "message" to schema { type = SchemaType.STRING }
                }
                required = listOf("code", "message")
            }
        }
        required = listOf("success", "error")
    }
}
```

## Response Status Code Guidelines

- **2xx Success**:
  - 200: OK - General success with content
  - 201: Created - Resource created
  - 202: Accepted - Async operation started
  - 204: No Content - Success with no body

- **4xx Client Error**:
  - 400: Bad Request - Invalid request data
  - 401: Unauthorized - Authentication required
  - 403: Forbidden - Insufficient permissions
  - 404: Not Found - Resource doesn't exist
  - 409: Conflict - State conflict
  - 422: Unprocessable Entity - Validation errors

- **5xx Server Error**:
  - 500: Internal Server Error - Unexpected error
  - 502: Bad Gateway - Upstream error
  - 503: Service Unavailable - Temporary unavailability

## Related Components

- [OperationBuilder](../paths/OperationBuilder.md) - Parent builder that uses ResponseBuilder
- [SchemaBuilder](../schema/SchemaBuilder.md) - For defining response schemas
- [RequestBodyBuilder](../request/RequestBodyBuilder.md) - For defining requests
- [ExamplesBuilder](../example/ExamplesBuilder.md) - For adding multiple examples