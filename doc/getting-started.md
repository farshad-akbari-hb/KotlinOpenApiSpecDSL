# Getting Started

This guide will help you set up the Kotlin OpenAPI Spec DSL library and create your first API specification.

## Installation

### Gradle (Kotlin DSL)

Add the following to your `build.gradle.kts`:

```kotlin
dependencies {
    implementation("me.farshad:kotlin-openapi-spec-dsl:1.0.0")
    
    // Required for JSON/YAML serialization
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.0")
    implementation("com.charleskorn.kaml:kaml:0.55.0")
}
```

Don't forget to apply the Kotlin serialization plugin:

```kotlin
plugins {
    kotlin("jvm") version "1.9.0"
    kotlin("plugin.serialization") version "1.9.0"
}
```

### Gradle (Groovy)

```groovy
dependencies {
    implementation 'me.farshad:kotlin-openapi-spec-dsl:1.0.0'
    implementation 'org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.0'
    implementation 'com.charleskorn.kaml:kaml:0.55.0'
}
```

### Maven

```xml
<dependency>
    <groupId>me.farshad</groupId>
    <artifactId>kotlin-openapi-spec-dsl</artifactId>
    <version>1.0.0</version>
</dependency>
```

## Your First API Specification

Let's create a simple "Hello World" API specification:

```kotlin
import me.farshad.openapi.*

fun main() {
    val spec = openApi {
        openapi = "3.1.0"
        
        info {
            title = "Hello World API"
            version = "1.0.0"
            description = "A simple API to get started"
        }
        
        paths {
            path("/hello") {
                get {
                    summary = "Say hello"
                    description = "Returns a greeting message"
                    
                    response("200", "Successful response") {
                        jsonContent {
                            schema {
                                type = SchemaType.OBJECT
                                properties {
                                    "message" to schema {
                                        type = SchemaType.STRING
                                        example = "Hello, World!"
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
    
    // Print as JSON
    println(spec.toJson())
    
    // Or save to file
    spec.toJsonFile("hello-api.json")
    spec.toYamlFile("hello-api.yaml")
}
```

## Understanding the Structure

The DSL follows the OpenAPI specification structure:

### 1. Root Level
```kotlin
openApi {
    openapi = "3.1.0"  // OpenAPI version (required)
    // Other root-level properties...
}
```

### 2. Info Section
```kotlin
info {
    title = "My API"           // Required
    version = "1.0.0"          // Required
    description = "API description"
    termsOfService = "https://example.com/terms"
    contact {
        name = "API Support"
        email = "support@example.com"
        url = "https://example.com/support"
    }
    license {
        name = "Apache 2.0"
        url = "https://www.apache.org/licenses/LICENSE-2.0"
    }
}
```

### 3. Servers
```kotlin
servers {
    server {
        url = "https://api.example.com"
        description = "Production server"
    }
    server {
        url = "https://staging.api.example.com"
        description = "Staging server"
    }
}
```

### 4. Paths and Operations
```kotlin
paths {
    path("/users") {
        get { /* ... */ }
        post { /* ... */ }
    }
    path("/users/{id}") {
        get { /* ... */ }
        put { /* ... */ }
        delete { /* ... */ }
    }
}
```

## Working with Data Classes

One of the most powerful features is automatic schema generation from Kotlin data classes:

```kotlin
import kotlinx.serialization.Serializable
import me.farshad.openapi.annotations.*

@Serializable
@SchemaDescription("Represents a user in the system")
data class User(
    @PropertyDescription("Unique identifier")
    val id: String,
    
    @PropertyDescription("User's full name")
    val name: String,
    
    @PropertyDescription("User's email address")
    val email: String,
    
    @PropertyDescription("User's age in years")
    val age: Int? = null
)

fun main() {
    val spec = openApi {
        openapi = "3.1.0"
        info {
            title = "User API"
            version = "1.0.0"
        }
        
        // Register the schema in components
        components {
            schema(User::class)
        }
        
        paths {
            path("/users") {
                get {
                    summary = "List users"
                    response("200", "List of users") {
                        jsonContent(listOf<User>())
                    }
                }
                
                post {
                    summary = "Create user"
                    requestBody("User to create") {
                        jsonContent(User::class)
                    }
                    response("201", "User created") {
                        jsonContent(User::class)
                    }
                }
            }
        }
    }
}
```

## Output Formats

The library supports both JSON and YAML output:

### JSON Output
```kotlin
// Pretty-printed JSON string
val json = spec.toJson()

// Minified JSON string
val minifiedJson = spec.toJsonCompact()

// Save to file
spec.toJsonFile("api-spec.json")
```

### YAML Output
```kotlin
// YAML string
val yaml = spec.toYaml()

// Save to file
spec.toYamlFile("api-spec.yaml")
```

## Complete Example

Here's a more complete example showing various features:

```kotlin
import me.farshad.openapi.*
import kotlinx.serialization.Serializable

@Serializable
data class Product(
    val id: String,
    val name: String,
    val price: Double,
    val description: String? = null
)

@Serializable
data class Error(
    val code: String,
    val message: String
)

fun main() {
    val spec = openApi {
        openapi = "3.1.0"
        
        info {
            title = "Product Catalog API"
            version = "1.0.0"
            description = "API for managing product catalog"
        }
        
        servers {
            server {
                url = "https://api.shop.com"
                description = "Production API"
            }
        }
        
        components {
            // Register reusable schemas
            schema(Product::class)
            schema(Error::class)
            
            // Define reusable parameters
            parameter("limitParam") {
                name = "limit"
                `in` = ParameterLocation.QUERY
                description = "Maximum number of items to return"
                schema {
                    type = SchemaType.INTEGER
                    minimum = 1
                    maximum = 100
                    default = 10
                }
            }
        }
        
        paths {
            path("/products") {
                get {
                    summary = "List products"
                    tags = listOf("Products")
                    
                    // Reference reusable parameter
                    parameter {
                        ref = "#/components/parameters/limitParam"
                    }
                    
                    parameter {
                        name = "offset"
                        `in` = ParameterLocation.QUERY
                        schema {
                            type = SchemaType.INTEGER
                            minimum = 0
                            default = 0
                        }
                    }
                    
                    response("200", "Successful response") {
                        jsonContent(listOf<Product>())
                    }
                    
                    response("400", "Bad request") {
                        jsonContent(Error::class)
                    }
                }
                
                post {
                    summary = "Create product"
                    tags = listOf("Products")
                    
                    requestBody("Product to create") {
                        jsonContent(Product::class)
                        required = true
                    }
                    
                    response("201", "Product created") {
                        jsonContent(Product::class)
                        headers {
                            header("Location") {
                                description = "URL of the created product"
                                schema {
                                    type = SchemaType.STRING
                                }
                            }
                        }
                    }
                }
            }
            
            path("/products/{productId}") {
                parameter {
                    name = "productId"
                    `in` = ParameterLocation.PATH
                    required = true
                    description = "Product identifier"
                    schema {
                        type = SchemaType.STRING
                    }
                }
                
                get {
                    summary = "Get product by ID"
                    tags = listOf("Products")
                    
                    response("200", "Product found") {
                        jsonContent(Product::class)
                    }
                    
                    response("404", "Product not found") {
                        jsonContent(Error::class)
                    }
                }
            }
        }
    }
    
    // Generate the specification
    spec.toYamlFile("product-api.yaml")
    println("API specification generated successfully!")
}
```

## Next Steps

Now that you have the basics down, explore:
- [Basic Usage](basic-usage.md) - Common patterns and conventions
- [Schema Definitions](schema-definitions.md) - Working with complex schemas
- [API Operations](api-operations.md) - Advanced operation configurations

## Troubleshooting

### Common Issues

1. **Serialization errors**: Make sure your data classes are annotated with `@Serializable`
2. **Missing imports**: The main imports you need are:
   ```kotlin
   import me.farshad.openapi.*
   import kotlinx.serialization.Serializable
   ```
3. **Schema not found**: If referencing schemas, ensure they're registered in the components section

### IDE Support

For the best development experience:
- Use IntelliJ IDEA with Kotlin plugin
- Enable auto-import for better DSL usage
- Use Ctrl+Space for auto-completion within DSL blocks