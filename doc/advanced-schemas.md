# Advanced Schemas

This guide covers complex schema compositions and advanced features including `oneOf`, `allOf`, `anyOf`, `not`, discriminators, and polymorphism.

## Table of Contents
- [Schema Composition Overview](#schema-composition-overview)
- [OneOf - Exactly One Schema](#oneof---exactly-one-schema)
- [AllOf - Combine Multiple Schemas](#allof---combine-multiple-schemas)
- [AnyOf - One or More Schemas](#anyof---one-or-more-schemas)
- [Not - Schema Negation](#not---schema-negation)
- [Discriminators](#discriminators)
- [Polymorphism](#polymorphism)
- [Complex Real-World Examples](#complex-real-world-examples)
- [Schema Reference Types](#schema-reference-types)
- [Circular References](#circular-references)

## Schema Composition Overview

The Kotlin OpenAPI Spec DSL provides powerful schema composition features through:

- **`oneOf`**: Validates against exactly one schema
- **`allOf`**: Validates against all schemas (intersection)
- **`anyOf`**: Validates against one or more schemas
- **`not`**: Validates that the schema does NOT match
- **Discriminators**: Helps identify which schema to use

### Import Schema Composition Helpers

```kotlin
import me.farshad.openapi.*
import me.farshad.openapi.builder.*
import kotlinx.serialization.Serializable
```

## OneOf - Exactly One Schema

### Basic OneOf Usage

```kotlin
// Define different payment method schemas
@Serializable
data class CreditCardPayment(
    val type: String = "credit_card",
    val cardNumber: String,
    val expiryMonth: Int,
    val expiryYear: Int,
    val cvv: String
)

@Serializable
data class PayPalPayment(
    val type: String = "paypal",
    val email: String,
    val paypalId: String
)

@Serializable
data class BankTransferPayment(
    val type: String = "bank_transfer",
    val accountNumber: String,
    val routingNumber: String,
    val bankName: String
)

// Using oneOf in schema
components {
    schema("PaymentMethod") {
        oneOf = listOf(
            SchemaReference.ReferenceTo("#/components/schemas/CreditCardPayment"),
            SchemaReference.ReferenceTo("#/components/schemas/PayPalPayment"),
            SchemaReference.ReferenceTo("#/components/schemas/BankTransferPayment")
        )
        discriminator {
            propertyName = "type"
            mapping = mapOf(
                "credit_card" to "#/components/schemas/CreditCardPayment",
                "paypal" to "#/components/schemas/PayPalPayment",
                "bank_transfer" to "#/components/schemas/BankTransferPayment"
            )
        }
    }
    
    // Register individual schemas
    schema(CreditCardPayment::class)
    schema(PayPalPayment::class)
    schema(BankTransferPayment::class)
}
```

### OneOf with Inline Schemas

```kotlin
schema {
    oneOf = listOf(
        // String schema
        SchemaReference.Schema(Schema(
            type = SchemaType.STRING,
            minLength = 1
        )),
        
        // Number schema
        SchemaReference.Schema(Schema(
            type = SchemaType.NUMBER,
            minimum = 0
        )),
        
        // Boolean schema
        SchemaReference.Schema(Schema(
            type = SchemaType.BOOLEAN
        ))
    )
    description = "Can be a string, number, or boolean"
}
```

### Using Helper Functions

```kotlin
// Using the oneOf helper function
val flexibleValue = oneOf(
    schema { 
        type = SchemaType.STRING 
        pattern = "^[A-Z]+$"
    },
    schema { 
        type = SchemaType.INTEGER
        minimum = 0
        maximum = 100
    },
    schema {
        type = SchemaType.ARRAY
        items = schema { type = SchemaType.STRING }
    }
)

// Using the or operator
val stringOrNumber = SchemaReference.Schema(Schema(type = SchemaType.STRING)) or 
                     SchemaReference.Schema(Schema(type = SchemaType.NUMBER))
```

## AllOf - Combine Multiple Schemas

### Basic AllOf Usage

```kotlin
// Base schemas
@Serializable
open class BaseEntity(
    val id: String,
    val createdAt: String,
    val updatedAt: String
)

@Serializable
data class AuditInfo(
    val createdBy: String,
    val updatedBy: String,
    val version: Int
)

// Combining schemas with allOf
components {
    schema("AuditedEntity") {
        allOf = listOf(
            SchemaReference.ReferenceTo("#/components/schemas/BaseEntity"),
            SchemaReference.ReferenceTo("#/components/schemas/AuditInfo"),
            SchemaReference.Schema(Schema(
                type = SchemaType.OBJECT,
                properties = mapOf(
                    "isDeleted" to Schema(type = SchemaType.BOOLEAN, default = false),
                    "deletedAt" to Schema(type = SchemaType.STRING, nullable = true)
                )
            ))
        )
        description = "Entity with full audit trail"
    }
}
```

### AllOf for Extension

```kotlin
// Define a base user schema
schema("BaseUser") {
    type = SchemaType.OBJECT
    required = listOf("id", "email")
    properties {
        "id" to schema { 
            type = SchemaType.STRING 
            format = "uuid"
        }
        "email" to schema { 
            type = SchemaType.STRING 
            format = "email"
        }
        "name" to schema { type = SchemaType.STRING }
    }
}

// Extend with additional properties
schema("AdminUser") {
    allOf = listOf(
        SchemaReference.ReferenceTo("#/components/schemas/BaseUser"),
        SchemaReference.Schema(Schema(
            type = SchemaType.OBJECT,
            required = listOf("adminLevel", "permissions"),
            properties = mapOf(
                "adminLevel" to Schema(
                    type = SchemaType.STRING,
                    enum = listOf("super", "regular", "readonly")
                ),
                "permissions" to Schema(
                    type = SchemaType.ARRAY,
                    items = Schema(type = SchemaType.STRING)
                ),
                "lastLoginAt" to Schema(
                    type = SchemaType.STRING,
                    format = "date-time"
                )
            )
        ))
    )
}
```

### Using AllOf Helper Functions

```kotlin
// Using the allOf helper
val completeUser = allOf(
    ref("#/components/schemas/BaseUser"),
    ref("#/components/schemas/ContactInfo"),
    ref("#/components/schemas/Preferences")
)

// Using the and operator
val userWithProfile = ref("#/components/schemas/User") and 
                      ref("#/components/schemas/Profile")

// Extending a schema
val extendedProduct = extendingSchema(
    baseRef = "#/components/schemas/Product",
    additionalProperties = mapOf(
        "discount" to Schema(type = SchemaType.NUMBER),
        "promotionCode" to Schema(type = SchemaType.STRING)
    ),
    additionalRequired = listOf("discount")
)
```

## AnyOf - One or More Schemas

### Basic AnyOf Usage

```kotlin
// Contact information - can have any combination
schema("ContactInfo") {
    anyOf = listOf(
        SchemaReference.Schema(Schema(
            type = SchemaType.OBJECT,
            properties = mapOf(
                "email" to Schema(type = SchemaType.STRING, format = "email")
            )
        )),
        SchemaReference.Schema(Schema(
            type = SchemaType.OBJECT,
            properties = mapOf(
                "phone" to Schema(type = SchemaType.STRING, pattern = "^\\+?[1-9]\\d{1,14}$")
            )
        )),
        SchemaReference.Schema(Schema(
            type = SchemaType.OBJECT,
            properties = mapOf(
                "address" to Schema(
                    type = SchemaType.OBJECT,
                    properties = mapOf(
                        "street" to Schema(type = SchemaType.STRING),
                        "city" to Schema(type = SchemaType.STRING),
                        "country" to Schema(type = SchemaType.STRING)
                    )
                )
            )
        ))
    )
    description = "At least one contact method required"
}
```

### AnyOf for Flexible Types

```kotlin
// Flexible identifier - can be various formats
schema("Identifier") {
    anyOf = listOf(
        // UUID format
        SchemaReference.Schema(Schema(
            type = SchemaType.STRING,
            format = "uuid"
        )),
        // Email format
        SchemaReference.Schema(Schema(
            type = SchemaType.STRING,
            format = "email"
        )),
        // Numeric ID
        SchemaReference.Schema(Schema(
            type = SchemaType.INTEGER,
            minimum = 1
        )),
        // Custom format
        SchemaReference.Schema(Schema(
            type = SchemaType.STRING,
            pattern = "^[A-Z]{2}[0-9]{6}$"
        ))
    )
}
```

## Not - Schema Negation

### Basic Not Usage

```kotlin
// Not empty string
schema {
    not = Schema(
        type = SchemaType.STRING,
        maxLength = 0
    )
    description = "Non-empty string"
}

// Not a specific value
schema {
    not = Schema(
        type = SchemaType.STRING,
        const = "forbidden"
    )
    description = "Any string except 'forbidden'"
}

// Not null
schema {
    not = Schema(
        type = SchemaType.NULL
    )
    description = "Any non-null value"
}
```

### Complex Not Conditions

```kotlin
// Not a specific pattern
schema("Username") {
    allOf = listOf(
        SchemaReference.Schema(Schema(
            type = SchemaType.STRING,
            minLength = 3,
            maxLength = 20
        )),
        SchemaReference.Schema(Schema(
            not = Schema(
                type = SchemaType.STRING,
                pattern = "^(admin|root|superuser).*"
            )
        ))
    )
    description = "Username that doesn't start with reserved words"
}
```

## Discriminators

### Basic Discriminator

```kotlin
// Animal hierarchy with discriminator
sealed class Animal {
    @Serializable
    data class Dog(
        val animalType: String = "dog",
        val breed: String,
        val barkVolume: Int
    )
    
    @Serializable
    data class Cat(
        val animalType: String = "cat",
        val breed: String,
        val hasClaws: Boolean
    )
    
    @Serializable
    data class Bird(
        val animalType: String = "bird",
        val species: String,
        val wingspan: Double
    )
}

components {
    schema("Animal") {
        oneOf = listOf(
            SchemaReference.ReferenceTo("#/components/schemas/Dog"),
            SchemaReference.ReferenceTo("#/components/schemas/Cat"),
            SchemaReference.ReferenceTo("#/components/schemas/Bird")
        )
        discriminator {
            propertyName = "animalType"
            mapping = mapOf(
                "dog" to "#/components/schemas/Dog",
                "cat" to "#/components/schemas/Cat",
                "bird" to "#/components/schemas/Bird"
            )
        }
    }
    
    schema(Animal.Dog::class)
    schema(Animal.Cat::class)
    schema(Animal.Bird::class)
}
```

### Using Discriminator Helper

```kotlin
// Using the discriminatedUnion helper
val shapeSchema = discriminatedUnion(
    propertyName = "shapeType",
    mapping = mapOf(
        "circle" to "#/components/schemas/Circle",
        "rectangle" to "#/components/schemas/Rectangle",
        "triangle" to "#/components/schemas/Triangle"
    )
)

// More complex discriminator with inline schemas
val eventSchema = discriminatedUnion(
    propertyName = "eventType",
    oneOfSchemas = listOf(
        SchemaReference.Schema(Schema(
            type = SchemaType.OBJECT,
            required = listOf("eventType", "userId", "timestamp"),
            properties = mapOf(
                "eventType" to Schema(type = SchemaType.STRING, const = "login"),
                "userId" to Schema(type = SchemaType.STRING),
                "timestamp" to Schema(type = SchemaType.STRING, format = "date-time"),
                "ipAddress" to Schema(type = SchemaType.STRING)
            )
        )),
        SchemaReference.Schema(Schema(
            type = SchemaType.OBJECT,
            required = listOf("eventType", "userId", "itemId", "quantity"),
            properties = mapOf(
                "eventType" to Schema(type = SchemaType.STRING, const = "purchase"),
                "userId" to Schema(type = SchemaType.STRING),
                "itemId" to Schema(type = SchemaType.STRING),
                "quantity" to Schema(type = SchemaType.INTEGER),
                "price" to Schema(type = SchemaType.NUMBER)
            )
        ))
    )
)
```

## Polymorphism

### Interface-based Polymorphism

```kotlin
// Define interface and implementations
interface Vehicle {
    val vehicleType: String
    val manufacturer: String
    val model: String
}

@Serializable
data class Car(
    override val vehicleType: String = "car",
    override val manufacturer: String,
    override val model: String,
    val numberOfDoors: Int,
    val fuelType: String
) : Vehicle

@Serializable
data class Motorcycle(
    override val vehicleType: String = "motorcycle",
    override val manufacturer: String,
    override val model: String,
    val engineSize: Int,
    val hasSidecar: Boolean
) : Vehicle

@Serializable
data class Truck(
    override val vehicleType: String = "truck",
    override val manufacturer: String,
    override val model: String,
    val payloadCapacity: Int,
    val numberOfAxles: Int
) : Vehicle

// Schema definition
components {
    schema("Vehicle") {
        oneOf = listOf(
            SchemaReference.ReferenceTo("#/components/schemas/Car"),
            SchemaReference.ReferenceTo("#/components/schemas/Motorcycle"),
            SchemaReference.ReferenceTo("#/components/schemas/Truck")
        )
        discriminator {
            propertyName = "vehicleType"
        }
    }
    
    schema(Car::class)
    schema(Motorcycle::class)
    schema(Truck::class)
}
```

### Generic Response Wrapper

```kotlin
// Generic API response with polymorphic data
@Serializable
data class ApiResponse<T>(
    val success: Boolean,
    val data: T? = null,
    val error: ErrorInfo? = null,
    val metadata: ResponseMetadata
)

@Serializable
data class ResponseMetadata(
    val timestamp: String,
    val version: String,
    val requestId: String
)

// Different response types
@Serializable
data class UserResponse(
    val user: User,
    val permissions: List<String>
)

@Serializable
data class ProductListResponse(
    val products: List<Product>,
    val totalCount: Int,
    val hasMore: Boolean
)

// Usage in paths
paths {
    path("/users/{id}") {
        get {
            response("200", "Success") {
                jsonContent {
                    schema {
                        allOf = listOf(
                            SchemaReference.ReferenceTo("#/components/schemas/ApiResponse"),
                            SchemaReference.Schema(Schema(
                                type = SchemaType.OBJECT,
                                properties = mapOf(
                                    "data" to Schema(
                                        ref = "#/components/schemas/UserResponse"
                                    )
                                )
                            ))
                        )
                    }
                }
            }
        }
    }
}
```

## Complex Real-World Examples

### E-commerce Order System

```kotlin
// Order with multiple item types
@Serializable
sealed class OrderItem {
    abstract val itemType: String
    abstract val quantity: Int
    abstract val price: Double
    
    @Serializable
    data class PhysicalProduct(
        override val itemType: String = "physical",
        override val quantity: Int,
        override val price: Double,
        val productId: String,
        val shippingWeight: Double,
        val dimensions: Dimensions
    ) : OrderItem()
    
    @Serializable
    data class DigitalProduct(
        override val itemType: String = "digital",
        override val quantity: Int,
        override val price: Double,
        val productId: String,
        val downloadUrl: String,
        val licenseKey: String?
    ) : OrderItem()
    
    @Serializable
    data class Service(
        override val itemType: String = "service",
        override val quantity: Int,
        override val price: Double,
        val serviceId: String,
        val scheduledDate: String,
        val duration: Int
    ) : OrderItem()
}

@Serializable
data class Dimensions(
    val length: Double,
    val width: Double,
    val height: Double,
    val unit: String = "cm"
)

// Order schema with mixed payment methods and items
components {
    schema("Order") {
        type = SchemaType.OBJECT
        required = listOf("id", "customerId", "items", "payment", "status")
        properties {
            "id" to schema { 
                type = SchemaType.STRING 
                format = "uuid"
            }
            "customerId" to schema { 
                type = SchemaType.STRING 
            }
            "items" to schema {
                type = SchemaType.ARRAY
                minItems = 1
                items = schema {
                    ref = "#/components/schemas/OrderItem"
                }
            }
            "payment" to schema {
                ref = "#/components/schemas/PaymentMethod"
            }
            "shipping" to schema {
                oneOf = listOf(
                    SchemaReference.ReferenceTo("#/components/schemas/StandardShipping"),
                    SchemaReference.ReferenceTo("#/components/schemas/ExpressShipping"),
                    SchemaReference.ReferenceTo("#/components/schemas/PickupInStore")
                )
                discriminator {
                    propertyName = "type"
                }
            }
            "status" to schema {
                type = SchemaType.STRING
                enum = listOf("pending", "processing", "shipped", "delivered", "cancelled")
            }
        }
    }
    
    // Register OrderItem schemas
    schema("OrderItem") {
        oneOf = listOf(
            SchemaReference.ReferenceTo("#/components/schemas/PhysicalProduct"),
            SchemaReference.ReferenceTo("#/components/schemas/DigitalProduct"),
            SchemaReference.ReferenceTo("#/components/schemas/Service")
        )
        discriminator {
            propertyName = "itemType"
        }
    }
}
```

### Multi-tenant Configuration System

```kotlin
// Configuration with environment-specific overrides
components {
    schema("Configuration") {
        type = SchemaType.OBJECT
        properties {
            "base" to schema {
                ref = "#/components/schemas/BaseConfig"
            }
            "overrides" to schema {
                type = SchemaType.OBJECT
                properties {
                    "development" to schema {
                        allOf = listOf(
                            SchemaReference.ReferenceTo("#/components/schemas/BaseConfig"),
                            SchemaReference.Schema(Schema(
                                type = SchemaType.OBJECT,
                                properties = mapOf(
                                    "debugMode" to Schema(type = SchemaType.BOOLEAN, default = true),
                                    "logLevel" to Schema(type = SchemaType.STRING, default = "DEBUG")
                                )
                            ))
                        )
                    }
                    "staging" to schema {
                        allOf = listOf(
                            SchemaReference.ReferenceTo("#/components/schemas/BaseConfig"),
                            SchemaReference.Schema(Schema(
                                type = SchemaType.OBJECT,
                                properties = mapOf(
                                    "enableMetrics" to Schema(type = SchemaType.BOOLEAN, default = true)
                                )
                            ))
                        )
                    }
                    "production" to schema {
                        allOf = listOf(
                            SchemaReference.ReferenceTo("#/components/schemas/BaseConfig"),
                            SchemaReference.Schema(Schema(
                                type = SchemaType.OBJECT,
                                required = listOf("ssl", "clustering"),
                                properties = mapOf(
                                    "ssl" to Schema(
                                        type = SchemaType.OBJECT,
                                        properties = mapOf(
                                            "enabled" to Schema(type = SchemaType.BOOLEAN, const = true),
                                            "certificate" to Schema(type = SchemaType.STRING),
                                            "key" to Schema(type = SchemaType.STRING)
                                        )
                                    ),
                                    "clustering" to Schema(
                                        type = SchemaType.OBJECT,
                                        properties = mapOf(
                                            "enabled" to Schema(type = SchemaType.BOOLEAN),
                                            "nodes" to Schema(
                                                type = SchemaType.ARRAY,
                                                minItems = 2,
                                                items = Schema(type = SchemaType.STRING)
                                            )
                                        )
                                    )
                                )
                            ))
                        )
                    }
                }
            }
        }
    }
}
```

## Schema Reference Types

### Working with SchemaReference

```kotlin
// Different ways to create schema references
val stringRef = SchemaReference.ReferenceTo("#/components/schemas/StringType")
val inlineSchema = SchemaReference.Schema(Schema(type = SchemaType.STRING))

// Using helper functions
val userRef = ref("#/components/schemas/User")
val productRef = ref(Product::class)  // Generates reference from class

// Nullable references
val nullableUser = nullable(ref("#/components/schemas/User"))

// Combining references
val userOrGuest = ref("#/components/schemas/User") or ref("#/components/schemas/Guest")
```

### Schema Reference Patterns

```kotlin
// Pattern 1: Direct class reference
components {
    schema(User::class)
    schema(Product::class)
    
    schema("Cart") {
        type = SchemaType.OBJECT
        properties {
            "user" to schema { ref = ref(User::class) }
            "items" to schema {
                type = SchemaType.ARRAY
                items = schema { ref = ref(Product::class) }
            }
        }
    }
}

// Pattern 2: String references
components {
    schema("OrderSummary") {
        type = SchemaType.OBJECT
        properties {
            "order" to schema { ref = "#/components/schemas/Order" }
            "customer" to schema { ref = "#/components/schemas/Customer" }
            "invoice" to schema { ref = "#/components/schemas/Invoice" }
        }
    }
}

// Pattern 3: Mixed inline and references
components {
    schema("FlexibleContent") {
        oneOf = listOf(
            ref("#/components/schemas/Article"),
            ref("#/components/schemas/Video"),
            SchemaReference.Schema(Schema(
                type = SchemaType.OBJECT,
                properties = mapOf(
                    "type" to Schema(type = SchemaType.STRING, const = "external"),
                    "url" to Schema(type = SchemaType.STRING, format = "uri")
                )
            ))
        )
    }
}
```

## Circular References

### Handling Recursive Structures

```kotlin
// Tree structure with circular reference
@Serializable
data class TreeNode(
    val id: String,
    val value: String,
    val children: List<TreeNode> = emptyList()  // Self-reference
)

// Graph structure with circular references
@Serializable
data class GraphNode(
    val id: String,
    val data: Map<String, Any>,
    val connections: List<GraphNode> = emptyList()
)

// File system structure
@Serializable
sealed class FileSystemEntry {
    abstract val name: String
    abstract val path: String
    
    @Serializable
    data class File(
        override val name: String,
        override val path: String,
        val size: Long,
        val mimeType: String
    ) : FileSystemEntry()
    
    @Serializable
    data class Directory(
        override val name: String,
        override val path: String,
        val entries: List<FileSystemEntry> = emptyList()  // Circular reference
    ) : FileSystemEntry()
}

// These will be handled correctly
components {
    schema(TreeNode::class)
    schema(GraphNode::class)
    schema(FileSystemEntry::class)
    schema(FileSystemEntry.File::class)
    schema(FileSystemEntry.Directory::class)
}
```

### Breaking Circular Dependencies

```kotlin
// Using references to break cycles
components {
    schema("Department") {
        type = SchemaType.OBJECT
        properties {
            "id" to schema { type = SchemaType.STRING }
            "name" to schema { type = SchemaType.STRING }
            "manager" to schema { ref = "#/components/schemas/Employee" }
            "employees" to schema {
                type = SchemaType.ARRAY
                items = schema { ref = "#/components/schemas/Employee" }
            }
        }
    }
    
    schema("Employee") {
        type = SchemaType.OBJECT
        properties {
            "id" to schema { type = SchemaType.STRING }
            "name" to schema { type = SchemaType.STRING }
            "department" to schema { ref = "#/components/schemas/Department" }
            "reportsTo" to schema { 
                ref = "#/components/schemas/Employee"
                nullable = true
            }
        }
    }
}
```

## Best Practices

1. **Use Discriminators**: Always use discriminators with `oneOf` for better client generation
2. **Keep It Simple**: Don't over-complicate schemas; sometimes multiple endpoints are clearer
3. **Document Everything**: Add descriptions to complex schemas explaining the validation rules
4. **Test Schema Validation**: Ensure your schemas actually validate the data you expect
5. **Consider Client Generation**: Design schemas that work well with code generators
6. **Use References**: Reuse schemas via references to maintain consistency
7. **Avoid Deep Nesting**: Deeply nested `allOf`/`oneOf` can be hard to understand

## Next Steps

- [API Operations](api-operations.md) - Using advanced schemas in API operations
- [Request & Response](request-response.md) - Complex request/response patterns
- [Reusable Components](reusable-components.md) - Building schema libraries