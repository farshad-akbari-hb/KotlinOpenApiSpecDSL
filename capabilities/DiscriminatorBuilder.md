# DiscriminatorBuilder

**Package**: `me.farshad.dsl.builder.schema`  
**File**: `DiscriminatorBuilder.kt`

## Overview

`DiscriminatorBuilder` is used to configure discriminators for polymorphic schemas in OpenAPI. Discriminators help API consumers and code generators determine which specific schema to use when validating polymorphic data, typically used with `oneOf` or `anyOf` compositions.

## Class Declaration

```kotlin
class DiscriminatorBuilder
```

## Properties

| Property | Type | Description |
|----------|------|-------------|
| `propertyName` | `String?` | The property name that contains the discriminator value |
| `mapping` | `MutableMap<String, String>` | Maps discriminator values to schema references |

## Key Methods

### `mapping(discriminatorValue: String, schemaRef: String)`
Adds a mapping from a discriminator value to a schema reference:

```kotlin
mapping("dog", "#/components/schemas/Dog")
mapping("cat", "#/components/schemas/Cat")
```

### `build(): Discriminator`
Builds the final `Discriminator` object. Validates that `propertyName` is not null.

## Usage Examples

### Basic Discriminator Usage

```kotlin
components {
    // Base schema
    schema("Pet") {
        oneOf = listOf(
            Schema(ref = "#/components/schemas/Dog"),
            Schema(ref = "#/components/schemas/Cat"),
            Schema(ref = "#/components/schemas/Bird")
        )
        
        discriminator {
            propertyName = "petType"
            mapping("dog", "#/components/schemas/Dog")
            mapping("cat", "#/components/schemas/Cat")
            mapping("bird", "#/components/schemas/Bird")
        }
    }
    
    // Specific schemas
    schema("Dog") {
        type = "object"
        properties {
            property("petType", "string") {
                const = JsonPrimitive("dog")
            }
            property("breed", "string")
            property("barkVolume", "integer")
        }
        required.addAll(listOf("petType", "breed"))
    }
    
    schema("Cat") {
        type = "object"
        properties {
            property("petType", "string") {
                const = JsonPrimitive("cat")
            }
            property("breed", "string")
            property("livesRemaining", "integer") {
                minimum = 0
                maximum = 9
            }
        }
        required.addAll(listOf("petType", "breed"))
    }
}
```

### Inheritance with Discriminator

```kotlin
components {
    // Abstract base vehicle
    schema("Vehicle") {
        type = "object"
        properties {
            property("type", "string") {
                description = "Vehicle type discriminator"
            }
            property("make", "string")
            property("model", "string")
            property("year", "integer")
        }
        required.addAll(listOf("type", "make", "model", "year"))
        
        discriminator {
            propertyName = "type"
        }
    }
    
    // Car extends Vehicle
    schema("Car") {
        allOf = listOf(
            Schema(ref = "#/components/schemas/Vehicle"),
            Schema(
                type = "object",
                properties = mapOf(
                    "type" to Schema(
                        type = "string",
                        const = JsonPrimitive("car")
                    ),
                    "doors" to Schema(type = "integer", minimum = 2, maximum = 5),
                    "fuelType" to Schema(
                        type = "string",
                        enum = listOf("gasoline", "diesel", "electric", "hybrid")
                    )
                ),
                required = listOf("doors", "fuelType")
            )
        )
    }
    
    // Motorcycle extends Vehicle
    schema("Motorcycle") {
        allOf = listOf(
            Schema(ref = "#/components/schemas/Vehicle"),
            Schema(
                type = "object",
                properties = mapOf(
                    "type" to Schema(
                        type = "string",
                        const = JsonPrimitive("motorcycle")
                    ),
                    "engineCC" to Schema(type = "integer", minimum = 50),
                    "hasSidecar" to Schema(type = "boolean", default = JsonPrimitive(false))
                ),
                required = listOf("engineCC")
            )
        )
    }
    
    // Polymorphic response
    schema("VehicleResponse") {
        oneOf = listOf(
            Schema(ref = "#/components/schemas/Car"),
            Schema(ref = "#/components/schemas/Motorcycle")
        )
        
        discriminator {
            propertyName = "type"
            mapping("car", "#/components/schemas/Car")
            mapping("motorcycle", "#/components/schemas/Motorcycle")
        }
    }
}
```

### API Error Responses with Discriminator

```kotlin
components {
    // Base error
    schema("ApiError") {
        type = "object"
        properties {
            property("errorType", "string") {
                description = "Error type discriminator"
            }
            property("message", "string")
            property("timestamp", "string") {
                format = "date-time"
            }
        }
        required.addAll(listOf("errorType", "message", "timestamp"))
        
        discriminator {
            propertyName = "errorType"
        }
    }
    
    // Validation error
    schema("ValidationError") {
        allOf = listOf(
            Schema(ref = "#/components/schemas/ApiError"),
            Schema(
                type = "object",
                properties = mapOf(
                    "errorType" to Schema(
                        type = "string",
                        const = JsonPrimitive("validation")
                    ),
                    "fieldErrors" to Schema(
                        type = "array",
                        items = Schema(
                            type = "object",
                            properties = mapOf(
                                "field" to Schema(type = "string"),
                                "message" to Schema(type = "string"),
                                "code" to Schema(type = "string")
                            ),
                            required = listOf("field", "message")
                        )
                    )
                ),
                required = listOf("fieldErrors")
            )
        )
    }
    
    // Authentication error
    schema("AuthenticationError") {
        allOf = listOf(
            Schema(ref = "#/components/schemas/ApiError"),
            Schema(
                type = "object",
                properties = mapOf(
                    "errorType" to Schema(
                        type = "string",
                        const = JsonPrimitive("authentication")
                    ),
                    "realm" to Schema(type = "string"),
                    "authScheme" to Schema(type = "string")
                ),
                required = listOf("realm")
            )
        )
    }
    
    // Error response union
    schema("ErrorResponse") {
        oneOf = listOf(
            Schema(ref = "#/components/schemas/ValidationError"),
            Schema(ref = "#/components/schemas/AuthenticationError"),
            Schema(ref = "#/components/schemas/ApiError")  // Generic fallback
        )
        
        discriminator {
            propertyName = "errorType"
            mapping("validation", "#/components/schemas/ValidationError")
            mapping("authentication", "#/components/schemas/AuthenticationError")
            // Other error types fall back to generic ApiError
        }
    }
}
```

### Event System with Discriminator

```kotlin
components {
    // Base event
    schema("Event") {
        type = "object"
        properties {
            property("eventId", "string") { format = "uuid" }
            property("eventType", "string")
            property("timestamp", "string") { format = "date-time" }
            property("userId", "string")
        }
        required.addAll(listOf("eventId", "eventType", "timestamp"))
        
        discriminator {
            propertyName = "eventType"
        }
    }
    
    // User event types
    schema("UserCreatedEvent") {
        allOf = listOf(
            Schema(ref = "#/components/schemas/Event"),
            Schema(
                type = "object",
                properties = mapOf(
                    "eventType" to Schema(
                        type = "string",
                        const = JsonPrimitive("user.created")
                    ),
                    "data" to Schema(
                        type = "object",
                        properties = mapOf(
                            "username" to Schema(type = "string"),
                            "email" to Schema(type = "string", format = "email"),
                            "registrationSource" to Schema(type = "string")
                        ),
                        required = listOf("username", "email")
                    )
                ),
                required = listOf("data")
            )
        )
    }
    
    schema("UserUpdatedEvent") {
        allOf = listOf(
            Schema(ref = "#/components/schemas/Event"),
            Schema(
                type = "object",
                properties = mapOf(
                    "eventType" to Schema(
                        type = "string",
                        const = JsonPrimitive("user.updated")
                    ),
                    "data" to Schema(
                        type = "object",
                        properties = mapOf(
                            "updatedFields" to Schema(
                                type = "array",
                                items = Schema(type = "string")
                            ),
                            "previousValues" to Schema(
                                type = "object",
                                additionalProperties = Schema()
                            ),
                            "newValues" to Schema(
                                type = "object",
                                additionalProperties = Schema()
                            )
                        ),
                        required = listOf("updatedFields")
                    )
                ),
                required = listOf("data")
            )
        )
    }
    
    // Event stream schema
    schema("EventStream") {
        type = "array"
        items {
            oneOf = listOf(
                Schema(ref = "#/components/schemas/UserCreatedEvent"),
                Schema(ref = "#/components/schemas/UserUpdatedEvent")
                // Add more event types as needed
            )
            
            discriminator {
                propertyName = "eventType"
                mapping("user.created", "#/components/schemas/UserCreatedEvent")
                mapping("user.updated", "#/components/schemas/UserUpdatedEvent")
            }
        }
    }
}
```

### Complex Mapping Example

```kotlin
// Payment processing with various methods
schema("PaymentRequest") {
    oneOf = listOf(
        Schema(ref = "#/components/schemas/CreditCardPayment"),
        Schema(ref = "#/components/schemas/BankTransferPayment"),
        Schema(ref = "#/components/schemas/CryptoPayment"),
        Schema(ref = "#/components/schemas/PayPalPayment")
    )
    
    discriminator {
        propertyName = "method"
        
        // Multiple values can map to the same schema
        mapping("visa", "#/components/schemas/CreditCardPayment")
        mapping("mastercard", "#/components/schemas/CreditCardPayment")
        mapping("amex", "#/components/schemas/CreditCardPayment")
        
        mapping("ach", "#/components/schemas/BankTransferPayment")
        mapping("wire", "#/components/schemas/BankTransferPayment")
        
        mapping("bitcoin", "#/components/schemas/CryptoPayment")
        mapping("ethereum", "#/components/schemas/CryptoPayment")
        
        mapping("paypal", "#/components/schemas/PayPalPayment")
    }
}
```

## Best Practices

1. **Consistent property name**: Use the same discriminator property name across related schemas.

2. **Use const values**: In sub-schemas, use `const` to fix the discriminator value.

3. **Document the discriminator**: Explain what the discriminator represents and list valid values.

4. **Complete mappings**: Provide mappings for all possible discriminator values.

5. **Hierarchical discriminators**: For complex hierarchies, consider nested discriminators.

## Common Patterns

### Type Field Pattern

```kotlin
discriminator {
    propertyName = "type"  // Common convention
    mapping("typeA", "#/components/schemas/TypeA")
    mapping("typeB", "#/components/schemas/TypeB")
}
```

### Kind Field Pattern

```kotlin
discriminator {
    propertyName = "kind"  // Alternative to "type"
    mapping("resource.v1", "#/components/schemas/ResourceV1")
    mapping("resource.v2", "#/components/schemas/ResourceV2")
}
```

### Class Name Pattern

```kotlin
discriminator {
    propertyName = "@class"  // Java-style class indicator
    mapping("com.example.Dog", "#/components/schemas/Dog")
    mapping("com.example.Cat", "#/components/schemas/Cat")
}
```

### Version-Based Pattern

```kotlin
discriminator {
    propertyName = "version"
    mapping("1.0", "#/components/schemas/PayloadV1")
    mapping("2.0", "#/components/schemas/PayloadV2")
    mapping("3.0", "#/components/schemas/PayloadV3")
}
```

## How Discriminators Work

1. **Property Identification**: The discriminator identifies which property in the data contains the type indicator.

2. **Mapping Resolution**: The mapping table translates discriminator values to specific schemas.

3. **Validation Process**:
   - First, check the discriminator property value
   - Use the mapping to find the appropriate schema
   - Validate the entire object against that specific schema

4. **Code Generation**: Tools use discriminators to generate proper inheritance hierarchies and deserialization logic.

Example validation flow:
```json
// Given this data:
{
  "petType": "dog",
  "breed": "Labrador",
  "barkVolume": 7
}

// With discriminator propertyName: "petType"
// And mapping: {"dog": "#/components/schemas/Dog"}
// The validator will:
// 1. Read petType value: "dog"
// 2. Look up mapping: "dog" -> Dog schema
// 3. Validate entire object against Dog schema
```

## Integration with Schema Composition

Discriminators work best with:

- **oneOf**: Most common use case for polymorphic types
- **anyOf**: Less common but supported
- **allOf**: Used in derived schemas to set discriminator values

```kotlin
// Parent with discriminator
schema("Shape") {
    oneOf = listOf(
        Schema(ref = "#/components/schemas/Circle"),
        Schema(ref = "#/components/schemas/Rectangle")
    )
    discriminator {
        propertyName = "shapeType"
    }
}

// Children set discriminator value
schema("Circle") {
    type = "object"
    properties {
        property("shapeType", "string") {
            const = JsonPrimitive("circle")  // Fixed discriminator value
        }
        property("radius", "number")
    }
}
```

## Related Builders

- [SchemaBuilder](SchemaBuilder.md) - Parent builder that uses discriminators
- [OneOfBuilder](OneOfBuilder.md) - Most common companion for discriminators
- [AllOfBuilder](AllOfBuilder.md) - Used for inheritance patterns
- [ComponentsBuilder](ComponentsBuilder.md) - Where polymorphic schemas are defined