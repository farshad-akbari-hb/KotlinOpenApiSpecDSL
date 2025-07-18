# AllOfBuilder

**Package**: `me.farshad.dsl.builder.schema`  
**File**: `CompositionBuilders.kt`

## Overview

`AllOfBuilder` is used to create `allOf` schema compositions in OpenAPI. An `allOf` schema validates data against all of the provided schemas - the data must be valid against every schema in the list. This is commonly used for schema inheritance and combining multiple schemas.

## Class Declaration

```kotlin
class AllOfBuilder
```

## Properties

| Property | Type | Description |
|----------|------|-------------|
| `schemas` | `MutableList<SchemaReference>` | List of schema references that must all be satisfied |

## Key Methods

### Schema Addition Methods

#### `schema(ref: String)`
Adds a schema reference by name:

```kotlin
schema("BaseModel")  // Auto-prefixes with #/components/schemas/
schema("#/components/schemas/BaseModel")  // Full reference path
```

#### `schema(clazz: KClass<*>)`
Adds a schema reference using a Kotlin class:

```kotlin
schema(BaseEntity::class)  // References #/components/schemas/BaseEntity
```

#### `schema(block: SchemaBuilder.() -> Unit)`
Adds an inline schema to the allOf:

```kotlin
schema {
    type = SchemaType.OBJECT
    property("additionalField", PropertyType.STRING)
}
```

### Build Method

#### `build(): List<SchemaReference>`
Returns the list of schema references for use in the parent SchemaBuilder.

## Usage Examples

### Basic Inheritance Pattern

```kotlin
// Base schema
components {
    schema("BaseEntity") {
        type = SchemaType.OBJECT
        property("id", PropertyType.STRING, required = true) {
            format = SchemaFormat.UUID
        }
        property("createdAt", PropertyType.STRING, required = true) {
            format = SchemaFormat.DATE_TIME
        }
        property("updatedAt", PropertyType.STRING, required = true) {
            format = SchemaFormat.DATE_TIME
        }
    }
    
    // Extended schema using allOf
    schema("User") {
        allOf {
            schema("BaseEntity")
            schema {
                type = SchemaType.OBJECT
                property("username", PropertyType.STRING, required = true)
                property("email", PropertyType.STRING, required = true) {
                    format = SchemaFormat.EMAIL
                }
                property("roles", PropertyType.ARRAY) {
                    items {
                        type = SchemaType.STRING
                    }
                }
            }
        }
    }
}
```

### Composing Multiple Traits

```kotlin
components {
    // Trait schemas
    schema("Timestamped") {
        type = SchemaType.OBJECT
        property("createdAt", PropertyType.STRING) {
            format = SchemaFormat.DATE_TIME
        }
        property("updatedAt", PropertyType.STRING) {
            format = SchemaFormat.DATE_TIME
        }
    }
    
    schema("Versioned") {
        type = SchemaType.OBJECT
        property("version", PropertyType.INTEGER, required = true) {
            description = "Entity version for optimistic locking"
        }
    }
    
    schema("SoftDeletable") {
        type = SchemaType.OBJECT
        property("deletedAt", PropertyType.STRING) {
            format = SchemaFormat.DATE_TIME
            description = "Soft deletion timestamp"
        }
    }
    
    // Compose all traits
    schema("AuditedEntity") {
        allOf {
            schema("Timestamped")
            schema("Versioned")
            schema("SoftDeletable")
            schema {
                type = SchemaType.OBJECT
                property("id", PropertyType.STRING, required = true) {
                    format = SchemaFormat.UUID
                }
                property("createdBy", PropertyType.STRING)
                property("updatedBy", PropertyType.STRING)
            }
        }
    }
}
```

### Adding Constraints to Existing Schema

```kotlin
// Original product schema
schema("Product") {
    type = SchemaType.OBJECT
    property("name", PropertyType.STRING)
    property("price", PropertyType.NUMBER)
    property("category", PropertyType.STRING)
}

// Premium product with additional constraints
schema("PremiumProduct") {
    allOf {
        schema("Product")
        schema {
            type = SchemaType.OBJECT
            property("warranty", PropertyType.OBJECT, required = true) {
                property("years", PropertyType.INTEGER, required = true)
                property("coverage", PropertyType.STRING, required = true)
            }
            property("premiumFeatures", PropertyType.ARRAY, required = true) {
                items {
                    type = SchemaType.STRING
                }
            }
        }
    }
}
```

### Mixin Pattern

```kotlin
schema("PaginatedUserResponse") {
    allOf {
        // Pagination mixin
        schema {
            type = SchemaType.OBJECT
            property("page", PropertyType.INTEGER, required = true)
            property("pageSize", PropertyType.INTEGER, required = true)
            property("totalPages", PropertyType.INTEGER, required = true)
            property("totalItems", PropertyType.INTEGER, required = true)
        }
        
        // User data
        schema {
            type = SchemaType.OBJECT
            property("users", PropertyType.ARRAY, required = true) {
                items {
                    type = SchemaType.OBJECT
                    // Reference to User schema
                }
            }
        }
        
        // Metadata mixin
        schema {
            type = SchemaType.OBJECT
            property("generatedAt", PropertyType.STRING) {
                format = SchemaFormat.DATE_TIME
            }
            property("requestId", PropertyType.STRING) {
                format = SchemaFormat.UUID
            }
        }
    }
}
```

### Using AllOf with Class References

```kotlin
// Compose using Kotlin class references
schema("ExtendedUser") {
    allOf(BaseUser::class, Timestamped::class, Auditable::class)
}

// Or mix references and inline schemas
schema("ProductWithMetadata") {
    allOf {
        schema(Product::class)
        schema(Timestamped::class)
        schema {
            type = SchemaType.OBJECT
            property("metadata", PropertyType.OBJECT) {
                property("tags", PropertyType.ARRAY) {
                    items { type = SchemaType.STRING }
                }
                property("category", PropertyType.STRING)
            }
        }
    }
}
```

### Role-Based Schema Extension

```kotlin
components {
    // Base user schema
    schema("BaseUser") {
        type = SchemaType.OBJECT
        property("id", PropertyType.STRING, required = true)
        property("username", PropertyType.STRING, required = true)
        property("email", PropertyType.STRING, required = true)
    }
    
    // Admin user with additional fields
    schema("AdminUser") {
        allOf {
            schema("BaseUser")
            schema {
                type = SchemaType.OBJECT
                property("adminLevel", PropertyType.INTEGER, required = true)
                property("permissions", PropertyType.ARRAY, required = true) {
                    items {
                        type = SchemaType.STRING
                    }
                }
                property("managedDepartments", PropertyType.ARRAY) {
                    items {
                        type = SchemaType.STRING
                    }
                }
            }
        }
    }
}
```

## Best Practices

1. **Use for inheritance**: AllOf is perfect for extending base schemas with additional properties.

2. **Avoid conflicts**: Ensure schemas don't have conflicting property definitions.

3. **Order matters for clarity**: Put base schemas first, then additions.

4. **Document the composition**: Explain why schemas are being combined.

5. **Keep it simple**: Don't over-compose - too many allOf schemas can be confusing.

## Common Patterns

### Base Entity Pattern

```kotlin
// Common pattern for entities with standard fields
schema("Entity") {
    allOf("Identifiable", "Timestamped", "Auditable")
}

// Or using Kotlin classes
schema("Entity") {
    allOf(Identifiable::class, Timestamped::class, Auditable::class)
}
```

### Trait Composition Pattern

```kotlin
// Compose multiple trait schemas
schema("CompleteEntity") {
    allOf {
        schema("BaseEntity")
        schema("Versioned")
        schema("SoftDeletable")
        schema("Auditable")
    }
}
```

### Progressive Enhancement Pattern

```kotlin
schema("EnhancedProduct") {
    allOf {
        // Base product
        schema("Product")
        
        // Add search capabilities
        schema("Searchable")
        
        // Add inventory tracking
        schema("Trackable")
        
        // Add custom attributes
        schema {
            type = SchemaType.OBJECT
            property("customAttributes", PropertyType.OBJECT)
        }
    }
}
```

## Validation Behavior

When validating against an allOf schema:

1. The data is validated against each schema in the list
2. Validation succeeds only if ALL schemas validate successfully
3. Properties from all schemas are merged
4. Required fields from all schemas must be present

Example validation:
```json
// Schema: allOf [
//   {type: "object", properties: {a: {type: "string"}}, required: ["a"]},
//   {type: "object", properties: {b: {type: "number"}}, required: ["b"]}
// ]

{"a": "hello", "b": 42}          ✓ Valid (satisfies both)
{"a": "hello"}                   ✗ Invalid (missing required 'b')
{"b": 42}                        ✗ Invalid (missing required 'a')
{"a": "hello", "b": "42"}        ✗ Invalid (b must be number)
{"a": "hello", "b": 42, "c": 1}  ✓ Valid (extra properties allowed)
```

## Related Builders

- [SchemaBuilder](SchemaBuilder.md) - Parent builder that uses AllOfBuilder
- [OneOfBuilder](OneOfBuilder.md) - For exclusive choice (exactly one must match)
- [AnyOfBuilder](AnyOfBuilder.md) - For inclusive choice (one or more must match)
- [DiscriminatorBuilder](DiscriminatorBuilder.md) - For discriminator configuration
- [SchemaComposition](SchemaComposition.md) - For advanced type-safe composition patterns