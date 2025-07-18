# ExampleBuilder

**Package**: `me.farshad.dsl.builder.example`  
**File**: `ExampleBuilder.kt`

## Overview

`ExampleBuilder` is responsible for building individual examples in OpenAPI specifications. Examples help API consumers understand the expected format and structure of requests and responses by providing concrete instances of data.

## Class Declaration

```kotlin
class ExampleBuilder
```

## Properties

| Property | Type | Description |
|----------|------|-------------|
| `summary` | `String?` | Short summary of the example |
| `description` | `String?` | Detailed description of the example |
| `value` | `JsonElement?` | The example value |
| `externalValue` | `String?` | URL pointing to an external example |

## Key Methods

### Value Setting Methods

#### `value(value: String)`
Sets a string value for the example:

```kotlin
value("john.doe@example.com")
```

#### `value(value: Number)`
Sets a numeric value for the example:

```kotlin
value(42)
value(99.99)
```

#### `value(value: Boolean)`
Sets a boolean value for the example:

```kotlin
value(true)
```

#### `value(value: JsonElement)`
Sets a JSON element value for the example:

```kotlin
value(jsonObjectOf(
    "name" to "John Doe",
    "age" to 30
))
```

#### `objectValue(block: JsonObjectBuilder.() -> Unit)`
Builds an object value using a DSL:

```kotlin
objectValue {
    "id" to "123"
    "name" to "Product Name"
    "price" to 29.99
    "inStock" to true
}
```

#### `arrayValue(block: JsonArrayBuilder.() -> Unit)`
Builds an array value using a DSL:

```kotlin
arrayValue {
    +jsonObjectOf("id" to 1, "name" to "Item 1")
    +jsonObjectOf("id" to 2, "name" to "Item 2")
    +"simple string"
    +123
}
```

### Build Method

#### `build(): Example`
Builds the final `Example` object. Note that either `value` or `externalValue` should be set, but not both.

## Usage Examples

### Simple Value Examples

```kotlin
// String example
example("EmailExample") {
    summary = "Valid email format"
    description = "Example of a properly formatted email address"
    value("user@example.com")
}

// Number example
example("PriceExample") {
    summary = "Product price"
    description = "Price in USD with two decimal places"
    value(19.99)
}

// Boolean example
example("ActiveFlagExample") {
    summary = "Active status"
    value(true)
}
```

### Object Examples

```kotlin
// User object example
example("UserExample") {
    summary = "Complete user object"
    description = "Example showing all user fields with typical values"
    objectValue {
        "id" to "123e4567-e89b-12d3-a456-426614174000"
        "username" to "johndoe"
        "email" to "john.doe@example.com"
        "profile" to jsonObjectOf(
            "firstName" to "John",
            "lastName" to "Doe",
            "age" to 30,
            "interests" to jsonArrayOf("coding", "reading", "gaming")
        )
        "createdAt" to "2023-01-15T10:30:00Z"
        "isActive" to true
    }
}

// Error response example
example("ValidationErrorExample") {
    summary = "Validation error response"
    description = "Example of validation error with field details"
    objectValue {
        "error" to jsonObjectOf(
            "code" to "VALIDATION_ERROR",
            "message" to "Request validation failed",
            "timestamp" to "2023-10-15T14:30:00Z",
            "details" to jsonArrayOf(
                jsonObjectOf(
                    "field" to "email",
                    "value" to "invalid-email",
                    "issue" to "Invalid email format"
                ),
                jsonObjectOf(
                    "field" to "age",
                    "value" to -5,
                    "issue" to "Age must be positive"
                )
            )
        )
        "traceId" to "550e8400-e29b-41d4-a716-446655440000"
    }
}
```

### Array Examples

```kotlin
// Simple array example
example("TagsExample") {
    summary = "Product tags"
    description = "Array of product tags"
    arrayValue {
        +"electronics"
        +"laptop"
        +"gaming"
        +"high-performance"
    }
}

// Array of objects example
example("ProductListExample") {
    summary = "List of products"
    description = "Paginated product list response"
    objectValue {
        "products" to jsonArrayOf(
            jsonObjectOf(
                "id" to "prod-001",
                "name" to "Gaming Laptop",
                "price" to 1299.99,
                "category" to "Electronics"
            ),
            jsonObjectOf(
                "id" to "prod-002",
                "name" to "Wireless Mouse",
                "price" to 49.99,
                "category" to "Accessories"
            )
        )
        "pagination" to jsonObjectOf(
            "page" to 1,
            "pageSize" to 20,
            "totalPages" to 5,
            "totalItems" to 87
        )
    }
}
```

### Complex Nested Examples

```kotlin
example("OrderExample") {
    summary = "Complete order"
    description = "Example of a full order with customer, items, and shipping"
    objectValue {
        "orderId" to "ORD-2023-001234"
        "status" to "processing"
        "customer" to jsonObjectOf(
            "id" to "CUST-789",
            "name" to "Jane Smith",
            "email" to "jane.smith@example.com",
            "loyaltyTier" to "gold"
        )
        "items" to jsonArrayOf(
            jsonObjectOf(
                "productId" to "PROD-456",
                "name" to "Smartphone",
                "quantity" to 1,
                "unitPrice" to 799.99,
                "discount" to jsonObjectOf(
                    "type" to "percentage",
                    "value" to 10,
                    "amount" to 80.00
                )
            ),
            jsonObjectOf(
                "productId" to "PROD-789",
                "name" to "Phone Case",
                "quantity" to 2,
                "unitPrice" to 19.99,
                "discount" to null
            )
        )
        "shipping" to jsonObjectOf(
            "method" to "express",
            "address" to jsonObjectOf(
                "street" to "123 Main St",
                "city" to "Anytown",
                "state" to "CA",
                "zipCode" to "12345",
                "country" to "US"
            ),
            "estimatedDelivery" to "2023-10-20"
        )
        "totals" to jsonObjectOf(
            "subtotal" to 839.97,
            "discount" to 80.00,
            "shipping" to 15.00,
            "tax" to 68.40,
            "total" to 843.37
        )
        "createdAt" to "2023-10-15T10:30:00Z"
    }
}
```

### External Value Example

```kotlin
example("LargeDatasetExample") {
    summary = "Large dataset example"
    description = "Example too large to include inline"
    externalValue = "https://api.example.com/examples/large-dataset.json"
}
```

### Examples in Different Contexts

```kotlin
// In request body
requestBody {
    jsonContent {
        schema {
            ref("CreateUserRequest")
        }
        examples = mapOf(
            "minimal" to Example(
                summary = "Minimal user creation",
                value = jsonObjectOf(
                    "username" to "newuser",
                    "email" to "new@example.com",
                    "password" to "SecurePass123!"
                )
            ),
            "complete" to Example(
                summary = "Complete user creation",
                value = jsonObjectOf(
                    "username" to "newuser",
                    "email" to "new@example.com",
                    "password" to "SecurePass123!",
                    "profile" to jsonObjectOf(
                        "firstName" to "New",
                        "lastName" to "User",
                        "bio" to "Software developer"
                    ),
                    "preferences" to jsonObjectOf(
                        "newsletter" to true,
                        "notifications" to "email"
                    )
                )
            )
        )
    }
}

// In response
response("200", "Success") {
    jsonContent {
        schema {
            ref("UserResponse")
        }
        example = Example(
            value = jsonObjectOf(
                "id" to "123",
                "username" to "johndoe",
                "email" to "john@example.com",
                "createdAt" to "2023-01-01T00:00:00Z"
            )
        )
    }
}
```

### Conditional Examples

```kotlin
// Payment method examples
example("CreditCardPayment") {
    summary = "Credit card payment"
    objectValue {
        "method" to "credit_card"
        "cardNumber" to "**** **** **** 1234"
        "cardHolder" to "John Doe"
        "expiryMonth" to 12
        "expiryYear" to 2025
    }
}

example("PayPalPayment") {
    summary = "PayPal payment"
    objectValue {
        "method" to "paypal"
        "email" to "user@example.com"
        "paypalId" to "PAYPAL-USER-123"
    }
}

example("BankTransferPayment") {
    summary = "Bank transfer payment"
    objectValue {
        "method" to "bank_transfer"
        "accountNumber" to "****5678"
        "routingNumber" to "****4321"
        "accountHolder" to "Jane Smith"
    }
}
```

## Best Practices

1. **Use meaningful summaries**: Provide clear, concise summaries that explain what the example shows.

2. **Include realistic data**: Use realistic values that API consumers might actually use.

3. **Cover edge cases**: Include examples for minimum, maximum, and edge case values.

4. **Show required vs optional**: Demonstrate both minimal and complete examples.

5. **Format consistently**: Use consistent formatting and naming conventions.

6. **Avoid sensitive data**: Never include real credentials, tokens, or personal information.

7. **Document special values**: Explain any special values or formats in the description.

## Common Patterns

### Success/Error Example Pattern

```kotlin
// Success example
example("SuccessResponse") {
    summary = "Successful operation"
    objectValue {
        "success" to true
        "data" to jsonObjectOf(
            "id" to "123",
            "message" to "Operation completed"
        )
        "timestamp" to "2023-10-15T10:00:00Z"
    }
}

// Error example
example("ErrorResponse") {
    summary = "Error response"
    objectValue {
        "success" to false
        "error" to jsonObjectOf(
            "code" to "ERR_001",
            "message" to "Operation failed"
        )
        "timestamp" to "2023-10-15T10:00:00Z"
    }
}
```

### Pagination Example Pattern

```kotlin
example("PaginatedResponse") {
    summary = "Paginated list response"
    objectValue {
        "data" to jsonArrayOf(/* items */)
        "pagination" to jsonObjectOf(
            "page" to 2,
            "pageSize" to 20,
            "totalPages" to 10,
            "totalItems" to 195,
            "hasNext" to true,
            "hasPrevious" to true
        )
        "links" to jsonObjectOf(
            "self" to "/api/items?page=2&size=20",
            "first" to "/api/items?page=1&size=20",
            "last" to "/api/items?page=10&size=20",
            "next" to "/api/items?page=3&size=20",
            "previous" to "/api/items?page=1&size=20"
        )
    }
}
```

### Enum Value Examples

```kotlin
// Show all possible enum values
example("StatusExamples") {
    summary = "All possible status values"
    arrayValue {
        +"pending"
        +"processing"
        +"completed"
        +"failed"
        +"cancelled"
    }
}
```

## Integration with Components

Examples can be defined in components and referenced:

```kotlin
components {
    // Define reusable examples
    example("ValidUser") {
        summary = "A valid user"
        objectValue {
            "id" to "123",
            "username" to "johndoe",
            "email" to "john@example.com"
        }
    }
    
    // Reference in operations
    paths {
        path("/users/{id}") {
            get {
                response("200", "Success") {
                    jsonContent("User") {
                        examples = mapOf(
                            "default" to Example(
                                ref = "#/components/examples/ValidUser"
                            )
                        )
                    }
                }
            }
        }
    }
}
```

## Related Builders

- [ExamplesBuilder](ExamplesBuilder.md) - For managing multiple examples
- [ComponentsBuilder](../components/ComponentsBuilder.md) - For defining reusable examples
- [MediaTypeBuilder](MediaTypeBuilder.md) - Where examples are often used
- [SchemaBuilder](../../../../../../../../capabilities/SchemaBuilder.md) - Can include inline examples