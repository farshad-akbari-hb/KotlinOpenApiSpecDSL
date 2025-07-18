# Schema Definitions

This guide covers how to define and work with schemas in the Kotlin OpenAPI Spec DSL, from basic types to complex data models.

## Table of Contents
- [Basic Schema Types](#basic-schema-types)
- [Object Schemas](#object-schemas)
- [Array Schemas](#array-schemas)
- [Enum Schemas](#enum-schemas)
- [Nullable Types](#nullable-types)
- [Schema Validation](#schema-validation)
- [Schema From Kotlin Classes](#schema-from-kotlin-classes)
- [Using Annotations](#using-annotations)
- [Schema References](#schema-references)
- [Additional Properties](#additional-properties)
- [Schema Examples](#schema-examples)

## Basic Schema Types

### Primitive Types

```kotlin
// String schema
schema {
    type = SchemaType.STRING
    description = "User's name"
    example = "John Doe"
}

// Integer schema
schema {
    type = SchemaType.INTEGER
    description = "User's age"
    minimum = 0
    maximum = 150
    example = 25
}

// Number (decimal) schema
schema {
    type = SchemaType.NUMBER
    description = "Product price"
    minimum = 0.01
    exclusiveMinimum = false
    multipleOf = 0.01  // Ensures 2 decimal places
    example = 19.99
}

// Boolean schema
schema {
    type = SchemaType.BOOLEAN
    description = "Is user active"
    default = true
}
```

### String Formats and Patterns

```kotlin
// Email format
schema {
    type = SchemaType.STRING
    format = "email"
    description = "User's email address"
}

// Date and time formats
schema {
    type = SchemaType.STRING
    format = "date"  // 2023-10-20
    description = "Birth date"
}

schema {
    type = SchemaType.STRING
    format = "date-time"  // 2023-10-20T15:30:00Z
    description = "Created timestamp"
}

// UUID format
schema {
    type = SchemaType.STRING
    format = "uuid"
    description = "Unique identifier"
}

// Password format (typically hidden in UI)
schema {
    type = SchemaType.STRING
    format = "password"
    minLength = 8
    description = "User password"
}

// Custom patterns with regex
schema {
    type = SchemaType.STRING
    pattern = "^[A-Z]{2}[0-9]{4}$"  // e.g., AB1234
    description = "Product code"
}

// URL format
schema {
    type = SchemaType.STRING
    format = "uri"
    description = "Website URL"
}

// Binary data
schema {
    type = SchemaType.STRING
    format = "binary"  // For file uploads
    description = "File content"
}

// Base64 encoded
schema {
    type = SchemaType.STRING
    format = "byte"  // Base64 encoded string
    description = "Encoded data"
}
```

### String Constraints

```kotlin
schema {
    type = SchemaType.STRING
    minLength = 3
    maxLength = 50
    pattern = "^[a-zA-Z0-9_]+$"  // Alphanumeric with underscores
    description = "Username"
}
```

## Object Schemas

### Basic Object Definition

```kotlin
schema {
    type = SchemaType.OBJECT
    description = "User profile"
    
    properties {
        "id" to schema {
            type = SchemaType.STRING
            format = "uuid"
        }
        
        "name" to schema {
            type = SchemaType.STRING
            minLength = 1
            maxLength = 100
        }
        
        "email" to schema {
            type = SchemaType.STRING
            format = "email"
        }
        
        "age" to schema {
            type = SchemaType.INTEGER
            minimum = 0
            maximum = 150
        }
        
        "isActive" to schema {
            type = SchemaType.BOOLEAN
            default = true
        }
    }
    
    required = listOf("id", "name", "email")
}
```

### Nested Objects

```kotlin
schema {
    type = SchemaType.OBJECT
    properties {
        "user" to schema {
            type = SchemaType.OBJECT
            properties {
                "name" to schema { type = SchemaType.STRING }
                "email" to schema { 
                    type = SchemaType.STRING 
                    format = "email"
                }
            }
            required = listOf("name", "email")
        }
        
        "address" to schema {
            type = SchemaType.OBJECT
            properties {
                "street" to schema { type = SchemaType.STRING }
                "city" to schema { type = SchemaType.STRING }
                "country" to schema { type = SchemaType.STRING }
                "postalCode" to schema { 
                    type = SchemaType.STRING 
                    pattern = "^[0-9]{5}$"
                }
            }
            required = listOf("street", "city", "country")
        }
        
        "preferences" to schema {
            type = SchemaType.OBJECT
            properties {
                "newsletter" to schema { 
                    type = SchemaType.BOOLEAN 
                    default = false
                }
                "notifications" to schema {
                    type = SchemaType.OBJECT
                    properties {
                        "email" to schema { type = SchemaType.BOOLEAN }
                        "sms" to schema { type = SchemaType.BOOLEAN }
                        "push" to schema { type = SchemaType.BOOLEAN }
                    }
                }
            }
        }
    }
}
```

## Array Schemas

### Basic Arrays

```kotlin
// Array of strings
schema {
    type = SchemaType.ARRAY
    description = "List of tags"
    items = schema {
        type = SchemaType.STRING
        minLength = 1
        maxLength = 20
    }
    minItems = 0
    maxItems = 10
    uniqueItems = true  // No duplicate values
}

// Array of numbers
schema {
    type = SchemaType.ARRAY
    description = "Price history"
    items = schema {
        type = SchemaType.NUMBER
        minimum = 0
    }
}

// Array of objects
schema {
    type = SchemaType.ARRAY
    description = "List of users"
    items = schema {
        type = SchemaType.OBJECT
        properties {
            "id" to schema { type = SchemaType.STRING }
            "name" to schema { type = SchemaType.STRING }
            "role" to schema { 
                type = SchemaType.STRING 
                enum = listOf("admin", "user", "guest")
            }
        }
        required = listOf("id", "name")
    }
}
```

### Tuple Arrays (Fixed Items)

```kotlin
// Coordinates [latitude, longitude]
schema {
    type = SchemaType.ARRAY
    description = "Geographic coordinates"
    items = listOf(
        schema {
            type = SchemaType.NUMBER
            minimum = -90
            maximum = 90
            description = "Latitude"
        },
        schema {
            type = SchemaType.NUMBER
            minimum = -180
            maximum = 180
            description = "Longitude"
        }
    )
    minItems = 2
    maxItems = 2
}
```

### Mixed Type Arrays

```kotlin
// Array that can contain different types
schema {
    type = SchemaType.ARRAY
    description = "Mixed content"
    items = schema {
        oneOf = listOf(
            schema { type = SchemaType.STRING },
            schema { type = SchemaType.NUMBER },
            schema { type = SchemaType.BOOLEAN }
        )
    }
}
```

## Enum Schemas

### String Enums

```kotlin
// Simple string enum
schema {
    type = SchemaType.STRING
    enum = listOf("pending", "approved", "rejected", "cancelled")
    description = "Request status"
}

// With default value
schema {
    type = SchemaType.STRING
    enum = listOf("small", "medium", "large", "xl")
    default = "medium"
    description = "T-shirt size"
}
```

### Enums from Kotlin

```kotlin
// Kotlin enum class
@Serializable
enum class OrderStatus {
    PENDING,
    PROCESSING,
    SHIPPED,
    DELIVERED,
    CANCELLED
}

// Using in OpenAPI spec
components {
    schema(OrderStatus::class)
}

// Or inline
schema {
    ref = "#/components/schemas/OrderStatus"
}
```

### Const Values

```kotlin
// Single constant value
schema {
    type = SchemaType.STRING
    const = "v1"
    description = "API version (always 'v1')"
}
```

## Nullable Types

### Basic Nullable

```kotlin
// Nullable string
schema {
    type = SchemaType.STRING
    nullable = true
    description = "Optional description"
}

// Nullable with default null
schema {
    type = SchemaType.STRING
    nullable = true
    default = null
    description = "Optional field that defaults to null"
}
```

### Nullable in Objects

```kotlin
schema {
    type = SchemaType.OBJECT
    properties {
        "requiredField" to schema {
            type = SchemaType.STRING
        }
        
        "optionalField" to schema {
            type = SchemaType.STRING
            nullable = true
        }
        
        "optionalWithDefault" to schema {
            type = SchemaType.STRING
            default = "default value"
        }
    }
    required = listOf("requiredField")  // Note: optionalField is not required
}
```

## Schema Validation

### Number Validation

```kotlin
schema {
    type = SchemaType.NUMBER
    
    // Range validation
    minimum = 0
    maximum = 100
    exclusiveMinimum = false  // >= 0
    exclusiveMaximum = true   // < 100
    
    // Multiple validation
    multipleOf = 0.25  // Must be divisible by 0.25
    
    description = "Percentage value (0-99.99)"
}

// Integer validation
schema {
    type = SchemaType.INTEGER
    minimum = 1
    maximum = 10
    description = "Rating from 1 to 10"
}
```

### String Validation

```kotlin
schema {
    type = SchemaType.STRING
    
    // Length constraints
    minLength = 3
    maxLength = 20
    
    // Pattern matching
    pattern = "^[a-zA-Z][a-zA-Z0-9_]*$"  // Must start with letter
    
    description = "Valid identifier"
}
```

### Array Validation

```kotlin
schema {
    type = SchemaType.ARRAY
    
    // Size constraints
    minItems = 1
    maxItems = 100
    
    // Unique items
    uniqueItems = true
    
    items = schema {
        type = SchemaType.STRING
        pattern = "^[A-Z0-9]+$"  // Uppercase alphanumeric
    }
    
    description = "List of unique codes"
}
```

### Object Validation

```kotlin
schema {
    type = SchemaType.OBJECT
    
    // Number of properties
    minProperties = 1
    maxProperties = 10
    
    properties {
        // Define known properties
    }
    
    // Control additional properties
    additionalProperties = false  // No extra properties allowed
}
```

## Schema From Kotlin Classes

### Basic Data Class

```kotlin
@Serializable
data class Product(
    val id: String,
    val name: String,
    val price: Double,
    val description: String? = null,
    val tags: List<String> = emptyList(),
    val inStock: Boolean = true
)

// Register in components
components {
    schema(Product::class)
}

// Use in responses
response("200", "Product details") {
    jsonContent(Product::class)
}
```

### Nested Data Classes

```kotlin
@Serializable
data class Address(
    val street: String,
    val city: String,
    val postalCode: String,
    val country: String
)

@Serializable
data class Customer(
    val id: String,
    val name: String,
    val email: String,
    val billingAddress: Address,
    val shippingAddress: Address? = null,
    val preferences: CustomerPreferences
)

@Serializable
data class CustomerPreferences(
    val newsletter: Boolean = false,
    val language: String = "en",
    val currency: String = "USD"
)

// Register all schemas
components {
    schema(Customer::class)
    schema(Address::class)
    schema(CustomerPreferences::class)
}
```

### Generic Types

```kotlin
@Serializable
data class PagedResponse<T>(
    val items: List<T>,
    val page: Int,
    val pageSize: Int,
    val total: Int
)

@Serializable
data class ApiResponse<T>(
    val success: Boolean,
    val data: T? = null,
    val error: ErrorInfo? = null
)

// Usage with specific types
response("200", "Paged products") {
    jsonContent(PagedResponse::class, Product::class)
}

response("200", "API response") {
    jsonContent(ApiResponse::class, User::class)
}
```

## Using Annotations

### Schema Description

```kotlin
@Serializable
@SchemaDescription("Represents a blog post with content and metadata")
data class BlogPost(
    @PropertyDescription("Unique identifier for the post")
    val id: String,
    
    @PropertyDescription("Post title (max 200 characters)")
    val title: String,
    
    @PropertyDescription("Post content in Markdown format")
    val content: String,
    
    @PropertyDescription("Post author information")
    val author: Author,
    
    @PropertyDescription("Publication date in ISO 8601 format")
    val publishedAt: String,
    
    @PropertyDescription("Last modification date")
    val updatedAt: String? = null,
    
    @PropertyDescription("Post categories for organization")
    val categories: List<String> = emptyList(),
    
    @PropertyDescription("SEO-friendly URL slug")
    val slug: String,
    
    @PropertyDescription("Whether the post is published")
    val isPublished: Boolean = false,
    
    @PropertyDescription("Number of views")
    val viewCount: Int = 0
)

@Serializable
@SchemaDescription("Author information")
data class Author(
    @PropertyDescription("Author's unique identifier")
    val id: String,
    
    @PropertyDescription("Author's display name")
    val name: String,
    
    @PropertyDescription("Author's email (not publicly visible)")
    val email: String,
    
    @PropertyDescription("Author's profile picture URL")
    val avatarUrl: String? = null,
    
    @PropertyDescription("Short author biography")
    val bio: String? = null
)
```

### Custom Validation Annotations

While the library uses the built-in annotations, you can extend functionality:

```kotlin
// Define validation constants
object Validation {
    const val USERNAME_PATTERN = "^[a-zA-Z0-9_]{3,20}$"
    const val PASSWORD_MIN_LENGTH = 8
    const val EMAIL_PATTERN = "^[A-Za-z0-9+_.-]+@([A-Za-z0-9.-]+\\.[A-Za-z]{2,})$"
}

@Serializable
@SchemaDescription("User registration request")
data class UserRegistration(
    @PropertyDescription("Username (3-20 characters, alphanumeric and underscore only)")
    val username: String,
    
    @PropertyDescription("Email address")
    val email: String,
    
    @PropertyDescription("Password (minimum 8 characters)")
    val password: String,
    
    @PropertyDescription("User's full name")
    val fullName: String,
    
    @PropertyDescription("Date of birth (YYYY-MM-DD)")
    val dateOfBirth: String,
    
    @PropertyDescription("Agree to terms and conditions")
    val acceptTerms: Boolean
)

// Add validation in schema definition
components {
    schema("UserRegistration") {
        type = SchemaType.OBJECT
        required = listOf("username", "email", "password", "fullName", "acceptTerms")
        properties {
            "username" to schema {
                type = SchemaType.STRING
                pattern = Validation.USERNAME_PATTERN
                minLength = 3
                maxLength = 20
            }
            "email" to schema {
                type = SchemaType.STRING
                format = "email"
                pattern = Validation.EMAIL_PATTERN
            }
            "password" to schema {
                type = SchemaType.STRING
                format = "password"
                minLength = Validation.PASSWORD_MIN_LENGTH
            }
            "fullName" to schema {
                type = SchemaType.STRING
                minLength = 1
                maxLength = 100
            }
            "dateOfBirth" to schema {
                type = SchemaType.STRING
                format = "date"
            }
            "acceptTerms" to schema {
                type = SchemaType.BOOLEAN
                const = true  // Must be true
            }
        }
    }
}
```

## Schema References

### Using References

```kotlin
// Define reusable schemas
components {
    schema("Money") {
        type = SchemaType.OBJECT
        required = listOf("amount", "currency")
        properties {
            "amount" to schema {
                type = SchemaType.NUMBER
                minimum = 0
                multipleOf = 0.01
            }
            "currency" to schema {
                type = SchemaType.STRING
                pattern = "^[A-Z]{3}$"  // ISO 4217 code
                example = "USD"
            }
        }
    }
    
    schema("Timestamp") {
        type = SchemaType.STRING
        format = "date-time"
        description = "ISO 8601 timestamp"
    }
}

// Reference in other schemas
schema {
    type = SchemaType.OBJECT
    properties {
        "price" to schema {
            ref = "#/components/schemas/Money"
        }
        "discountedPrice" to schema {
            ref = "#/components/schemas/Money"
        }
        "createdAt" to schema {
            ref = "#/components/schemas/Timestamp"
        }
        "updatedAt" to schema {
            ref = "#/components/schemas/Timestamp"
        }
    }
}
```

### Circular References

```kotlin
@Serializable
data class Category(
    val id: String,
    val name: String,
    val parentId: String? = null,
    val children: List<Category> = emptyList()  // Self-reference
)

@Serializable
data class Employee(
    val id: String,
    val name: String,
    val managerId: String? = null,
    val directReports: List<Employee> = emptyList()  // Self-reference
)

// These will be handled correctly by the schema generator
components {
    schema(Category::class)
    schema(Employee::class)
}
```

## Additional Properties

### Dynamic Properties

```kotlin
// Allow any additional properties
schema {
    type = SchemaType.OBJECT
    properties {
        "id" to schema { type = SchemaType.STRING }
        "type" to schema { type = SchemaType.STRING }
    }
    additionalProperties = true  // Allow any additional properties
}

// Additional properties with schema
schema {
    type = SchemaType.OBJECT
    properties {
        "knownField" to schema { type = SchemaType.STRING }
    }
    additionalProperties = schema {
        type = SchemaType.STRING  // All additional properties must be strings
    }
}

// No additional properties (strict)
schema {
    type = SchemaType.OBJECT
    properties {
        "field1" to schema { type = SchemaType.STRING }
        "field2" to schema { type = SchemaType.NUMBER }
    }
    additionalProperties = false  // Only field1 and field2 allowed
}
```

### Pattern Properties

```kotlin
// Properties matching patterns
schema {
    type = SchemaType.OBJECT
    patternProperties = mapOf(
        "^[a-z]+_id$" to schema {  // Properties ending with _id
            type = SchemaType.STRING
            format = "uuid"
        },
        "^is_" to schema {  // Properties starting with is_
            type = SchemaType.BOOLEAN
        },
        "_at$" to schema {  // Properties ending with _at
            type = SchemaType.STRING
            format = "date-time"
        }
    )
}
```

## Schema Examples

### Providing Examples

```kotlin
schema {
    type = SchemaType.OBJECT
    properties {
        "name" to schema {
            type = SchemaType.STRING
            example = "John Doe"
        }
        "age" to schema {
            type = SchemaType.INTEGER
            example = 30
        }
        "email" to schema {
            type = SchemaType.STRING
            format = "email"
            example = "john.doe@example.com"
        }
    }
    // Object-level example
    example = mapOf(
        "name" to "John Doe",
        "age" to 30,
        "email" to "john.doe@example.com"
    )
}
```

### Multiple Examples

```kotlin
requestBody("User data") {
    jsonContent {
        schema {
            ref = "#/components/schemas/User"
        }
        
        examples {
            example("minimal") {
                summary = "Minimal user data"
                value = mapOf(
                    "name" to "Jane",
                    "email" to "jane@example.com"
                )
            }
            
            example("complete") {
                summary = "Complete user data"
                value = mapOf(
                    "name" to "Jane Smith",
                    "email" to "jane.smith@example.com",
                    "age" to 28,
                    "phone" to "+1234567890",
                    "address" to mapOf(
                        "street" to "123 Main St",
                        "city" to "Anytown",
                        "country" to "USA"
                    )
                )
            }
            
            example("withPreferences") {
                summary = "User with preferences"
                value = mapOf(
                    "name" to "Jane Doe",
                    "email" to "jane@example.com",
                    "preferences" to mapOf(
                        "newsletter" to true,
                        "notifications" to mapOf(
                            "email" to true,
                            "sms" to false
                        )
                    )
                )
            }
        }
    }
}
```

## Best Practices

1. **Use Kotlin Data Classes**: Leverage automatic schema generation from data classes
2. **Add Descriptions**: Always add descriptions to make your API self-documenting
3. **Use Appropriate Formats**: Use standard formats (email, date, uuid) for better validation
4. **Set Constraints**: Add validation constraints (min/max, patterns) where appropriate
5. **Provide Examples**: Include realistic examples for complex schemas
6. **Reuse Schemas**: Define common schemas in components and reference them
7. **Keep It Simple**: Don't over-complicate schemas; use composition for complex structures

## Next Steps

- [Advanced Schemas](advanced-schemas.md) - Schema composition with oneOf, allOf, anyOf
- [API Operations](api-operations.md) - Using schemas in API operations
- [Reusable Components](reusable-components.md) - Creating schema libraries