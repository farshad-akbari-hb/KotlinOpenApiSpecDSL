# SchemaBuilder

**Package**: `me.farshad.dsl.builder.schema`  
**File**: `SchemaBuilder.kt`

## Overview

`SchemaBuilder` is one of the most comprehensive builders in the library, responsible for creating OpenAPI schema definitions. It supports the full JSON Schema specification including types, validation, composition, and more.

## Class Declaration

```kotlin
class SchemaBuilder
```

## Properties

### Basic Properties

| Property | Type | Description |
|----------|------|-------------|
| `type` | `String?` | Schema type (string, number, integer, boolean, array, object, null) |
| `format` | `String?` | Format hint (e.g., date-time, email, uuid, uri, etc.) |
| `title` | `String?` | Schema title |
| `description` | `String?` | Schema description |
| `default` | `JsonElement?` | Default value |
| `example` | `JsonElement?` | Example value |
| `examples` | `List<JsonElement>?` | Multiple example values |

### Type-Specific Properties

| Property | Type | Description |
|----------|------|-------------|
| `nullable` | `Boolean?` | Whether the value can be null |
| `readOnly` | `Boolean?` | Value is read-only (only in responses) |
| `writeOnly` | `Boolean?` | Value is write-only (only in requests) |
| `deprecated` | `Boolean?` | Whether the schema is deprecated |
| `const` | `JsonElement?` | Constant value |
| `enum` | `List<Any>?` | Enumeration of valid values |

### Numeric Validation Properties

| Property | Type | Description |
|----------|------|-------------|
| `minimum` | `Number?` | Minimum value (inclusive by default) |
| `maximum` | `Number?` | Maximum value (inclusive by default) |
| `exclusiveMinimum` | `Boolean?` | Whether minimum is exclusive |
| `exclusiveMaximum` | `Boolean?` | Whether maximum is exclusive |
| `multipleOf` | `Number?` | Value must be multiple of this |

### String Validation Properties

| Property | Type | Description |
|----------|------|-------------|
| `minLength` | `Int?` | Minimum string length |
| `maxLength` | `Int?` | Maximum string length |
| `pattern` | `String?` | Regular expression pattern |

### Array Validation Properties

| Property | Type | Description |
|----------|------|-------------|
| `minItems` | `Int?` | Minimum array length |
| `maxItems` | `Int?` | Maximum array length |
| `uniqueItems` | `Boolean?` | Whether array items must be unique |
| `items` | `Schema?` | Schema for array items |

### Object Validation Properties

| Property | Type | Description |
|----------|------|-------------|
| `properties` | `MutableMap<String, Schema>` | Object properties |
| `required` | `MutableList<String>` | Required property names |
| `additionalProperties` | `Schema?` | Schema for additional properties |
| `minProperties` | `Int?` | Minimum number of properties |
| `maxProperties` | `Int?` | Maximum number of properties |

### Composition Properties

| Property | Type | Description |
|----------|------|-------------|
| `oneOf` | `List<Schema>?` | One-of schema composition |
| `allOf` | `List<Schema>?` | All-of schema composition |
| `anyOf` | `List<Schema>?` | Any-of schema composition |
| `not` | `Schema?` | Not schema (negation) |
| `discriminator` | `Discriminator?` | Discriminator for polymorphism |

### Reference Properties

| Property | Type | Description |
|----------|------|-------------|
| `ref` | `String?` | Reference to another schema |

## Key Methods

### Property Methods

#### `property(name: String, block: SchemaBuilder.() -> Unit)`
Adds a property to an object schema:

```kotlin
property("username") {
    type = "string"
    minLength = 3
    maxLength = 20
    pattern = "^[a-zA-Z0-9_]+$"
}
```

#### `property(name: String, type: String, block: SchemaBuilder.() -> Unit = {})`
Adds a typed property:

```kotlin
property("age", "integer") {
    minimum = 0
    maximum = 150
}
```

### Items Configuration

#### `items(block: SchemaBuilder.() -> Unit)`
Configures array item schema:

```kotlin
items {
    type = "string"
    enum = listOf("red", "green", "blue")
}
```

### Additional Properties

#### `additionalProperties(block: SchemaBuilder.() -> Unit)`
Configures additional properties for objects:

```kotlin
additionalProperties {
    type = "string"
}
```

### Reference Methods

#### `ref(schemaName: String)`
References another schema:

```kotlin
ref("User")  // Creates reference to #/components/schemas/User
```

### Composition Methods

#### `oneOf(vararg schemas: Schema)`
Creates a one-of composition:

```kotlin
oneOf(
    Schema(type = "string"),
    Schema(type = "number")
)
```

#### `allOf(vararg schemas: Schema)`
Creates an all-of composition:

```kotlin
allOf(
    Schema(ref = "#/components/schemas/BaseModel"),
    Schema(properties = mapOf("extraField" to Schema(type = "string")))
)
```

#### `anyOf(vararg schemas: Schema)`
Creates an any-of composition:

```kotlin
anyOf(
    Schema(type = "string"),
    Schema(type = "array", items = Schema(type = "string"))
)
```

### Discriminator Method

#### `discriminator(block: DiscriminatorBuilder.() -> Unit)`
Configures discriminator for polymorphic schemas:

```kotlin
discriminator {
    propertyName = "type"
    mapping("dog", "#/components/schemas/Dog")
    mapping("cat", "#/components/schemas/Cat")
}
```

### Build Method

#### `build(): Schema`
Builds the final `Schema` object with all configurations.

## Usage Examples

### Basic Type Schemas

```kotlin
// String schema
schema {
    type = "string"
    minLength = 1
    maxLength = 100
    pattern = "^[A-Za-z ]+$"
    example = JsonPrimitive("John Doe")
}

// Number schema
schema {
    type = "number"
    format = "double"
    minimum = 0.0
    maximum = 999.99
    multipleOf = 0.01
    example = JsonPrimitive(19.99)
}

// Integer schema
schema {
    type = "integer"
    format = "int32"
    minimum = 1
    maximum = 100
    example = JsonPrimitive(42)
}

// Boolean schema
schema {
    type = "boolean"
    default = JsonPrimitive(false)
}
```

### Object Schema

```kotlin
schema {
    type = "object"
    title = "User"
    description = "User account information"
    
    property("id") {
        type = "string"
        format = "uuid"
        readOnly = true
        description = "Unique user identifier"
    }
    
    property("username") {
        type = "string"
        minLength = 3
        maxLength = 30
        pattern = "^[a-zA-Z0-9_-]+$"
        description = "Unique username"
    }
    
    property("email") {
        type = "string"
        format = "email"
        description = "User's email address"
    }
    
    property("age") {
        type = "integer"
        minimum = 18
        maximum = 120
        description = "User's age"
    }
    
    property("roles") {
        type = "array"
        description = "User roles"
        items {
            type = "string"
            enum = listOf("admin", "user", "moderator")
        }
        minItems = 1
        uniqueItems = true
    }
    
    property("profile") {
        type = "object"
        properties {
            property("bio", "string") {
                maxLength = 500
            }
            property("avatar", "string") {
                format = "uri"
            }
            property("preferences") {
                type = "object"
                additionalProperties {
                    type = "boolean"
                }
            }
        }
    }
    
    property("createdAt") {
        type = "string"
        format = "date-time"
        readOnly = true
    }
    
    required.addAll(listOf("username", "email", "roles"))
    
    example = jsonObjectOf(
        "id" to "123e4567-e89b-12d3-a456-426614174000",
        "username" to "johndoe",
        "email" to "john@example.com",
        "age" to 25,
        "roles" to jsonArrayOf("user"),
        "profile" to jsonObjectOf(
            "bio" to "Software developer",
            "avatar" to "https://example.com/avatar.jpg"
        )
    )
}
```

### Array Schema

```kotlin
// Simple array
schema {
    type = "array"
    items {
        type = "string"
    }
    minItems = 1
    maxItems = 10
    uniqueItems = true
}

// Array of objects
schema {
    type = "array"
    description = "List of products"
    items {
        type = "object"
        properties {
            property("id", "integer")
            property("name", "string")
            property("price", "number") {
                minimum = 0
            }
        }
        required.add("name")
        required.add("price")
    }
}
```

### Enum Schema

```kotlin
// String enum
schema {
    type = "string"
    enum = listOf("pending", "processing", "completed", "failed")
    default = JsonPrimitive("pending")
}

// Numeric enum
schema {
    type = "integer"
    enum = listOf(1, 2, 3, 5, 8, 13, 21)
    description = "Fibonacci numbers"
}
```

### Nullable Schema

```kotlin
schema {
    type = "string"
    format = "date"
    nullable = true
    description = "Optional date field"
}
```

### Schema Composition Examples

#### OneOf - Union Types

```kotlin
schema {
    oneOf(
        Schema(
            type = "object",
            properties = mapOf(
                "creditCard" to Schema(
                    type = "object",
                    properties = mapOf(
                        "number" to Schema(type = "string"),
                        "cvv" to Schema(type = "string")
                    )
                )
            ),
            required = listOf("creditCard")
        ),
        Schema(
            type = "object",
            properties = mapOf(
                "bankAccount" to Schema(
                    type = "object",
                    properties = mapOf(
                        "accountNumber" to Schema(type = "string"),
                        "routingNumber" to Schema(type = "string")
                    )
                )
            ),
            required = listOf("bankAccount")
        )
    )
    
    discriminator {
        propertyName = "paymentMethod"
    }
}
```

#### AllOf - Inheritance

```kotlin
schema {
    allOf(
        Schema(ref = "#/components/schemas/BaseEntity"),
        Schema(
            type = "object",
            properties = mapOf(
                "productName" to Schema(type = "string"),
                "price" to Schema(type = "number", minimum = 0)
            ),
            required = listOf("productName", "price")
        )
    )
}
```

#### AnyOf - Flexible Types

```kotlin
schema {
    anyOf(
        Schema(type = "string"),
        Schema(type = "array", items = Schema(type = "string"))
    )
    description = "Can be a single string or array of strings"
}
```

### Complex Validation Example

```kotlin
schema {
    type = "object"
    title = "Address"
    
    property("street") {
        type = "string"
        minLength = 1
        maxLength = 100
    }
    
    property("city") {
        type = "string"
        minLength = 1
        maxLength = 50
    }
    
    property("state") {
        type = "string"
        pattern = "^[A-Z]{2}$"
        description = "Two-letter state code"
    }
    
    property("zipCode") {
        type = "string"
        pattern = "^\\d{5}(-\\d{4})?$"
        description = "5-digit ZIP or ZIP+4"
    }
    
    property("country") {
        type = "string"
        enum = listOf("US", "CA", "MX")
        default = JsonPrimitive("US")
    }
    
    required.addAll(listOf("street", "city", "state", "zipCode"))
    
    // Conditional validation using dependencies
    additionalProperties = Schema(const = JsonPrimitive(false))
}
```

### Schema with External Documentation

```kotlin
schema {
    type = "object"
    title = "GeoJSON Point"
    description = "A GeoJSON Point geometry"
    externalDocs = ExternalDocumentation(
        url = "https://tools.ietf.org/html/rfc7946#section-3.1.2",
        description = "GeoJSON Point specification"
    )
    
    property("type") {
        type = "string"
        const = JsonPrimitive("Point")
    }
    
    property("coordinates") {
        type = "array"
        items {
            type = "number"
        }
        minItems = 2
        maxItems = 3
        description = "[longitude, latitude, elevation?]"
    }
    
    required.addAll(listOf("type", "coordinates"))
}
```

## Best Practices

1. **Use appropriate types**: Choose the most specific type for your data.

2. **Add descriptions**: Always include descriptions for complex schemas.

3. **Set validation constraints**: Use min/max, pattern, etc. to enforce data quality.

4. **Provide examples**: Include realistic examples to aid understanding.

5. **Use references**: For reusable schemas, define in components and reference.

6. **Consider nullability**: Be explicit about nullable fields.

7. **Mark read/write only fields**: Use readOnly for generated fields, writeOnly for passwords.

## Common Patterns

### Pagination Schema

```kotlin
schema("PaginatedResponse") {
    type = "object"
    
    property("data") {
        type = "array"
        items {
            // Generic items, specified per use
        }
    }
    
    property("pagination") {
        type = "object"
        properties {
            property("page", "integer") { minimum = 1 }
            property("pageSize", "integer") { minimum = 1; maximum = 100 }
            property("totalPages", "integer") { readOnly = true }
            property("totalItems", "integer") { readOnly = true }
        }
        required.addAll(listOf("page", "pageSize", "totalPages", "totalItems"))
    }
    
    required.addAll(listOf("data", "pagination"))
}
```

### Error Response Schema

```kotlin
schema("ErrorResponse") {
    type = "object"
    
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
            property("details") {
                type = "array"
                items {
                    type = "object"
                    properties {
                        property("field", "string")
                        property("issue", "string")
                    }
                }
            }
            property("timestamp", "string") {
                format = "date-time"
            }
        }
        required.addAll(listOf("code", "message", "timestamp"))
    }
    
    required.add("error")
}
```

### Polymorphic Schema Pattern

```kotlin
// Base schema
schema("Animal") {
    type = "object"
    discriminator {
        propertyName = "species"
    }
    
    property("species", "string") {
        description = "Animal species"
    }
    property("name", "string")
    
    required.addAll(listOf("species", "name"))
}

// Derived schemas
schema("Dog") {
    allOf(
        Schema(ref = "#/components/schemas/Animal"),
        Schema(
            type = "object",
            properties = mapOf(
                "breed" to Schema(type = "string"),
                "goodBoy" to Schema(type = "boolean", default = JsonPrimitive(true))
            )
        )
    )
}

schema("Cat") {
    allOf(
        Schema(ref = "#/components/schemas/Animal"),
        Schema(
            type = "object",
            properties = mapOf(
                "breed" to Schema(type = "string"),
                "livesRemaining" to Schema(
                    type = "integer",
                    minimum = 0,
                    maximum = 9,
                    default = JsonPrimitive(9)
                )
            )
        )
    )
}
```

## Related Builders

- [ComponentsBuilder](../components/ComponentsBuilder.md) - For managing reusable schemas
- [OneOfBuilder](OneOfBuilder.md) - For one-of compositions
- [AllOfBuilder](AllOfBuilder.md) - For all-of compositions
- [AnyOfBuilder](AnyOfBuilder.md) - For any-of compositions
- [DiscriminatorBuilder](DiscriminatorBuilder.md) - For discriminator configuration