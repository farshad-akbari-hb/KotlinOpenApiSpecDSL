# DiscriminatorBuilder

**Package**: `me.farshad.dsl.builder.schema`  
**File**: `DiscriminatorBuilder.kt`

## Overview

`DiscriminatorBuilder` is used to configure discriminators for polymorphic schemas in OpenAPI. Discriminators help API consumers and code generators determine which specific schema to use when validating polymorphic data, typically used with `oneOf` or `anyOf` compositions.

## Class Declaration

```kotlin
class DiscriminatorBuilder(private val propertyName: String)
```

## Constructor Parameters

| Parameter | Type | Description |
|-----------|------|-------------|
| `propertyName` | `String` | The property name that contains the discriminator value |

## Properties

| Property | Type | Description |
|----------|------|-------------|
| `mapping` | `MutableMap<String, String>` | Maps discriminator values to schema references |

## Key Methods

### `mapping(value: String, schemaRef: String)`
Adds a mapping from a discriminator value to a schema reference:

```kotlin
mapping("dog", "#/components/schemas/Dog")
mapping("cat", "#/components/schemas/Cat")
```

### `mapping(value: String, clazz: KClass<*>)`
Adds a mapping from a discriminator value to a Kotlin class:

```kotlin
mapping("dog", Dog::class)  // Auto-generates #/components/schemas/Dog
mapping("cat", Cat::class)  // Auto-generates #/components/schemas/Cat
```

### `build(): Discriminator`
Builds the final `Discriminator` object with the property name and mappings.

## Usage Examples

### Basic Discriminator Usage

```kotlin
components {
    // Base schema with discriminator
    schema("Pet") {
        oneOf("Dog", "Cat", "Bird")
        
        discriminator("petType") {
            mapping("dog", "#/components/schemas/Dog")
            mapping("cat", "#/components/schemas/Cat")
            mapping("bird", "#/components/schemas/Bird")
        }
    }
    
    // Specific schemas
    schema("Dog") {
        type = SchemaType.OBJECT
        property("petType", PropertyType.STRING, required = true)
        property("breed", PropertyType.STRING, required = true)
        property("barkVolume", PropertyType.INTEGER)
    }
    
    schema("Cat") {
        type = SchemaType.OBJECT
        property("petType", PropertyType.STRING, required = true)
        property("breed", PropertyType.STRING, required = true)
        property("livesRemaining", PropertyType.INTEGER)
    }
}
```

### Inheritance with Discriminator

```kotlin
components {
    // Abstract base vehicle
    schema("Vehicle") {
        type = SchemaType.OBJECT
        property("type", PropertyType.STRING, required = true) {
            description = "Vehicle type discriminator"
        }
        property("make", PropertyType.STRING, required = true)
        property("model", PropertyType.STRING, required = true)
        property("year", PropertyType.INTEGER, required = true)
        
        discriminator("type")
    }
    
    // Car extends Vehicle
    schema("Car") {
        allOf {
            schema("Vehicle")
            schema {
                type = SchemaType.OBJECT
                property("type", PropertyType.STRING)
                property("doors", PropertyType.INTEGER, required = true)
                property("fuelType", PropertyType.STRING, required = true)
            }
        }
    }
    
    // Motorcycle extends Vehicle
    schema("Motorcycle") {
        allOf {
            schema("Vehicle")
            schema {
                type = SchemaType.OBJECT
                property("type", PropertyType.STRING)
                property("engineCC", PropertyType.INTEGER, required = true)
                property("hasSidecar", PropertyType.BOOLEAN)
            }
        }
    }
    
    // Polymorphic response
    schema("VehicleResponse") {
        oneOf(Car::class, Motorcycle::class)
        
        discriminator("type") {
            mapping("car", Car::class)
            mapping("motorcycle", Motorcycle::class)
        }
    }
}
```

### API Error Responses with Discriminator

```kotlin
components {
    // Base error
    schema("ApiError") {
        type = SchemaType.OBJECT
        property("errorType", PropertyType.STRING, required = true) {
            description = "Error type discriminator"
        }
        property("message", PropertyType.STRING, required = true)
        property("timestamp", PropertyType.STRING, required = true) {
            format = SchemaFormat.DATE_TIME
        }
        
        discriminator("errorType")
    }
    
    // Validation error
    schema("ValidationError") {
        allOf {
            schema("ApiError")
            schema {
                type = SchemaType.OBJECT
                property("errorType", PropertyType.STRING)
                property("fieldErrors", PropertyType.ARRAY, required = true) {
                    items {
                        type = SchemaType.OBJECT
                        property("field", PropertyType.STRING, required = true)
                        property("message", PropertyType.STRING, required = true)
                        property("code", PropertyType.STRING)
                    }
                }
            }
        }
    }
    
    // Authentication error
    schema("AuthenticationError") {
        allOf {
            schema("ApiError")
            schema {
                type = SchemaType.OBJECT
                property("errorType", PropertyType.STRING)
                property("realm", PropertyType.STRING, required = true)
                property("authScheme", PropertyType.STRING)
            }
        }
    }
    
    // Error response union
    schema("ErrorResponse") {
        oneOf(ValidationError::class, AuthenticationError::class, ApiError::class)
        
        discriminator("errorType") {
            mapping("validation", ValidationError::class)
            mapping("authentication", AuthenticationError::class)
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
        type = SchemaType.OBJECT
        property("eventId", PropertyType.STRING, required = true) {
            format = SchemaFormat.UUID
        }
        property("eventType", PropertyType.STRING, required = true)
        property("timestamp", PropertyType.STRING, required = true) {
            format = SchemaFormat.DATE_TIME
        }
        property("userId", PropertyType.STRING)
        
        discriminator("eventType")
    }
    
    // User event types
    schema("UserCreatedEvent") {
        allOf {
            schema("Event")
            schema {
                type = SchemaType.OBJECT
                property("eventType", PropertyType.STRING)
                property("data", PropertyType.OBJECT, required = true) {
                    property("username", PropertyType.STRING, required = true)
                    property("email", PropertyType.STRING, required = true) {
                        format = SchemaFormat.EMAIL
                    }
                    property("registrationSource", PropertyType.STRING)
                }
            }
        }
    }
    
    schema("UserUpdatedEvent") {
        allOf {
            schema("Event")
            schema {
                type = SchemaType.OBJECT
                property("eventType", PropertyType.STRING)
                property("data", PropertyType.OBJECT, required = true) {
                    property("updatedFields", PropertyType.ARRAY, required = true) {
                        items { type = SchemaType.STRING }
                    }
                    property("previousValues", PropertyType.OBJECT)
                    property("newValues", PropertyType.OBJECT)
                }
            }
        }
    }
    
    // Event stream schema
    schema("EventStream") {
        type = SchemaType.ARRAY
        items {
            type = SchemaType.OBJECT
            oneOf(UserCreatedEvent::class, UserUpdatedEvent::class)
            
            discriminator("eventType") {
                mapping("user.created", UserCreatedEvent::class)
                mapping("user.updated", UserUpdatedEvent::class)
            }
        }
    }
}
```

### Complex Mapping Example

```kotlin
// Payment processing with various methods
schema("PaymentRequest") {
    oneOf(
        CreditCardPayment::class,
        BankTransferPayment::class,
        CryptoPayment::class,
        PayPalPayment::class
    )
    
    discriminator("method") {
        // Multiple values can map to the same schema
        mapping("visa", CreditCardPayment::class)
        mapping("mastercard", CreditCardPayment::class)
        mapping("amex", CreditCardPayment::class)
        
        mapping("ach", BankTransferPayment::class)
        mapping("wire", BankTransferPayment::class)
        
        mapping("bitcoin", CryptoPayment::class)
        mapping("ethereum", CryptoPayment::class)
        
        mapping("paypal", PayPalPayment::class)
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
discriminator("type") {  // Common convention
    mapping("typeA", TypeA::class)
    mapping("typeB", TypeB::class)
}
```

### Kind Field Pattern

```kotlin
discriminator("kind") {  // Alternative to "type"
    mapping("resource.v1", ResourceV1::class)
    mapping("resource.v2", ResourceV2::class)
}
```

### Class Name Pattern

```kotlin
discriminator("@class") {  // Java-style class indicator
    mapping("com.example.Dog", Dog::class)
    mapping("com.example.Cat", Cat::class)
}
```

### Version-Based Pattern

```kotlin
discriminator("version") {
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
    oneOf(Circle::class, Rectangle::class)
    discriminator("shapeType")
}

// Children set discriminator value
schema("Circle") {
    type = SchemaType.OBJECT
    property("shapeType", PropertyType.STRING) {
        // Fixed discriminator value
    }
    property("radius", PropertyType.NUMBER)
}
```

## Related Builders

- [SchemaBuilder](SchemaBuilder.md) - Parent builder that uses discriminators
- [OneOfBuilder](OneOfBuilder.md) - Most common companion for discriminators
- [AllOfBuilder](AllOfBuilder.md) - Used for inheritance patterns
- [AnyOfBuilder](AnyOfBuilder.md) - Less common but supports discriminators
- [SchemaComposition](SchemaComposition.md) - For advanced type-safe composition patterns