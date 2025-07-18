# ContactBuilder

**Package**: `me.farshad.dsl.builder.info`  
**File**: `ContactBuilder.kt`

## Overview

`ContactBuilder` is responsible for building contact information within the API info section. It provides a way to specify how users can contact the API provider for support or questions.

## Class Declaration

```kotlin
class ContactBuilder
```

## Properties

| Property | Type | Description | Required |
|----------|------|-------------|----------|
| `name` | `String?` | The name of the contact person/organization | No |
| `url` | `String?` | URL pointing to contact information | No |
| `email` | `String?` | Email address of the contact person/organization | No |

## Key Methods

### `build(): Contact`
Builds the final `Contact` object. All fields are optional, so this method can return a Contact object with any combination of fields set.

## Usage Examples

### Basic Example

```kotlin
contact {
    email = "support@example.com"
}
```

### Complete Example

```kotlin
contact {
    name = "API Support Team"
    url = "https://support.example.com"
    email = "api-support@example.com"
}
```

### Organization Contact

```kotlin
contact {
    name = "Example Corp Developer Relations"
    url = "https://developer.example.com/contact"
    email = "developer-relations@example.com"
}
```

## Usage Within InfoBuilder

The `ContactBuilder` is typically used within an `InfoBuilder` context:

```kotlin
info {
    title = "Customer API"
    version = "1.0.0"
    
    contact {
        name = "Customer API Team"
        email = "customer-api@example.com"
        url = "https://example.com/api/support"
    }
}
```

## Best Practices

1. **Provide multiple contact methods**: Include both email and URL when possible to give users options.

2. **Use monitored email addresses**: Ensure the email address is actively monitored and responses are timely.

3. **Link to comprehensive support**: The URL should point to documentation, FAQs, or a support portal.

4. **Be specific with names**: Use team names rather than individual names for better long-term maintenance.

## Common Patterns

### Environment-Specific Contacts

```kotlin
contact {
    name = "API Support"
    email = when (environment) {
        "production" -> "prod-support@example.com"
        "staging" -> "staging-support@example.com"
        else -> "dev-support@example.com"
    }
    url = "https://${environment}.example.com/support"
}
```

### Support Tier Contacts

```kotlin
contact {
    name = when (apiTier) {
        "enterprise" -> "Enterprise Support Team"
        "premium" -> "Premium Support Team"
        else -> "Standard Support"
    }
    email = "${apiTier}-support@example.com"
    url = "https://support.example.com/${apiTier}"
}
```

### Minimal Contact

```kotlin
// Sometimes only an email is needed
contact {
    email = "api@example.com"
}
```

### Developer Portal Integration

```kotlin
contact {
    name = "Developer Portal"
    url = "https://developers.example.com"
    // Email might be handled through the portal
}
```

## Field Guidelines

### `name` Field
- Use descriptive team or department names
- Avoid personal names that may change
- Consider including the service name

Examples:
- "Payment API Support Team"
- "Developer Relations"
- "Technical Support Department"

### `url` Field
- Should be a valid HTTP/HTTPS URL
- Point to relevant support resources
- Consider including:
  - Documentation
  - FAQ pages
  - Support ticket systems
  - Community forums

### `email` Field
- Use a group email address
- Ensure it's monitored regularly
- Consider using role-based addresses

Examples:
- `api-support@example.com`
- `developers@example.com`
- `technical-support@example.com`

## Integration Example

Here's how `ContactBuilder` fits into a complete API specification:

```kotlin
openApi {
    openapi = "3.1.0"
    
    info {
        title = "Enterprise Resource Planning API"
        version = "2.0.0"
        description = "API for managing enterprise resources"
        
        contact {
            name = "ERP API Support Team"
            url = "https://erp.example.com/api/docs"
            email = "erp-api-support@example.com"
        }
        
        license("Commercial", "https://example.com/licenses/api")
    }
    
    // ... rest of specification
}
```

## Related Builders

- [InfoBuilder](InfoBuilder.md) - Parent builder that uses ContactBuilder
- [OpenApiBuilder](../core/OpenApiBuilder.md) - Root builder for the entire specification