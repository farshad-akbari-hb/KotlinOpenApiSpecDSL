# PropertyDescription

**Package**: `me.farshad.dsl.annotation`  
**File**: `Annotations.kt`

## Overview

`@PropertyDescription` is a property-level annotation used to provide descriptions for individual properties in Kotlin data classes when they are converted to OpenAPI schemas. This annotation enhances API documentation by adding detailed explanations for each field in a schema.

## Annotation Declaration

```kotlin
@Target(AnnotationTarget.PROPERTY)
@Retention(AnnotationRetention.RUNTIME)
annotation class PropertyDescription(val value: String)
```

## Properties

| Property | Type | Description |
|----------|------|-------------|
| `value` | `String` | The description text for the property |

## Annotation Details

- **Target**: `AnnotationTarget.PROPERTY` - Can only be applied to properties
- **Retention**: `AnnotationRetention.RUNTIME` - Available at runtime for reflection
- **Purpose**: Provides property-level documentation in OpenAPI specifications

## Usage Examples

### Basic Usage

```kotlin
data class User(
    @PropertyDescription("Unique identifier for the user")
    val id: String,
    
    @PropertyDescription("User's chosen username")
    val username: String,
    
    @PropertyDescription("User's email address")
    val email: String,
    
    @PropertyDescription("Account creation timestamp")
    val createdAt: String
)
```

### Detailed Property Descriptions

```kotlin
data class Product(
    @PropertyDescription("Stock Keeping Unit - unique product identifier")
    val sku: String,
    
    @PropertyDescription("Product display name, maximum 200 characters")
    val name: String,
    
    @PropertyDescription("HTML-formatted product description for display")
    val description: String,
    
    @PropertyDescription("Current price in USD, must be greater than 0")
    val price: Double,
    
    @PropertyDescription("Number of units in stock, null means unlimited")
    val stockQuantity: Int?,
    
    @PropertyDescription("Product weight in kilograms for shipping calculation")
    val weight: Double,
    
    @PropertyDescription("Whether the product is currently available for purchase")
    val isActive: Boolean
)
```

### Validation Hints in Descriptions

```kotlin
data class CreateUserRequest(
    @PropertyDescription("Username must be 3-20 characters, alphanumeric with underscores")
    val username: String,
    
    @PropertyDescription("Valid email address, will be used for account verification")
    val email: String,
    
    @PropertyDescription("Password must be at least 8 characters with mixed case and numbers")
    val password: String,
    
    @PropertyDescription("User's age, must be between 13 and 120")
    val age: Int?,
    
    @PropertyDescription("ISO 3166-1 alpha-2 country code (e.g., 'US', 'GB', 'DE')")
    val countryCode: String
)
```

### API Response Documentation

```kotlin
data class ApiResponse<T>(
    @PropertyDescription("Whether the request was successful")
    val success: Boolean,
    
    @PropertyDescription("Response data, null if success is false")
    val data: T?,
    
    @PropertyDescription("Error information, null if success is true")
    val error: ErrorInfo?,
    
    @PropertyDescription("ISO 8601 timestamp of when the response was generated")
    val timestamp: String,
    
    @PropertyDescription("Unique request identifier for debugging")
    val requestId: String
)
```

### Complex Types Documentation

```kotlin
data class Order(
    @PropertyDescription("Unique order identifier in format ORD-YYYY-NNNNNN")
    val orderId: String,
    
    @PropertyDescription("Customer who placed the order")
    val customer: Customer,
    
    @PropertyDescription("List of items in the order, minimum 1 item required")
    val items: List<OrderItem>,
    
    @PropertyDescription("Shipping address for physical goods")
    val shippingAddress: Address?,
    
    @PropertyDescription("Selected shipping method and cost")
    val shipping: ShippingInfo,
    
    @PropertyDescription("Payment details including method and transaction ID")
    val payment: PaymentInfo,
    
    @PropertyDescription("Current order status in the fulfillment workflow")
    val status: OrderStatus,
    
    @PropertyDescription("Special instructions from customer, maximum 500 characters")
    val notes: String?,
    
    @PropertyDescription("Total order amount including taxes and shipping")
    val totalAmount: Double,
    
    @PropertyDescription("Applied discount information if any")
    val discount: DiscountInfo?,
    
    @PropertyDescription("Order creation timestamp in UTC")
    val createdAt: String,
    
    @PropertyDescription("Last update timestamp in UTC")
    val updatedAt: String
)
```

### Enum Property Documentation

```kotlin
data class UserAccount(
    @PropertyDescription("Unique user identifier")
    val id: String,
    
    @PropertyDescription("Account status: 'active' - normal, 'suspended' - temporarily disabled, 'deleted' - marked for deletion")
    val status: AccountStatus,
    
    @PropertyDescription("User role determining permissions: 'admin' - full access, 'user' - standard access, 'guest' - limited access")
    val role: UserRole,
    
    @PropertyDescription("Subscription tier: 'free' - basic features, 'pro' - advanced features, 'enterprise' - all features")
    val subscriptionTier: SubscriptionTier
)
```

### Format and Constraint Documentation

```kotlin
data class ContactInfo(
    @PropertyDescription("Full name in format 'FirstName LastName'")
    val fullName: String,
    
    @PropertyDescription("Primary email address, must be unique in the system")
    val email: String,
    
    @PropertyDescription("Phone number in E.164 format (e.g., +1234567890)")
    val phoneNumber: String,
    
    @PropertyDescription("Date of birth in YYYY-MM-DD format")
    val dateOfBirth: String,
    
    @PropertyDescription("Preferred contact time in 24-hour format HH:MM")
    val preferredContactTime: String?,
    
    @PropertyDescription("Website URL including protocol (http:// or https://)")
    val website: String?
)
```

### Nullable and Optional Fields

```kotlin
data class UserProfile(
    @PropertyDescription("User's display name, defaults to username if not provided")
    val displayName: String?,
    
    @PropertyDescription("Profile biography, maximum 500 characters, supports markdown")
    val bio: String?,
    
    @PropertyDescription("URL to user's avatar image, must be HTTPS")
    val avatarUrl: String?,
    
    @PropertyDescription("User's location, free-form text")
    val location: String?,
    
    @PropertyDescription("Verified email address, null until email verification is completed")
    val verifiedEmail: String?,
    
    @PropertyDescription("Two-factor authentication enabled flag, defaults to false")
    val twoFactorEnabled: Boolean = false
)
```

## Integration with Schema Generation

When using annotated properties with schema generation:

```kotlin
@SchemaDescription("User profile information")
data class UserProfile(
    @PropertyDescription("Unique user identifier generated by the system")
    val id: String,
    
    @PropertyDescription("Username for login, must be unique")
    val username: String,
    
    @PropertyDescription("User's email address for notifications")
    val email: String
)

// In OpenApiBuilder
components {
    schema<UserProfile>()  // Includes both class and property descriptions
}
```

Generated OpenAPI Schema:
```yaml
components:
  schemas:
    UserProfile:
      type: object
      description: "User profile information"
      properties:
        id:
          type: string
          description: "Unique user identifier generated by the system"
        username:
          type: string
          description: "Username for login, must be unique"
        email:
          type: string
          description: "User's email address for notifications"
```

## Best Practices

### 1. Include Type Information

```kotlin
// Good - includes type context
@PropertyDescription("Creation timestamp in ISO 8601 format")
val createdAt: String

// Less helpful
@PropertyDescription("When it was created")
val createdAt: String
```

### 2. Document Constraints

```kotlin
@PropertyDescription("Product price in USD, must be between 0.01 and 999999.99")
val price: Double

@PropertyDescription("Username between 3-30 characters, alphanumeric plus underscore")
val username: String
```

### 3. Explain Business Logic

```kotlin
@PropertyDescription("Order total after discounts but before taxes and shipping")
val subtotal: Double

@PropertyDescription("Loyalty points earned, calculated as 1 point per dollar spent")
val pointsEarned: Int
```

### 4. Document Relationships

```kotlin
@PropertyDescription("ID of the user who created this record")
val createdBy: String

@PropertyDescription("Parent category ID, null for top-level categories")
val parentCategoryId: String?
```

### 5. Clarify Units and Formats

```kotlin
@PropertyDescription("Distance in kilometers, rounded to 2 decimal places")
val distance: Double

@PropertyDescription("Temperature in Celsius, range -273.15 to 5000")
val temperature: Double

@PropertyDescription("Duration in seconds")
val duration: Long
```

## Common Patterns

### ID Fields

```kotlin
@PropertyDescription("Unique identifier in UUID v4 format")
val id: String

@PropertyDescription("MongoDB ObjectId as 24-character hex string")
val _id: String

@PropertyDescription("Auto-incrementing integer ID starting from 1")
val id: Long
```

### Timestamp Fields

```kotlin
@PropertyDescription("Record creation time in UTC, ISO 8601 format")
val createdAt: String

@PropertyDescription("Last modification time in UTC, automatically updated")
val updatedAt: String

@PropertyDescription("Soft delete timestamp, null if not deleted")
val deletedAt: String?
```

### Status and State Fields

```kotlin
@PropertyDescription("Current processing status, see StatusEnum for possible values")
val status: String

@PropertyDescription("Whether the feature is enabled for this account")
val featureEnabled: Boolean

@PropertyDescription("Account verification status: pending, verified, or rejected")
val verificationStatus: String
```

### Reference Fields

```kotlin
@PropertyDescription("Reference to the User who owns this resource")
val userId: String

@PropertyDescription("Foreign key to the Product table")
val productId: String

@PropertyDescription("Array of Tag IDs associated with this item")
val tagIds: List<String>
```

## Complex Examples

### E-commerce Product

```kotlin
data class Product(
    @PropertyDescription("Unique SKU assigned by inventory system")
    val sku: String,
    
    @PropertyDescription("Product title for display, SEO-optimized")
    val title: String,
    
    @PropertyDescription("Brief product summary, maximum 160 characters")
    val summary: String,
    
    @PropertyDescription("Full product description with HTML formatting")
    val description: String,
    
    @PropertyDescription("Base price before any discounts, in cents to avoid floating point issues")
    val priceInCents: Int,
    
    @PropertyDescription("Compare-at price for showing savings, null if no comparison")
    val compareAtPriceInCents: Int?,
    
    @PropertyDescription("Available inventory across all warehouses")
    val availableQuantity: Int,
    
    @PropertyDescription("Minimum order quantity, defaults to 1")
    val minimumOrderQuantity: Int = 1,
    
    @PropertyDescription("Product images ordered by display priority")
    val images: List<ProductImage>,
    
    @PropertyDescription("Custom attributes as key-value pairs for filtering")
    val attributes: Map<String, String>,
    
    @PropertyDescription("SEO meta description, falls back to summary if null")
    val metaDescription: String?,
    
    @PropertyDescription("Search keywords, comma-separated, maximum 10 keywords")
    val searchKeywords: String?,
    
    @PropertyDescription("Whether the product is visible in catalog")
    val isPublished: Boolean,
    
    @PropertyDescription("Scheduled publish date, null means immediately")
    val publishAt: String?,
    
    @PropertyDescription("Average rating from customer reviews, 0-5 scale")
    val averageRating: Double,
    
    @PropertyDescription("Total number of customer reviews")
    val reviewCount: Int
)
```

### API Configuration

```kotlin
data class ApiConfiguration(
    @PropertyDescription("API version in semver format (e.g., '1.2.3')")
    val version: String,
    
    @PropertyDescription("Base URL for all API endpoints, must include protocol")
    val baseUrl: String,
    
    @PropertyDescription("Request timeout in milliseconds, default 30000")
    val timeoutMs: Int = 30000,
    
    @PropertyDescription("Maximum retry attempts for failed requests, 0 disables retries")
    val maxRetries: Int = 3,
    
    @PropertyDescription("Delay between retries in milliseconds, doubles after each attempt")
    val retryDelayMs: Int = 1000,
    
    @PropertyDescription("API key for authentication, required for all requests")
    val apiKey: String,
    
    @PropertyDescription("Additional headers to include in all requests")
    val defaultHeaders: Map<String, String>,
    
    @PropertyDescription("Enable request/response logging for debugging")
    val debugMode: Boolean = false,
    
    @PropertyDescription("Proxy configuration, null for direct connection")
    val proxyConfig: ProxyConfig?,
    
    @PropertyDescription("SSL certificate validation, disable only for development")
    val validateSsl: Boolean = true
)
```

## Limitations

1. **Property Level Only**: Cannot be applied to classes (use `@SchemaDescription` instead)
2. **Single Description**: Each property can have only one description
3. **No Dynamic Content**: Descriptions must be compile-time constants
4. **No Localization**: Built-in support for only one language

## Related Annotations

- [SchemaDescription](SchemaDescription.md) - For class-level descriptions
- See also: [ComponentsBuilder](../builder/components/ComponentsBuilder.md) for schema generation
- See also: [SchemaBuilder](../../../../../../../capabilities/SchemaBuilder.md) for manual schema construction