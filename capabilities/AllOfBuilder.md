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
| `schemas` | `MutableList<Schema>` | List of schemas that must all be satisfied |

## Key Methods

### Schema Addition Methods

#### `schema(block: SchemaBuilder.() -> Unit)`
Adds an inline schema to the allOf:

```kotlin
schema {
    type = "object"
    properties {
        property("additionalField", "string")
    }
}
```

#### `schema(type: String, block: SchemaBuilder.() -> Unit = {})`
Adds a typed schema to the allOf:

```kotlin
schema("object") {
    properties {
        property("id", "string")
    }
    required.add("id")
}
```

#### `schemaRef(schemaName: String)`
Adds a schema reference to the allOf:

```kotlin
schemaRef("BaseModel")  // References #/components/schemas/BaseModel
```

#### `schemaRef<T>()`
Adds a schema reference using a Kotlin class (with reified type):

```kotlin
schemaRef<BaseEntity>()  // References the BaseEntity class schema
```

### Build Method

#### `build(): Schema`
Builds the final Schema object with allOf composition.

## Usage Examples

### Basic Inheritance Pattern

```kotlin
// Base schema
components {
    schema("BaseEntity") {
        type = "object"
        properties {
            property("id", "string") {
                format = "uuid"
                readOnly = true
            }
            property("createdAt", "string") {
                format = "date-time"
                readOnly = true
            }
            property("updatedAt", "string") {
                format = "date-time"
                readOnly = true
            }
        }
        required.addAll(listOf("id", "createdAt", "updatedAt"))
    }
    
    // Extended schema using allOf
    schema("User") {
        allOf = listOf(
            Schema(ref = "#/components/schemas/BaseEntity"),
            Schema(
                type = "object",
                properties = mapOf(
                    "username" to Schema(type = "string", minLength = 3),
                    "email" to Schema(type = "string", format = "email"),
                    "roles" to Schema(
                        type = "array",
                        items = Schema(type = "string")
                    )
                ),
                required = listOf("username", "email")
            )
        )
    }
}
```

### Composing Multiple Traits

```kotlin
components {
    // Trait schemas
    schema("Timestamped") {
        type = "object"
        properties {
            property("createdAt", "string") { format = "date-time" }
            property("updatedAt", "string") { format = "date-time" }
        }
    }
    
    schema("Versioned") {
        type = "object"
        properties {
            property("version", "integer") {
                minimum = 1
                description = "Entity version for optimistic locking"
            }
        }
        required.add("version")
    }
    
    schema("SoftDeletable") {
        type = "object"
        properties {
            property("deletedAt", "string") {
                format = "date-time"
                nullable = true
                description = "Soft deletion timestamp"
            }
        }
    }
    
    // Compose all traits
    schema("AuditedEntity") {
        allOf = listOf(
            Schema(ref = "#/components/schemas/Timestamped"),
            Schema(ref = "#/components/schemas/Versioned"),
            Schema(ref = "#/components/schemas/SoftDeletable"),
            Schema(
                type = "object",
                properties = mapOf(
                    "id" to Schema(type = "string", format = "uuid"),
                    "createdBy" to Schema(type = "string"),
                    "updatedBy" to Schema(type = "string")
                ),
                required = listOf("id")
            )
        )
    }
}
```

### Adding Constraints to Existing Schema

```kotlin
// Original product schema
schema("Product") {
    type = "object"
    properties {
        property("name", "string")
        property("price", "number")
        property("category", "string")
    }
}

// Premium product with additional constraints
schema("PremiumProduct") {
    allOf = listOf(
        Schema(ref = "#/components/schemas/Product"),
        Schema(
            type = "object",
            properties = mapOf(
                "price" to Schema(
                    type = "number",
                    minimum = 100,
                    description = "Premium products must cost at least $100"
                ),
                "warranty" to Schema(
                    type = "object",
                    properties = mapOf(
                        "years" to Schema(type = "integer", minimum = 2),
                        "coverage" to Schema(type = "string")
                    ),
                    required = listOf("years", "coverage")
                ),
                "premiumFeatures" to Schema(
                    type = "array",
                    items = Schema(type = "string"),
                    minItems = 1
                )
            ),
            required = listOf("warranty", "premiumFeatures")
        )
    )
}
```

### Mixin Pattern

```kotlin
val paginatedUserResponse = allOfBuilder {
    // Pagination mixin
    schema {
        type = "object"
        properties {
            property("page", "integer") { minimum = 1 }
            property("pageSize", "integer") { minimum = 1; maximum = 100 }
            property("totalPages", "integer")
            property("totalItems", "integer")
        }
        required.addAll(listOf("page", "pageSize", "totalPages", "totalItems"))
    }
    
    // User data
    schema {
        type = "object"
        properties {
            property("users") {
                type = "array"
                items { ref("User") }
            }
        }
        required.add("users")
    }
    
    // Metadata mixin
    schema {
        type = "object"
        properties {
            property("generatedAt", "string") { format = "date-time" }
            property("requestId", "string") { format = "uuid" }
        }
    }
}
```

### Complex Validation Composition

```kotlin
schema("ValidatedUserInput") {
    allOf = listOf(
        // Base user schema
        Schema(ref = "#/components/schemas/UserInput"),
        
        // Additional validation rules
        Schema(
            type = "object",
            properties = mapOf(
                "password" to Schema(
                    type = "string",
                    minLength = 8,
                    maxLength = 128,
                    pattern = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]"
                ),
                "age" to Schema(
                    type = "integer",
                    minimum = 18,
                    maximum = 120
                ),
                "email" to Schema(
                    type = "string",
                    format = "email",
                    pattern = "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$"
                )
            )
        ),
        
        // Business rules
        Schema(
            type = "object",
            not = Schema(
                properties = mapOf(
                    "username" to Schema(
                        type = "string",
                        enum = listOf("admin", "root", "system")
                    )
                )
            )
        )
    )
}
```

### Extending Third-Party Schemas

```kotlin
// Extending an external schema with local additions
schema("ExtendedGeoJSON") {
    allOf = listOf(
        // Reference to external GeoJSON schema
        Schema(ref = "https://geojson.org/schema/Feature.json"),
        
        // Local extensions
        Schema(
            type = "object",
            properties = mapOf(
                "properties" to Schema(
                    type = "object",
                    properties = mapOf(
                        "customId" to Schema(type = "string"),
                        "metadata" to Schema(
                            type = "object",
                            additionalProperties = Schema(type = "string")
                        ),
                        "tags" to Schema(
                            type = "array",
                            items = Schema(type = "string"),
                            uniqueItems = true
                        )
                    ),
                    required = listOf("customId")
                )
            )
        )
    )
}
```

### Role-Based Schema Extension

```kotlin
components {
    // Base user schema
    schema("BaseUser") {
        type = "object"
        properties {
            property("id", "string")
            property("username", "string")
            property("email", "string")
        }
        required.addAll(listOf("id", "username", "email"))
    }
    
    // Admin user with additional fields
    schema("AdminUser") {
        allOf = listOf(
            Schema(ref = "#/components/schemas/BaseUser"),
            Schema(
                type = "object",
                properties = mapOf(
                    "adminLevel" to Schema(
                        type = "integer",
                        minimum = 1,
                        maximum = 5
                    ),
                    "permissions" to Schema(
                        type = "array",
                        items = Schema(
                            type = "string",
                            enum = listOf("read", "write", "delete", "manage_users")
                        )
                    ),
                    "managedDepartments" to Schema(
                        type = "array",
                        items = Schema(type = "string")
                    )
                ),
                required = listOf("adminLevel", "permissions")
            )
        )
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
    allOf = listOf(
        schemaRef("Identifiable"),    // Has 'id'
        schemaRef("Timestamped"),     // Has 'createdAt', 'updatedAt'
        schemaRef("Auditable")        // Has 'createdBy', 'updatedBy'
    )
}
```

### Feature Flag Pattern

```kotlin
// Composing schemas based on features
schema("User") {
    val schemas = mutableListOf(
        Schema(ref = "#/components/schemas/BaseUser")
    )
    
    if (features.contains("2FA")) {
        schemas.add(Schema(ref = "#/components/schemas/TwoFactorAuth"))
    }
    
    if (features.contains("GDPR")) {
        schemas.add(Schema(ref = "#/components/schemas/GdprCompliant"))
    }
    
    allOf = schemas
}
```

### Validation Layers Pattern

```kotlin
schema("ValidatedInput") {
    allOf = listOf(
        // Structure
        Schema(ref = "#/components/schemas/InputStructure"),
        
        // Format validation
        Schema(ref = "#/components/schemas/FormatRules"),
        
        // Business rules
        Schema(ref = "#/components/schemas/BusinessConstraints"),
        
        // Security rules
        Schema(ref = "#/components/schemas/SecurityRequirements")
    )
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
- [ComponentsBuilder](ComponentsBuilder.md) - For defining reusable schemas