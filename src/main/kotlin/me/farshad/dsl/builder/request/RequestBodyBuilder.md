# RequestBodyBuilder

**Package**: `me.farshad.dsl.builder.request`  
**File**: `RequestBodyBuilder.kt`

## Overview

`RequestBodyBuilder` is responsible for building request body configurations in OpenAPI operations. Currently, it only supports JSON content type (`application/json`) with various methods for defining schemas.

## Class Declaration

```kotlin
class RequestBodyBuilder
```

## Properties

| Property | Type | Description |
|----------|------|-------------|
| `description` | `String?` | Description of the request body |
| `required` | `Boolean` | Whether the request body is required (default: false) |
| `content` | `MutableMap<String, MediaType>` | Map of content types to media type definitions (internal) |

## Key Methods

### JSON Content Methods

#### `jsonContent(schemaRef: String? = null, block: SchemaBuilder.() -> Unit = {})`
Adds JSON content with optional schema reference or inline schema:

```kotlin
// With schema reference
jsonContent("User")

// With inline schema
jsonContent {
    type = SchemaType.OBJECT
    properties {
        "name" to schema {
            type = SchemaType.STRING
        }
        "email" to schema {
            type = SchemaType.STRING
            format = SchemaFormat.EMAIL
        }
    }
    required = listOf("name", "email")
}
```

#### `jsonContent(schemaClass: KClass<*>)`
Adds JSON content using a Kotlin class reference:

```kotlin
jsonContent(User::class)  // References #/components/schemas/User
```

#### `jsonContent(schemaClass: KClass<*>, example: Any)`
Adds JSON content with a Kotlin class reference and an example:

```kotlin
jsonContent(User::class, User("John Doe", "john@example.com"))
```

#### `jsonContent(schemaRef: String? = null, example: Any, block: SchemaBuilder.() -> Unit = {})`
Adds JSON content with schema and example:

```kotlin
jsonContent("User", mapOf("name" to "John", "email" to "john@example.com"))
```

### Example Methods

#### `example(value: Any)`
Sets an example for the JSON content:

```kotlin
jsonContent(User::class)
example(mapOf(
    "name" to "John Doe",
    "email" to "john@example.com"
))
```

#### `examples(block: ExamplesBuilder.() -> Unit)`
Sets multiple examples for the JSON content:

```kotlin
jsonContent(User::class)
examples {
    example("user1") {
        summary = "Standard user"
        value = mapOf("name" to "John", "email" to "john@example.com")
    }
    example("admin") {
        summary = "Admin user"
        value = mapOf("name" to "Admin", "email" to "admin@example.com", "role" to "admin")
    }
}
```

### Build Method

#### `build(): RequestBody`
Builds the final `RequestBody` object with all configurations.

## Usage Examples

### Basic JSON Request Body with Schema Reference

```kotlin
requestBody {
    description = "User registration data"
    required = true
    jsonContent("CreateUserRequest")
}
```

### JSON Request Body with Kotlin Class

```kotlin
requestBody {
    description = "User data"
    required = true
    jsonContent(User::class)
}
```

### JSON Request Body with Inline Schema

```kotlin
requestBody {
    description = "Login credentials"
    required = true
    
    jsonContent {
        type = SchemaType.OBJECT
        properties {
            "username" to schema {
                type = SchemaType.STRING
                minLength = 3
                maxLength = 30
            }
            "password" to schema {
                type = SchemaType.STRING
                minLength = 8
                description = "Must contain letters and numbers"
            }
        }
        required = listOf("username", "password")
    }
}
```

### Request Body with Example

```kotlin
requestBody {
    description = "Product data"
    required = true
    
    jsonContent(Product::class, Product(
        name = "Laptop",
        price = 999.99,
        category = "Electronics"
    ))
}
```

### Request Body with Multiple Examples

```kotlin
requestBody {
    description = "Search query"
    required = true
    
    jsonContent {
        type = SchemaType.OBJECT
        properties {
            "query" to schema {
                type = SchemaType.STRING
                description = "Search terms"
            }
            "filters" to schema {
                type = SchemaType.OBJECT
                additionalProperties = Schema(type = SchemaType.STRING)
            }
        }
        required = listOf("query")
    }
    
    examples {
        example("simple") {
            summary = "Simple search"
            value = mapOf("query" to "laptop")
        }
        example("filtered") {
            summary = "Search with filters"
            value = mapOf(
                "query" to "laptop",
                "filters" to mapOf(
                    "category" to "electronics",
                    "brand" to "dell"
                )
            )
        }
    }
}
```

### Complex Nested Object Request

```kotlin
requestBody {
    description = "Create order request"
    required = true
    
    jsonContent {
        type = SchemaType.OBJECT
        properties {
            "customer" to schema {
                type = SchemaType.OBJECT
                properties {
                    "id" to schema {
                        type = SchemaType.STRING
                        format = SchemaFormat.UUID
                    }
                    "email" to schema {
                        type = SchemaType.STRING
                        format = SchemaFormat.EMAIL
                    }
                    "name" to schema {
                        type = SchemaType.STRING
                    }
                }
                required = listOf("email", "name")
            }
            
            "items" to schema {
                type = SchemaType.ARRAY
                minItems = 1
                items = Schema(
                    type = SchemaType.OBJECT,
                    properties = mapOf(
                        "productId" to Schema(type = SchemaType.STRING),
                        "quantity" to Schema(
                            type = SchemaType.INTEGER,
                            minimum = 1
                        ),
                        "price" to Schema(
                            type = SchemaType.NUMBER,
                            minimum = 0
                        )
                    ),
                    required = listOf("productId", "quantity", "price")
                )
            }
            
            "shippingAddress" to schema {
                ref = "#/components/schemas/Address"
            }
        }
        required = listOf("customer", "items", "shippingAddress")
    }
}
```

## Current Limitations

1. **Only JSON Support**: The current implementation only supports `application/json` content type. Other content types like XML, form data, or multipart are not yet implemented.

2. **String References**: The `jsonContent(schemaRef: String?)` method still uses string references, which doesn't provide compile-time safety.

3. **No MediaType Builder**: Direct MediaType configuration is not exposed through a builder pattern.

4. **Limited Content Negotiation**: Cannot specify multiple content types for the same request body.

## Best Practices

1. **Use Class References When Possible**: Prefer `jsonContent(MyClass::class)` over string references for better type safety.

2. **Provide Examples**: Include realistic examples to help API consumers understand the expected data format.

3. **Set Required Appropriately**: Mark request bodies as required when the endpoint cannot function without them.

4. **Use Schema References for Reusable Types**: Define complex types in components and reference them rather than repeating inline schemas.

## Related Components

- [OperationBuilder](../paths/OperationBuilder.md) - Parent builder that uses RequestBodyBuilder
- [SchemaBuilder](../schema/SchemaBuilder.md) - For defining request schemas
- [ResponseBuilder](../response/ResponseBuilder.md) - For defining responses
- [ExamplesBuilder](../example/ExamplesBuilder.md) - For adding multiple examples