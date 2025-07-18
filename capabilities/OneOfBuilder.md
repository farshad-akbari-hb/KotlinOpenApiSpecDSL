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
| `schemas` | `MutableList<Schema>` | List of schemas for the oneOf composition |
| `discriminator` | `Discriminator?` | Optional discriminator for polymorphic schemas |

## Key Methods

### Schema Addition Methods

#### `schema(block: SchemaBuilder.() -> Unit)`
Adds an inline schema to the oneOf:

```kotlin
schema {
    type = "object"
    properties {
        property("name", "string")
        property("age", "integer")
    }
}
```

#### `schema(type: String, block: SchemaBuilder.() -> Unit = {})`
Adds a typed schema to the oneOf:

```kotlin
schema("string") {
    minLength = 5
    maxLength = 50
}
```

#### `schemaRef(schemaName: String)`
Adds a schema reference to the oneOf:

```kotlin
schemaRef("User")  // References #/components/schemas/User
```

#### `schemaRef<T>()`
Adds a schema reference using a Kotlin class (with reified type):

```kotlin
schemaRef<User>()  // References the User class schema
```

### Discriminator Method

#### `discriminator(block: DiscriminatorBuilder.() -> Unit)`
Configures a discriminator for the oneOf:

```kotlin
discriminator {
    propertyName = "type"
    mapping("user", "#/components/schemas/User")
    mapping("admin", "#/components/schemas/Admin")
}
```

### Build Method

#### `build(): Schema`
Builds the final Schema object with oneOf composition.

## Usage Examples

### Basic OneOf - Union of Primitive Types

```kotlin
val mixedValue = oneOfBuilder {
    schema("string")
    schema("number")
    schema("boolean")
}
// Validates: "hello", 123, true
// Invalid: null, [1,2,3], {"key": "value"}
```

### OneOf with Objects

```kotlin
val response = oneOfBuilder {
    // Success response
    schema {
        type = "object"
        properties {
            property("success", "boolean") {
                const = JsonPrimitive(true)
            }
            property("data") {
                type = "object"
                additionalProperties { type = "string" }
            }
        }
        required.add("success")
        required.add("data")
    }
    
    // Error response
    schema {
        type = "object"
        properties {
            property("success", "boolean") {
                const = JsonPrimitive(false)
            }
            property("error") {
                type = "object"
                properties {
                    property("code", "string")
                    property("message", "string")
                }
                required.addAll(listOf("code", "message"))
            }
        }
        required.add("success")
        required.add("error")
    }
}
```

### OneOf with Discriminator

```kotlin
val paymentMethod = oneOfBuilder {
    schemaRef("CreditCardPayment")
    schemaRef("BankTransferPayment")
    schemaRef("PayPalPayment")
    
    discriminator {
        propertyName = "paymentType"
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
        type = "object"
        properties {
            property("status", "string") {
                const = JsonPrimitive("success")
            }
            property("data") {
                type = "object"
                additionalProperties { type = "string" }
            }
            property("timestamp", "string") {
                format = "date-time"
            }
        }
        required.addAll(listOf("status", "data", "timestamp"))
    }
    
    schema("ErrorResponse") {
        type = "object"
        properties {
            property("status", "string") {
                const = JsonPrimitive("error")
            }
            property("error") {
                type = "object"
                properties {
                    property("code", "string")
                    property("message", "string")
                    property("details") {
                        type = "array"
                        items { type = "string" }
                    }
                }
                required.addAll(listOf("code", "message"))
            }
            property("timestamp", "string") {
                format = "date-time"
            }
        }
        required.addAll(listOf("status", "error", "timestamp"))
    }
    
    // OneOf composition
    schema("ApiResponse") {
        oneOf = listOf(
            Schema(ref = "#/components/schemas/SuccessResponse"),
            Schema(ref = "#/components/schemas/ErrorResponse")
        )
        discriminator = Discriminator(
            propertyName = "status"
        )
    }
}
```

### OneOf for Nullable Types

```kotlin
// Nullable string implementation using oneOf
val nullableString = oneOfBuilder {
    schema("string")
    schema("null")
}

// More complex nullable object
val nullableUser = oneOfBuilder {
    schemaRef("User")
    schema("null")
}
```

### OneOf for Different Formats

```kotlin
val flexibleDate = oneOfBuilder {
    // Unix timestamp
    schema("integer") {
        description = "Unix timestamp in seconds"
        example = JsonPrimitive(1234567890)
    }
    
    // ISO 8601 string
    schema("string") {
        format = "date-time"
        description = "ISO 8601 date-time string"
        example = JsonPrimitive("2023-01-01T00:00:00Z")
    }
    
    // Date object
    schema {
        type = "object"
        properties {
            property("year", "integer") { minimum = 1900 }
            property("month", "integer") { minimum = 1; maximum = 12 }
            property("day", "integer") { minimum = 1; maximum = 31 }
        }
        required.addAll(listOf("year", "month", "day"))
    }
}
```

### OneOf for Polymorphic Types

```kotlin
// In components
components {
    // Base notification
    schema("Notification") {
        type = "object"
        properties {
            property("id", "string")
            property("type", "string")
            property("timestamp", "string") { format = "date-time" }
        }
        required.addAll(listOf("id", "type", "timestamp"))
    }
    
    // Email notification
    schema("EmailNotification") {
        allOf(
            Schema(ref = "#/components/schemas/Notification"),
            Schema(
                type = "object",
                properties = mapOf(
                    "subject" to Schema(type = "string"),
                    "body" to Schema(type = "string"),
                    "to" to Schema(type = "array", items = Schema(type = "string", format = "email"))
                ),
                required = listOf("subject", "body", "to")
            )
        )
    }
    
    // SMS notification
    schema("SmsNotification") {
        allOf(
            Schema(ref = "#/components/schemas/Notification"),
            Schema(
                type = "object",
                properties = mapOf(
                    "message" to Schema(type = "string", maxLength = 160),
                    "phoneNumber" to Schema(type = "string", pattern = "^\\+[1-9]\\d{1,14}$")
                ),
                required = listOf("message", "phoneNumber")
            )
        )
    }
    
    // Push notification
    schema("PushNotification") {
        allOf(
            Schema(ref = "#/components/schemas/Notification"),
            Schema(
                type = "object",
                properties = mapOf(
                    "title" to Schema(type = "string"),
                    "body" to Schema(type = "string"),
                    "deviceToken" to Schema(type = "string")
                ),
                required = listOf("title", "body", "deviceToken")
            )
        )
    }
    
    // OneOf for all notification types
    schema("AnyNotification") {
        oneOf = listOf(
            Schema(ref = "#/components/schemas/EmailNotification"),
            Schema(ref = "#/components/schemas/SmsNotification"),
            Schema(ref = "#/components/schemas/PushNotification")
        )
        discriminator = Discriminator(
            propertyName = "type",
            mapping = mapOf(
                "email" to "#/components/schemas/EmailNotification",
                "sms" to "#/components/schemas/SmsNotification",
                "push" to "#/components/schemas/PushNotification"
            )
        )
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
    oneOf = listOf(
        Schema(
            type = "object",
            properties = mapOf(
                "ok" to Schema(type = "boolean", const = JsonPrimitive(true)),
                "result" to Schema(type = "object")
            ),
            required = listOf("ok", "result")
        ),
        Schema(
            type = "object",
            properties = mapOf(
                "ok" to Schema(type = "boolean", const = JsonPrimitive(false)),
                "error" to Schema(type = "string")
            ),
            required = listOf("ok", "error")
        )
    )
    discriminator = Discriminator(propertyName = "ok")
}
```

### Flexible Input Pattern

```kotlin
schema("FlexibleId") {
    oneOf = listOf(
        Schema(type = "string", format = "uuid"),
        Schema(type = "integer", minimum = 1)
    )
    description = "ID can be either a UUID string or a positive integer"
}
```

### Type Migration Pattern

```kotlin
schema("UserIdentifier") {
    oneOf = listOf(
        // Legacy format
        Schema(type = "integer", deprecated = true),
        // New format
        Schema(type = "string", format = "uuid")
    )
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