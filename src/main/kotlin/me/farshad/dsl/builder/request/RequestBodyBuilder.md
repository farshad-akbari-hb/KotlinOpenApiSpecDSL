# RequestBodyBuilder

**Package**: `me.farshad.dsl.builder.request`  
**File**: `RequestBodyBuilder.kt`

## Overview

`RequestBodyBuilder` is responsible for building request body configurations in OpenAPI operations. It defines what data can be sent to an API endpoint, supporting multiple content types and schemas.

## Class Declaration

```kotlin
class RequestBodyBuilder
```

## Properties

| Property | Type | Description |
|----------|------|-------------|
| `description` | `String?` | Description of the request body |
| `required` | `Boolean` | Whether the request body is required (default: false) |
| `content` | `MutableMap<String, MediaType>` | Map of content types to media type definitions |

## Key Methods

### Content Configuration Methods

#### `content(mediaType: String, block: MediaTypeBuilder.() -> Unit)`
Adds content for a specific media type:

```kotlin
content("application/json") {
    schema {
        ref("User")
    }
    example = jsonObjectOf(
        "name" to "John Doe",
        "email" to "john@example.com"
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
        property("name", "string")
        property("email", "string") {
            format = "email"
        }
    }
    required.addAll(listOf("name", "email"))
}
```

#### `jsonContent(schemaRef: String)`
Adds JSON content with schema reference:

```kotlin
jsonContent("CreateUserRequest")  // References #/components/schemas/CreateUserRequest
```

#### `jsonContent(schemaClass: KClass<*>)`
Adds JSON content using a Kotlin class:

```kotlin
jsonContent(CreateUserRequest::class)
```

#### `xmlContent(block: SchemaBuilder.() -> Unit)`
Adds XML content:

```kotlin
xmlContent {
    type = "object"
    xml = Xml(name = "User")
    properties {
        property("name", "string")
        property("email", "string")
    }
}
```

#### `formContent(block: SchemaBuilder.() -> Unit)`
Adds form URL-encoded content:

```kotlin
formContent {
    type = "object"
    properties {
        property("username", "string")
        property("password", "string")
        property("rememberMe", "boolean") {
            default = JsonPrimitive(false)
        }
    }
    required.addAll(listOf("username", "password"))
}
```

#### `multipartContent(block: SchemaBuilder.() -> Unit)`
Adds multipart form data content:

```kotlin
multipartContent {
    type = "object"
    properties {
        property("file") {
            type = "string"
            format = "binary"
            description = "File to upload"
        }
        property("description", "string") {
            maxLength = 500
        }
        property("tags") {
            type = "array"
            items { type = "string" }
        }
    }
    required.add("file")
}
```

### Build Method

#### `build(): RequestBody`
Builds the final `RequestBody` object with all configurations.

## Usage Examples

### Basic JSON Request Body

```kotlin
requestBody {
    description = "User registration data"
    required = true
    
    jsonContent {
        type = "object"
        properties {
            property("username", "string") {
                minLength = 3
                maxLength = 30
                pattern = "^[a-zA-Z0-9_]+$"
            }
            property("email", "string") {
                format = "email"
            }
            property("password", "string") {
                minLength = 8
                description = "Must contain letters and numbers"
            }
            property("age", "integer") {
                minimum = 18
                maximum = 120
            }
        }
        required.addAll(listOf("username", "email", "password"))
        
        example = jsonObjectOf(
            "username" to "johndoe",
            "email" to "john@example.com",
            "password" to "SecurePass123",
            "age" to 25
        )
    }
}
```

### Multiple Content Types

```kotlin
requestBody {
    description = "Product data in multiple formats"
    required = true
    
    // JSON format
    jsonContent {
        ref("Product")
        example = jsonObjectOf(
            "name" to "Laptop",
            "price" to 999.99,
            "category" to "Electronics"
        )
    }
    
    // XML format
    content("application/xml") {
        schema {
            ref("Product")
        }
        example = """
            <Product>
                <name>Laptop</name>
                <price>999.99</price>
                <category>Electronics</category>
            </Product>
        """.trimIndent()
    }
    
    // YAML format
    content("application/x-yaml") {
        schema {
            ref("Product")
        }
        example = """
            name: Laptop
            price: 999.99
            category: Electronics
        """.trimIndent()
    }
}
```

### File Upload Request

```kotlin
requestBody {
    description = "File upload with metadata"
    required = true
    
    multipartContent {
        type = "object"
        properties {
            // File part
            property("file") {
                type = "string"
                format = "binary"
                description = "The file to upload"
            }
            
            // Metadata parts
            property("fileName", "string") {
                description = "Original filename"
                maxLength = 255
            }
            
            property("fileType", "string") {
                description = "MIME type"
                enum = listOf(
                    "image/jpeg",
                    "image/png",
                    "application/pdf",
                    "text/plain"
                )
            }
            
            property("description", "string") {
                description = "File description"
                maxLength = 1000
            }
            
            property("tags") {
                type = "array"
                description = "File tags for categorization"
                items {
                    type = "string"
                    maxLength = 50
                }
                maxItems = 10
            }
        }
        required.addAll(listOf("file", "fileName", "fileType"))
    }
}
```

### Form Login Request

```kotlin
requestBody {
    description = "User login credentials"
    required = true
    
    formContent {
        type = "object"
        properties {
            property("username", "string") {
                description = "Username or email"
            }
            property("password", "string") {
                description = "User password"
            }
            property("rememberMe", "boolean") {
                description = "Keep user logged in"
                default = JsonPrimitive(false)
            }
            property("captcha", "string") {
                description = "CAPTCHA response (if required)"
            }
        }
        required.addAll(listOf("username", "password"))
    }
}
```

### Complex Nested Object Request

```kotlin
requestBody {
    description = "Create order request"
    required = true
    
    jsonContent {
        type = "object"
        properties {
            property("customer") {
                type = "object"
                properties {
                    property("id", "string") {
                        format = "uuid"
                    }
                    property("email", "string") {
                        format = "email"
                    }
                    property("name", "string")
                }
                required.addAll(listOf("email", "name"))
            }
            
            property("items") {
                type = "array"
                minItems = 1
                items {
                    type = "object"
                    properties {
                        property("productId", "string")
                        property("quantity", "integer") {
                            minimum = 1
                        }
                        property("price", "number") {
                            description = "Unit price at time of order"
                            minimum = 0
                        }
                    }
                    required.addAll(listOf("productId", "quantity", "price"))
                }
            }
            
            property("shippingAddress") {
                ref("Address")
            }
            
            property("paymentMethod") {
                oneOf = listOf(
                    Schema(ref = "#/components/schemas/CreditCard"),
                    Schema(ref = "#/components/schemas/PayPal"),
                    Schema(ref = "#/components/schemas/BankTransfer")
                )
                discriminator = Discriminator(propertyName = "type")
            }
            
            property("notes", "string") {
                description = "Order notes"
                maxLength = 500
            }
        }
        required.addAll(listOf("customer", "items", "shippingAddress", "paymentMethod"))
    }
}
```

### Request with Examples

```kotlin
requestBody {
    description = "Search query"
    required = true
    
    jsonContent {
        type = "object"
        properties {
            property("query", "string") {
                description = "Search terms"
            }
            property("filters") {
                type = "object"
                additionalProperties {
                    type = "string"
                }
            }
            property("sort") {
                type = "object"
                properties {
                    property("field", "string")
                    property("order", "string") {
                        enum = listOf("asc", "desc")
                    }
                }
            }
            property("pagination") {
                type = "object"
                properties {
                    property("page", "integer") { minimum = 1 }
                    property("limit", "integer") { minimum = 1; maximum = 100 }
                }
            }
        }
        required.add("query")
        
        // Multiple examples
        examples = mapOf(
            "simple" to Example(
                summary = "Simple search",
                value = jsonObjectOf("query" to "laptop")
            ),
            "filtered" to Example(
                summary = "Search with filters",
                value = jsonObjectOf(
                    "query" to "laptop",
                    "filters" to jsonObjectOf(
                        "category" to "electronics",
                        "brand" to "dell"
                    )
                )
            ),
            "full" to Example(
                summary = "Search with all options",
                value = jsonObjectOf(
                    "query" to "laptop",
                    "filters" to jsonObjectOf(
                        "category" to "electronics",
                        "minPrice" to "500",
                        "maxPrice" to "1500"
                    ),
                    "sort" to jsonObjectOf(
                        "field" to "price",
                        "order" to "asc"
                    ),
                    "pagination" to jsonObjectOf(
                        "page" to 1,
                        "limit" to 20
                    )
                )
            )
        )
    }
}
```

### Streaming Request Body

```kotlin
requestBody {
    description = "Video stream upload"
    required = true
    
    content("application/octet-stream") {
        schema {
            type = "string"
            format = "binary"
            description = "Raw video data stream"
        }
    }
    
    content("video/mp4") {
        schema {
            type = "string"
            format = "binary"
            description = "MP4 video file"
        }
    }
}
```

## Best Practices

1. **Always describe request bodies**: Provide clear descriptions of what data is expected.

2. **Mark required appropriately**: Set `required = true` for endpoints that need a body.

3. **Use schema references**: For complex types, define in components and reference.

4. **Provide examples**: Include realistic examples to help API consumers.

5. **Support appropriate content types**: Only include content types your API actually accepts.

6. **Validate thoroughly**: Use schema constraints to validate input data.

## Common Patterns

### PATCH Request Pattern

```kotlin
requestBody {
    description = "Partial update using JSON Patch"
    required = true
    
    content("application/json-patch+json") {
        schema {
            type = "array"
            items {
                type = "object"
                properties {
                    property("op", "string") {
                        enum = listOf("add", "remove", "replace", "move", "copy", "test")
                    }
                    property("path", "string")
                    property("value") {
                        description = "The value to apply"
                    }
                    property("from", "string") {
                        description = "Source path for move/copy operations"
                    }
                }
                required.addAll(listOf("op", "path"))
            }
        }
    }
}
```

### Bulk Operations Pattern

```kotlin
requestBody {
    description = "Bulk operation request"
    required = true
    
    jsonContent {
        type = "object"
        properties {
            property("operations") {
                type = "array"
                items {
                    type = "object"
                    properties {
                        property("action", "string") {
                            enum = listOf("create", "update", "delete")
                        }
                        property("resource", "string")
                        property("data") {
                            description = "Operation-specific data"
                        }
                    }
                    required.allAll(listOf("action", "resource"))
                }
                minItems = 1
                maxItems = 100
            }
        }
        required.add("operations")
    }
}
```

### Conditional Content Pattern

```kotlin
// Different schemas based on a type field
requestBody {
    required = true
    
    jsonContent {
        oneOf = listOf(
            Schema(
                type = "object",
                properties = mapOf(
                    "type" to Schema(type = "string", const = JsonPrimitive("email")),
                    "to" to Schema(type = "string", format = "email"),
                    "subject" to Schema(type = "string"),
                    "body" to Schema(type = "string")
                ),
                required = listOf("type", "to", "subject", "body")
            ),
            Schema(
                type = "object",
                properties = mapOf(
                    "type" to Schema(type = "string", const = JsonPrimitive("sms")),
                    "phoneNumber" to Schema(type = "string"),
                    "message" to Schema(type = "string", maxLength = 160)
                ),
                required = listOf("type", "phoneNumber", "message")
            )
        )
        discriminator = Discriminator(propertyName = "type")
    }
}
```

## Related Builders

- [OperationBuilder](../paths/OperationBuilder.md) - Parent builder that uses RequestBodyBuilder
- [SchemaBuilder](../../../../../../../../capabilities/SchemaBuilder.md) - For defining request schemas
- [ResponseBuilder](../response/ResponseBuilder.md) - For defining responses
- [MediaTypeBuilder](MediaTypeBuilder.md) - For configuring media types