# SchemaComposition

**Package**: `me.farshad.dsl.builder.schema`  
**File**: `SchemaComposition.kt`

## Overview

`SchemaComposition.kt` provides advanced type-safe schema composition features for the OpenAPI DSL. It introduces the `SchemaReference` sealed class, operator overloading for idiomatic Kotlin syntax, and helper extension functions for common schema patterns.

## SchemaReference

### Sealed Class Declaration

```kotlin
@Serializable(with = SchemaReferenceSerializer::class)
sealed class SchemaReference
```

`SchemaReference` represents either a reference to a schema or an inline schema definition, enabling type-safe composition in oneOf, allOf, and anyOf.

### Implementations

#### `SchemaReference.Ref`
A reference to a schema by its path:

```kotlin
data class Ref(val path: String) : SchemaReference()
```

#### `SchemaReference.Inline`
An inline schema definition:

```kotlin
data class Inline(val schema: Schema) : SchemaReference()
```

## Core Functions

### Reference Creation Functions

#### `schemaRef(path: String): SchemaReference`
Creates a schema reference from a string path:

```kotlin
val userRef = schemaRef("#/components/schemas/User")
```

#### `schemaRef(kClass: KClass<T>): SchemaReference`
Creates a schema reference from a Kotlin class:

```kotlin
val userRef = schemaRef(User::class)  // Generates #/components/schemas/User
```

#### `schemaRef<T>(): SchemaReference`
Creates a schema reference using reified type:

```kotlin
val userRef = schemaRef<User>()  // Type-safe reference
```

#### `inlineSchema(block: SchemaBuilder.() -> Unit): SchemaReference`
Creates an inline schema reference:

```kotlin
val schema = inlineSchema {
    type = SchemaType.STRING
    format = SchemaFormat.EMAIL
}
```

## Operator Overloading

### OneOf Composition with `or`

```kotlin
// Create oneOf with two schemas
val schema = schemaRef<Dog>() or schemaRef<Cat>()

// Chain multiple schemas
val schema = schemaRef<Dog>() or schemaRef<Cat>() or schemaRef<Bird>()
```

### AllOf Composition with `and`

```kotlin
// Combine two schemas
val schema = schemaRef<Timestamped>() and schemaRef<Auditable>()

// Chain multiple schemas
val schema = schemaRef<BaseEntity>() and schemaRef<Timestamped>() and schemaRef<Versioned>()
```

## Helper Extension Functions

### `discriminatedUnion`
Creates a discriminated union (oneOf with discriminator) for polymorphic types:

```kotlin
schema {
    discriminatedUnion(
        "type",
        "dog" to Dog::class,
        "cat" to Cat::class,
        "bird" to Bird::class
    )
}
```

### `nullable`
Creates a nullable schema using anyOf with null:

```kotlin
schema {
    nullable {
        type = SchemaType.STRING
        format = SchemaFormat.EMAIL
    }
}
// Equivalent to anyOf: [string, null]
```

### `extending`
Creates a schema that extends another schema using allOf:

```kotlin
// Single inheritance
schema {
    extending(BaseEntity::class) {
        property("username", PropertyType.STRING, required = true)
        property("email", PropertyType.STRING, required = true)
    }
}

// Multiple inheritance
schema {
    extending(BaseEntity::class, Timestamped::class, Auditable::class) {
        property("additionalField", PropertyType.STRING)
    }
}
```

### `oneOfClasses`
Creates a oneOf schema from varargs of classes with optional discriminator:

```kotlin
schema {
    oneOfClasses(
        Dog::class, Cat::class, Bird::class,
        discriminatorProperty = "type",
        discriminatorMappings = mapOf(
            "dog" to Dog::class,
            "cat" to Cat::class,
            "bird" to Bird::class
        )
    )
}
```

### `allOfClasses`
Creates an allOf schema from varargs of classes:

```kotlin
schema {
    allOfClasses(BaseEntity::class, Timestamped::class) {
        property("extraField", PropertyType.STRING)
    }
}
```

### `choice`
Alias for oneOf - creates a schema representing a choice between types:

```kotlin
schema {
    choice {
        schema { type = SchemaType.STRING }
        schema { type = SchemaType.NUMBER }
    }
}
```

### `combine`
Alias for allOf - combines multiple schemas:

```kotlin
schema {
    combine {
        schema(BaseSchema::class)
        schema {
            type = SchemaType.OBJECT
            property("additionalProp", PropertyType.STRING)
        }
    }
}
```

### `optionalSchema<T>`
Creates a schema for optional fields (type or null):

```kotlin
schema {
    optionalSchema<User>()  // anyOf: [User, null]
}
```

## Usage Examples

### Polymorphic API Response

```kotlin
// Define polymorphic response with discriminator
schema("ApiResponse") {
    discriminatedUnion(
        "status",
        "success" to SuccessResponse::class,
        "error" to ErrorResponse::class,
        "pending" to PendingResponse::class
    )
}
```

### Nullable Fields Pattern

```kotlin
schema("UserProfile") {
    type = SchemaType.OBJECT
    
    property("id", PropertyType.STRING, required = true)
    property("username", PropertyType.STRING, required = true)
    
    // Nullable optional field
    property("bio", PropertyType.STRING) {
        nullable {
            type = SchemaType.STRING
            description = "User biography"
        }
    }
}
```

### Entity Extension Pattern

```kotlin
// Base entity
schema("BaseEntity") {
    type = SchemaType.OBJECT
    property("id", PropertyType.STRING, required = true) {
        format = SchemaFormat.UUID
    }
    property("createdAt", PropertyType.STRING, required = true) {
        format = SchemaFormat.DATE_TIME
    }
}

// Extended entity
schema("User") {
    extending(BaseEntity::class) {
        property("username", PropertyType.STRING, required = true)
        property("email", PropertyType.STRING, required = true) {
            format = SchemaFormat.EMAIL
        }
    }
}
```

### Complex Composition with Operators

```kotlin
// Create a complex schema using operators
val timestampedUser = schemaRef<User>() and schemaRef<Timestamped>()
val flexibleInput = schemaRef<StringInput>() or schemaRef<ArrayInput>()

// Use in schema definition
schema("ComplexEntity") {
    allOf = timestampedUser
    
    property("data", PropertyType.OBJECT) {
        oneOf = flexibleInput
    }
}
```

### Type-Safe Schema Building

```kotlin
// Define reusable schema references
val userSchema = schemaRef<User>()
val adminSchema = schemaRef<Admin>()
val guestSchema = schemaRef<Guest>()

// Compose them type-safely
schema("Account") {
    oneOfClasses(
        User::class,
        Admin::class,
        Guest::class,
        discriminatorProperty = "accountType"
    )
}

// Or use the operator syntax
schema("FlexibleAccount") {
    oneOf = userSchema or adminSchema or guestSchema
}
```

## Best Practices

1. **Prefer Type-Safe References**: Use class-based references (`schemaRef<T>()` or `schemaRef(T::class)`) over string references when possible.

2. **Use Helper Functions**: Leverage helper functions like `discriminatedUnion`, `extending`, and `nullable` for common patterns.

3. **Operator Syntax**: Use `or` and `and` operators for simple compositions where readability is improved.

4. **Consistent Patterns**: Choose either builder methods or operators consistently within a schema definition.

5. **Discriminator Usage**: Always use discriminators with polymorphic oneOf schemas for better code generation support.

## Custom Serialization

The `SchemaReferenceSerializer` handles serialization of both reference strings and inline schemas:

- `Ref` instances serialize to schemas with `$ref` property
- `Inline` instances serialize to their contained schema

This enables seamless JSON/YAML generation while maintaining type safety in Kotlin.

## Related Files

- [SchemaBuilder](SchemaBuilder.md) - Main schema builder that uses these composition features
- [OneOfBuilder](OneOfBuilder.md) - Builder for oneOf compositions
- [AllOfBuilder](AllOfBuilder.md) - Builder for allOf compositions
- [AnyOfBuilder](AnyOfBuilder.md) - Builder for anyOf compositions
- [DiscriminatorBuilder](DiscriminatorBuilder.md) - Builder for discriminators