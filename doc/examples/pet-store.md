# Pet Store API Example

This is a complete example of a Pet Store API specification using the Kotlin OpenAPI Spec DSL, demonstrating various features and best practices.

## Complete API Specification

```kotlin
import me.farshad.openapi.*
import kotlinx.serialization.Serializable

// Data Models
@Serializable
enum class PetStatus {
    available, pending, sold
}

@Serializable
enum class OrderStatus {
    placed, approved, delivered
}

@Serializable
data class Category(
    val id: Long,
    val name: String
)

@Serializable
data class Tag(
    val id: Long,
    val name: String
)

@Serializable
data class Pet(
    val id: Long,
    val name: String,
    val category: Category? = null,
    val photoUrls: List<String> = emptyList(),
    val tags: List<Tag> = emptyList(),
    val status: PetStatus = PetStatus.available
)

@Serializable
data class User(
    val id: Long,
    val username: String,
    val firstName: String? = null,
    val lastName: String? = null,
    val email: String,
    val password: String,
    val phone: String? = null,
    val userStatus: Int = 0
)

@Serializable
data class Order(
    val id: Long,
    val petId: Long,
    val quantity: Int = 1,
    val shipDate: String? = null,
    val status: OrderStatus = OrderStatus.placed,
    val complete: Boolean = false
)

@Serializable
data class ApiResponse(
    val code: Int,
    val type: String,
    val message: String
)

// API Specification
fun createPetStoreApi() = openApi {
    openapi = "3.1.0"
    
    info {
        title = "Pet Store API"
        version = "1.0.0"
        description = """
            This is a sample Pet Store API that demonstrates the capabilities
            of the Kotlin OpenAPI Spec DSL. You can find out more about the
            project at [https://github.com/example/kotlin-openapi-dsl](https://github.com/example/kotlin-openapi-dsl).
        """.trimIndent()
        
        termsOfService = "https://petstore.example.com/terms"
        
        contact {
            name = "Pet Store API Team"
            email = "api@petstore.example.com"
            url = "https://petstore.example.com/support"
        }
        
        license {
            name = "Apache 2.0"
            url = "https://www.apache.org/licenses/LICENSE-2.0.html"
        }
    }
    
    servers {
        server {
            url = "https://petstore.example.com/api/v1"
            description = "Production server"
        }
        
        server {
            url = "https://staging.petstore.example.com/api/v1"
            description = "Staging server"
        }
        
        server {
            url = "http://localhost:8080/api/v1"
            description = "Development server"
        }
    }
    
    tags {
        tag {
            name = "pet"
            description = "Everything about your Pets"
            externalDocs {
                description = "Find out more"
                url = "https://petstore.example.com/docs/pets"
            }
        }
        
        tag {
            name = "store"
            description = "Access to Pet Store orders"
        }
        
        tag {
            name = "user"
            description = "Operations about users"
        }
    }
    
    // Security schemes
    components {
        securityScheme("api_key") {
            type = SecuritySchemeType.API_KEY
            name = "api_key"
            `in` = ApiKeyLocation.HEADER
            description = "API key for general access"
        }
        
        securityScheme("petstore_auth") {
            type = SecuritySchemeType.OAUTH2
            description = "OAuth2 authentication"
            flows {
                implicit {
                    authorizationUrl = "https://petstore.example.com/oauth/authorize"
                    scopes {
                        scope("write:pets", "modify pets in your account")
                        scope("read:pets", "read your pets")
                        scope("write:orders", "modify orders")
                        scope("read:orders", "read orders")
                    }
                }
                
                authorizationCode {
                    authorizationUrl = "https://petstore.example.com/oauth/authorize"
                    tokenUrl = "https://petstore.example.com/oauth/token"
                    scopes {
                        scope("write:pets", "modify pets in your account")
                        scope("read:pets", "read your pets")
                        scope("write:orders", "modify orders")
                        scope("read:orders", "read orders")
                    }
                }
            }
        }
        
        // Reusable schemas
        schema(Pet::class)
        schema(Category::class)
        schema(Tag::class)
        schema(User::class)
        schema(Order::class)
        schema(ApiResponse::class)
        schema(PetStatus::class)
        schema(OrderStatus::class)
        
        // Reusable parameters
        parameter("limitParam") {
            name = "limit"
            `in` = ParameterLocation.QUERY
            description = "Maximum number of items to return"
            schema {
                type = SchemaType.INTEGER
                minimum = 1
                maximum = 100
                default = 20
            }
        }
        
        parameter("offsetParam") {
            name = "offset"
            `in` = ParameterLocation.QUERY
            description = "Number of items to skip"
            schema {
                type = SchemaType.INTEGER
                minimum = 0
                default = 0
            }
        }
        
        // Reusable responses
        response("NotFound") {
            description = "Resource not found"
            jsonContent {
                schema {
                    ref = "#/components/schemas/ApiResponse"
                }
                example = mapOf(
                    "code" to 404,
                    "type" to "error",
                    "message" to "Resource not found"
                )
            }
        }
        
        response("Unauthorized") {
            description = "Unauthorized access"
            jsonContent {
                schema {
                    ref = "#/components/schemas/ApiResponse"
                }
            }
        }
    }
    
    // Pet endpoints
    paths {
        path("/pets") {
            post {
                summary = "Add a new pet to the store"
                description = "Add a new pet to the store"
                operationId = "addPet"
                tags = listOf("pet")
                
                requestBody("Pet object to add") {
                    required = true
                    jsonContent(Pet::class)
                    
                    content("application/xml") {
                        schema {
                            ref = "#/components/schemas/Pet"
                        }
                    }
                }
                
                response("201", "Pet created successfully") {
                    jsonContent(Pet::class)
                    headers {
                        header("Location") {
                            description = "URL of the created pet"
                            schema {
                                type = SchemaType.STRING
                                format = "uri"
                            }
                        }
                    }
                }
                
                response("400", "Invalid input") {
                    jsonContent(ApiResponse::class)
                }
                
                security {
                    requirement("petstore_auth") {
                        scopes = listOf("write:pets")
                    }
                }
            }
            
            get {
                summary = "List pets"
                description = "Returns a list of pets based on query parameters"
                operationId = "findPets"
                tags = listOf("pet")
                
                parameter {
                    name = "status"
                    `in` = ParameterLocation.QUERY
                    description = "Status values to filter by"
                    schema {
                        type = SchemaType.ARRAY
                        items = schema {
                            ref = "#/components/schemas/PetStatus"
                        }
                    }
                    style = "form"
                    explode = true
                }
                
                parameter {
                    name = "tags"
                    `in` = ParameterLocation.QUERY
                    description = "Tags to filter by"
                    schema {
                        type = SchemaType.ARRAY
                        items = schema {
                            type = SchemaType.STRING
                        }
                    }
                    style = "form"
                    explode = true
                }
                
                parameter {
                    ref = "#/components/parameters/limitParam"
                }
                
                parameter {
                    ref = "#/components/parameters/offsetParam"
                }
                
                response("200", "Successful operation") {
                    jsonContent(listOf<Pet>())
                    
                    headers {
                        header("X-Total-Count") {
                            description = "Total number of pets"
                            schema {
                                type = SchemaType.INTEGER
                            }
                        }
                        header("X-Next-Page") {
                            description = "URL of next page"
                            schema {
                                type = SchemaType.STRING
                                format = "uri"
                            }
                        }
                    }
                }
                
                response("400", "Invalid status value") {
                    jsonContent(ApiResponse::class)
                }
                
                security {
                    requirement("petstore_auth") {
                        scopes = listOf("read:pets")
                    }
                }
            }
        }
        
        path("/pets/{petId}") {
            parameter {
                name = "petId"
                `in` = ParameterLocation.PATH
                required = true
                description = "ID of pet to return"
                schema {
                    type = SchemaType.INTEGER
                    format = "int64"
                }
            }
            
            get {
                summary = "Find pet by ID"
                description = "Returns a single pet"
                operationId = "getPetById"
                tags = listOf("pet")
                
                response("200", "Successful operation") {
                    jsonContent(Pet::class)
                    
                    content("application/xml") {
                        schema {
                            ref = "#/components/schemas/Pet"
                        }
                    }
                }
                
                response {
                    ref = "#/components/responses/NotFound"
                }
                
                security {
                    requirement("api_key")
                    requirement("petstore_auth") {
                        scopes = listOf("read:pets")
                    }
                }
            }
            
            put {
                summary = "Update an existing pet"
                description = "Update an existing pet by ID"
                operationId = "updatePet"
                tags = listOf("pet")
                
                requestBody("Pet object to update") {
                    required = true
                    jsonContent(Pet::class)
                }
                
                response("200", "Pet updated successfully") {
                    jsonContent(Pet::class)
                }
                
                response("400", "Invalid ID supplied")
                response("404", "Pet not found")
                response("422", "Validation exception")
                
                security {
                    requirement("petstore_auth") {
                        scopes = listOf("write:pets")
                    }
                }
            }
            
            delete {
                summary = "Delete a pet"
                description = "Delete a pet"
                operationId = "deletePet"
                tags = listOf("pet")
                
                parameter {
                    name = "api_key"
                    `in` = ParameterLocation.HEADER
                    description = "API key for authorization"
                    schema {
                        type = SchemaType.STRING
                    }
                }
                
                response("204", "Pet deleted successfully")
                response("400", "Invalid ID supplied")
                response("404", "Pet not found")
                
                security {
                    requirement("petstore_auth") {
                        scopes = listOf("write:pets")
                    }
                }
            }
        }
        
        path("/pets/{petId}/uploadImage") {
            post {
                summary = "Upload pet image"
                description = "Upload an image for a pet"
                operationId = "uploadFile"
                tags = listOf("pet")
                
                parameter {
                    name = "petId"
                    `in` = ParameterLocation.PATH
                    required = true
                    schema {
                        type = SchemaType.INTEGER
                        format = "int64"
                    }
                }
                
                parameter {
                    name = "additionalMetadata"
                    `in` = ParameterLocation.QUERY
                    description = "Additional metadata about the image"
                    schema {
                        type = SchemaType.STRING
                    }
                }
                
                requestBody("Image file") {
                    content("multipart/form-data") {
                        schema {
                            type = SchemaType.OBJECT
                            properties {
                                "file" to schema {
                                    type = SchemaType.STRING
                                    format = "binary"
                                }
                            }
                        }
                    }
                }
                
                response("200", "Successful operation") {
                    jsonContent(ApiResponse::class)
                }
                
                security {
                    requirement("petstore_auth") {
                        scopes = listOf("write:pets")
                    }
                }
            }
        }
        
        // Store endpoints
        path("/store/inventory") {
            get {
                summary = "Get store inventory"
                description = "Returns a map of status codes to quantities"
                operationId = "getInventory"
                tags = listOf("store")
                
                response("200", "Successful operation") {
                    jsonContent {
                        schema {
                            type = SchemaType.OBJECT
                            additionalProperties = schema {
                                type = SchemaType.INTEGER
                                format = "int32"
                            }
                        }
                        
                        example = mapOf(
                            "available" to 100,
                            "pending" to 50,
                            "sold" to 150
                        )
                    }
                }
                
                security {
                    requirement("api_key")
                }
            }
        }
        
        path("/store/order") {
            post {
                summary = "Place an order for a pet"
                description = "Place a new order in the store"
                operationId = "placeOrder"
                tags = listOf("store")
                
                requestBody("Order object") {
                    required = true
                    jsonContent(Order::class)
                }
                
                response("201", "Order placed successfully") {
                    jsonContent(Order::class)
                    headers {
                        header("Location") {
                            schema {
                                type = SchemaType.STRING
                                format = "uri"
                            }
                        }
                    }
                }
                
                response("400", "Invalid order")
                
                security {
                    requirement("api_key")
                }
            }
        }
        
        path("/store/order/{orderId}") {
            parameter {
                name = "orderId"
                `in` = ParameterLocation.PATH
                required = true
                description = "ID of order to fetch"
                schema {
                    type = SchemaType.INTEGER
                    format = "int64"
                    minimum = 1
                }
            }
            
            get {
                summary = "Find purchase order by ID"
                description = "Get a single order by ID"
                operationId = "getOrderById"
                tags = listOf("store")
                
                response("200", "Successful operation") {
                    jsonContent(Order::class)
                }
                
                response("400", "Invalid ID supplied")
                response("404", "Order not found")
            }
            
            delete {
                summary = "Delete purchase order by ID"
                description = "Delete an order"
                operationId = "deleteOrder"
                tags = listOf("store")
                
                response("204", "Order deleted")
                response("400", "Invalid ID supplied")
                response("404", "Order not found")
            }
        }
        
        // User endpoints
        path("/users") {
            post {
                summary = "Create user"
                description = "Create a new user account"
                operationId = "createUser"
                tags = listOf("user")
                
                requestBody("User object") {
                    required = true
                    jsonContent(User::class)
                }
                
                response("201", "User created") {
                    jsonContent(User::class)
                }
                
                response("400", "Invalid user data")
                response("409", "User already exists")
            }
            
            get {
                summary = "List users"
                description = "Get a list of users (admin only)"
                operationId = "listUsers"
                tags = listOf("user")
                
                parameter {
                    ref = "#/components/parameters/limitParam"
                }
                
                parameter {
                    ref = "#/components/parameters/offsetParam"
                }
                
                response("200", "Successful operation") {
                    jsonContent(listOf<User>())
                }
                
                security {
                    requirement("petstore_auth") {
                        scopes = listOf("read:pets")  // Admin scope
                    }
                }
            }
        }
        
        path("/users/login") {
            post {
                summary = "Log user into the system"
                description = "Log in with username and password"
                operationId = "loginUser"
                tags = listOf("user")
                
                requestBody("Login credentials") {
                    jsonContent {
                        schema {
                            type = SchemaType.OBJECT
                            required = listOf("username", "password")
                            properties {
                                "username" to schema {
                                    type = SchemaType.STRING
                                }
                                "password" to schema {
                                    type = SchemaType.STRING
                                    format = "password"
                                }
                            }
                        }
                    }
                }
                
                response("200", "Successful operation") {
                    jsonContent {
                        schema {
                            type = SchemaType.OBJECT
                            properties {
                                "token" to schema {
                                    type = SchemaType.STRING
                                    description = "Authentication token"
                                }
                                "expiresIn" to schema {
                                    type = SchemaType.INTEGER
                                    description = "Token expiration time in seconds"
                                }
                            }
                        }
                    }
                    
                    headers {
                        header("X-Rate-Limit") {
                            description = "Calls per hour allowed"
                            schema {
                                type = SchemaType.INTEGER
                            }
                        }
                        
                        header("X-Expires-After") {
                            description = "Token expiration time"
                            schema {
                                type = SchemaType.STRING
                                format = "date-time"
                            }
                        }
                    }
                }
                
                response("400", "Invalid username/password")
            }
        }
        
        path("/users/logout") {
            post {
                summary = "Log out current user"
                description = "Log out the current logged in user session"
                operationId = "logoutUser"
                tags = listOf("user")
                
                response("200", "Successful operation")
                
                security {
                    requirement("api_key")
                }
            }
        }
        
        path("/users/{username}") {
            parameter {
                name = "username"
                `in` = ParameterLocation.PATH
                required = true
                description = "Username to fetch"
                schema {
                    type = SchemaType.STRING
                }
            }
            
            get {
                summary = "Get user by username"
                description = "Get user by username"
                operationId = "getUserByName"
                tags = listOf("user")
                
                response("200", "Successful operation") {
                    jsonContent(User::class)
                }
                
                response("400", "Invalid username supplied")
                response("404", "User not found")
                
                security {
                    requirement("api_key")
                    requirement("petstore_auth") {
                        scopes = listOf("read:pets")
                    }
                }
            }
            
            put {
                summary = "Update user"
                description = "Update user information"
                operationId = "updateUser"
                tags = listOf("user")
                
                requestBody("Updated user object") {
                    required = true
                    jsonContent(User::class)
                }
                
                response("200", "User updated") {
                    jsonContent(User::class)
                }
                
                response("400", "Invalid user supplied")
                response("404", "User not found")
                
                security {
                    requirement("api_key")
                    requirement("petstore_auth") {
                        scopes = listOf("write:pets")
                    }
                }
            }
            
            delete {
                summary = "Delete user"
                description = "Delete user account"
                operationId = "deleteUser"
                tags = listOf("user")
                
                response("204", "User deleted")
                response("400", "Invalid username supplied")
                response("404", "User not found")
                
                security {
                    requirement("api_key")
                    requirement("petstore_auth") {
                        scopes = listOf("write:pets")
                    }
                }
            }
        }
    }
}

// Generate the specification
fun main() {
    val spec = createPetStoreApi()
    
    // Save as JSON
    spec.toJsonFile("petstore-api.json")
    
    // Save as YAML
    spec.toYamlFile("petstore-api.yaml")
    
    println("Pet Store API specification generated!")
}
```

## Key Features Demonstrated

### 1. **Complete CRUD Operations**
- Create, Read, Update, Delete for Pets, Orders, and Users
- Proper HTTP methods and status codes
- RESTful URL patterns

### 2. **Authentication & Authorization**
- API Key authentication
- OAuth2 with multiple flows (implicit and authorization code)
- Scoped permissions (read:pets, write:pets, etc.)
- Multiple security schemes per endpoint

### 3. **Request/Response Handling**
- Multiple content types (JSON, XML)
- File uploads with multipart/form-data
- Comprehensive error responses
- Response headers for metadata

### 4. **Reusable Components**
- Shared schemas for common data models
- Reusable parameters (limit, offset)
- Common error responses
- Security scheme definitions

### 5. **API Documentation**
- Detailed descriptions for all operations
- External documentation links
- Tags for logical grouping
- Contact and license information

### 6. **Advanced Features**
- Query parameter arrays with different styles
- Path parameter validation
- Optional vs required parameters
- Default values for parameters

## Usage Example

```kotlin
// Client usage example (pseudo-code)
val petStoreClient = PetStoreApiClient(
    baseUrl = "https://petstore.example.com/api/v1",
    apiKey = "your-api-key"
)

// Create a new pet
val newPet = Pet(
    id = 1,
    name = "Fluffy",
    category = Category(1, "Dogs"),
    photoUrls = listOf("https://example.com/fluffy.jpg"),
    tags = listOf(Tag(1, "friendly"), Tag(2, "trained")),
    status = PetStatus.available
)

val createdPet = petStoreClient.addPet(newPet)

// Search for available pets
val availablePets = petStoreClient.findPets(
    status = listOf(PetStatus.available),
    limit = 10
)

// Place an order
val order = Order(
    id = 1,
    petId = createdPet.id,
    quantity = 1,
    status = OrderStatus.placed
)

val placedOrder = petStoreClient.placeOrder(order)
```

## Testing the API

The generated specification can be:
1. Imported into Postman or Insomnia for testing
2. Used with Swagger UI for interactive documentation
3. Used to generate client SDKs in various languages
4. Validated using OpenAPI validators

## Next Steps

- Customize the security schemes for your needs
- Add webhook support for order notifications
- Implement pagination headers consistently
- Add rate limiting information
- Extend with custom vendor extensions