# Kotlin OpenAPI DSL - Builder Classes Reference

This directory contains comprehensive documentation for all builder classes in the Kotlin OpenAPI DSL library. Each builder is documented in its own file with detailed explanations, properties, methods, and usage examples.

## Builder Classes by Category

### Core Builders
- [**OpenApiBuilder**](OpenApiBuilder.md) - Main entry point for creating OpenAPI specifications

### Info Builders
- [**InfoBuilder**](InfoBuilder.md) - Builds API information metadata
- [**ContactBuilder**](ContactBuilder.md) - Builds contact information
- [**ServerBuilder**](ServerBuilder.md) - Builds server configurations with variables

### Path Builders
- [**PathsBuilder**](PathsBuilder.md) - Manages API paths and endpoints
- [**PathItemBuilder**](PathItemBuilder.md) - Builds individual path items with operations
- [**OperationBuilder**](OperationBuilder.md) - Builds API operations (GET, POST, etc.)

### Schema Builders
- [**SchemaBuilder**](SchemaBuilder.md) - Comprehensive schema definition builder
- [**OneOfBuilder**](OneOfBuilder.md) - Builds oneOf schema compositions
- [**AllOfBuilder**](AllOfBuilder.md) - Builds allOf schema compositions
- [**AnyOfBuilder**](AnyOfBuilder.md) - Builds anyOf schema compositions
- [**DiscriminatorBuilder**](DiscriminatorBuilder.md) - Configures discriminators for polymorphic types

### Request/Response Builders
- [**RequestBodyBuilder**](RequestBodyBuilder.md) - Builds request body configurations
- [**ResponseBuilder**](ResponseBuilder.md) - Builds response configurations

### Component Builders
- [**ComponentsBuilder**](ComponentsBuilder.md) - Manages reusable components (schemas, examples, etc.)

### Example Builders
- [**ExampleBuilder**](ExampleBuilder.md) - Builds individual examples
- [**ExamplesBuilder**](ExamplesBuilder.md) - Manages collections of examples

## Quick Reference

| Builder | Purpose | Key Features |
|---------|---------|--------------|
| **OpenApiBuilder** | Main specification builder | Orchestrates all builders, JSON/YAML export |
| **InfoBuilder** | API metadata | Title, version, description, contact, license |
| **ContactBuilder** | Contact details | Name, email, URL |
| **ServerBuilder** | Server configuration | URL templates, variables, descriptions |
| **PathsBuilder** | Path management | HTTP endpoints and operations |
| **PathItemBuilder** | Single path configuration | All HTTP methods, shared parameters |
| **OperationBuilder** | Operation details | Parameters, request/response, security |
| **SchemaBuilder** | Schema definitions | Types, validation, composition, references |
| **OneOfBuilder** | Exclusive choice schemas | Exactly one schema must match |
| **AllOfBuilder** | Schema intersection | All schemas must match (inheritance) |
| **AnyOfBuilder** | Schema union | One or more schemas must match |
| **DiscriminatorBuilder** | Polymorphism support | Type discrimination for oneOf/anyOf |
| **RequestBodyBuilder** | Request configuration | Content types, schemas, examples |
| **ResponseBuilder** | Response configuration | Status codes, headers, content |
| **ComponentsBuilder** | Reusable definitions | Schemas, examples, parameters, etc. |
| **ExampleBuilder** | Single example | Value, summary, description |
| **ExamplesBuilder** | Multiple examples | Named examples for various scenarios |

## Usage Example

Here's a quick example showing how these builders work together:

```kotlin
val spec = openApi {
    openapi = "3.1.0"
    
    info {
        title = "My API"
        version = "1.0.0"
        contact {
            name = "API Support"
            email = "support@example.com"
        }
    }
    
    servers {
        server("https://api.example.com") {
            description = "Production server"
        }
    }
    
    paths {
        path("/users") {
            get {
                summary = "List users"
                response("200", "Success") {
                    jsonContent {
                        type = "array"
                        items { ref("User") }
                    }
                }
            }
            
            post {
                summary = "Create user"
                requestBody {
                    required = true
                    jsonContent(CreateUserRequest::class)
                }
                response("201", "Created") {
                    jsonContent(User::class)
                }
            }
        }
    }
    
    components {
        schema<User>()
        schema<CreateUserRequest>()
    }
}

// Export as JSON or YAML
val json = spec.toJson()
val yaml = spec.toYaml()
```

## Annotation Classes

In addition to builders, the library provides annotations for enhancing schema generation:

- [**SchemaDescription**](SchemaDescription.md) - Class-level descriptions for schemas  
- [**PropertyDescription**](PropertyDescription.md) - Property-level descriptions for schema fields

## Package Structure

```
me.farshad.dsl.
├── annotation/          # Annotation classes
│   └── Annotations.kt
├── builder/            # All builder classes
│   ├── components/     # ComponentsBuilder
│   ├── core/          # OpenApiBuilder and extensions
│   ├── example/       # Example builders
│   ├── info/          # Info-related builders
│   ├── paths/         # Path and operation builders
│   ├── request/       # Request body builder
│   ├── response/      # Response builder
│   └── schema/        # Schema and composition builders
├── serializer/        # Custom serializers
└── spec/             # Data model specifications
```

## Getting Started

1. Start with [OpenApiBuilder](OpenApiBuilder.md) to understand the main entry point
2. Learn about [SchemaBuilder](SchemaBuilder.md) for defining data models
3. Explore [PathsBuilder](PathsBuilder.md) and [OperationBuilder](OperationBuilder.md) for API endpoints
4. Use [ComponentsBuilder](ComponentsBuilder.md) for reusable definitions
5. Refer to specific builders as needed for detailed configurations

## Additional Resources

- For schema composition patterns, see the composition builders (OneOf, AllOf, AnyOf)
- For API examples, check [ExampleBuilder](ExampleBuilder.md) and [ExamplesBuilder](ExamplesBuilder.md)
- For request/response configuration, see [RequestBodyBuilder](RequestBodyBuilder.md) and [ResponseBuilder](ResponseBuilder.md)