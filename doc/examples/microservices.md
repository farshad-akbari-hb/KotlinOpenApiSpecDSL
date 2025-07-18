# Microservices API Example

This example demonstrates a microservices architecture with multiple services sharing common schemas and patterns, including service discovery, distributed tracing, and event-driven communication.

## Architecture Overview

The example includes:
- **User Service**: Authentication and user management
- **Product Service**: Product catalog management
- **Order Service**: Order processing and management
- **Payment Service**: Payment processing
- **Notification Service**: Email/SMS notifications
- **API Gateway**: Unified entry point with routing

## Shared Components Library

```kotlin
// shared/CommonSchemas.kt
package com.example.api.shared

import me.farshad.openapi.*
import kotlinx.serialization.Serializable

// Common Enums
@Serializable
enum class Currency {
    USD, EUR, GBP, JPY
}

@Serializable
enum class Country {
    US, UK, DE, FR, JP
}

// Common Value Objects
@Serializable
data class Money(
    val amount: Double,
    val currency: Currency
)

@Serializable
data class Address(
    val street: String,
    val city: String,
    val state: String? = null,
    val postalCode: String,
    val country: Country
)

@Serializable
data class ContactInfo(
    val email: String,
    val phone: String? = null,
    val alternateEmail: String? = null
)

// Common DTOs
@Serializable
data class ErrorResponse(
    val error: ErrorDetails,
    val traceId: String,
    val timestamp: String,
    val service: String
)

@Serializable
data class ErrorDetails(
    val code: String,
    val message: String,
    val details: Map<String, Any>? = null
)

@Serializable
data class PageRequest(
    val page: Int = 1,
    val size: Int = 20,
    val sort: String? = null
)

@Serializable
data class PageResponse<T>(
    val content: List<T>,
    val page: Int,
    val size: Int,
    val totalElements: Long,
    val totalPages: Int,
    val first: Boolean,
    val last: Boolean
)

// Event Base Classes
@Serializable
abstract class DomainEvent {
    abstract val eventId: String
    abstract val eventType: String
    abstract val aggregateId: String
    abstract val timestamp: String
    abstract val version: Int
}

// Common Security
fun ComponentsBuilder.addCommonSecuritySchemes() {
    securityScheme("bearerAuth") {
        type = SecuritySchemeType.HTTP
        scheme = "bearer"
        bearerFormat = "JWT"
        description = "JWT token issued by User Service"
    }
    
    securityScheme("apiKey") {
        type = SecuritySchemeType.API_KEY
        name = "X-API-Key"
        `in` = ApiKeyLocation.HEADER
        description = "Service-to-service API key"
    }
    
    securityScheme("oauth2") {
        type = SecuritySchemeType.OAUTH2
        flows {
            clientCredentials {
                tokenUrl = "https://auth.example.com/oauth/token"
                scopes {
                    scope("service.read", "Read access for services")
                    scope("service.write", "Write access for services")
                    scope("service.admin", "Admin access for services")
                }
            }
        }
    }
}

// Common Parameters
fun ComponentsBuilder.addCommonParameters() {
    parameter("X-Request-ID") {
        name = "X-Request-ID"
        `in` = ParameterLocation.HEADER
        required = true
        description = "Unique request ID for distributed tracing"
        schema {
            type = SchemaType.STRING
            format = "uuid"
        }
    }
    
    parameter("X-Correlation-ID") {
        name = "X-Correlation-ID"
        `in` = ParameterLocation.HEADER
        description = "Correlation ID for tracking requests across services"
        schema {
            type = SchemaType.STRING
            format = "uuid"
        }
    }
    
    parameter("X-User-ID") {
        name = "X-User-ID"
        `in` = ParameterLocation.HEADER
        description = "User ID passed from API Gateway"
        schema {
            type = SchemaType.STRING
        }
    }
}

// Common Responses
fun ComponentsBuilder.addCommonResponses() {
    response("BadRequest") {
        description = "Bad request"
        jsonContent(ErrorResponse::class)
    }
    
    response("Unauthorized") {
        description = "Authentication required"
        jsonContent(ErrorResponse::class)
        headers {
            header("WWW-Authenticate") {
                schema { type = SchemaType.STRING }
            }
        }
    }
    
    response("Forbidden") {
        description = "Access forbidden"
        jsonContent(ErrorResponse::class)
    }
    
    response("NotFound") {
        description = "Resource not found"
        jsonContent(ErrorResponse::class)
    }
    
    response("Conflict") {
        description = "Resource conflict"
        jsonContent(ErrorResponse::class)
    }
    
    response("TooManyRequests") {
        description = "Rate limit exceeded"
        jsonContent(ErrorResponse::class)
        headers {
            header("Retry-After") {
                schema { type = SchemaType.INTEGER }
            }
        }
    }
    
    response("ServiceUnavailable") {
        description = "Service temporarily unavailable"
        jsonContent(ErrorResponse::class)
        headers {
            header("Retry-After") {
                schema { type = SchemaType.INTEGER }
            }
        }
    }
}
```

## User Service API

```kotlin
// services/UserServiceApi.kt
package com.example.api.services.user

import me.farshad.openapi.*
import com.example.api.shared.*
import kotlinx.serialization.Serializable

// Domain Models
@Serializable
enum class UserStatus {
    ACTIVE, INACTIVE, SUSPENDED, DELETED
}

@Serializable
data class User(
    val id: String,
    val username: String,
    val email: String,
    val profile: UserProfile,
    val status: UserStatus,
    val roles: List<String>,
    val createdAt: String,
    val updatedAt: String,
    val lastLoginAt: String? = null
)

@Serializable
data class UserProfile(
    val firstName: String,
    val lastName: String,
    val displayName: String? = null,
    val avatarUrl: String? = null,
    val dateOfBirth: String? = null,
    val address: Address? = null,
    val phoneNumber: String? = null
)

@Serializable
data class CreateUserRequest(
    val username: String,
    val email: String,
    val password: String,
    val profile: UserProfile,
    val roles: List<String> = listOf("user")
)

@Serializable
data class LoginRequest(
    val username: String,
    val password: String,
    val rememberMe: Boolean = false
)

@Serializable
data class LoginResponse(
    val accessToken: String,
    val refreshToken: String? = null,
    val tokenType: String = "Bearer",
    val expiresIn: Int,
    val user: User
)

// Events
@Serializable
data class UserCreatedEvent(
    override val eventId: String,
    override val eventType: String = "user.created",
    override val aggregateId: String,
    override val timestamp: String,
    override val version: Int = 1,
    val user: User
) : DomainEvent()

@Serializable
data class UserUpdatedEvent(
    override val eventId: String,
    override val eventType: String = "user.updated",
    override val aggregateId: String,
    override val timestamp: String,
    override val version: Int = 1,
    val userId: String,
    val changes: Map<String, Any>
) : DomainEvent()

// API Specification
fun createUserServiceApi() = openApi {
    openapi = "3.1.0"
    
    info {
        title = "User Service API"
        version = "1.0.0"
        description = "User authentication and management service"
    }
    
    servers {
        server {
            url = "https://user-service.example.com"
            description = "User service endpoint"
        }
    }
    
    components {
        addCommonSecuritySchemes()
        addCommonParameters()
        addCommonResponses()
        
        // User-specific schemas
        schema(User::class)
        schema(UserProfile::class)
        schema(UserStatus::class)
        schema(CreateUserRequest::class)
        schema(LoginRequest::class)
        schema(LoginResponse::class)
        schema(UserCreatedEvent::class)
        schema(UserUpdatedEvent::class)
    }
    
    paths {
        path("/auth/login") {
            post {
                summary = "User login"
                operationId = "login"
                tags = listOf("Authentication")
                
                parameter { ref = "#/components/parameters/X-Request-ID" }
                
                requestBody("Login credentials") {
                    required = true
                    jsonContent(LoginRequest::class)
                }
                
                response("200", "Login successful") {
                    jsonContent(LoginResponse::class)
                    
                    headers {
                        header("Set-Cookie") {
                            description = "Refresh token cookie"
                            schema { type = SchemaType.STRING }
                        }
                    }
                }
                
                response { ref = "#/components/responses/Unauthorized" }
                response { ref = "#/components/responses/TooManyRequests" }
            }
        }
        
        path("/auth/refresh") {
            post {
                summary = "Refresh access token"
                operationId = "refreshToken"
                tags = listOf("Authentication")
                
                parameter {
                    name = "refreshToken"
                    `in` = ParameterLocation.COOKIE
                    required = true
                    schema { type = SchemaType.STRING }
                }
                
                response("200", "Token refreshed") {
                    jsonContent {
                        schema {
                            type = SchemaType.OBJECT
                            properties {
                                "accessToken" to schema { type = SchemaType.STRING }
                                "expiresIn" to schema { type = SchemaType.INTEGER }
                            }
                        }
                    }
                }
            }
        }
        
        path("/users") {
            post {
                summary = "Create user"
                operationId = "createUser"
                tags = listOf("Users")
                
                parameter { ref = "#/components/parameters/X-Request-ID" }
                
                requestBody("User data") {
                    required = true
                    jsonContent(CreateUserRequest::class)
                }
                
                response("201", "User created") {
                    jsonContent(User::class)
                    
                    headers {
                        header("Location") {
                            schema { type = SchemaType.STRING }
                        }
                    }
                }
                
                response { ref = "#/components/responses/BadRequest" }
                response { ref = "#/components/responses/Conflict" }
                
                // Async event publishing
                callbacks {
                    callback("userCreated") {
                        expression("amqp://events/user.created") {
                            publish {
                                requestBody("User created event") {
                                    jsonContent(UserCreatedEvent::class)
                                }
                            }
                        }
                    }
                }
            }
            
            get {
                summary = "List users"
                operationId = "listUsers"
                tags = listOf("Users")
                
                security {
                    requirement("bearerAuth")
                    requirement("apiKey")
                }
                
                parameter { ref = "#/components/parameters/X-Request-ID" }
                parameter { ref = "#/components/parameters/X-User-ID" }
                
                parameter {
                    name = "status"
                    `in` = ParameterLocation.QUERY
                    schema {
                        ref = "#/components/schemas/UserStatus"
                    }
                }
                
                parameter {
                    name = "role"
                    `in` = ParameterLocation.QUERY
                    schema { type = SchemaType.STRING }
                }
                
                parameter {
                    name = "page"
                    `in` = ParameterLocation.QUERY
                    schema {
                        type = SchemaType.INTEGER
                        default = 1
                    }
                }
                
                parameter {
                    name = "size"
                    `in` = ParameterLocation.QUERY
                    schema {
                        type = SchemaType.INTEGER
                        default = 20
                    }
                }
                
                response("200", "User list") {
                    jsonContent {
                        schema {
                            ref = "#/components/schemas/PageResponseUser"
                        }
                    }
                }
            }
        }
        
        path("/users/{userId}") {
            parameter {
                name = "userId"
                `in` = ParameterLocation.PATH
                required = true
                schema { type = SchemaType.STRING }
            }
            
            get {
                summary = "Get user"
                operationId = "getUser"
                tags = listOf("Users")
                
                security {
                    requirement("bearerAuth")
                }
                
                parameter { ref = "#/components/parameters/X-Request-ID" }
                
                response("200", "User found") {
                    jsonContent(User::class)
                }
                
                response { ref = "#/components/responses/NotFound" }
            }
        }
        
        // Health check
        path("/health") {
            get {
                summary = "Health check"
                operationId = "healthCheck"
                tags = listOf("Health")
                
                response("200", "Service healthy") {
                    jsonContent {
                        schema {
                            type = SchemaType.OBJECT
                            properties {
                                "status" to schema { 
                                    type = SchemaType.STRING 
                                    const = "UP"
                                }
                                "service" to schema { 
                                    type = SchemaType.STRING 
                                    const = "user-service"
                                }
                                "version" to schema { type = SchemaType.STRING }
                                "timestamp" to schema { 
                                    type = SchemaType.STRING 
                                    format = "date-time"
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
```

## Order Service API

```kotlin
// services/OrderServiceApi.kt
package com.example.api.services.order

import me.farshad.openapi.*
import com.example.api.shared.*
import kotlinx.serialization.Serializable

// Domain Models
@Serializable
enum class OrderStatus {
    PENDING, CONFIRMED, PROCESSING, SHIPPED, DELIVERED, CANCELLED, REFUNDED
}

@Serializable
data class Order(
    val id: String,
    val userId: String,
    val items: List<OrderItem>,
    val shippingAddress: Address,
    val billingAddress: Address,
    val subtotal: Money,
    val tax: Money,
    val shipping: Money,
    val total: Money,
    val status: OrderStatus,
    val paymentId: String? = null,
    val trackingNumber: String? = null,
    val notes: String? = null,
    val createdAt: String,
    val updatedAt: String
)

@Serializable
data class OrderItem(
    val productId: String,
    val productName: String,
    val quantity: Int,
    val unitPrice: Money,
    val totalPrice: Money,
    val metadata: Map<String, Any> = emptyMap()
)

@Serializable
data class CreateOrderRequest(
    val userId: String,
    val items: List<OrderItemRequest>,
    val shippingAddress: Address,
    val billingAddress: Address? = null,
    val notes: String? = null
)

@Serializable
data class OrderItemRequest(
    val productId: String,
    val quantity: Int,
    val metadata: Map<String, Any> = emptyMap()
)

// Events
@Serializable
data class OrderCreatedEvent(
    override val eventId: String,
    override val eventType: String = "order.created",
    override val aggregateId: String,
    override val timestamp: String,
    override val version: Int = 1,
    val order: Order
) : DomainEvent()

@Serializable
data class OrderStatusChangedEvent(
    override val eventId: String,
    override val eventType: String = "order.status_changed",
    override val aggregateId: String,
    override val timestamp: String,
    override val version: Int = 1,
    val orderId: String,
    val previousStatus: OrderStatus,
    val newStatus: OrderStatus,
    val reason: String? = null
) : DomainEvent()

// API Specification
fun createOrderServiceApi() = openApi {
    openapi = "3.1.0"
    
    info {
        title = "Order Service API"
        version = "1.0.0"
        description = "Order management and processing service"
    }
    
    servers {
        server {
            url = "https://order-service.example.com"
            description = "Order service endpoint"
        }
    }
    
    components {
        addCommonSecuritySchemes()
        addCommonParameters()
        addCommonResponses()
        
        // Order-specific schemas
        schema(Order::class)
        schema(OrderItem::class)
        schema(OrderStatus::class)
        schema(CreateOrderRequest::class)
        schema(OrderItemRequest::class)
        schema(OrderCreatedEvent::class)
        schema(OrderStatusChangedEvent::class)
        
        // Additional schemas
        schema(Money::class)
        schema(Address::class)
    }
    
    // Default security
    security {
        requirement("bearerAuth")
    }
    
    paths {
        path("/orders") {
            post {
                summary = "Create order"
                operationId = "createOrder"
                tags = listOf("Orders")
                
                parameter { ref = "#/components/parameters/X-Request-ID" }
                parameter { ref = "#/components/parameters/X-User-ID" }
                parameter { ref = "#/components/parameters/X-Correlation-ID" }
                
                requestBody("Order data") {
                    required = true
                    jsonContent(CreateOrderRequest::class)
                }
                
                response("201", "Order created") {
                    jsonContent(Order::class)
                    
                    headers {
                        header("Location") {
                            schema { type = SchemaType.STRING }
                        }
                    }
                }
                
                response { ref = "#/components/responses/BadRequest" }
                response("422", "Invalid order items") {
                    jsonContent(ErrorResponse::class)
                }
                
                // Service calls
                callbacks {
                    // Call Product Service to validate items
                    callback("validateProducts") {
                        expression("https://product-service.example.com/products/validate") {
                            post {
                                requestBody("Product IDs") {
                                    jsonContent {
                                        schema {
                                            type = SchemaType.OBJECT
                                            properties {
                                                "productIds" to schema {
                                                    type = SchemaType.ARRAY
                                                    items = schema { type = SchemaType.STRING }
                                                }
                                            }
                                        }
                                    }
                                }
                                response("200", "Products valid")
                            }
                        }
                    }
                    
                    // Publish event
                    callback("orderCreated") {
                        expression("amqp://events/order.created") {
                            publish {
                                requestBody("Order created event") {
                                    jsonContent(OrderCreatedEvent::class)
                                }
                            }
                        }
                    }
                }
            }
            
            get {
                summary = "List orders"
                operationId = "listOrders"
                tags = listOf("Orders")
                
                parameter { ref = "#/components/parameters/X-Request-ID" }
                parameter { ref = "#/components/parameters/X-User-ID" }
                
                parameter {
                    name = "status"
                    `in` = ParameterLocation.QUERY
                    schema {
                        type = SchemaType.ARRAY
                        items = schema {
                            ref = "#/components/schemas/OrderStatus"
                        }
                    }
                    style = "form"
                    explode = true
                }
                
                parameter {
                    name = "userId"
                    `in` = ParameterLocation.QUERY
                    description = "Filter by user (admin only)"
                    schema { type = SchemaType.STRING }
                }
                
                parameter {
                    name = "createdAfter"
                    `in` = ParameterLocation.QUERY
                    schema {
                        type = SchemaType.STRING
                        format = "date-time"
                    }
                }
                
                parameter {
                    name = "page"
                    `in` = ParameterLocation.QUERY
                    schema {
                        type = SchemaType.INTEGER
                        default = 1
                    }
                }
                
                parameter {
                    name = "size"
                    `in` = ParameterLocation.QUERY
                    schema {
                        type = SchemaType.INTEGER
                        default = 20
                    }
                }
                
                response("200", "Order list") {
                    jsonContent {
                        schema {
                            ref = "#/components/schemas/PageResponseOrder"
                        }
                    }
                }
            }
        }
        
        path("/orders/{orderId}") {
            parameter {
                name = "orderId"
                `in` = ParameterLocation.PATH
                required = true
                schema { type = SchemaType.STRING }
            }
            
            get {
                summary = "Get order"
                operationId = "getOrder"
                tags = listOf("Orders")
                
                parameter { ref = "#/components/parameters/X-Request-ID" }
                
                response("200", "Order found") {
                    jsonContent(Order::class)
                }
                
                response { ref = "#/components/responses/NotFound" }
            }
            
            patch {
                summary = "Update order status"
                operationId = "updateOrderStatus"
                tags = listOf("Orders")
                
                parameter { ref = "#/components/parameters/X-Request-ID" }
                parameter { ref = "#/components/parameters/X-User-ID" }
                
                requestBody("Status update") {
                    required = true
                    jsonContent {
                        schema {
                            type = SchemaType.OBJECT
                            required = listOf("status")
                            properties {
                                "status" to schema {
                                    ref = "#/components/schemas/OrderStatus"
                                }
                                "reason" to schema {
                                    type = SchemaType.STRING
                                    description = "Reason for status change"
                                }
                                "trackingNumber" to schema {
                                    type = SchemaType.STRING
                                    description = "Tracking number for shipped orders"
                                }
                            }
                        }
                    }
                }
                
                response("200", "Order updated") {
                    jsonContent(Order::class)
                }
                
                response { ref = "#/components/responses/BadRequest" }
                response("409", "Invalid status transition") {
                    jsonContent(ErrorResponse::class)
                }
                
                callbacks {
                    callback("statusChanged") {
                        expression("amqp://events/order.status_changed") {
                            publish {
                                requestBody("Status changed event") {
                                    jsonContent(OrderStatusChangedEvent::class)
                                }
                            }
                        }
                    }
                }
            }
        }
        
        // Inter-service endpoints
        path("/internal/orders/user/{userId}") {
            get {
                summary = "Get orders by user (internal)"
                operationId = "getOrdersByUser"
                tags = listOf("Internal")
                
                security {
                    requirement("apiKey")
                }
                
                parameter {
                    name = "userId"
                    `in` = ParameterLocation.PATH
                    required = true
                    schema { type = SchemaType.STRING }
                }
                
                parameter { ref = "#/components/parameters/X-Request-ID" }
                parameter { ref = "#/components/parameters/X-Correlation-ID" }
                
                response("200", "User orders") {
                    jsonContent(listOf<Order>())
                }
            }
        }
    }
}
```

## API Gateway

```kotlin
// gateway/ApiGatewaySpec.kt
package com.example.api.gateway

import me.farshad.openapi.*
import com.example.api.shared.*

fun createApiGatewaySpec() = openApi {
    openapi = "3.1.0"
    
    info {
        title = "E-Commerce API Gateway"
        version = "1.0.0"
        description = """
            Unified API gateway for all microservices.
            
            This gateway handles:
            - Authentication and authorization
            - Request routing
            - Rate limiting
            - Response aggregation
            - Circuit breaking
            - Distributed tracing
        """.trimIndent()
    }
    
    servers {
        server {
            url = "https://api.example.com"
            description = "Production API gateway"
        }
        
        server {
            url = "https://staging-api.example.com"
            description = "Staging API gateway"
        }
    }
    
    // Extensions for gateway configuration
    extensions {
        "x-gateway-config" to mapOf(
            "rateLimit" to mapOf(
                "default" to 1000,
                "authenticated" to 5000,
                "window" to "1h"
            ),
            "timeout" to mapOf(
                "default" to 30,
                "long-running" to 120
            ),
            "retry" to mapOf(
                "attempts" to 3,
                "backoff" to "exponential"
            )
        )
    }
    
    components {
        addCommonSecuritySchemes()
        
        // Gateway-specific responses
        response("GatewayTimeout") {
            description = "Gateway timeout"
            jsonContent {
                schema {
                    type = SchemaType.OBJECT
                    properties {
                        "error" to schema {
                            type = SchemaType.OBJECT
                            properties {
                                "code" to schema {
                                    type = SchemaType.STRING
                                    const = "GATEWAY_TIMEOUT"
                                }
                                "message" to schema {
                                    type = SchemaType.STRING
                                    default = "The upstream service did not respond in time"
                                }
                                "service" to schema {
                                    type = SchemaType.STRING
                                    description = "The service that timed out"
                                }
                            }
                        }
                        "traceId" to schema { type = SchemaType.STRING }
                    }
                }
            }
        }
    }
    
    // Gateway routes
    paths {
        // Authentication endpoints (proxied to User Service)
        path("/auth/login") {
            post {
                summary = "User login"
                operationId = "login"
                tags = listOf("Authentication")
                
                extensions {
                    "x-gateway-backend" to mapOf(
                        "service" to "user-service",
                        "path" to "/auth/login",
                        "timeout" to 10
                    )
                }
                
                requestBody("Login credentials") {
                    jsonContent {
                        schema {
                            type = SchemaType.OBJECT
                            required = listOf("username", "password")
                            properties {
                                "username" to schema { type = SchemaType.STRING }
                                "password" to schema { 
                                    type = SchemaType.STRING 
                                    format = "password"
                                }
                            }
                        }
                    }
                }
                
                response("200", "Login successful") {
                    jsonContent {
                        schema {
                            type = SchemaType.OBJECT
                            properties {
                                "accessToken" to schema { type = SchemaType.STRING }
                                "refreshToken" to schema { type = SchemaType.STRING }
                                "expiresIn" to schema { type = SchemaType.INTEGER }
                            }
                        }
                    }
                }
            }
        }
        
        // Aggregated endpoints
        path("/dashboard") {
            get {
                summary = "Get user dashboard"
                description = "Aggregates data from multiple services"
                operationId = "getDashboard"
                tags = listOf("Dashboard")
                
                security {
                    requirement("bearerAuth")
                }
                
                extensions {
                    "x-gateway-aggregate" to listOf(
                        mapOf(
                            "service" to "user-service",
                            "path" to "/users/{userId}",
                            "field" to "user"
                        ),
                        mapOf(
                            "service" to "order-service",
                            "path" to "/orders?userId={userId}&limit=5",
                            "field" to "recentOrders"
                        ),
                        mapOf(
                            "service" to "product-service",
                            "path" to "/products/recommendations?userId={userId}",
                            "field" to "recommendations"
                        )
                    )
                }
                
                response("200", "Dashboard data") {
                    jsonContent {
                        schema {
                            type = SchemaType.OBJECT
                            properties {
                                "user" to schema {
                                    ref = "#/components/schemas/User"
                                }
                                "recentOrders" to schema {
                                    type = SchemaType.ARRAY
                                    items = schema {
                                        ref = "#/components/schemas/Order"
                                    }
                                }
                                "recommendations" to schema {
                                    type = SchemaType.ARRAY
                                    items = schema {
                                        ref = "#/components/schemas/Product"
                                    }
                                }
                                "notifications" to schema {
                                    type = SchemaType.ARRAY
                                    items = schema {
                                        type = SchemaType.OBJECT
                                        properties {
                                            "id" to schema { type = SchemaType.STRING }
                                            "message" to schema { type = SchemaType.STRING }
                                            "type" to schema { type = SchemaType.STRING }
                                            "read" to schema { type = SchemaType.BOOLEAN }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                
                response("504", "Gateway timeout") {
                    ref = "#/components/responses/GatewayTimeout"
                }
            }
        }
        
        // Circuit breaker example
        path("/orders") {
            post {
                summary = "Create order with saga"
                description = "Creates order using distributed saga pattern"
                operationId = "createOrderSaga"
                tags = listOf("Orders")
                
                security {
                    requirement("bearerAuth")
                }
                
                extensions {
                    "x-gateway-saga" to mapOf(
                        "steps" to listOf(
                            mapOf(
                                "name" to "validate-products",
                                "service" to "product-service",
                                "path" to "/products/validate",
                                "compensate" to null
                            ),
                            mapOf(
                                "name" to "reserve-inventory",
                                "service" to "inventory-service",
                                "path" to "/inventory/reserve",
                                "compensate" to "/inventory/release"
                            ),
                            mapOf(
                                "name" to "create-order",
                                "service" to "order-service",
                                "path" to "/orders",
                                "compensate" to "/orders/{orderId}/cancel"
                            ),
                            mapOf(
                                "name" to "process-payment",
                                "service" to "payment-service",
                                "path" to "/payments",
                                "compensate" to "/payments/{paymentId}/refund"
                            )
                        ),
                        "timeout" to 60
                    )
                }
                
                requestBody("Order request") {
                    jsonContent {
                        ref = "#/components/schemas/CreateOrderRequest"
                    }
                }
                
                response("201", "Order created") {
                    jsonContent {
                        ref = "#/components/schemas/Order"
                    }
                }
                
                response("503", "Service unavailable") {
                    description = "Circuit breaker open"
                    jsonContent {
                        schema {
                            type = SchemaType.OBJECT
                            properties {
                                "error" to schema {
                                    type = SchemaType.OBJECT
                                    properties {
                                        "code" to schema {
                                            type = SchemaType.STRING
                                            const = "CIRCUIT_BREAKER_OPEN"
                                        }
                                        "message" to schema { type = SchemaType.STRING }
                                        "service" to schema { type = SchemaType.STRING }
                                        "retryAfter" to schema { type = SchemaType.INTEGER }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
```

## Service Communication Patterns

### Event Bus Schema

```kotlin
// events/EventSchemas.kt
package com.example.api.events

import me.farshad.openapi.*
import kotlinx.serialization.Serializable

@Serializable
data class EventEnvelope<T>(
    val eventId: String,
    val eventType: String,
    val source: String,
    val timestamp: String,
    val correlationId: String? = null,
    val causationId: String? = null,
    val data: T,
    val metadata: Map<String, String> = emptyMap()
)

// Define event schemas for AsyncAPI
fun createEventCatalog() = mapOf(
    "user.created" to UserCreatedEvent::class,
    "user.updated" to UserUpdatedEvent::class,
    "order.created" to OrderCreatedEvent::class,
    "order.status_changed" to OrderStatusChangedEvent::class,
    "payment.processed" to PaymentProcessedEvent::class,
    "inventory.reserved" to InventoryReservedEvent::class,
    "notification.sent" to NotificationSentEvent::class
)
```

### Service Discovery Integration

```kotlin
// discovery/ServiceRegistry.kt
package com.example.api.discovery

@Serializable
data class ServiceInstance(
    val serviceId: String,
    val instanceId: String,
    val host: String,
    val port: Int,
    val secure: Boolean = true,
    val metadata: Map<String, String> = emptyMap(),
    val healthCheckUrl: String,
    val status: ServiceStatus,
    val registeredAt: String,
    val lastHeartbeat: String
)

@Serializable
enum class ServiceStatus {
    UP, DOWN, STARTING, OUT_OF_SERVICE, UNKNOWN
}
```

## Deployment Configuration

```yaml
# docker-compose.yml
version: '3.8'

services:
  api-gateway:
    image: api-gateway:latest
    ports:
      - "8080:8080"
    environment:
      - CONSUL_HOST=consul
      - JAEGER_AGENT_HOST=jaeger
    depends_on:
      - consul
      - jaeger

  user-service:
    image: user-service:latest
    environment:
      - DB_HOST=postgres
      - AMQP_HOST=rabbitmq
      - CONSUL_HOST=consul
    depends_on:
      - postgres
      - rabbitmq
      - consul

  order-service:
    image: order-service:latest
    environment:
      - DB_HOST=postgres
      - AMQP_HOST=rabbitmq
      - CONSUL_HOST=consul
    depends_on:
      - postgres
      - rabbitmq
      - consul

  # Infrastructure services
  consul:
    image: consul:latest
    ports:
      - "8500:8500"

  jaeger:
    image: jaegertracing/all-in-one:latest
    ports:
      - "16686:16686"
      - "14268:14268"

  rabbitmq:
    image: rabbitmq:3-management
    ports:
      - "5672:5672"
      - "15672:15672"

  postgres:
    image: postgres:15
    environment:
      - POSTGRES_DB=microservices
      - POSTGRES_USER=admin
      - POSTGRES_PASSWORD=secret
```

## Testing Strategy

```kotlin
// Integration test example
class OrderSagaIntegrationTest {
    @Test
    fun `should create order successfully`() {
        // Given
        val orderRequest = CreateOrderRequest(
            userId = "user123",
            items = listOf(
                OrderItemRequest("product1", 2),
                OrderItemRequest("product2", 1)
            ),
            shippingAddress = testAddress()
        )
        
        // When
        val response = apiGateway.post("/orders") {
            bearerAuth(userToken)
            jsonBody(orderRequest)
        }
        
        // Then
        assertThat(response.status).isEqualTo(201)
        assertThat(response.body<Order>()).satisfies { order ->
            assertThat(order.status).isEqualTo(OrderStatus.CONFIRMED)
            assertThat(order.items).hasSize(2)
        }
        
        // Verify events published
        verifyEventPublished("order.created")
        verifyEventPublished("inventory.reserved")
        verifyEventPublished("payment.processed")
    }
    
    @Test
    fun `should rollback on payment failure`() {
        // Test saga compensation...
    }
}
```

## Key Features Demonstrated

### 1. **Microservices Architecture**
- Service separation by domain
- Shared component library
- Inter-service communication
- Service discovery integration

### 2. **API Gateway Patterns**
- Request routing
- Response aggregation
- Circuit breaking
- Rate limiting
- Authentication forwarding

### 3. **Event-Driven Communication**
- Domain events
- Event sourcing patterns
- Asynchronous messaging
- Saga pattern for distributed transactions

### 4. **Distributed Tracing**
- Correlation IDs
- Request tracing headers
- Service mesh integration

### 5. **Security**
- Service-to-service authentication
- User authentication at gateway
- API key for internal services
- OAuth2 for external clients

### 6. **Resilience Patterns**
- Circuit breakers
- Retries with backoff
- Timeouts
- Bulkheads
- Health checks

## Best Practices

1. **API Design**
   - Use consistent schemas across services
   - Version APIs independently
   - Document service dependencies
   - Use correlation IDs for tracing

2. **Security**
   - Authenticate at the gateway
   - Use service mesh for internal communication
   - Rotate API keys regularly
   - Implement rate limiting per service

3. **Monitoring**
   - Implement health checks
   - Use distributed tracing
   - Monitor circuit breaker states
   - Track service dependencies

4. **Testing**
   - Contract testing between services
   - Integration tests with test containers
   - Chaos engineering for resilience
   - Performance testing for each service

## Next Steps

- Implement GraphQL federation at gateway
- Add gRPC for internal communication
- Implement CQRS with event sourcing
- Add service mesh (Istio/Linkerd)
- Implement API versioning strategy