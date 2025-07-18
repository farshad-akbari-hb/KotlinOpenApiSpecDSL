# ExamplesBuilder

**Package**: `me.farshad.dsl.builder.example`  
**File**: `ExamplesBuilder.kt`

## Overview

`ExamplesBuilder` is responsible for managing collections of examples in OpenAPI specifications. It allows you to define multiple named examples for a single schema, parameter, or media type, giving API consumers various scenarios to understand the API better.

## Class Declaration

```kotlin
class ExamplesBuilder
```

## Properties

| Property | Type | Description |
|----------|------|-------------|
| `examples` | `MutableMap<String, Example>` | Map of example names to Example objects |

## Key Methods

### Example Definition Methods

#### `example(name: String, block: ExampleBuilder.() -> Unit)`
Adds a named example with full configuration:

```kotlin
example("success") {
    summary = "Successful response"
    description = "Example of a successful API response"
    objectValue {
        "status" to "success"
        "data" to jsonObjectOf(/* ... */)
    }
}
```

#### `example(name: String, value: JsonElement)`
Adds a simple named example with just a value:

```kotlin
example("minimal", jsonObjectOf("name" to "John", "email" to "john@example.com"))
example("complete", jsonObjectOf(
    "name" to "John Doe",
    "email" to "john@example.com",
    "age" to 30,
    "address" to jsonObjectOf(/* ... */)
))
```

#### `ref(exampleName: String)`
Adds a reference to an example defined in components:

```kotlin
ref("UserExample")  // References #/components/examples/UserExample
```

### Build Method

#### `build(): Map<String, Example>`
Returns the map of examples that have been defined.

## Usage Examples

### Multiple Request Examples

```kotlin
requestBody {
    jsonContent {
        schema {
            ref("CreateUserRequest")
        }
        examples {
            example("minimal") {
                summary = "Minimal user creation"
                description = "Only required fields"
                objectValue {
                    "username" to "newuser"
                    "email" to "user@example.com"
                    "password" to "SecurePass123!"
                }
            }
            
            example("withProfile") {
                summary = "User with profile"
                description = "Including optional profile information"
                objectValue {
                    "username" to "newuser"
                    "email" to "user@example.com"
                    "password" to "SecurePass123!"
                    "profile" to jsonObjectOf(
                        "firstName" to "New",
                        "lastName" to "User",
                        "bio" to "Software developer",
                        "avatar" to "https://example.com/avatar.jpg"
                    )
                }
            }
            
            example("withPreferences") {
                summary = "User with preferences"
                description = "Including notification preferences"
                objectValue {
                    "username" to "newuser"
                    "email" to "user@example.com"
                    "password" to "SecurePass123!"
                    "preferences" to jsonObjectOf(
                        "emailNotifications" to true,
                        "smsNotifications" to false,
                        "theme" to "dark",
                        "language" to "en"
                    )
                }
            }
            
            example("complete") {
                summary = "Complete user object"
                description = "All possible fields populated"
                objectValue {
                    "username" to "newuser"
                    "email" to "user@example.com"
                    "password" to "SecurePass123!"
                    "profile" to jsonObjectOf(
                        "firstName" to "New",
                        "lastName" to "User",
                        "bio" to "Full-stack developer with 5 years experience",
                        "avatar" to "https://example.com/avatar.jpg",
                        "location" to "San Francisco, CA",
                        "website" to "https://johndoe.dev"
                    )
                    "preferences" to jsonObjectOf(
                        "emailNotifications" to true,
                        "smsNotifications" to false,
                        "theme" to "dark",
                        "language" to "en",
                        "timezone" to "America/Los_Angeles"
                    )
                    "socialLinks" to jsonObjectOf(
                        "twitter" to "@johndoe",
                        "github" to "johndoe",
                        "linkedin" to "john-doe"
                    )
                }
            }
        }
    }
}
```

### Response Status Examples

```kotlin
response("200", "Successful operation") {
    jsonContent {
        schema {
            ref("ApiResponse")
        }
        examples {
            example("success") {
                summary = "Successful response"
                objectValue {
                    "success" to true
                    "data" to jsonObjectOf(
                        "id" to "123",
                        "message" to "Operation completed successfully"
                    )
                    "timestamp" to "2023-10-15T10:00:00Z"
                }
            }
            
            example("successWithWarning") {
                summary = "Success with warnings"
                description = "Operation succeeded but with warnings"
                objectValue {
                    "success" to true
                    "data" to jsonObjectOf(
                        "id" to "123",
                        "message" to "Operation completed with warnings"
                    )
                    "warnings" to jsonArrayOf(
                        "Field 'oldField' is deprecated, use 'newField' instead",
                        "Rate limit almost reached: 95% of quota used"
                    )
                    "timestamp" to "2023-10-15T10:00:00Z"
                }
            }
            
            example("partialSuccess") {
                summary = "Partial success"
                description = "Some operations succeeded, some failed"
                objectValue {
                    "success" to true
                    "data" to jsonObjectOf(
                        "processed" to 8,
                        "failed" to 2,
                        "results" to jsonArrayOf(/* ... */)
                    )
                    "errors" to jsonArrayOf(
                        jsonObjectOf(
                            "index" to 3,
                            "error" to "Invalid data format"
                        ),
                        jsonObjectOf(
                            "index" to 7,
                            "error" to "Duplicate entry"
                        )
                    )
                    "timestamp" to "2023-10-15T10:00:00Z"
                }
            }
        }
    }
}
```

### Error Response Examples

```kotlin
response("400", "Bad Request") {
    jsonContent {
        schema {
            ref("ErrorResponse")
        }
        examples {
            example("validationError") {
                summary = "Validation error"
                description = "Request failed validation"
                objectValue {
                    "error" to jsonObjectOf(
                        "code" to "VALIDATION_ERROR",
                        "message" to "Request validation failed",
                        "details" to jsonArrayOf(
                            jsonObjectOf(
                                "field" to "email",
                                "value" to "not-an-email",
                                "issue" to "Invalid email format"
                            ),
                            jsonObjectOf(
                                "field" to "age",
                                "value" to -5,
                                "issue" to "Age must be positive"
                            )
                        )
                    )
                    "timestamp" to "2023-10-15T10:00:00Z"
                    "traceId" to "550e8400-e29b-41d4-a716-446655440000"
                }
            }
            
            example("missingRequired") {
                summary = "Missing required fields"
                objectValue {
                    "error" to jsonObjectOf(
                        "code" to "MISSING_REQUIRED_FIELD",
                        "message" to "Required fields are missing",
                        "details" to jsonArrayOf(
                            jsonObjectOf(
                                "field" to "username",
                                "issue" to "Field is required"
                            ),
                            jsonObjectOf(
                                "field" to "email",
                                "issue" to "Field is required"
                            )
                        )
                    )
                    "timestamp" to "2023-10-15T10:00:00Z"
                    "traceId" to "660e8400-e29b-41d4-a716-446655440001"
                }
            }
            
            example("malformedJson") {
                summary = "Malformed JSON"
                objectValue {
                    "error" to jsonObjectOf(
                        "code" to "MALFORMED_JSON",
                        "message" to "Request body contains invalid JSON",
                        "details" to "Unexpected character at position 42: expected ',' but found '}'"
                    )
                    "timestamp" to "2023-10-15T10:00:00Z"
                    "traceId" to "770e8400-e29b-41d4-a716-446655440002"
                }
            }
        }
    }
}
```

### Query Parameter Examples

```kotlin
queryParameter("filter") {
    description = "Filter expression"
    schema {
        type = "string"
    }
    examples {
        example("simple") {
            summary = "Simple equality filter"
            value("status:active")
        }
        
        example("multiple") {
            summary = "Multiple conditions"
            value("status:active AND category:electronics")
        }
        
        example("complex") {
            summary = "Complex filter with operators"
            value("(price:>100 AND price:<500) OR category:premium")
        }
        
        example("withDates") {
            summary = "Date range filter"
            value("created:>=2023-01-01 AND created:<=2023-12-31")
        }
    }
}
```

### Different Data Formats

```kotlin
response("200", "Data in requested format") {
    // JSON examples
    content("application/json") {
        schema { ref("UserData") }
        examples {
            example("json") {
                summary = "JSON format"
                objectValue {
                    "users" to jsonArrayOf(
                        jsonObjectOf(
                            "id" to 1,
                            "name" to "John Doe",
                            "email" to "john@example.com"
                        )
                    )
                }
            }
        }
    }
    
    // XML examples
    content("application/xml") {
        schema { ref("UserData") }
        examples {
            example("xml") {
                summary = "XML format"
                value("""
                    <users>
                        <user>
                            <id>1</id>
                            <name>John Doe</name>
                            <email>john@example.com</email>
                        </user>
                    </users>
                """.trimIndent())
            }
        }
    }
    
    // CSV examples
    content("text/csv") {
        schema { type = "string" }
        examples {
            example("csv") {
                summary = "CSV format"
                value("""
                    id,name,email
                    1,"John Doe",john@example.com
                    2,"Jane Smith",jane@example.com
                """.trimIndent())
            }
        }
    }
}
```

### Locale-Specific Examples

```kotlin
examples {
    example("en-US") {
        summary = "English (US) response"
        objectValue {
            "message" to "Welcome to our service!"
            "date" to "10/15/2023"
            "currency" to "$1,234.56"
        }
    }
    
    example("en-GB") {
        summary = "English (UK) response"
        objectValue {
            "message" to "Welcome to our service!"
            "date" to "15/10/2023"
            "currency" to "£1,234.56"
        }
    }
    
    example("es-ES") {
        summary = "Spanish response"
        objectValue {
            "message" to "¡Bienvenido a nuestro servicio!"
            "date" to "15/10/2023"
            "currency" to "1.234,56 €"
        }
    }
    
    example("ja-JP") {
        summary = "Japanese response"
        objectValue {
            "message" to "私たちのサービスへようこそ！"
            "date" to "2023/10/15"
            "currency" to "¥1,234"
        }
    }
}
```

### Using Component References

```kotlin
// Define examples in components
components {
    example("StandardUser") {
        summary = "Standard user object"
        objectValue {
            "id" to "123",
            "username" to "johndoe",
            "role" to "user"
        }
    }
    
    example("AdminUser") {
        summary = "Admin user object"
        objectValue {
            "id" to "456",
            "username" to "admin",
            "role" to "admin",
            "permissions" to jsonArrayOf("read", "write", "delete")
        }
    }
}

// Use in operations
paths {
    path("/users/{id}") {
        get {
            response("200", "User found") {
                jsonContent("User") {
                    examples {
                        ref("StandardUser")
                        ref("AdminUser")
                        
                        // Can mix references with inline examples
                        example("guestUser") {
                            summary = "Guest user"
                            objectValue {
                                "id" to "guest",
                                "username" to "guest",
                                "role" to "guest"
                            }
                        }
                    }
                }
            }
        }
    }
}
```

## Best Practices

1. **Name examples clearly**: Use descriptive names that indicate the scenario.

2. **Provide variety**: Include examples for different use cases and edge cases.

3. **Keep consistent**: Use consistent data across related examples.

4. **Document thoroughly**: Use summaries and descriptions to explain each example.

5. **Cover error cases**: Include examples for various error scenarios.

6. **Show optional fields**: Demonstrate which fields are optional with different examples.

7. **Use realistic data**: Make examples as realistic as possible.

## Common Patterns

### Progressive Disclosure Pattern

```kotlin
examples {
    example("minimal") {
        summary = "Minimal required data"
        // Only required fields
    }
    
    example("typical") {
        summary = "Typical use case"
        // Common fields that most users provide
    }
    
    example("complete") {
        summary = "All fields populated"
        // Every possible field
    }
}
```

### Scenario-Based Pattern

```kotlin
examples {
    example("newCustomer") {
        summary = "New customer checkout"
        // First-time customer data
    }
    
    example("returningCustomer") {
        summary = "Returning customer"
        // Customer with saved preferences
    }
    
    example("guestCheckout") {
        summary = "Guest checkout"
        // Minimal checkout data
    }
}
```

### Status-Based Pattern

```kotlin
examples {
    example("pending") {
        summary = "Pending status"
        objectValue { "status" to "pending" /* ... */ }
    }
    
    example("processing") {
        summary = "Processing status"
        objectValue { "status" to "processing" /* ... */ }
    }
    
    example("completed") {
        summary = "Completed status"
        objectValue { "status" to "completed" /* ... */ }
    }
    
    example("failed") {
        summary = "Failed status"
        objectValue { "status" to "failed" /* ... */ }
    }
}
```

## Related Builders

- [ExampleBuilder](ExampleBuilder.md) - For building individual examples
- [ComponentsBuilder](../components/ComponentsBuilder.md) - For defining reusable examples
- [MediaTypeBuilder](MediaTypeBuilder.md) - Where examples are used
- [ParameterBuilder](ParameterBuilder.md) - Can include parameter examples