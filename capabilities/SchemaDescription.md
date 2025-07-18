# SchemaDescription

**Package**: `me.farshad.dsl.annotation`  
**File**: `Annotations.kt`

## Overview

`@SchemaDescription` is a class-level annotation used to provide descriptions for Kotlin data classes when they are converted to OpenAPI schemas. This annotation enhances the generated API documentation by adding human-readable descriptions to schema definitions.

## Annotation Declaration

```kotlin
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
annotation class SchemaDescription(val value: String)
```

## Properties

| Property | Type | Description |
|----------|------|-------------|
| `value` | `String` | The description text for the schema |

## Annotation Details

- **Target**: `AnnotationTarget.CLASS` - Can only be applied to classes
- **Retention**: `AnnotationRetention.RUNTIME` - Available at runtime for reflection
- **Purpose**: Provides schema-level documentation in OpenAPI specifications

## Usage Examples

### Basic Usage

```kotlin
@SchemaDescription("Represents a user account in the system")
data class User(
    val id: String,
    val username: String,
    val email: String,
    val createdAt: String
)
```

### Detailed Description

```kotlin
@SchemaDescription("""
    Customer information including personal details, contact information,
    and preferences. This model is used for both B2C and B2B customers
    with appropriate fields populated based on customer type.
""")
data class Customer(
    val id: String,
    val type: CustomerType,
    val name: String,
    val email: String,
    val phone: String?,
    val company: Company?,
    val preferences: CustomerPreferences
)
```

### Domain Model Examples

```kotlin
@SchemaDescription("Product information for the e-commerce catalog")
data class Product(
    val sku: String,
    val name: String,
    val description: String,
    val price: Double,
    val category: String,
    val inStock: Boolean
)

@SchemaDescription("Order containing customer information, items, and payment details")
data class Order(
    val orderId: String,
    val customerId: String,
    val items: List<OrderItem>,
    val totalAmount: Double,
    val status: OrderStatus,
    val createdAt: String
)

@SchemaDescription("Shopping cart that persists across user sessions")
data class ShoppingCart(
    val cartId: String,
    val userId: String?,
    val items: List<CartItem>,
    val expiresAt: String
)
```

### API Request/Response Models

```kotlin
@SchemaDescription("Request payload for creating a new user account")
data class CreateUserRequest(
    val username: String,
    val email: String,
    val password: String,
    val profile: UserProfile?
)

@SchemaDescription("Response returned after successful user creation")
data class CreateUserResponse(
    val userId: String,
    val username: String,
    val email: String,
    val createdAt: String,
    val links: Links
)

@SchemaDescription("Standardized error response for all API endpoints")
data class ErrorResponse(
    val error: ErrorDetails,
    val timestamp: String,
    val path: String,
    val traceId: String
)
```

### Nested Model Descriptions

```kotlin
@SchemaDescription("Physical address for shipping or billing")
data class Address(
    val street1: String,
    val street2: String?,
    val city: String,
    val state: String,
    val postalCode: String,
    val country: String
)

@SchemaDescription("Complete user profile including addresses and preferences")
data class UserProfile(
    val firstName: String,
    val lastName: String,
    val dateOfBirth: String?,
    val phoneNumber: String?,
    val billingAddress: Address,
    val shippingAddresses: List<Address>,
    val preferences: UserPreferences
)
```

## Integration with Schema Generation

When using the `ComponentsBuilder.schema()` method with annotated classes:

```kotlin
components {
    // Automatically includes the description from @SchemaDescription
    schema<User>()
    schema<Product>()
    schema<Order>()
}
```

Generated OpenAPI Schema:
```yaml
components:
  schemas:
    User:
      type: object
      description: "Represents a user account in the system"
      properties:
        id:
          type: string
        username:
          type: string
        email:
          type: string
        createdAt:
          type: string
```

## Best Practices

### 1. Be Descriptive but Concise

```kotlin
// Good
@SchemaDescription("User account with authentication and profile information")

// Too vague
@SchemaDescription("User data")

// Too verbose
@SchemaDescription("This class represents a user entity in our system database with all the fields needed for authentication, authorization, profile management, and various other features that users might need")
```

### 2. Include Business Context

```kotlin
@SchemaDescription("""
    Payment method used for processing transactions.
    Supports credit cards, bank transfers, and digital wallets.
    PCI compliance required for credit card data.
""")
data class PaymentMethod(
    val type: PaymentType,
    val details: Map<String, Any>
)
```

### 3. Mention Constraints and Rules

```kotlin
@SchemaDescription("""
    Product inventory record tracking stock levels.
    Stock is updated in real-time and reservations expire after 15 minutes.
""")
data class Inventory(
    val productId: String,
    val available: Int,
    val reserved: Int,
    val lastUpdated: String
)
```

### 4. API Version Information

```kotlin
@SchemaDescription("User model v2 - includes social profiles (added in API v2.0)")
data class UserV2(
    val id: String,
    val username: String,
    val email: String,
    val socialProfiles: Map<String, String>? // New in v2
)
```

### 5. Use for Enum Classes

```kotlin
@SchemaDescription("Order status throughout the fulfillment lifecycle")
enum class OrderStatus {
    PENDING,
    CONFIRMED,
    PROCESSING,
    SHIPPED,
    DELIVERED,
    CANCELLED,
    REFUNDED
}
```

## Common Patterns

### Entity Documentation

```kotlin
@SchemaDescription("Core business entity representing a product in our catalog")
data class ProductEntity(
    val id: String,
    val name: String,
    val description: String,
    val metadata: Map<String, Any>
)
```

### DTO Documentation

```kotlin
@SchemaDescription("Data transfer object for product information in API responses")
data class ProductDTO(
    val id: String,
    val name: String,
    val price: String, // Formatted with currency
    val availability: String
)
```

### Event Documentation

```kotlin
@SchemaDescription("Event emitted when an order status changes")
data class OrderStatusChangedEvent(
    val orderId: String,
    val previousStatus: OrderStatus,
    val newStatus: OrderStatus,
    val changedAt: String,
    val changedBy: String
)
```

### Configuration Documentation

```kotlin
@SchemaDescription("Application settings that can be modified by administrators")
data class AppSettings(
    val maintenanceMode: Boolean,
    val maxUploadSize: Long,
    val allowedFileTypes: List<String>,
    val features: Map<String, Boolean>
)
```

## Combining with PropertyDescription

```kotlin
@SchemaDescription("Comprehensive product information for e-commerce platform")
data class Product(
    @PropertyDescription("Unique SKU identifier")
    val sku: String,
    
    @PropertyDescription("Display name shown to customers")
    val name: String,
    
    @PropertyDescription("Rich text description with HTML support")
    val description: String,
    
    @PropertyDescription("Current price in USD")
    val price: Double,
    
    @PropertyDescription("Whether the product is available for purchase")
    val available: Boolean
)
```

## Multi-line Descriptions

For complex schemas, use multi-line strings:

```kotlin
@SchemaDescription("""
    Financial transaction record for payment processing.
    
    Contains all information required for:
    - Transaction authorization
    - Settlement processing  
    - Reconciliation
    - Audit trail
    
    Sensitive fields are encrypted at rest and masked in logs.
""")
data class Transaction(
    val transactionId: String,
    val amount: Double,
    val currency: String,
    val status: TransactionStatus,
    val metadata: TransactionMetadata
)
```

## Limitations

1. **Single Language**: Descriptions are in a single language (no i18n support)
2. **Static Text**: Descriptions must be compile-time constants
3. **Class Level Only**: Cannot be applied to properties (use `@PropertyDescription` instead)
4. **No Markdown**: While multi-line strings work, markdown formatting may not render in all tools

## Related Annotations

- [PropertyDescription](PropertyDescription.md) - For property-level descriptions
- See also: [ComponentsBuilder](ComponentsBuilder.md) for schema generation
- See also: [SchemaBuilder](SchemaBuilder.md) for manual schema construction