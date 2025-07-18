# OneOfBuilder

**Package**: `me.farshad.dsl.builder.schema`  
**File**: `CompositionBuilders.kt`

## Overview

`OneOfBuilder` is used to create `oneOf` schema compositions in OpenAPI. A `oneOf` schema validates against exactly one of the provided schemas - the data must be valid against one and only one of the schemas.

## Class Declaration

```kotlin
class OneOfBuilder
```

## Properties

| Property | Type | Description |
|----------|------|-------------|
| `schemas` | `MutableList<SchemaReference>` | List of schema references for the oneOf composition |

Note: Discriminator configuration is handled at the SchemaBuilder level, not in OneOfBuilder.

## Key Methods

### Schema Addition Methods

#### `schema(ref: String)`
Adds a schema reference by name:

```kotlin
schema("User")  // Auto-prefixes with #/components/schemas/
schema("#/components/schemas/User")  // Full reference path
```

#### `schema(clazz: KClass<*>)`
Adds a schema reference using a Kotlin class:

```kotlin
schema(User::class)  // References #/components/schemas/User
```

#### `schema(block: SchemaBuilder.() -> Unit)`
Adds an inline schema to the oneOf:

```kotlin
schema {
    type = SchemaType.OBJECT
    property("name", PropertyType.STRING)
    property("age", PropertyType.INTEGER)
}
```

### Build Method

#### `build(): List<SchemaReference>`
Returns the list of schema references for use in the parent SchemaBuilder.

## Usage Examples

### Basic OneOf - Union of Primitive Types

```kotlin
schema {
    oneOf {
        schema { type = SchemaType.STRING }
        schema { type = SchemaType.NUMBER }
        schema { type = SchemaType.BOOLEAN }
    }
}
// Validates: "hello", 123, true
// Invalid: null, [1,2,3], {"key": "value"}
```

### OneOf with Objects

```kotlin
schema {
    oneOf {
        // Success response
        schema {
            type = SchemaType.OBJECT
            property("success", PropertyType.BOOLEAN, required = true)
            property("data", PropertyType.OBJECT, required = true)
        }
        
        // Error response
        schema {
            type = SchemaType.OBJECT
            property("success", PropertyType.BOOLEAN, required = true)
            property("error", PropertyType.OBJECT, required = true) {
                property("code", PropertyType.STRING, required = true)
                property("message", PropertyType.STRING, required = true)
            }
        }
    }
}
```

### OneOf with Discriminator

```kotlin
schema {
    oneOf {
        schema("CreditCardPayment")
        schema("BankTransferPayment")
        schema("PayPalPayment")
    }
    
    discriminator("paymentType") {
        mapping("credit_card", "#/components/schemas/CreditCardPayment")
        mapping("bank_transfer", "#/components/schemas/BankTransferPayment")
        mapping("paypal", "#/components/schemas/PayPalPayment")
    }
}
```

### Complex OneOf Example - API Response

```kotlin
components {
    // Define base schemas
    schema("SuccessResponse") {
        type = SchemaType.OBJECT
        property("status", PropertyType.STRING, required = true)
        property("data", PropertyType.OBJECT, required = true)
        property("timestamp", PropertyType.STRING, required = true) {
            format = SchemaFormat.DATE_TIME
        }
    }
    
    schema("ErrorResponse") {
        type = SchemaType.OBJECT
        property("status", PropertyType.STRING, required = true)
        property("error", PropertyType.OBJECT, required = true) {
            property("code", PropertyType.STRING, required = true)
            property("message", PropertyType.STRING, required = true)
            property("details", PropertyType.ARRAY) {
                items { type = SchemaType.STRING }
            }
        }
        property("timestamp", PropertyType.STRING, required = true) {
            format = SchemaFormat.DATE_TIME
        }
    }
    
    // OneOf composition with discriminator
    schema("ApiResponse") {
        oneOf("SuccessResponse", "ErrorResponse")
        discriminator("status")
    }
}
```

### OneOf for Nullable Types

```kotlin
// Nullable string implementation using oneOf
schema {
    oneOf {
        schema { type = SchemaType.STRING }
        schema { type = SchemaType.NULL }
    }
}

// More complex nullable object
schema {
    oneOf {
        schema("User")
        schema { type = SchemaType.NULL }
    }
}
```

### OneOf for Different Formats

```kotlin
schema {
    oneOf {
        // Unix timestamp
        schema {
            type = SchemaType.INTEGER
            description = "Unix timestamp in seconds"
            example(1234567890)
        }
        
        // ISO 8601 string
        schema {
            type = SchemaType.STRING
            format = SchemaFormat.DATE_TIME
            description = "ISO 8601 date-time string"
            example("2023-01-01T00:00:00Z")
        }
        
        // Date object
        schema {
            type = SchemaType.OBJECT
            property("year", PropertyType.INTEGER, required = true)
            property("month", PropertyType.INTEGER, required = true)
            property("day", PropertyType.INTEGER, required = true)
        }
    }
}
```

### OneOf for Polymorphic Types

```kotlin
// Using class references for type-safe composition
schema {
    oneOf(EmailNotification::class, SmsNotification::class, PushNotification::class)
    
    discriminator("type") {
        mapping("email", EmailNotification::class)
        mapping("sms", SmsNotification::class)
        mapping("push", PushNotification::class)
    }
}

// Or using string references
schema("AnyNotification") {
    oneOf("EmailNotification", "SmsNotification", "PushNotification")
    
    discriminator("type") {
        mapping("email", "#/components/schemas/EmailNotification")
        mapping("sms", "#/components/schemas/SmsNotification")
        mapping("push", "#/components/schemas/PushNotification")
    }
}
```

## Best Practices

1. **Use discriminators for objects**: When using oneOf with objects, always include a discriminator for clarity.

2. **Make schemas mutually exclusive**: Ensure schemas in oneOf don't overlap to avoid ambiguity.

3. **Provide clear examples**: Include examples showing valid data for each schema option.

4. **Consider validation order**: List more specific schemas before general ones.

5. **Document the choice**: Explain when/why each schema option should be used.

## Common Patterns

### Success/Error Response Pattern

```kotlin
schema("ApiResult") {
    oneOf {
        schema {
            type = SchemaType.OBJECT
            property("ok", PropertyType.BOOLEAN, required = true)
            property("result", PropertyType.OBJECT, required = true)
        }
        schema {
            type = SchemaType.OBJECT
            property("ok", PropertyType.BOOLEAN, required = true)
            property("error", PropertyType.STRING, required = true)
        }
    }
    discriminator("ok")
}
```

### Flexible Input Pattern

```kotlin
schema("FlexibleId") {
    oneOf {
        schema {
            type = SchemaType.STRING
            format = SchemaFormat.UUID
        }
        schema {
            type = SchemaType.INTEGER
        }
    }
    description = "ID can be either a UUID string or a positive integer"
}
```

### Type Migration Pattern

```kotlin
schema("UserIdentifier") {
    oneOf {
        // Legacy format
        schema { type = SchemaType.INTEGER }
        // New format
        schema {
            type = SchemaType.STRING
            format = SchemaFormat.UUID
        }
    }
    description = "User ID - integers are deprecated, use UUID strings"
}
```

## Validation Behavior

When validating against a oneOf schema:

1. The data is validated against each schema in the list
2. Validation succeeds if exactly ONE schema validates successfully
3. Validation fails if:
   - No schemas match
   - Multiple schemas match
   - Any validation error occurs

Example validation:
```json
// Schema: oneOf [string, number]

"hello"     ✓ Valid (matches string only)
123         ✓ Valid (matches number only)
true        ✗ Invalid (matches neither)
"123"       ✓ Valid (matches string only, not number)
```

## Related Builders

- [SchemaBuilder](SchemaBuilder.md) - Parent builder that uses OneOfBuilder
- [AllOfBuilder](AllOfBuilder.md) - For intersection types (all must match)
- [AnyOfBuilder](AnyOfBuilder.md) - For union types (one or more must match)
- [DiscriminatorBuilder](DiscriminatorBuilder.md) - For configuring discriminators
- [SchemaComposition](SchemaComposition.md) - For advanced type-safe composition patterns