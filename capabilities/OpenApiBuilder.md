# OpenApiBuilder

**Package**: `me.farshad.dsl.builder.core`  
**File**: `OpenApiBuilder.kt`

## Overview

`OpenApiBuilder` is the main entry point for creating OpenAPI specifications using the Kotlin DSL. It orchestrates all other builders and provides methods to export the specification in JSON or YAML format.

## Class Declaration

```kotlin
class OpenApiBuilder
```

## Properties

| Property | Type | Description | Required |
|----------|------|-------------|----------|
| `openapi` | `String` | OpenAPI specification version | Yes (default: "3.1.0") |
| `info` | `Info?` | API information metadata | Yes |
| `servers` | `List<Server>?` | Server configurations | No |
| `paths` | `Paths?` | API paths and operations | No |
| `components` | `Components?` | Reusable components | No |
| `tags` | `List<Tag>?` | Tags for grouping operations | No |
| `externalDocs` | `ExternalDocumentation?` | External documentation reference | No |

## Key Methods

### Builder Methods

#### `info(block: InfoBuilder.() -> Unit)`
Configures the API information section.

```kotlin
info {
    title = "My API"
    version = "1.0.0"
    description = "API for managing resources"
}
```

#### `servers(block: ServersBuilder.() -> Unit)`
Defines server configurations with support for variables.

```kotlin
servers {
    server("https://api.example.com") {
        description = "Production server"
    }
    server("https://{environment}.api.example.com") {
        description = "Environment-specific server"
        variable("environment", "dev") {
            description = "Server environment"
            enum = listOf("dev", "staging", "prod")
        }
    }
}
```

#### `paths(block: PathsBuilder.() -> Unit)`
Defines API paths and their operations.

```kotlin
paths {
    path("/users") {
        get {
            summary = "List all users"
            // ... operation details
        }
        post {
            summary = "Create a new user"
            // ... operation details
        }
    }
}
```

#### `components(block: ComponentsBuilder.() -> Unit)`
Defines reusable components including schemas, examples, and security schemes.

```kotlin
components {
    schema<User>()
    schema("Error") {
        type = "object"
        properties {
            property("code", "integer")
            property("message", "string")
        }
    }
}
```

#### `tags(vararg tags: Tag)`
Adds tags for organizing operations.

```kotlin
tags(
    Tag("users", "User operations"),
    Tag("auth", "Authentication endpoints")
)
```

#### `externalDocs(url: String, description: String? = null)`
Sets external documentation reference.

```kotlin
externalDocs("https://docs.example.com", "Extended API documentation")
```

### Build and Export Methods

#### `build(): OpenApiSpec`
Builds the final OpenAPI specification object.

```kotlin
val spec: OpenApiSpec = openApiBuilder.build()
```

#### `toJson(prettyPrint: Boolean = true): String`
Exports the specification as a JSON string.

```kotlin
val jsonString = openApiBuilder.toJson()
// or compact JSON
val compactJson = openApiBuilder.toJson(prettyPrint = false)
```

#### `toYaml(): String`
Exports the specification as a YAML string.

```kotlin
val yamlString = openApiBuilder.toYaml()
```

## Extension Functions

### `openApi(block: OpenApiBuilder.() -> Unit): OpenApiBuilder`
Top-level function to create an OpenAPI specification using the DSL.

```kotlin
val spec = openApi {
    openapi = "3.1.0"
    info {
        title = "My API"
        version = "1.0.0"
    }
    // ... rest of the configuration
}
```

## Complete Example

```kotlin
val apiSpec = openApi {
    openapi = "3.1.0"
    
    info {
        title = "Pet Store API"
        version = "1.0.0"
        description = "A sample Pet Store API"
        termsOfService = "https://example.com/terms"
        contact {
            name = "API Support"
            email = "support@example.com"
            url = "https://support.example.com"
        }
        license("MIT", "https://opensource.org/licenses/MIT")
    }
    
    servers {
        server("https://api.petstore.com/v1") {
            description = "Production server"
        }
        server("https://sandbox.petstore.com/v1") {
            description = "Sandbox server"
        }
    }
    
    tags(
        Tag("pets", "Everything about pets"),
        Tag("store", "Access to pet store orders")
    )
    
    paths {
        path("/pets") {
            get {
                tags = listOf("pets")
                summary = "List all pets"
                operationId = "listPets"
                
                queryParameter("limit") {
                    description = "Maximum number of pets to return"
                    schema {
                        type = "integer"
                        maximum = 100
                    }
                }
                
                response("200", "Successful response") {
                    jsonContent {
                        type = "array"
                        items {
                            ref("Pet")
                        }
                    }
                }
            }
            
            post {
                tags = listOf("pets")
                summary = "Create a pet"
                operationId = "createPet"
                
                requestBody {
                    required = true
                    jsonContent(Pet::class)
                }
                
                response("201", "Pet created") {
                    jsonContent(Pet::class)
                }
            }
        }
    }
    
    components {
        schema<Pet>()
        schema<Error>()
    }
    
    externalDocs("https://docs.petstore.com", "Find more info here")
}

// Export as JSON
val json = apiSpec.toJson()

// Export as YAML
val yaml = apiSpec.toYaml()
```

## Best Practices

1. **Always define info section**: The `info` section is required by the OpenAPI specification.

2. **Use type-safe schema references**: Prefer `schema<T>()` over string-based schema names when possible.

3. **Organize with tags**: Use tags to group related operations for better API documentation.

4. **Define reusable components**: Use the components section for schemas, examples, and other elements used multiple times.

5. **Validate the output**: The generated JSON/YAML should be validated against the OpenAPI 3.1.0 specification.

## Related Builders

- [InfoBuilder](InfoBuilder.md) - For configuring API information
- [PathsBuilder](PathsBuilder.md) - For defining API paths
- [ComponentsBuilder](ComponentsBuilder.md) - For reusable components
- [ServerBuilder](ServerBuilder.md) - For server configurations