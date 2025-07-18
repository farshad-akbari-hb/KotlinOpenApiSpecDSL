# InfoBuilder

**Package**: `me.farshad.dsl.builder.info`  
**File**: `InfoBuilder.kt`

## Overview

`InfoBuilder` is responsible for building the API information section of an OpenAPI specification. This section contains metadata about the API including its title, version, description, and contact information.

## Class Declaration

```kotlin
class InfoBuilder
```

## Properties

| Property | Type | Description | Required |
|----------|------|-------------|----------|
| `title` | `String?` | The title of the API | Yes |
| `version` | `String?` | The version of the API | Yes |
| `description` | `String?` | A description of the API | No |
| `termsOfService` | `String?` | URL to the Terms of Service | No |
| `contact` | `Contact?` | Contact information | No |
| `license` | `License?` | License information | No |

## Key Methods

### `contact(block: ContactBuilder.() -> Unit)`
Configures contact information for the API.

```kotlin
contact {
    name = "API Support Team"
    email = "support@example.com"
    url = "https://support.example.com"
}
```

### `license(name: String, url: String? = null)`
Sets the license information for the API.

```kotlin
// Simple license
license("MIT")

// License with URL
license("Apache 2.0", "https://www.apache.org/licenses/LICENSE-2.0")
```

### `build(): Info`
Builds the final `Info` object. This method validates that required fields are present.

**Validation:**
- Throws exception if `title` is null or blank
- Throws exception if `version` is null or blank

## Usage Examples

### Basic Example

```kotlin
info {
    title = "User Management API"
    version = "1.0.0"
}
```

### Complete Example

```kotlin
info {
    title = "E-Commerce Platform API"
    version = "2.1.0"
    description = """
        Complete API for managing an e-commerce platform including:
        - Product catalog management
        - Order processing
        - Customer management
        - Inventory tracking
    """.trimIndent()
    
    termsOfService = "https://example.com/terms"
    
    contact {
        name = "E-Commerce API Team"
        email = "api-support@example.com"
        url = "https://developer.example.com/support"
    }
    
    license("MIT", "https://opensource.org/licenses/MIT")
}
```

### Version Management Example

```kotlin
info {
    title = "Payment Processing API"
    version = System.getenv("API_VERSION") ?: "1.0.0-dev"
    description = "API for processing payments. Version: ${version}"
}
```

## Best Practices

1. **Always provide meaningful titles**: Use clear, descriptive titles that indicate the API's purpose.

2. **Follow semantic versioning**: Use semantic versioning (MAJOR.MINOR.PATCH) for the version field.

3. **Include comprehensive descriptions**: Provide detailed descriptions that help users understand the API's capabilities.

4. **Provide contact information**: Always include contact details for API support.

5. **Specify license clearly**: Include license information to clarify usage terms.

## Common Patterns

### Multi-line Descriptions

```kotlin
info {
    title = "Analytics API"
    version = "3.0.0"
    description = """
        # Analytics API
        
        This API provides access to analytics data including:
        
        ## Features
        - Real-time metrics
        - Historical data analysis
        - Custom report generation
        - Data export capabilities
        
        ## Rate Limits
        - 1000 requests per hour for standard tier
        - 10000 requests per hour for premium tier
    """.trimIndent()
}
```

### Environment-Specific Information

```kotlin
info {
    title = "Order Management API"
    version = "1.5.0"
    description = when (environment) {
        "production" -> "Production API - Use with caution"
        "staging" -> "Staging API - For testing only"
        else -> "Development API - Not for production use"
    }
    
    contact {
        email = when (environment) {
            "production" -> "prod-support@example.com"
            else -> "dev-support@example.com"
        }
    }
}
```

## Validation Rules

The `InfoBuilder` enforces the following validation rules:

1. **Title is required**: Must be non-null and non-blank
2. **Version is required**: Must be non-null and non-blank
3. **URLs must be valid**: Terms of service, contact URL, and license URL should be valid URLs

## Related Builders

- [OpenApiBuilder](../core/OpenApiBuilder.md) - Parent builder that uses InfoBuilder
- [ContactBuilder](ContactBuilder.md) - For building contact information
- [ServerBuilder](ServerBuilder.md) - For server configurations (sibling in OpenApiBuilder)