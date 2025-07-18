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
| `schemas` | `MutableList<SchemaReference>` | List of schema references where at least one must be satisfied |

## Key Methods

### Schema Addition Methods

#### `schema(ref: String)`
Adds a schema reference by name:

```kotlin
schema("EmailAddress")  // Auto-prefixes with #/components/schemas/
schema("#/components/schemas/EmailAddress")  // Full reference path
```

#### `schema(clazz: KClass<*>)`
Adds a schema reference using a Kotlin class:

```kotlin
schema(Address::class)  // References #/components/schemas/Address
```

#### `schema(block: SchemaBuilder.() -> Unit)`
Adds an inline schema to the anyOf:

```kotlin
schema {
    type = SchemaType.STRING
    format = SchemaFormat.EMAIL
}
```

### Build Method

#### `build(): List<SchemaReference>`
Returns the list of schema references for use in the parent SchemaBuilder.

## Usage Examples

### Flexible Input Types

```kotlin
// Accept single value or array of values
schema {
    anyOf {
        // Single string
        schema { type = SchemaType.STRING }
        
        // Array of strings
        schema {
            type = SchemaType.ARRAY
            items {
                type = SchemaType.STRING
            }
        }
    }
}
// Valid: "hello", ["hello", "world"], ["single"]
```

### Multiple Valid Formats

```kotlin
schema("ContactInfo") {
    anyOf {
        // Email format
        schema {
            type = SchemaType.OBJECT
            property("type", PropertyType.STRING, required = true)
            property("value", PropertyType.STRING, required = true) {
                format = SchemaFormat.EMAIL
            }
        }
        
        // Phone format
        schema {
            type = SchemaType.OBJECT
            property("type", PropertyType.STRING, required = true)
            property("value", PropertyType.STRING, required = true)
        }
        
        // Address format
        schema {
            type = SchemaType.OBJECT
            property("type", PropertyType.STRING, required = true)
            property("street", PropertyType.STRING, required = true)
            property("city", PropertyType.STRING, required = true)
            property("country", PropertyType.STRING, required = true)
        }
    }
}
```

### Optional Properties Pattern

```kotlin
// User with optional extended profile
schema("UserProfile") {
    anyOf {
        // Basic user
        schema {
            type = SchemaType.OBJECT
            property("username", PropertyType.STRING, required = true)
            property("email", PropertyType.STRING, required = true) {
                format = SchemaFormat.EMAIL
            }
        }
        
        // User with profile
        schema {
            type = SchemaType.OBJECT
            property("username", PropertyType.STRING, required = true)
            property("email", PropertyType.STRING, required = true) {
                format = SchemaFormat.EMAIL
            }
            property("profile", PropertyType.OBJECT, required = true) {
                property("bio", PropertyType.STRING)
                property("avatar", PropertyType.STRING) {
                    format = SchemaFormat.URL
                }
                property("location", PropertyType.STRING)
            }
        }
    }
}
```

### Backward Compatibility

```kotlin
// Supporting old and new API formats
schema("ApiRequest") {
    anyOf {
        // New format (preferred)
        schema {
            type = SchemaType.OBJECT
            property("version", PropertyType.STRING, required = true)
            property("data", PropertyType.OBJECT, required = true) {
                property("items", PropertyType.ARRAY) {
                    items {
                        type = SchemaType.OBJECT
                    }
                }
                property("metadata", PropertyType.OBJECT)
            }
        }
        
        // Legacy format (still supported)
        schema {
            type = SchemaType.OBJECT
            property("items", PropertyType.ARRAY, required = true) {
                items {
                    type = SchemaType.OBJECT
                }
            }
            property("meta", PropertyType.STRING)
        }
    }
}
```

### Conditional Requirements

```kotlin
// Payment method with different requirements
schema("PaymentDetails") {
    anyOf {
        // Credit card requires CVV
        schema {
            type = SchemaType.OBJECT
            property("method", PropertyType.STRING, required = true)
            property("cardNumber", PropertyType.STRING, required = true)
            property("cvv", PropertyType.STRING, required = true)
            property("expiryDate", PropertyType.STRING, required = true)
        }
        
        // Bank transfer doesn't require CVV
        schema {
            type = SchemaType.OBJECT
            property("method", PropertyType.STRING, required = true)
            property("accountNumber", PropertyType.STRING, required = true)
            property("routingNumber", PropertyType.STRING, required = true)
        }
        
        // PayPal only needs email
        schema {
            type = SchemaType.OBJECT
            property("method", PropertyType.STRING, required = true)
            property("email", PropertyType.STRING, required = true) {
                format = SchemaFormat.EMAIL
            }
        }
    }
}
```

### Mixed Type Values

```kotlin
// Configuration value that can be various types
schema("ConfigValue") {
    anyOf {
        // Boolean flag
        schema { type = SchemaType.BOOLEAN }
        
        // Numeric value
        schema { type = SchemaType.NUMBER }
        
        // String value
        schema { type = SchemaType.STRING }
        
        // Array of strings
        schema {
            type = SchemaType.ARRAY
            items {
                type = SchemaType.STRING
            }
        }
        
        // Key-value object
        schema {
            type = SchemaType.OBJECT
            // For dynamic properties
        }
    }
}
```

### Partial Updates

```kotlin
// Update request that can update different field combinations
schema("UserUpdate") {
    anyOf {
        // Update profile only
        schema {
            type = SchemaType.OBJECT
            property("profile", PropertyType.OBJECT, required = true)
        }
        
        // Update credentials only
        schema {
            type = SchemaType.OBJECT
            property("password", PropertyType.STRING, required = true)
            property("passwordConfirm", PropertyType.STRING, required = true)
        }
        
        // Update preferences only
        schema {
            type = SchemaType.OBJECT
            property("preferences", PropertyType.OBJECT, required = true)
        }
        
        // Update any combination
        schema {
            type = SchemaType.OBJECT
            property("profile", PropertyType.OBJECT)
            property("password", PropertyType.STRING)
            property("passwordConfirm", PropertyType.STRING)
            property("preferences", PropertyType.OBJECT)
        }
    }
}
```

### Localized Content

```kotlin
// Content that can be a simple string or localized object
schema("LocalizableText") {
    anyOf {
        // Simple string (default language)
        schema { type = SchemaType.STRING }
        
        // Localized strings
        schema {
            type = SchemaType.OBJECT
            property("en", PropertyType.STRING)
            property("es", PropertyType.STRING)
            property("fr", PropertyType.STRING)
            property("de", PropertyType.STRING)
        }
    }
    
    examples {
        example("simple", "Hello World")
        example("localized", mapOf(
            "en" to "Hello World",
            "es" to "Hola Mundo",
            "fr" to "Bonjour le monde"
        ))
    }
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
    anyOf("BasicFeature", "EnhancedFeature", "PremiumFeature")
}

// Or using class references
schema("Feature") {
    anyOf(BasicFeature::class, EnhancedFeature::class, PremiumFeature::class)
}
```

### Format Migration Pattern

```kotlin
// Supporting multiple API versions
schema("Request") {
    anyOf {
        schema("RequestV3")    // Current
        schema("RequestV2")    // Legacy
        schema("RequestV1")    // Deprecated
    }
}
```

### Multi-Format Input Pattern

```kotlin
// Accept data in multiple formats
schema("DateInput") {
    anyOf {
        // ISO string
        schema {
            type = SchemaType.STRING
            format = SchemaFormat.DATE_TIME
        }
        
        // Unix timestamp
        schema {
            type = SchemaType.INTEGER
        }
        
        // Structured object
        schema {
            type = SchemaType.OBJECT
            property("year", PropertyType.INTEGER)
            property("month", PropertyType.INTEGER)
            property("day", PropertyType.INTEGER)
        }
    }
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
- [DiscriminatorBuilder](DiscriminatorBuilder.md) - For discriminator configuration
- [SchemaComposition](SchemaComposition.md) - For advanced type-safe composition patterns