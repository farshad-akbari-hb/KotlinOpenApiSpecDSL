# SchemaBuilder

**Package**: `me.farshad.dsl.builder.schema`  
**File**: `SchemaBuilder.kt`

## Overview

`SchemaBuilder` is the main builder for creating OpenAPI schema definitions. It supports basic JSON Schema features including types, properties, items, schema composition (oneOf, allOf, anyOf, not), discriminators, and examples.

## Class Declaration

```kotlin
class SchemaBuilder
```

## Properties

### Basic Properties

| Property | Type | Description |
|----------|------|-------------|
| `type` | `SchemaType?` | Schema type enum (STRING, NUMBER, INTEGER, BOOLEAN, ARRAY, OBJECT, NULL) |
| `format` | `SchemaFormat?` | Format enum (INT32, INT64, DATE_TIME, EMAIL, PASSWORD, URL) |
| `description` | `String?` | Schema description |
| `example` | `JsonElement?` | Example value |

### Object Properties

| Property | Type | Description |
|----------|------|-------------|
| `properties` | `MutableMap<String, Schema>` | Object properties |
| `required` | `MutableList<String>` | Required property names (internal) |

### Array Properties

| Property | Type | Description |
|----------|------|-------------|
| `items` | `Schema?` | Schema for array items |

### Composition Properties

| Property | Type | Description |
|----------|------|-------------|
| `oneOf` | `List<String>?` | One-of schema composition (backward compatibility) |

Note: The builder internally uses `SchemaReference` for composition but provides backward compatibility for string-based oneOf.

## Key Methods

### Property Methods

#### `property(name: String, type: PropertyType, required: Boolean = false, block: SchemaBuilder.() -> Unit = {})`
Adds a typed property to an object schema:

```kotlin
property("username", PropertyType.STRING, required = true) {
    description = "User's username"
}
```

### Items Configuration

#### `items(block: SchemaBuilder.() -> Unit)`
Configures array item schema:

```kotlin
items {
    type = SchemaType.STRING
}
```

### Example Methods

#### `example(value: Any)`
Sets an example value (automatically converts to JsonElement):

```kotlin
example("john.doe@example.com")
```

#### `examples(block: ExamplesBuilder.() -> Unit)`
Sets multiple named examples:

```kotlin
examples {
    example("valid", "john.doe@example.com")
    example("invalid", "not-an-email")
}
```

### Composition Methods

#### `oneOf(vararg refs: String)`
Creates a one-of composition with string references:

```kotlin
oneOf("User", "Admin")  // Auto-prefixes with #/components/schemas/
```

#### `oneOf(vararg classes: KClass<*>)`
Creates a one-of composition with Kotlin classes:

```kotlin
oneOf(User::class, Admin::class)
```

#### `oneOf(block: OneOfBuilder.() -> Unit)`
Creates a one-of composition using builder:

```kotlin
oneOf {
    schema("User")
    schema(Admin::class)
    schema {
        type = SchemaType.STRING
    }
}
```

#### `allOf(vararg refs: String)`
Creates an all-of composition with string references:

```kotlin
allOf("BaseModel", "Timestamped")
```

#### `allOf(vararg classes: KClass<*>)`
Creates an all-of composition with Kotlin classes:

```kotlin
allOf(BaseModel::class, Timestamped::class)
```

#### `allOf(block: AllOfBuilder.() -> Unit)`
Creates an all-of composition using builder:

```kotlin
allOf {
    schema("BaseModel")
    schema {
        type = SchemaType.OBJECT
        property("extraField", PropertyType.STRING)
    }
}
```

#### `anyOf(vararg refs: String)`
Creates an any-of composition with string references:

```kotlin
anyOf("StringValue", "ArrayValue")
```

#### `anyOf(vararg classes: KClass<*>)`
Creates an any-of composition with Kotlin classes:

```kotlin
anyOf(StringValue::class, ArrayValue::class)
```

#### `anyOf(block: AnyOfBuilder.() -> Unit)`
Creates an any-of composition using builder:

```kotlin
anyOf {
    schema { type = SchemaType.STRING }
    schema { type = SchemaType.NUMBER }
}
```

#### `not(ref: String)`
Creates a not schema with string reference:

```kotlin
not("InvalidSchema")
```

#### `not(clazz: KClass<*>)`
Creates a not schema with Kotlin class:

```kotlin
not(InvalidSchema::class)
```

#### `not(block: SchemaBuilder.() -> Unit)`
Creates a not schema using builder:

```kotlin
not {
    type = SchemaType.STRING
    description = "Must not be a string"
}
```

### Discriminator Method

#### `discriminator(propertyName: String, block: DiscriminatorBuilder.() -> Unit = {})`
Configures discriminator for polymorphic schemas:

```kotlin
discriminator("type") {
    mapping("dog", "#/components/schemas/Dog")
    mapping("cat", Cat::class)
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
    type = SchemaType.STRING
    format = SchemaFormat.EMAIL
    description = "User's email address"
    example("john.doe@example.com")
}

// Number schema
schema {
    type = SchemaType.NUMBER
    description = "Product price"
    example(19.99)
}

// Integer schema
schema {
    type = SchemaType.INTEGER
    format = SchemaFormat.INT32
    description = "User age"
    example(42)
}

// Boolean schema
schema {
    type = SchemaType.BOOLEAN
    description = "Is active flag"
    example(true)
}
```

### Object Schema

```kotlin
schema {
    type = SchemaType.OBJECT
    description = "User account information"
    
    property("id", PropertyType.STRING) {
        format = SchemaFormat.UUID
        description = "Unique user identifier"
    }
    
    property("username", PropertyType.STRING, required = true) {
        description = "Unique username"
    }
    
    property("email", PropertyType.STRING, required = true) {
        format = SchemaFormat.EMAIL
        description = "User's email address"
    }
    
    property("age", PropertyType.INTEGER) {
        description = "User's age"
    }
    
    property("roles", PropertyType.ARRAY, required = true) {
        description = "User roles"
        items {
            type = SchemaType.STRING
        }
    }
    
    property("profile", PropertyType.OBJECT) {
        property("bio", PropertyType.STRING) {
            description = "User biography"
        }
        property("avatar", PropertyType.STRING) {
            format = SchemaFormat.URL
        }
    }
    
    property("createdAt", PropertyType.STRING) {
        format = SchemaFormat.DATE_TIME
        description = "Account creation timestamp"
    }
    
    example(mapOf(
        "id" to "123e4567-e89b-12d3-a456-426614174000",
        "username" to "johndoe",
        "email" to "john@example.com",
        "age" to 25,
        "roles" to listOf("user"),
        "profile" to mapOf(
            "bio" to "Software developer",
            "avatar" to "https://example.com/avatar.jpg"
        )
    ))
}
```

### Array Schema

```kotlin
// Simple array
schema {
    type = SchemaType.ARRAY
    items {
        type = SchemaType.STRING
    }
}

// Array of objects
schema {
    type = SchemaType.ARRAY
    description = "List of products"
    items {
        type = SchemaType.OBJECT
        property("id", PropertyType.INTEGER)
        property("name", PropertyType.STRING, required = true)
        property("price", PropertyType.NUMBER, required = true)
    }
}
```

### Schema Composition Examples

#### OneOf - Union Types

```kotlin
// Using builder DSL
schema {
    oneOf {
        schema {
            type = SchemaType.OBJECT
            property("creditCard", PropertyType.OBJECT, required = true) {
                property("number", PropertyType.STRING, required = true)
                property("cvv", PropertyType.STRING, required = true)
            }
        }
        schema {
            type = SchemaType.OBJECT
            property("bankAccount", PropertyType.OBJECT, required = true) {
                property("accountNumber", PropertyType.STRING, required = true)
                property("routingNumber", PropertyType.STRING, required = true)
            }
        }
    }
    
    discriminator("paymentMethod") {
        mapping("credit", "#/components/schemas/CreditCardPayment")
        mapping("bank", "#/components/schemas/BankPayment")
    }
}

// Using class references
schema {
    oneOf(CreditCardPayment::class, BankPayment::class, PayPalPayment::class)
    discriminator("type")
}
```

#### AllOf - Inheritance

```kotlin
// Using builder DSL
schema {
    allOf {
        schema("BaseEntity")
        schema {
            type = SchemaType.OBJECT
            property("productName", PropertyType.STRING, required = true)
            property("price", PropertyType.NUMBER, required = true)
        }
    }
}

// Using class references
schema {
    allOf(BaseEntity::class, Timestamped::class)
}
```

#### AnyOf - Flexible Types

```kotlin
schema {
    anyOf {
        schema { type = SchemaType.STRING }
        schema { 
            type = SchemaType.ARRAY
            items { type = SchemaType.STRING }
        }
    }
    description = "Can be a single string or array of strings"
}
```

### Complex Object Example

```kotlin
schema {
    type = SchemaType.OBJECT
    description = "Address information"
    
    property("street", PropertyType.STRING, required = true) {
        description = "Street address"
    }
    
    property("city", PropertyType.STRING, required = true) {
        description = "City name"
    }
    
    property("state", PropertyType.STRING, required = true) {
        description = "Two-letter state code"
    }
    
    property("zipCode", PropertyType.STRING, required = true) {
        description = "5-digit ZIP or ZIP+4"
    }
    
    property("country", PropertyType.STRING) {
        description = "Country code"
    }
}
```

## Best Practices

1. **Use appropriate types**: Use the type-safe enums (SchemaType, PropertyType, SchemaFormat) instead of strings.

2. **Add descriptions**: Always include descriptions for documentation.

3. **Provide examples**: Use the `example()` method to provide sample values.

4. **Use type-safe references**: Prefer class references over string references when possible.

5. **Leverage composition builders**: Use oneOf, allOf, anyOf builders for complex schemas.

## Common Patterns

### Pagination Schema

```kotlin
schema("PaginatedResponse") {
    type = SchemaType.OBJECT
    
    property("data", PropertyType.ARRAY, required = true) {
        items {
            // Generic items, specified per use
        }
    }
    
    property("pagination", PropertyType.OBJECT, required = true) {
        property("page", PropertyType.INTEGER, required = true)
        property("pageSize", PropertyType.INTEGER, required = true)
        property("totalPages", PropertyType.INTEGER, required = true)
        property("totalItems", PropertyType.INTEGER, required = true)
    }
}
```

### Error Response Schema

```kotlin
schema("ErrorResponse") {
    type = SchemaType.OBJECT
    
    property("error", PropertyType.OBJECT, required = true) {
        property("code", PropertyType.STRING, required = true) {
            description = "Error code"
            example("VALIDATION_ERROR")
        }
        property("message", PropertyType.STRING, required = true) {
            description = "Human-readable error message"
        }
        property("details", PropertyType.ARRAY) {
            items {
                type = SchemaType.OBJECT
                property("field", PropertyType.STRING)
                property("issue", PropertyType.STRING)
            }
        }
        property("timestamp", PropertyType.STRING, required = true) {
            format = SchemaFormat.DATE_TIME
        }
    }
}
```

### Polymorphic Schema Pattern

```kotlin
// Base schema
schema("Animal") {
    type = SchemaType.OBJECT
    discriminator("species")
    
    property("species", PropertyType.STRING, required = true) {
        description = "Animal species"
    }
    property("name", PropertyType.STRING, required = true)
}

// Derived schemas using composition
schema("Dog") {
    allOf {
        schema("Animal")
        schema {
            type = SchemaType.OBJECT
            property("breed", PropertyType.STRING)
            property("goodBoy", PropertyType.BOOLEAN) {
                example(true)
            }
        }
    }
}

schema("Cat") {
    allOf {
        schema("Animal")
        schema {
            type = SchemaType.OBJECT
            property("breed", PropertyType.STRING)
            property("livesRemaining", PropertyType.INTEGER) {
                example(9)
            }
        }
    }
}
```

## Related Builders

- [OneOfBuilder](OneOfBuilder.md) - For one-of compositions
- [AllOfBuilder](AllOfBuilder.md) - For all-of compositions
- [AnyOfBuilder](AnyOfBuilder.md) - For any-of compositions
- [DiscriminatorBuilder](DiscriminatorBuilder.md) - For discriminator configuration
- [SchemaComposition](SchemaComposition.md) - For advanced type-safe composition patterns