# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Build Commands

- **Build the project**: `./gradlew build`
- **Run tests**: `./gradlew test`
- **Run a specific test**: `./gradlew test --tests "me.farshad.SchemaCompositionTest.testOneOfWithStringReferences"`
- **Clean build**: `./gradlew clean build`
- **Generate test report**: Tests reports are generated in `build/reports/tests/test/index.html`

## High-Level Architecture

This is a Kotlin DSL (Domain Specific Language) library for generating OpenAPI 3.1.0 specifications. The library uses Kotlinx Serialization for JSON/YAML generation and provides a type-safe, idiomatic Kotlin API for building OpenAPI specs.

### Core Components

1. **DSL Builder Classes** (`OpenApiSpec.kt`):
   - `OpenApiBuilder`: Main entry point for building specs
   - Various builder classes for each OpenAPI component (Info, Paths, Operations, etc.)
   - Extension functions for JSON/YAML serialization

2. **Data Models** (`Specs.kt`):
   - Serializable data classes representing OpenAPI 3.1.0 specification structures
   - All classes use `@Serializable` annotation for kotlinx.serialization support
   - Enums for parameter locations, schema types, and formats

3. **Schema Composition** (`SchemaComposition.kt`):
   - Advanced schema composition features (oneOf, allOf, anyOf, not)
   - `SchemaReference` sealed class for type-safe schema references
   - Helper functions for common patterns (discriminated unions, nullable schemas, extending schemas)
   - Operator overloading for idiomatic Kotlin syntax (`or`, `and`)

4. **Annotations** (`Annotations.kt`):
   - `@SchemaDescription`: Class-level descriptions
   - `@PropertyDescription`: Property-level descriptions
   - Used for automatic schema generation from Kotlin data classes

5. **Custom Serializers**:
   - `SchemaReferenceSerializer`: Handles both string references and inline schemas
   - `MediaTypeSerializer`: Custom serialization for MediaType
   - `ExampleSerializer`: Custom serialization for Example
   - `JsonElementYamlSerializer`: YAML serialization support

### Key Design Patterns

- **Builder Pattern**: All DSL components use builders for fluent API
- **Type Safety**: Extensive use of Kotlin's type system for compile-time safety
- **Sealed Classes**: Used for discriminated unions (e.g., `SchemaReference`)
- **Extension Functions**: For adding functionality to existing classes
- **Reified Generics**: For type-safe schema references from Kotlin classes

### Usage Pattern

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
                summary = "List users"
                response("200", "Success") {
                    jsonContent(User::class)
                }
            }
        }
    }
    components {
        schema(User::class)  // Auto-generates schema from data class
    }
}

// Serialize to JSON or YAML
val json = spec.toJson()
val yaml = spec.toYaml()
```

### Testing Approach

The project uses JUnit 5 for testing. Tests focus on:
- Schema composition features (oneOf, allOf, anyOf)
- Discriminator functionality
- JSON serialization correctness
- DSL builder behavior

## Development Guidelines

- When adding new OpenAPI features, follow the existing pattern:
  1. Add data classes in `Specs.kt`
  2. Add builder classes in `OpenApiSpec.kt`
  3. Add serialization support if needed
  4. Write comprehensive tests

- The DSL should remain idiomatic Kotlin - prefer extension functions and operator overloading where it makes sense

- All OpenAPI 3.1.0 features should map cleanly to Kotlin constructs