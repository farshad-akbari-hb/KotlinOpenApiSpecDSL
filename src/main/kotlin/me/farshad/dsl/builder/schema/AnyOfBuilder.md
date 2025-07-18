# AnyOfBuilder

**Package**: `me.farshad.dsl.builder.schema`  
**File**: `CompositionBuilders.kt`

## Overview

`AnyOfBuilder` is used to create `anyOf` schema compositions in OpenAPI. An `anyOf` schema validates data against one or more of the provided schemas - the data must be valid against at least one schema in the list, but can match multiple schemas.

## Class Declaration

```kotlin
class AnyOfBuilder
```

## Properties

| Property | Type | Description |
|----------|------|-------------|
| `schemas` | `MutableList<Schema>` | List of schemas where at least one must be satisfied |

## Key Methods

### Schema Addition Methods

#### `schema(block: SchemaBuilder.() -> Unit)`
Adds an inline schema to the anyOf:

```kotlin
schema {
    type = "string"
    format = "email"
}
```

#### `schema(type: String, block: SchemaBuilder.() -> Unit = {})`
Adds a typed schema to the anyOf:

```kotlin
schema("array") {
    items {
        type = "string"
    }
}
```

#### `schemaRef(schemaName: String)`
Adds a schema reference to the anyOf:

```kotlin
schemaRef("EmailAddress")  // References #/components/schemas/EmailAddress
```

#### `schemaRef<T>()`
Adds a schema reference using a Kotlin class (with reified type):

```kotlin
schemaRef<Address>()  // References the Address class schema
```

### Build Method

#### `build(): Schema`
Builds the final Schema object with anyOf composition.

## Usage Examples

### Flexible Input Types

```kotlin
// Accept single value or array of values
val flexibleStringInput = anyOfBuilder {
    // Single string
    schema("string")
    
    // Array of strings
    schema("array") {
        items {
            type = "string"
        }
    }
}
// Valid: "hello", ["hello", "world"], ["single"]
```

### Multiple Valid Formats

```kotlin
schema("ContactInfo") {
    anyOf = listOf(
        // Email format
        Schema(
            type = "object",
            properties = mapOf(
                "type" to Schema(type = "string", const = JsonPrimitive("email")),
                "value" to Schema(type = "string", format = "email")
            ),
            required = listOf("type", "value")
        ),
        
        // Phone format
        Schema(
            type = "object",
            properties = mapOf(
                "type" to Schema(type = "string", const = JsonPrimitive("phone")),
                "value" to Schema(type = "string", pattern = "^\\+?[1-9]\\d{1,14}$")
            ),
            required = listOf("type", "value")
        ),
        
        // Address format
        Schema(
            type = "object",
            properties = mapOf(
                "type" to Schema(type = "string", const = JsonPrimitive("address")),
                "street" to Schema(type = "string"),
                "city" to Schema(type = "string"),
                "country" to Schema(type = "string")
            ),
            required = listOf("type", "street", "city", "country")
        )
    )
}
```

### Optional Properties Pattern

```kotlin
// User with optional extended profile
schema("UserProfile") {
    anyOf = listOf(
        // Basic user
        Schema(
            type = "object",
            properties = mapOf(
                "username" to Schema(type = "string"),
                "email" to Schema(type = "string", format = "email")
            ),
            required = listOf("username", "email")
        ),
        
        // User with profile
        Schema(
            type = "object",
            properties = mapOf(
                "username" to Schema(type = "string"),
                "email" to Schema(type = "string", format = "email"),
                "profile" to Schema(
                    type = "object",
                    properties = mapOf(
                        "bio" to Schema(type = "string"),
                        "avatar" to Schema(type = "string", format = "uri"),
                        "location" to Schema(type = "string")
                    )
                )
            ),
            required = listOf("username", "email", "profile")
        )
    )
}
```

### Backward Compatibility

```kotlin
// Supporting old and new API formats
schema("ApiRequest") {
    anyOf = listOf(
        // New format (preferred)
        Schema(
            type = "object",
            properties = mapOf(
                "version" to Schema(type = "string", const = JsonPrimitive("2.0")),
                "data" to Schema(
                    type = "object",
                    properties = mapOf(
                        "items" to Schema(type = "array", items = Schema(ref = "#/components/schemas/Item")),
                        "metadata" to Schema(type = "object")
                    )
                )
            ),
            required = listOf("version", "data")
        ),
        
        // Legacy format (deprecated but supported)
        Schema(
            type = "object",
            properties = mapOf(
                "items" to Schema(type = "array", items = Schema(ref = "#/components/schemas/LegacyItem")),
                "meta" to Schema(type = "string")
            ),
            required = listOf("items"),
            deprecated = true
        )
    )
}
```

### Conditional Requirements

```kotlin
// Payment method with different requirements
schema("PaymentDetails") {
    anyOf = listOf(
        // Credit card requires CVV
        Schema(
            type = "object",
            properties = mapOf(
                "method" to Schema(type = "string", const = JsonPrimitive("credit_card")),
                "cardNumber" to Schema(type = "string"),
                "cvv" to Schema(type = "string"),
                "expiryDate" to Schema(type = "string")
            ),
            required = listOf("method", "cardNumber", "cvv", "expiryDate")
        ),
        
        // Bank transfer doesn't require CVV
        Schema(
            type = "object",
            properties = mapOf(
                "method" to Schema(type = "string", const = JsonPrimitive("bank_transfer")),
                "accountNumber" to Schema(type = "string"),
                "routingNumber" to Schema(type = "string")
            ),
            required = listOf("method", "accountNumber", "routingNumber")
        ),
        
        // PayPal only needs email
        Schema(
            type = "object",
            properties = mapOf(
                "method" to Schema(type = "string", const = JsonPrimitive("paypal")),
                "email" to Schema(type = "string", format = "email")
            ),
            required = listOf("method", "email")
        )
    )
}
```

### Mixed Type Values

```kotlin
// Configuration value that can be various types
schema("ConfigValue") {
    anyOf = listOf(
        // Boolean flag
        Schema(type = "boolean"),
        
        // Numeric value
        Schema(type = "number"),
        
        // String value
        Schema(type = "string"),
        
        // Array of strings
        Schema(
            type = "array",
            items = Schema(type = "string")
        ),
        
        // Key-value object
        Schema(
            type = "object",
            additionalProperties = Schema(type = "string")
        )
    )
}
```

### Partial Updates

```kotlin
// Update request that can update different field combinations
schema("UserUpdate") {
    anyOf = listOf(
        // Update profile only
        Schema(
            type = "object",
            properties = mapOf(
                "profile" to Schema(ref = "#/components/schemas/Profile")
            ),
            required = listOf("profile")
        ),
        
        // Update credentials only
        Schema(
            type = "object",
            properties = mapOf(
                "password" to Schema(type = "string", minLength = 8),
                "passwordConfirm" to Schema(type = "string")
            ),
            required = listOf("password", "passwordConfirm")
        ),
        
        // Update preferences only
        Schema(
            type = "object",
            properties = mapOf(
                "preferences" to Schema(
                    type = "object",
                    additionalProperties = Schema(type = "boolean")
                )
            ),
            required = listOf("preferences")
        ),
        
        // Update any combination
        Schema(
            type = "object",
            properties = mapOf(
                "profile" to Schema(ref = "#/components/schemas/Profile"),
                "password" to Schema(type = "string", minLength = 8),
                "passwordConfirm" to Schema(type = "string"),
                "preferences" to Schema(type = "object")
            ),
            minProperties = 1
        )
    )
}
```

### Localized Content

```kotlin
// Content that can be a simple string or localized object
schema("LocalizableText") {
    anyOf = listOf(
        // Simple string (default language)
        Schema(type = "string"),
        
        // Localized strings
        Schema(
            type = "object",
            properties = mapOf(
                "en" to Schema(type = "string"),
                "es" to Schema(type = "string"),
                "fr" to Schema(type = "string"),
                "de" to Schema(type = "string")
            ),
            minProperties = 1,
            additionalProperties = Schema(type = "string")
        )
    )
    
    examples = listOf(
        JsonPrimitive("Hello World"),
        jsonObjectOf(
            "en" to "Hello World",
            "es" to "Hola Mundo",
            "fr" to "Bonjour le monde"
        )
    )
}
```

## Best Practices

1. **Use for flexibility**: AnyOf is ideal when multiple valid formats exist.

2. **Order by preference**: List preferred schemas first.

3. **Avoid too much overlap**: Schemas should be distinct enough to avoid confusion.

4. **Document the options**: Clearly explain when to use each schema variant.

5. **Consider validation complexity**: Too many options can make validation errors hard to understand.

## Common Patterns

### Progressive Enhancement Pattern

```kotlin
// Basic required fields with optional enhancements
schema("Feature") {
    anyOf = listOf(
        schemaRef("BasicFeature"),
        schemaRef("EnhancedFeature"),
        schemaRef("PremiumFeature")
    )
}
```

### Format Migration Pattern

```kotlin
// Supporting multiple API versions
schema("Request") {
    anyOf = listOf(
        schemaRef("RequestV3"),    // Current
        schemaRef("RequestV2"),    // Legacy
        schemaRef("RequestV1")     // Deprecated
    )
}
```

### Multi-Format Input Pattern

```kotlin
// Accept data in multiple formats
schema("DateInput") {
    anyOf = listOf(
        // ISO string
        Schema(type = "string", format = "date-time"),
        
        // Unix timestamp
        Schema(type = "integer"),
        
        // Structured object
        Schema(
            type = "object",
            properties = mapOf(
                "year" to Schema(type = "integer"),
                "month" to Schema(type = "integer"),
                "day" to Schema(type = "integer")
            )
        )
    )
}
```

## Validation Behavior

When validating against an anyOf schema:

1. The data is validated against each schema in the list
2. Validation succeeds if at least ONE schema validates successfully
3. Unlike oneOf, multiple schemas can match
4. Validation fails only if NO schemas match

Example validation:
```json
// Schema: anyOf [
//   {type: "string"},
//   {type: "number"},
//   {type: "array", items: {type: "string"}}
// ]

"hello"           ✓ Valid (matches string)
123               ✓ Valid (matches number)
["a", "b"]        ✓ Valid (matches array)
true              ✗ Invalid (matches none)
null              ✗ Invalid (matches none)
```

## AnyOf vs OneOf vs AllOf

| Composition | Must Match | Use Case |
|-------------|------------|----------|
| **anyOf** | One or more | Flexible validation, multiple valid formats |
| **oneOf** | Exactly one | Mutually exclusive options, discriminated unions |
| **allOf** | All | Inheritance, combining schemas |

Example comparison:
```kotlin
// anyOf: Accept string OR number OR both formatted as array
anyOf = listOf(
    Schema(type = "string"),
    Schema(type = "number")
)
// Valid: "hello", 123, matches both if data could be interpreted as either

// oneOf: Accept EITHER string OR number, not both
oneOf = listOf(
    Schema(type = "string"),
    Schema(type = "number")  
)
// Valid: "hello", 123
// Invalid: any value that could match both schemas

// allOf: Must be valid string AND follow email pattern
allOf = listOf(
    Schema(type = "string"),
    Schema(pattern = "^[\\w\\.-]+@[\\w\\.-]+\\.\\w+$")
)
// Valid: "user@example.com"
// Invalid: "not-an-email", 123
```

## Related Builders

- [SchemaBuilder](SchemaBuilder.md) - Parent builder that uses AnyOfBuilder
- [OneOfBuilder](OneOfBuilder.md) - For mutually exclusive options
- [AllOfBuilder](AllOfBuilder.md) - For combining all schemas
- [ComponentsBuilder](../components/ComponentsBuilder.md) - For defining reusable schemas