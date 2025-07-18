# ServerBuilder

**Package**: `me.farshad.dsl.builder.info`  
**File**: `ServerBuilder.kt`

## Overview

`ServerBuilder` is responsible for building server configurations in an OpenAPI specification. It supports URL templates with variables, allowing for flexible server definitions that can adapt to different environments or configurations.

## Class Declaration

```kotlin
class ServerBuilder
```

## Properties

| Property | Type | Description | Required |
|----------|------|-------------|----------|
| `url` | `String?` | Server URL (may contain variable templates) | Yes |
| `description` | `String?` | Description of the server | No |
| `variables` | `MutableMap<String, ServerVariable>` | Server URL variables | No |

## Key Methods

### `variable(name: String, block: ServerVariableBuilder.() -> Unit)`
Defines a server URL variable with detailed configuration.

```kotlin
variable("environment") {
    default = "production"
    description = "Server environment"
    enum = listOf("production", "staging", "development")
}
```

### `variable(name: String, default: String, description: String? = null, enum: List<String>? = null)`
Quick method to define a server URL variable.

```kotlin
variable("port", "443", "Server port", listOf("443", "8443"))
```

### `build(): Server`
Builds the final `Server` object. Validates that the URL is not null or blank.

## Server Variable Configuration

The `ServerVariableBuilder` nested class provides:

| Property | Type | Description | Required |
|----------|------|-------------|----------|
| `default` | `String?` | Default value for the variable | Yes |
| `description` | `String?` | Description of the variable | No |
| `enum` | `List<String>?` | Enumeration of valid values | No |

## Usage Examples

### Basic Server

```kotlin
server("https://api.example.com") {
    description = "Production server"
}
```

### Server with Variables

```kotlin
server("https://{environment}.api.example.com/{version}") {
    description = "Environment-specific server"
    
    variable("environment") {
        default = "prod"
        description = "Server environment"
        enum = listOf("prod", "staging", "dev")
    }
    
    variable("version", "v1", "API version", listOf("v1", "v2"))
}
```

### Multiple Servers Example

```kotlin
servers {
    server("https://api.example.com") {
        description = "Production server (US East)"
    }
    
    server("https://eu.api.example.com") {
        description = "Production server (EU)"
    }
    
    server("https://{tenant}.api.example.com") {
        description = "Tenant-specific server"
        variable("tenant") {
            default = "default"
            description = "Customer tenant identifier"
        }
    }
    
    server("http://localhost:{port}") {
        description = "Local development server"
        variable("port") {
            default = "8080"
            description = "Server port"
            enum = listOf("8080", "8081", "3000")
        }
    }
}
```

## Advanced Patterns

### Region-Based Servers

```kotlin
servers {
    server("https://{region}.api.example.com") {
        description = "Regional API endpoint"
        
        variable("region") {
            default = "us-east-1"
            description = "AWS region"
            enum = listOf(
                "us-east-1",
                "us-west-2", 
                "eu-west-1",
                "ap-southeast-1"
            )
        }
    }
}
```

### Protocol and Port Variables

```kotlin
server("{protocol}://{host}:{port}/api") {
    description = "Configurable server"
    
    variable("protocol") {
        default = "https"
        enum = listOf("https", "http")
        description = "Protocol to use"
    }
    
    variable("host") {
        default = "api.example.com"
        description = "Server hostname"
    }
    
    variable("port") {
        default = "443"
        description = "Server port"
        enum = listOf("443", "8443", "80", "8080")
    }
}
```

### Environment-Specific Configuration

```kotlin
servers {
    // Production servers
    server("https://api.example.com") {
        description = "Production server - Primary"
    }
    
    server("https://api-failover.example.com") {
        description = "Production server - Failover"
    }
    
    // Staging server
    server("https://staging-api.example.com") {
        description = "Staging environment"
    }
    
    // Development server
    server("https://{developer}.dev.example.com") {
        description = "Developer-specific environment"
        variable("developer") {
            default = "shared"
            description = "Developer identifier or 'shared' for team server"
        }
    }
}
```

### Versioned API Servers

```kotlin
server("https://api.example.com/{version}") {
    description = "Versioned API endpoint"
    
    variable("version") {
        default = "v2"
        description = "API version"
        enum = listOf("v1", "v2", "v3-beta")
    }
}
```

## Best Practices

1. **Always provide descriptions**: Help users understand what each server is for.

2. **Use meaningful variable names**: Variable names should be self-explanatory.

3. **Provide sensible defaults**: Default values should work for most users.

4. **Limit enum values**: When using enums, include only valid, supported values.

5. **Order servers by priority**: List production servers first, then staging, then development.

6. **Consider geographic distribution**: If you have regional servers, make it clear in descriptions.

## Common Use Cases

### Multi-Tenant SaaS

```kotlin
server("https://{tenant}.{region}.saas-app.com/api/{version}") {
    description = "Multi-tenant SaaS endpoint"
    
    variable("tenant") {
        default = "demo"
        description = "Customer tenant identifier"
    }
    
    variable("region") {
        default = "us"
        description = "Geographic region"
        enum = listOf("us", "eu", "asia")
    }
    
    variable("version") {
        default = "v1"
        description = "API version"
        enum = listOf("v1", "v2")
    }
}
```

### Microservices Architecture

```kotlin
servers {
    server("https://gateway.example.com") {
        description = "API Gateway - routes to all services"
    }
    
    server("https://{service}.example.com") {
        description = "Direct service access"
        variable("service") {
            default = "users"
            description = "Microservice name"
            enum = listOf("users", "orders", "products", "payments")
        }
    }
}
```

## Validation Rules

1. **URL is required**: Must be non-null and non-blank
2. **Variables must have defaults**: Each variable must have a default value
3. **Variable names must match**: Variables defined must match placeholders in URL
4. **Enum values must include default**: If enum is specified, it must contain the default value

## Related Builders

- [OpenApiBuilder](OpenApiBuilder.md) - Parent builder that manages servers
- [InfoBuilder](InfoBuilder.md) - Sibling builder for API information