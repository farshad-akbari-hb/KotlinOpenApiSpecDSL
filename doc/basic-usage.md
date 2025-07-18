# Basic Usage

This guide covers common patterns and basic usage of the Kotlin OpenAPI Spec DSL for typical API scenarios.

## Table of Contents
- [CRUD Operations](#crud-operations)
- [Response Handling](#response-handling)
- [Request Validation](#request-validation)
- [Error Handling](#error-handling)
- [Pagination](#pagination)
- [Filtering and Sorting](#filtering-and-sorting)
- [File Upload/Download](#file-uploaddownload)

## CRUD Operations

Here's how to implement standard CRUD (Create, Read, Update, Delete) operations:

```kotlin
import me.farshad.openapi.*
import kotlinx.serialization.Serializable

@Serializable
data class Book(
    val id: String,
    val title: String,
    val author: String,
    val isbn: String,
    val publishedYear: Int
)

@Serializable
data class CreateBookRequest(
    val title: String,
    val author: String,
    val isbn: String,
    val publishedYear: Int
)

@Serializable
data class UpdateBookRequest(
    val title: String? = null,
    val author: String? = null,
    val publishedYear: Int? = null
)

val bookApiSpec = openApi {
    openapi = "3.1.0"
    info {
        title = "Book Library API"
        version = "1.0.0"
    }
    
    components {
        schema(Book::class)
        schema(CreateBookRequest::class)
        schema(UpdateBookRequest::class)
    }
    
    paths {
        // List all books
        path("/books") {
            get {
                summary = "List all books"
                description = "Retrieve a list of all books in the library"
                tags = listOf("Books")
                
                response("200", "Successful response") {
                    jsonContent(listOf<Book>())
                }
            }
            
            // Create a new book
            post {
                summary = "Create a new book"
                tags = listOf("Books")
                
                requestBody("Book to create") {
                    jsonContent(CreateBookRequest::class)
                    required = true
                }
                
                response("201", "Book created successfully") {
                    jsonContent(Book::class)
                }
                
                response("400", "Invalid request") {
                    jsonContent {
                        schema {
                            type = SchemaType.OBJECT
                            properties {
                                "error" to schema {
                                    type = SchemaType.STRING
                                }
                            }
                        }
                    }
                }
            }
        }
        
        // Single book operations
        path("/books/{bookId}") {
            parameter {
                name = "bookId"
                `in` = ParameterLocation.PATH
                required = true
                description = "Unique book identifier"
                schema {
                    type = SchemaType.STRING
                    pattern = "^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$"
                }
            }
            
            // Get a single book
            get {
                summary = "Get book by ID"
                tags = listOf("Books")
                
                response("200", "Book found") {
                    jsonContent(Book::class)
                }
                
                response("404", "Book not found") {
                    jsonContent {
                        example = mapOf("error" to "Book not found")
                    }
                }
            }
            
            // Update a book (partial update)
            patch {
                summary = "Update book details"
                description = "Partially update book information"
                tags = listOf("Books")
                
                requestBody("Fields to update") {
                    jsonContent(UpdateBookRequest::class)
                    required = true
                }
                
                response("200", "Book updated") {
                    jsonContent(Book::class)
                }
                
                response("404", "Book not found")
            }
            
            // Replace a book (full update)
            put {
                summary = "Replace book"
                description = "Replace entire book information"
                tags = listOf("Books")
                
                requestBody("New book data") {
                    jsonContent(CreateBookRequest::class)
                    required = true
                }
                
                response("200", "Book replaced") {
                    jsonContent(Book::class)
                }
            }
            
            // Delete a book
            delete {
                summary = "Delete book"
                tags = listOf("Books")
                
                response("204", "Book deleted successfully")
                response("404", "Book not found")
            }
        }
    }
}
```

## Response Handling

### Multiple Response Types

```kotlin
path("/users/{id}") {
    get {
        summary = "Get user by ID"
        
        // Success responses
        response("200", "User found") {
            jsonContent(User::class)
            
            headers {
                header("X-Rate-Limit-Remaining") {
                    description = "Number of requests remaining"
                    schema {
                        type = SchemaType.INTEGER
                    }
                }
            }
        }
        
        // Client errors
        response("400", "Invalid ID format") {
            jsonContent {
                schema {
                    type = SchemaType.OBJECT
                    properties {
                        "error" to schema {
                            type = SchemaType.STRING
                            enum = listOf("INVALID_ID_FORMAT")
                        }
                        "message" to schema {
                            type = SchemaType.STRING
                        }
                    }
                }
            }
        }
        
        response("404", "User not found") {
            jsonContent {
                example = mapOf(
                    "error" to "NOT_FOUND",
                    "message" to "User with specified ID does not exist"
                )
            }
        }
        
        // Server errors
        response("500", "Internal server error") {
            jsonContent {
                schema {
                    ref = "#/components/schemas/Error"
                }
            }
        }
    }
}
```

### Different Content Types

```kotlin
path("/reports/{id}") {
    get {
        summary = "Download report"
        
        response("200", "Report content") {
            content("application/pdf") {
                schema {
                    type = SchemaType.STRING
                    format = "binary"
                }
            }
            
            content("application/json") {
                schema {
                    type = SchemaType.OBJECT
                    properties {
                        "title" to schema { type = SchemaType.STRING }
                        "data" to schema { 
                            type = SchemaType.ARRAY
                            items = schema { type = SchemaType.OBJECT }
                        }
                    }
                }
            }
            
            content("text/csv") {
                schema {
                    type = SchemaType.STRING
                }
                example = "id,name,value\n1,Item1,100\n2,Item2,200"
            }
        }
    }
}
```

## Request Validation

### Parameter Validation

```kotlin
path("/search") {
    get {
        summary = "Search items"
        
        parameter {
            name = "q"
            `in` = ParameterLocation.QUERY
            required = true
            description = "Search query"
            schema {
                type = SchemaType.STRING
                minLength = 3
                maxLength = 100
            }
        }
        
        parameter {
            name = "category"
            `in` = ParameterLocation.QUERY
            description = "Filter by category"
            schema {
                type = SchemaType.STRING
                enum = listOf("electronics", "books", "clothing", "food")
            }
        }
        
        parameter {
            name = "minPrice"
            `in` = ParameterLocation.QUERY
            schema {
                type = SchemaType.NUMBER
                minimum = 0
                exclusiveMinimum = true
            }
        }
        
        parameter {
            name = "maxPrice"
            `in` = ParameterLocation.QUERY
            schema {
                type = SchemaType.NUMBER
                maximum = 10000
            }
        }
        
        response("200", "Search results") {
            jsonContent(listOf<SearchResult>())
        }
    }
}
```

### Request Body Validation

```kotlin
@Serializable
data class CreateUserRequest(
    val username: String,
    val email: String,
    val password: String,
    val age: Int
)

path("/users") {
    post {
        summary = "Create user"
        
        requestBody("User data") {
            required = true
            jsonContent {
                schema {
                    type = SchemaType.OBJECT
                    required = listOf("username", "email", "password")
                    properties {
                        "username" to schema {
                            type = SchemaType.STRING
                            pattern = "^[a-zA-Z0-9_]{3,20}$"
                            description = "Alphanumeric username (3-20 characters)"
                        }
                        "email" to schema {
                            type = SchemaType.STRING
                            format = "email"
                        }
                        "password" to schema {
                            type = SchemaType.STRING
                            format = "password"
                            minLength = 8
                            description = "Minimum 8 characters"
                        }
                        "age" to schema {
                            type = SchemaType.INTEGER
                            minimum = 18
                            maximum = 120
                        }
                    }
                }
                
                // Provide examples
                examples {
                    example("valid-user") {
                        summary = "Valid user creation"
                        value = mapOf(
                            "username" to "john_doe",
                            "email" to "john@example.com",
                            "password" to "securePass123",
                            "age" to 25
                        )
                    }
                }
            }
        }
        
        response("201", "User created")
        response("400", "Validation error")
    }
}
```

## Error Handling

### Standardized Error Responses

```kotlin
// Define reusable error schemas
@Serializable
data class ErrorResponse(
    val error: ErrorDetails,
    val timestamp: String,
    val path: String
)

@Serializable
data class ErrorDetails(
    val code: String,
    val message: String,
    val details: Map<String, String>? = null
)

@Serializable
data class ValidationError(
    val field: String,
    val message: String,
    val rejectedValue: String? = null
)

// In your OpenAPI spec
components {
    schema(ErrorResponse::class)
    schema(ErrorDetails::class)
    schema(ValidationError::class)
    
    // Define reusable error responses
    response("BadRequest") {
        description = "Bad request"
        jsonContent {
            schema {
                ref = "#/components/schemas/ErrorResponse"
            }
            example = mapOf(
                "error" to mapOf(
                    "code" to "BAD_REQUEST",
                    "message" to "Invalid request data",
                    "details" to mapOf(
                        "username" to "Username already exists"
                    )
                ),
                "timestamp" to "2023-10-20T15:30:00Z",
                "path" to "/api/users"
            )
        }
    }
    
    response("NotFound") {
        description = "Resource not found"
        jsonContent(ErrorResponse::class)
    }
    
    response("ServerError") {
        description = "Internal server error"
        jsonContent(ErrorResponse::class)
    }
}

// Use in paths
paths {
    path("/users/{id}") {
        get {
            response("200", "Success") {
                jsonContent(User::class)
            }
            response {
                ref = "#/components/responses/NotFound"
            }
            response {
                ref = "#/components/responses/ServerError"
            }
        }
    }
}
```

## Pagination

### Offset-based Pagination

```kotlin
@Serializable
data class PaginatedResponse<T>(
    val items: List<T>,
    val total: Int,
    val offset: Int,
    val limit: Int,
    val hasMore: Boolean
)

path("/products") {
    get {
        summary = "List products with pagination"
        
        parameter {
            name = "offset"
            `in` = ParameterLocation.QUERY
            description = "Number of items to skip"
            schema {
                type = SchemaType.INTEGER
                minimum = 0
                default = 0
            }
        }
        
        parameter {
            name = "limit"
            `in` = ParameterLocation.QUERY
            description = "Maximum items to return"
            schema {
                type = SchemaType.INTEGER
                minimum = 1
                maximum = 100
                default = 20
            }
        }
        
        response("200", "Paginated products") {
            jsonContent {
                schema {
                    type = SchemaType.OBJECT
                    properties {
                        "items" to schema {
                            type = SchemaType.ARRAY
                            items = schema {
                                ref = "#/components/schemas/Product"
                            }
                        }
                        "total" to schema {
                            type = SchemaType.INTEGER
                            description = "Total number of items"
                        }
                        "offset" to schema {
                            type = SchemaType.INTEGER
                        }
                        "limit" to schema {
                            type = SchemaType.INTEGER
                        }
                        "hasMore" to schema {
                            type = SchemaType.BOOLEAN
                        }
                    }
                }
            }
            
            headers {
                header("X-Total-Count") {
                    description = "Total number of items"
                    schema {
                        type = SchemaType.INTEGER
                    }
                }
            }
        }
    }
}
```

### Cursor-based Pagination

```kotlin
path("/feed") {
    get {
        summary = "Get feed with cursor pagination"
        
        parameter {
            name = "cursor"
            `in` = ParameterLocation.QUERY
            description = "Pagination cursor"
            schema {
                type = SchemaType.STRING
            }
        }
        
        parameter {
            name = "limit"
            `in` = ParameterLocation.QUERY
            schema {
                type = SchemaType.INTEGER
                default = 50
            }
        }
        
        response("200", "Feed items") {
            jsonContent {
                schema {
                    type = SchemaType.OBJECT
                    properties {
                        "items" to schema {
                            type = SchemaType.ARRAY
                            items = schema {
                                ref = "#/components/schemas/FeedItem"
                            }
                        }
                        "nextCursor" to schema {
                            type = SchemaType.STRING
                            nullable = true
                            description = "Cursor for next page (null if no more items)"
                        }
                        "prevCursor" to schema {
                            type = SchemaType.STRING
                            nullable = true
                        }
                    }
                }
            }
        }
    }
}
```

## Filtering and Sorting

```kotlin
path("/products") {
    get {
        summary = "List products with filtering and sorting"
        
        // Filtering parameters
        parameter {
            name = "category"
            `in` = ParameterLocation.QUERY
            description = "Filter by category"
            schema {
                type = SchemaType.ARRAY
                items = schema {
                    type = SchemaType.STRING
                }
            }
            style = "form"
            explode = true  // Allows ?category=electronics&category=books
        }
        
        parameter {
            name = "priceMin"
            `in` = ParameterLocation.QUERY
            schema {
                type = SchemaType.NUMBER
                minimum = 0
            }
        }
        
        parameter {
            name = "priceMax"
            `in` = ParameterLocation.QUERY
            schema {
                type = SchemaType.NUMBER
            }
        }
        
        parameter {
            name = "inStock"
            `in` = ParameterLocation.QUERY
            schema {
                type = SchemaType.BOOLEAN
            }
        }
        
        // Sorting parameters
        parameter {
            name = "sortBy"
            `in` = ParameterLocation.QUERY
            description = "Field to sort by"
            schema {
                type = SchemaType.STRING
                enum = listOf("name", "price", "createdAt", "popularity")
                default = "createdAt"
            }
        }
        
        parameter {
            name = "sortOrder"
            `in` = ParameterLocation.QUERY
            description = "Sort order"
            schema {
                type = SchemaType.STRING
                enum = listOf("asc", "desc")
                default = "desc"
            }
        }
        
        // Search parameter
        parameter {
            name = "search"
            `in` = ParameterLocation.QUERY
            description = "Full-text search query"
            schema {
                type = SchemaType.STRING
            }
        }
        
        response("200", "Filtered and sorted products") {
            jsonContent(listOf<Product>())
        }
    }
}
```

## File Upload/Download

### File Upload

```kotlin
path("/upload") {
    post {
        summary = "Upload file"
        
        requestBody("File to upload") {
            required = true
            
            // Single file upload
            content("multipart/form-data") {
                schema {
                    type = SchemaType.OBJECT
                    properties {
                        "file" to schema {
                            type = SchemaType.STRING
                            format = "binary"
                            description = "File to upload"
                        }
                        "description" to schema {
                            type = SchemaType.STRING
                            description = "File description"
                        }
                    }
                    required = listOf("file")
                }
            }
        }
        
        response("201", "File uploaded") {
            jsonContent {
                schema {
                    type = SchemaType.OBJECT
                    properties {
                        "fileId" to schema {
                            type = SchemaType.STRING
                        }
                        "filename" to schema {
                            type = SchemaType.STRING
                        }
                        "size" to schema {
                            type = SchemaType.INTEGER
                            description = "File size in bytes"
                        }
                        "mimeType" to schema {
                            type = SchemaType.STRING
                        }
                        "uploadedAt" to schema {
                            type = SchemaType.STRING
                            format = "date-time"
                        }
                    }
                }
            }
        }
    }
}

// Multiple file upload
path("/upload/multiple") {
    post {
        summary = "Upload multiple files"
        
        requestBody("Files to upload") {
            content("multipart/form-data") {
                schema {
                    type = SchemaType.OBJECT
                    properties {
                        "files" to schema {
                            type = SchemaType.ARRAY
                            items = schema {
                                type = SchemaType.STRING
                                format = "binary"
                            }
                            maxItems = 10
                        }
                    }
                }
            }
        }
        
        response("201", "Files uploaded") {
            jsonContent {
                schema {
                    type = SchemaType.ARRAY
                    items = schema {
                        ref = "#/components/schemas/FileInfo"
                    }
                }
            }
        }
    }
}
```

### File Download

```kotlin
path("/files/{fileId}") {
    get {
        summary = "Download file"
        
        parameter {
            name = "fileId"
            `in` = ParameterLocation.PATH
            required = true
            schema {
                type = SchemaType.STRING
            }
        }
        
        response("200", "File content") {
            content("application/octet-stream") {
                schema {
                    type = SchemaType.STRING
                    format = "binary"
                }
            }
            
            headers {
                header("Content-Disposition") {
                    description = "Attachment; filename=\"example.pdf\""
                    schema {
                        type = SchemaType.STRING
                    }
                }
                header("Content-Type") {
                    schema {
                        type = SchemaType.STRING
                    }
                }
                header("Content-Length") {
                    schema {
                        type = SchemaType.INTEGER
                    }
                }
            }
        }
        
        response("404", "File not found")
    }
}
```

## Complete Example: Todo API

Here's a complete example bringing together many of these concepts:

```kotlin
import me.farshad.openapi.*
import kotlinx.serialization.Serializable

@Serializable
enum class TodoStatus {
    PENDING, IN_PROGRESS, COMPLETED, CANCELLED
}

@Serializable
data class Todo(
    val id: String,
    val title: String,
    val description: String? = null,
    val status: TodoStatus = TodoStatus.PENDING,
    val createdAt: String,
    val updatedAt: String,
    val completedAt: String? = null,
    val tags: List<String> = emptyList()
)

@Serializable
data class CreateTodoRequest(
    val title: String,
    val description: String? = null,
    val tags: List<String> = emptyList()
)

@Serializable
data class UpdateTodoRequest(
    val title: String? = null,
    val description: String? = null,
    val status: TodoStatus? = null,
    val tags: List<String>? = null
)

val todoApi = openApi {
    openapi = "3.1.0"
    
    info {
        title = "Todo API"
        version = "1.0.0"
        description = "A simple todo list management API"
    }
    
    servers {
        server {
            url = "https://api.todos.com/v1"
            description = "Production server"
        }
    }
    
    tags {
        tag {
            name = "Todos"
            description = "Todo operations"
        }
    }
    
    components {
        schema(Todo::class)
        schema(CreateTodoRequest::class)
        schema(UpdateTodoRequest::class)
        schema(TodoStatus::class)
    }
    
    paths {
        path("/todos") {
            get {
                summary = "List todos"
                tags = listOf("Todos")
                
                // Filtering
                parameter {
                    name = "status"
                    `in` = ParameterLocation.QUERY
                    schema {
                        ref = "#/components/schemas/TodoStatus"
                    }
                }
                
                parameter {
                    name = "tags"
                    `in` = ParameterLocation.QUERY
                    description = "Filter by tags"
                    schema {
                        type = SchemaType.ARRAY
                        items = schema {
                            type = SchemaType.STRING
                        }
                    }
                    style = "form"
                    explode = true
                }
                
                // Pagination
                parameter {
                    name = "page"
                    `in` = ParameterLocation.QUERY
                    schema {
                        type = SchemaType.INTEGER
                        minimum = 1
                        default = 1
                    }
                }
                
                parameter {
                    name = "pageSize"
                    `in` = ParameterLocation.QUERY
                    schema {
                        type = SchemaType.INTEGER
                        minimum = 1
                        maximum = 100
                        default = 20
                    }
                }
                
                // Sorting
                parameter {
                    name = "sortBy"
                    `in` = ParameterLocation.QUERY
                    schema {
                        type = SchemaType.STRING
                        enum = listOf("createdAt", "updatedAt", "title", "status")
                        default = "createdAt"
                    }
                }
                
                parameter {
                    name = "sortOrder"
                    `in` = ParameterLocation.QUERY
                    schema {
                        type = SchemaType.STRING
                        enum = listOf("asc", "desc")
                        default = "desc"
                    }
                }
                
                response("200", "Todo list") {
                    jsonContent {
                        schema {
                            type = SchemaType.OBJECT
                            properties {
                                "items" to schema {
                                    type = SchemaType.ARRAY
                                    items = schema {
                                        ref = "#/components/schemas/Todo"
                                    }
                                }
                                "pagination" to schema {
                                    type = SchemaType.OBJECT
                                    properties {
                                        "page" to schema { type = SchemaType.INTEGER }
                                        "pageSize" to schema { type = SchemaType.INTEGER }
                                        "total" to schema { type = SchemaType.INTEGER }
                                        "totalPages" to schema { type = SchemaType.INTEGER }
                                    }
                                }
                            }
                        }
                    }
                }
            }
            
            post {
                summary = "Create todo"
                tags = listOf("Todos")
                
                requestBody("Todo to create") {
                    jsonContent(CreateTodoRequest::class)
                    required = true
                }
                
                response("201", "Todo created") {
                    jsonContent(Todo::class)
                    headers {
                        header("Location") {
                            description = "URL of created todo"
                            schema {
                                type = SchemaType.STRING
                            }
                        }
                    }
                }
                
                response("400", "Invalid request")
            }
        }
        
        path("/todos/{todoId}") {
            parameter {
                name = "todoId"
                `in` = ParameterLocation.PATH
                required = true
                schema {
                    type = SchemaType.STRING
                    format = "uuid"
                }
            }
            
            get {
                summary = "Get todo"
                tags = listOf("Todos")
                
                response("200", "Todo details") {
                    jsonContent(Todo::class)
                }
                response("404", "Todo not found")
            }
            
            patch {
                summary = "Update todo"
                tags = listOf("Todos")
                
                requestBody("Updates") {
                    jsonContent(UpdateTodoRequest::class)
                    required = true
                }
                
                response("200", "Todo updated") {
                    jsonContent(Todo::class)
                }
                response("404", "Todo not found")
            }
            
            delete {
                summary = "Delete todo"
                tags = listOf("Todos")
                
                response("204", "Todo deleted")
                response("404", "Todo not found")
            }
        }
    }
}
```

## Next Steps

- [Schema Definitions](schema-definitions.md) - Deep dive into schema creation and validation
- [API Operations](api-operations.md) - Advanced operation configurations
- [Security](security.md) - Implementing authentication and authorization