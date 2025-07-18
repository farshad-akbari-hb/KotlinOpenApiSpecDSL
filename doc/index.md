# Kotlin OpenAPI Spec DSL Documentation

Welcome to the comprehensive guide for the Kotlin OpenAPI Spec DSL library. This library provides a type-safe, idiomatic Kotlin way to generate OpenAPI 3.1.0 specifications.

## What is this library?

The Kotlin OpenAPI Spec DSL is a powerful library that allows you to:
- Generate OpenAPI 3.1.0 specifications using Kotlin's type-safe DSL
- Automatically create schemas from Kotlin data classes
- Build complex API specifications with minimal boilerplate
- Export specifications as JSON or YAML
- Leverage Kotlin's type system for compile-time safety

## Quick Example

```kotlin
val spec = openApi {
    openapi = "3.1.0"
    info {
        title = "My API"
        version = "1.0.0"
    }
    paths {
        path("/users") {
            get {
                summary = "List all users"
                response("200", "Success") {
                    jsonContent(listOf<User>())
                }
            }
        }
    }
}

// Export as JSON or YAML
println(spec.toJson())
println(spec.toYaml())
```

## Documentation Structure

### Getting Started
- **[Getting Started Guide](getting-started.md)** - Installation, setup, and your first API specification
- **[Basic Usage](basic-usage.md)** - Common patterns and simple API definitions

### Working with Schemas
- **[Schema Definitions](schema-definitions.md)** - Creating and using schemas from Kotlin classes
- **[Advanced Schemas](advanced-schemas.md)** - Complex schema compositions, discriminators, and polymorphism

### API Design
- **[API Operations](api-operations.md)** - Defining paths, operations, and HTTP methods
- **[Request & Response](request-response.md)** - Parameters, request bodies, and responses
- **[Security](security.md)** - Authentication and authorization schemes

### Advanced Topics
- **[Reusable Components](reusable-components.md)** - Shared schemas, parameters, and responses
- **[Advanced Features](advanced-features.md)** - Webhooks, callbacks, links, and extensions
- **[Best Practices](best-practices.md)** - Tips, patterns, and recommendations

### Examples
- **[Pet Store API](examples/pet-store.md)** - Classic pet store example with CRUD operations
- **[Blog API](examples/blog-api.md)** - Content management system API example
- **[Microservices](examples/microservices.md)** - Multiple services with shared schemas

## Key Features

### 🚀 Type-Safe DSL
Write OpenAPI specs with full IDE support, auto-completion, and compile-time validation.

### 🔄 Automatic Schema Generation
Convert Kotlin data classes to OpenAPI schemas automatically with annotation support.

### 🧩 Schema Composition
Build complex schemas using `oneOf`, `allOf`, `anyOf`, and `not` with idiomatic Kotlin syntax.

### 📝 Rich Documentation
Add descriptions, examples, and metadata at every level of your API specification.

### 🔐 Security Schemes
Define multiple authentication methods including API keys, OAuth2, and bearer tokens.

### 📦 Reusable Components
Create libraries of shared schemas, parameters, and responses for consistency.

## Prerequisites

- Kotlin 1.9 or higher
- Gradle or Maven
- Basic understanding of OpenAPI/Swagger specifications

## Quick Links

- [OpenAPI 3.1.0 Specification](https://spec.openapis.org/oas/v3.1.0)
- [Kotlin DSL Documentation](https://kotlinlang.org/docs/type-safe-builders.html)
- [Kotlinx Serialization](https://github.com/Kotlin/kotlinx.serialization)

## Getting Help

If you encounter any issues or have questions:
1. Check the relevant documentation section
2. Look at the [examples](examples/) for working code
3. Review the test cases in the source code
4. Open an issue on the project repository

Let's get started with [installing and setting up](getting-started.md) the library!