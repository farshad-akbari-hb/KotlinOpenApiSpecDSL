# Advanced Features

This guide covers advanced OpenAPI 3.1 features including webhooks, callbacks, links, and extensions.

## Table of Contents
- [Webhooks](#webhooks)
- [Callbacks](#callbacks)
- [Links](#links)
- [Extensions](#extensions)
- [External Documentation](#external-documentation)
- [Tags and Groups](#tags-and-groups)
- [Server Variables](#server-variables)
- [Runtime Expressions](#runtime-expressions)
- [Complete Examples](#complete-examples)

## Webhooks

Webhooks are HTTP callbacks that your API can send to notify clients of events. OpenAPI 3.1 introduced first-class support for documenting webhooks.

### Basic Webhook Definition

```kotlin
openApi {
    openapi = "3.1.0"
    
    // Regular API paths
    paths {
        path("/subscriptions") {
            post {
                summary = "Create webhook subscription"
                requestBody("Subscription details") {
                    jsonContent {
                        schema {
                            type = SchemaType.OBJECT
                            required = listOf("url", "events")
                            properties {
                                "url" to schema {
                                    type = SchemaType.STRING
                                    format = "uri"
                                    description = "URL to receive webhook calls"
                                }
                                "events" to schema {
                                    type = SchemaType.ARRAY
                                    items = schema {
                                        type = SchemaType.STRING
                                        enum = listOf(
                                            "order.created",
                                            "order.updated",
                                            "order.cancelled",
                                            "payment.completed",
                                            "payment.failed"
                                        )
                                    }
                                }
                                "secret" to schema {
                                    type = SchemaType.STRING
                                    description = "Shared secret for webhook verification"
                                }
                            }
                        }
                    }
                }
                response("201", "Subscription created")
            }
        }
    }
    
    // Webhook definitions
    webhooks {
        webhook("orderCreated") {
            post {
                summary = "Order created event"
                description = """
                    Sent when a new order is created.
                    
                    The webhook will be retried up to 3 times with exponential backoff
                    if your endpoint doesn't respond with 2xx status.
                """.trimIndent()
                
                requestBody("Order created payload") {
                    required = true
                    jsonContent {
                        schema {
                            type = SchemaType.OBJECT
                            properties {
                                "event" to schema {
                                    type = SchemaType.STRING
                                    const = "order.created"
                                }
                                "timestamp" to schema {
                                    type = SchemaType.STRING
                                    format = "date-time"
                                }
                                "data" to schema {
                                    ref = "#/components/schemas/Order"
                                }
                            }
                        }
                    }
                    
                    // Webhook-specific headers
                    headers {
                        header("X-Webhook-Signature") {
                            description = "HMAC-SHA256 signature of the payload"
                            required = true
                            schema {
                                type = SchemaType.STRING
                            }
                        }
                        header("X-Webhook-ID") {
                            description = "Unique webhook delivery ID"
                            schema {
                                type = SchemaType.STRING
                                format = "uuid"
                            }
                        }
                        header("X-Webhook-Timestamp") {
                            description = "Unix timestamp of the webhook"
                            schema {
                                type = SchemaType.INTEGER
                            }
                        }
                    }
                }
                
                // Expected responses from webhook endpoint
                response("200", "Webhook processed successfully")
                response("202", "Webhook accepted for processing")
                
                // Error responses trigger retry
                response("500", "Server error - webhook will be retried")
                response("503", "Service unavailable - webhook will be retried")
            }
        }
        
        webhook("paymentStatusChanged") {
            post {
                summary = "Payment status change"
                description = "Sent when payment status changes"
                
                requestBody("Payment status payload") {
                    jsonContent {
                        schema {
                            type = SchemaType.OBJECT
                            properties {
                                "event" to schema {
                                    type = SchemaType.STRING
                                    enum = listOf("payment.completed", "payment.failed")
                                }
                                "timestamp" to schema {
                                    type = SchemaType.STRING
                                    format = "date-time"
                                }
                                "data" to schema {
                                    type = SchemaType.OBJECT
                                    properties {
                                        "paymentId" to schema { type = SchemaType.STRING }
                                        "orderId" to schema { type = SchemaType.STRING }
                                        "status" to schema {
                                            type = SchemaType.STRING
                                            enum = listOf("completed", "failed")
                                        }
                                        "amount" to schema {
                                            ref = "#/components/schemas/Money"
                                        }
                                        "failureReason" to schema {
                                            type = SchemaType.STRING
                                            nullable = true
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                
                response("200", "Webhook received")
            }
        }
    }
}
```

### Webhook Security

```kotlin
// Define webhook with security verification
webhook("secureWebhook") {
    post {
        summary = "Secure webhook with signature verification"
        description = """
            This webhook includes a signature for verification.
            
            To verify the webhook:
            1. Get the raw request body
            2. Get the signature from X-Webhook-Signature header
            3. Compute HMAC-SHA256 of body using your webhook secret
            4. Compare computed signature with header signature
            
            Example (pseudo-code):
            ```
            signature = request.headers['X-Webhook-Signature']
            expected = hmac_sha256(webhook_secret, request.body)
            if (!secure_compare(signature, expected)) {
                return 401
            }
            ```
        """.trimIndent()
        
        parameters {
            parameter {
                name = "X-Webhook-Signature"
                `in` = ParameterLocation.HEADER
                required = true
                description = "HMAC-SHA256 signature"
                schema {
                    type = SchemaType.STRING
                    pattern = "^sha256=[a-f0-9]{64}$"
                }
            }
            
            parameter {
                name = "X-Webhook-Timestamp"
                `in` = ParameterLocation.HEADER
                required = true
                description = "Unix timestamp (prevent replay attacks)"
                schema {
                    type = SchemaType.INTEGER
                }
            }
        }
        
        requestBody("Webhook payload") {
            jsonContent {
                // Payload schema
            }
        }
    }
}
```

## Callbacks

Callbacks allow you to define asynchronous, out-of-band requests that your API will make to client-provided URLs.

### Basic Callback Example

```kotlin
paths {
    path("/async-jobs") {
        post {
            summary = "Create async job"
            description = "Creates a long-running job and calls back when complete"
            
            requestBody("Job request") {
                jsonContent {
                    schema {
                        type = SchemaType.OBJECT
                        required = listOf("callbackUrl", "jobData")
                        properties {
                            "callbackUrl" to schema {
                                type = SchemaType.STRING
                                format = "uri"
                                description = "URL to call when job completes"
                            }
                            "jobData" to schema {
                                type = SchemaType.OBJECT
                                description = "Job-specific data"
                            }
                        }
                    }
                }
            }
            
            response("202", "Job accepted") {
                jsonContent {
                    schema {
                        type = SchemaType.OBJECT
                        properties {
                            "jobId" to schema {
                                type = SchemaType.STRING
                                format = "uuid"
                            }
                            "status" to schema {
                                type = SchemaType.STRING
                                const = "pending"
                            }
                            "estimatedCompletionTime" to schema {
                                type = SchemaType.STRING
                                format = "date-time"
                            }
                        }
                    }
                }
            }
            
            // Define callbacks
            callbacks {
                callback("jobCompletion") {
                    // Expression to derive callback URL
                    expression("\$request.body#/callbackUrl") {
                        post {
                            summary = "Job completion notification"
                            
                            requestBody("Job result") {
                                jsonContent {
                                    schema {
                                        type = SchemaType.OBJECT
                                        properties {
                                            "jobId" to schema {
                                                type = SchemaType.STRING
                                                format = "uuid"
                                            }
                                            "status" to schema {
                                                type = SchemaType.STRING
                                                enum = listOf("completed", "failed")
                                            }
                                            "result" to schema {
                                                description = "Job result data"
                                            }
                                            "error" to schema {
                                                type = SchemaType.STRING
                                                nullable = true
                                            }
                                            "completedAt" to schema {
                                                type = SchemaType.STRING
                                                format = "date-time"
                                            }
                                        }
                                    }
                                }
                            }
                            
                            response("200", "Callback received")
                            response("410", "Callback URL no longer valid")
                        }
                    }
                }
                
                callback("progressUpdate") {
                    expression("\$request.body#/callbackUrl") {
                        post {
                            summary = "Job progress update"
                            
                            requestBody("Progress update") {
                                jsonContent {
                                    schema {
                                        type = SchemaType.OBJECT
                                        properties {
                                            "jobId" to schema { type = SchemaType.STRING }
                                            "progress" to schema {
                                                type = SchemaType.INTEGER
                                                minimum = 0
                                                maximum = 100
                                            }
                                            "message" to schema { type = SchemaType.STRING }
                                        }
                                    }
                                }
                            }
                            
                            response("200", "Update received")
                        }
                    }
                }
            }
        }
    }
}
```

### Complex Callback Example

```kotlin
// Payment processing with multiple callbacks
path("/payments") {
    post {
        summary = "Process payment"
        
        requestBody("Payment request") {
            jsonContent {
                schema {
                    type = SchemaType.OBJECT
                    required = listOf("amount", "currency", "callbacks")
                    properties {
                        "amount" to schema { type = SchemaType.NUMBER }
                        "currency" to schema { type = SchemaType.STRING }
                        "callbacks" to schema {
                            type = SchemaType.OBJECT
                            properties {
                                "onSuccess" to schema {
                                    type = SchemaType.STRING
                                    format = "uri"
                                }
                                "onFailure" to schema {
                                    type = SchemaType.STRING
                                    format = "uri"
                                }
                                "on3dsRequired" to schema {
                                    type = SchemaType.STRING
                                    format = "uri"
                                }
                            }
                        }
                    }
                }
            }
        }
        
        callbacks {
            callback("paymentSuccess") {
                expression("\$request.body#/callbacks/onSuccess") {
                    post {
                        requestBody("Success notification") {
                            jsonContent {
                                schema {
                                    type = SchemaType.OBJECT
                                    properties {
                                        "paymentId" to schema { type = SchemaType.STRING }
                                        "status" to schema { 
                                            type = SchemaType.STRING 
                                            const = "success"
                                        }
                                        "transactionId" to schema { type = SchemaType.STRING }
                                    }
                                }
                            }
                        }
                        response("200", "OK")
                    }
                }
            }
            
            callback("paymentFailure") {
                expression("\$request.body#/callbacks/onFailure") {
                    post {
                        requestBody("Failure notification") {
                            jsonContent {
                                schema {
                                    type = SchemaType.OBJECT
                                    properties {
                                        "paymentId" to schema { type = SchemaType.STRING }
                                        "status" to schema { 
                                            type = SchemaType.STRING 
                                            const = "failed"
                                        }
                                        "reason" to schema { type = SchemaType.STRING }
                                        "code" to schema { type = SchemaType.STRING }
                                    }
                                }
                            }
                        }
                        response("200", "OK")
                    }
                }
            }
            
            callback("3dsChallenge") {
                expression("\$request.body#/callbacks/on3dsRequired") {
                    post {
                        requestBody("3DS challenge required") {
                            jsonContent {
                                schema {
                                    type = SchemaType.OBJECT
                                    properties {
                                        "paymentId" to schema { type = SchemaType.STRING }
                                        "challengeUrl" to schema { 
                                            type = SchemaType.STRING 
                                            format = "uri"
                                        }
                                        "returnUrl" to schema { 
                                            type = SchemaType.STRING 
                                            format = "uri"
                                        }
                                    }
                                }
                            }
                        }
                        response("200", "Challenge URL received")
                    }
                }
            }
        }
    }
}
```

## Links

Links provide a way to describe relationships between operations and how response values can be used as input to other operations.

### Basic Links

```kotlin
paths {
    path("/users") {
        post {
            operationId = "createUser"
            summary = "Create a user"
            
            response("201", "User created") {
                jsonContent(User::class)
                
                links {
                    link("GetUserByUserId") {
                        operationId = "getUserById"
                        parameters = mapOf(
                            "userId" to "\$response.body#/id"
                        )
                        description = "The `id` value returned can be used as `userId` parameter"
                    }
                    
                    link("GetUserPosts") {
                        operationId = "getUserPosts" 
                        parameters = mapOf(
                            "userId" to "\$response.body#/id"
                        )
                    }
                    
                    link("UpdateUser") {
                        operationId = "updateUser"
                        parameters = mapOf(
                            "userId" to "\$response.body#/id"
                        )
                        requestBody = "\$response.body"
                    }
                }
            }
        }
    }
    
    path("/users/{userId}") {
        get {
            operationId = "getUserById"
            parameters {
                parameter {
                    name = "userId"
                    `in` = ParameterLocation.PATH
                    required = true
                    schema { type = SchemaType.STRING }
                }
            }
        }
        
        put {
            operationId = "updateUser"
            parameters {
                parameter {
                    name = "userId"
                    `in` = ParameterLocation.PATH
                    required = true
                    schema { type = SchemaType.STRING }
                }
            }
        }
    }
    
    path("/users/{userId}/posts") {
        get {
            operationId = "getUserPosts"
            // ...
        }
    }
}
```

### Advanced Links with Runtime Expressions

```kotlin
response("200", "Order created") {
    jsonContent {
        schema {
            type = SchemaType.OBJECT
            properties {
                "orderId" to schema { type = SchemaType.STRING }
                "customerId" to schema { type = SchemaType.STRING }
                "items" to schema {
                    type = SchemaType.ARRAY
                    items = schema {
                        type = SchemaType.OBJECT
                        properties {
                            "productId" to schema { type = SchemaType.STRING }
                            "quantity" to schema { type = SchemaType.INTEGER }
                        }
                    }
                }
                "paymentId" to schema { type = SchemaType.STRING }
            }
        }
    }
    
    links {
        // Link using operation ID
        link("getOrderStatus") {
            operationId = "getOrderById"
            parameters = mapOf(
                "orderId" to "\$response.body#/orderId"
            )
        }
        
        // Link using operation reference
        link("getCustomer") {
            operationRef = "#/paths/~1customers~1{customerId}/get"
            parameters = mapOf(
                "customerId" to "\$response.body#/customerId"
            )
        }
        
        // Link with complex expression
        link("getFirstProduct") {
            operationId = "getProductById"
            parameters = mapOf(
                "productId" to "\$response.body#/items/0/productId"
            )
        }
        
        // Link with request body from response
        link("updateOrderItems") {
            operationId = "updateOrderItems"
            parameters = mapOf(
                "orderId" to "\$response.body#/orderId"
            )
            requestBody = "\$response.body#/items"
        }
        
        // Link with server override
        link("trackShipment") {
            operationId = "getShipmentTracking"
            parameters = mapOf(
                "orderId" to "\$response.body#/orderId"
            )
            server {
                url = "https://shipping.example.com"
            }
        }
    }
}
```

## Extensions

Extensions (x- properties) allow you to add custom metadata to your OpenAPI specification.

### Common Extension Patterns

```kotlin
openApi {
    openapi = "3.1.0"
    
    // Root-level extensions
    extensions {
        "x-api-id" to "user-service"
        "x-audience" to "external"
        "x-maturity" to "stable"
    }
    
    info {
        title = "User API"
        version = "1.0.0"
        
        // Info extensions
        extensions {
            "x-logo" to mapOf(
                "url" to "https://example.com/logo.png",
                "altText" to "Company Logo"
            )
            "x-api-category" to "Core Services"
        }
    }
    
    paths {
        path("/users") {
            // Path-level extensions
            extensions {
                "x-middleware" to listOf("auth", "rateLimit", "logging")
                "x-stability-level" to "stable"
            }
            
            get {
                summary = "List users"
                
                // Operation extensions
                extensions {
                    "x-auth-required" to true
                    "x-rate-limit" to mapOf(
                        "requests" to 100,
                        "window" to "1h"
                    )
                    "x-cache" to mapOf(
                        "ttl" to 300,
                        "vary" to listOf("Authorization", "Accept-Language")
                    )
                    "x-code-samples" to listOf(
                        mapOf(
                            "lang" to "curl",
                            "label" to "cURL",
                            "source" to """
                                curl -X GET https://api.example.com/users \
                                  -H 'Authorization: Bearer YOUR_TOKEN' \
                                  -H 'Accept: application/json'
                            """.trimIndent()
                        ),
                        mapOf(
                            "lang" to "javascript",
                            "label" to "JavaScript",
                            "source" to """
                                const response = await fetch('https://api.example.com/users', {
                                  headers: {
                                    'Authorization': 'Bearer YOUR_TOKEN',
                                    'Accept': 'application/json'
                                  }
                                });
                                const users = await response.json();
                            """.trimIndent()
                        )
                    )
                }
            }
        }
    }
    
    components {
        schema("User") {
            type = SchemaType.OBJECT
            
            // Schema extensions
            extensions {
                "x-database-table" to "users"
                "x-immutable-fields" to listOf("id", "createdAt")
                "x-searchable-fields" to listOf("email", "username", "name")
                "x-indices" to listOf(
                    mapOf(
                        "fields" to listOf("email"),
                        "unique" to true
                    ),
                    mapOf(
                        "fields" to listOf("username"),
                        "unique" to true
                    )
                )
            }
        }
    }
}
```

### Vendor-Specific Extensions

```kotlin
// AWS API Gateway extensions
paths {
    path("/pets") {
        get {
            extensions {
                "x-amazon-apigateway-integration" to mapOf(
                    "type" to "AWS_PROXY",
                    "httpMethod" to "POST",
                    "uri" to "arn:aws:lambda:us-east-1:123456789:function:getPets",
                    "responses" to mapOf(
                        "default" to mapOf(
                            "statusCode" to "200"
                        )
                    )
                )
            }
        }
    }
}

// Redoc extensions
openApi {
    info {
        extensions {
            "x-logo" to mapOf(
                "url" to "https://example.com/logo.png",
                "backgroundColor" to "#FFFFFF"
            )
        }
    }
    
    tags {
        tag {
            name = "Users"
            description = "User operations"
            extensions {
                "x-displayName" to "User Management"
                "x-traitTag" to true
            }
        }
    }
}

// Internal tooling extensions
components {
    schema("Order") {
        extensions {
            "x-generated-from" to "OrderEntity.kt"
            "x-last-modified" to "2023-10-20T15:30:00Z"
            "x-owner-team" to "orders-team"
            "x-sla" to mapOf(
                "response-time" to "200ms",
                "availability" to "99.9%"
            )
        }
    }
}
```

## External Documentation

```kotlin
openApi {
    // Global external docs
    externalDocs {
        description = "Find more info here"
        url = "https://docs.example.com"
    }
    
    tags {
        tag {
            name = "Users"
            description = "User management"
            
            // Tag-specific docs
            externalDocs {
                description = "User API Guide"
                url = "https://docs.example.com/users"
            }
        }
    }
    
    paths {
        path("/complex-operation") {
            post {
                summary = "Complex operation"
                
                // Operation-specific docs
                externalDocs {
                    description = "Detailed guide for this operation"
                    url = "https://docs.example.com/guides/complex-operation"
                }
            }
        }
    }
    
    components {
        schema("ComplexSchema") {
            type = SchemaType.OBJECT
            
            // Schema docs
            externalDocs {
                description = "Schema documentation"
                url = "https://docs.example.com/schemas/complex"
            }
        }
    }
}
```

## Tags and Groups

### Advanced Tag Organization

```kotlin
openApi {
    // Define tag hierarchy and metadata
    tags {
        // Main category
        tag {
            name = "User Management"
            description = "Operations related to user management"
            extensions {
                "x-tag-group" to "Core APIs"
                "x-order" to 1
            }
        }
        
        // Sub-categories
        tag {
            name = "User Profile"
            description = "User profile operations"
            extensions {
                "x-parent-tag" to "User Management"
                "x-order" to 1
            }
        }
        
        tag {
            name = "User Authentication"
            description = "Authentication operations"
            extensions {
                "x-parent-tag" to "User Management"
                "x-order" to 2
            }
        }
        
        // Feature tags
        tag {
            name = "Beta"
            description = "Beta features"
            extensions {
                "x-tag-type" to "maturity"
                "x-badge" to mapOf(
                    "color" to "orange",
                    "text" to "BETA"
                )
            }
        }
        
        tag {
            name = "Deprecated"
            description = "Deprecated operations"
            extensions {
                "x-tag-type" to "lifecycle"
                "x-badge" to mapOf(
                    "color" to "red",
                    "text" to "DEPRECATED"
                )
            }
        }
    }
    
    paths {
        path("/users/profile") {
            get {
                tags = listOf("User Profile", "Stable")
                // ...
            }
            
            put {
                tags = listOf("User Profile", "Beta")
                // ...
            }
        }
    }
}
```

## Server Variables

```kotlin
openApi {
    servers {
        server {
            url = "https://{environment}.api.example.com/{version}"
            description = "API server"
            
            variables {
                variable("environment") {
                    default = "prod"
                    enum = listOf("prod", "staging", "dev")
                    description = "Server environment"
                }
                
                variable("version") {
                    default = "v1"
                    enum = listOf("v1", "v2", "v3")
                    description = "API version"
                }
            }
        }
        
        server {
            url = "{protocol}://{host}:{port}/api"
            description = "Custom server"
            
            variables {
                variable("protocol") {
                    default = "https"
                    enum = listOf("http", "https")
                }
                
                variable("host") {
                    default = "localhost"
                    description = "Server hostname"
                }
                
                variable("port") {
                    default = "443"
                    enum = listOf("443", "8443", "80", "8080")
                }
            }
        }
    }
}
```

## Runtime Expressions

Runtime expressions allow you to reference values from requests and responses dynamically.

### Expression Reference

```kotlin
// Available expressions:
// $url - The full URL of the request
// $method - The HTTP method
// $request.header.{name} - Request header value
// $request.query.{name} - Query parameter value
// $request.path.{name} - Path parameter value
// $request.body - Request body
// $request.body#{json-pointer} - Specific value from request body
// $response.header.{name} - Response header value
// $response.body - Response body
// $response.body#{json-pointer} - Specific value from response body

// Example usage in callbacks
callbacks {
    callback("statusUpdate") {
        // URL comes from request header
        expression("\$request.header.X-Callback-URL") {
            post {
                // ...
            }
        }
    }
    
    callback("dynamicCallback") {
        // URL constructed from multiple sources
        expression("{$request.body#/callbackBase}/webhook/{$request.path.userId}") {
            post {
                // ...
            }
        }
    }
}

// Example usage in links
links {
    link("getRelated") {
        operationId = "getResource"
        parameters = mapOf(
            // From response body
            "resourceId" to "\$response.body#/data/id",
            // From response header
            "version" to "\$response.header.X-API-Version",
            // Complex JSON pointer
            "firstTag" to "\$response.body#/data/tags/0"
        )
    }
}
```

## Complete Examples

### Event-Driven API with Webhooks

```kotlin
val eventDrivenApi = openApi {
    openapi = "3.1.0"
    info {
        title = "Event-Driven Order API"
        version = "1.0.0"
    }
    
    paths {
        path("/webhooks/subscribe") {
            post {
                summary = "Subscribe to events"
                requestBody("Subscription") {
                    jsonContent {
                        schema {
                            type = SchemaType.OBJECT
                            required = listOf("url", "events", "secret")
                            properties {
                                "url" to schema { 
                                    type = SchemaType.STRING 
                                    format = "uri"
                                }
                                "events" to schema {
                                    type = SchemaType.ARRAY
                                    items = schema {
                                        type = SchemaType.STRING
                                        enum = listOf(
                                            "order.*",
                                            "order.created",
                                            "order.updated",
                                            "order.shipped",
                                            "order.delivered",
                                            "order.cancelled"
                                        )
                                    }
                                }
                                "secret" to schema {
                                    type = SchemaType.STRING
                                    minLength = 32
                                }
                                "filters" to schema {
                                    type = SchemaType.OBJECT
                                    properties {
                                        "customerId" to schema { type = SchemaType.STRING }
                                        "minAmount" to schema { type = SchemaType.NUMBER }
                                        "regions" to schema {
                                            type = SchemaType.ARRAY
                                            items = schema { type = SchemaType.STRING }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                response("201", "Subscription created") {
                    jsonContent {
                        schema {
                            type = SchemaType.OBJECT
                            properties {
                                "subscriptionId" to schema { type = SchemaType.STRING }
                                "status" to schema { 
                                    type = SchemaType.STRING 
                                    const = "active"
                                }
                            }
                        }
                    }
                }
            }
        }
    }
    
    webhooks {
        webhook("orderEvent") {
            post {
                summary = "Order event notification"
                
                parameters {
                    parameter {
                        name = "X-Event-Type"
                        `in` = ParameterLocation.HEADER
                        required = true
                        schema {
                            type = SchemaType.STRING
                            enum = listOf(
                                "order.created",
                                "order.updated",
                                "order.shipped",
                                "order.delivered",
                                "order.cancelled"
                            )
                        }
                    }
                    
                    parameter {
                        name = "X-Event-ID"
                        `in` = ParameterLocation.HEADER
                        required = true
                        schema {
                            type = SchemaType.STRING
                            format = "uuid"
                        }
                    }
                    
                    parameter {
                        name = "X-Signature"
                        `in` = ParameterLocation.HEADER
                        required = true
                        schema {
                            type = SchemaType.STRING
                        }
                    }
                }
                
                requestBody("Event payload") {
                    jsonContent {
                        schema {
                            oneOf = listOf(
                                SchemaReference.ReferenceTo("#/components/schemas/OrderCreatedEvent"),
                                SchemaReference.ReferenceTo("#/components/schemas/OrderShippedEvent"),
                                SchemaReference.ReferenceTo("#/components/schemas/OrderDeliveredEvent")
                            )
                            discriminator {
                                propertyName = "eventType"
                            }
                        }
                    }
                }
                
                response("200", "Event processed")
                response("202", "Event queued for processing")
                response("410", "Subscription no longer active")
            }
        }
    }
}
```

### Async API with Callbacks

```kotlin
val asyncApi = openApi {
    openapi = "3.1.0"
    info {
        title = "Async Processing API"
        version = "1.0.0"
    }
    
    paths {
        path("/video/transcode") {
            post {
                summary = "Transcode video"
                description = "Submit video for transcoding with progress callbacks"
                
                requestBody("Transcode request") {
                    jsonContent {
                        schema {
                            type = SchemaType.OBJECT
                            required = listOf("videoUrl", "outputFormats", "callbacks")
                            properties {
                                "videoUrl" to schema {
                                    type = SchemaType.STRING
                                    format = "uri"
                                }
                                "outputFormats" to schema {
                                    type = SchemaType.ARRAY
                                    items = schema {
                                        type = SchemaType.STRING
                                        enum = listOf("mp4", "webm", "hls")
                                    }
                                }
                                "quality" to schema {
                                    type = SchemaType.STRING
                                    enum = listOf("low", "medium", "high", "4k")
                                    default = "high"
                                }
                                "callbacks" to schema {
                                    type = SchemaType.OBJECT
                                    required = listOf("onComplete")
                                    properties {
                                        "onStart" to schema {
                                            type = SchemaType.STRING
                                            format = "uri"
                                        }
                                        "onProgress" to schema {
                                            type = SchemaType.STRING
                                            format = "uri"
                                        }
                                        "onComplete" to schema {
                                            type = SchemaType.STRING
                                            format = "uri"
                                        }
                                        "onError" to schema {
                                            type = SchemaType.STRING
                                            format = "uri"
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                
                response("202", "Transcoding job accepted") {
                    jsonContent {
                        schema {
                            type = SchemaType.OBJECT
                            properties {
                                "jobId" to schema { type = SchemaType.STRING }
                                "status" to schema { 
                                    type = SchemaType.STRING 
                                    const = "queued"
                                }
                                "estimatedDuration" to schema {
                                    type = SchemaType.INTEGER
                                    description = "Estimated processing time in seconds"
                                }
                            }
                        }
                    }
                    
                    links {
                        link("getJobStatus") {
                            operationId = "getTranscodeJob"
                            parameters = mapOf(
                                "jobId" to "\$response.body#/jobId"
                            )
                        }
                        
                        link("cancelJob") {
                            operationId = "cancelTranscodeJob"
                            parameters = mapOf(
                                "jobId" to "\$response.body#/jobId"
                            )
                        }
                    }
                }
                
                callbacks {
                    callback("onStart") {
                        expression("\$request.body#/callbacks/onStart") {
                            post {
                                requestBody("Job started") {
                                    jsonContent {
                                        schema {
                                            type = SchemaType.OBJECT
                                            properties {
                                                "jobId" to schema { type = SchemaType.STRING }
                                                "startedAt" to schema { 
                                                    type = SchemaType.STRING 
                                                    format = "date-time"
                                                }
                                            }
                                        }
                                    }
                                }
                                response("200", "OK")
                            }
                        }
                    }
                    
                    callback("onProgress") {
                        expression("\$request.body#/callbacks/onProgress") {
                            post {
                                requestBody("Progress update") {
                                    jsonContent {
                                        schema {
                                            type = SchemaType.OBJECT
                                            properties {
                                                "jobId" to schema { type = SchemaType.STRING }
                                                "progress" to schema {
                                                    type = SchemaType.INTEGER
                                                    minimum = 0
                                                    maximum = 100
                                                }
                                                "currentStep" to schema {
                                                    type = SchemaType.STRING
                                                    enum = listOf(
                                                        "downloading",
                                                        "analyzing",
                                                        "transcoding",
                                                        "uploading"
                                                    )
                                                }
                                                "eta" to schema {
                                                    type = SchemaType.INTEGER
                                                    description = "Seconds remaining"
                                                }
                                            }
                                        }
                                    }
                                }
                                response("200", "OK")
                            }
                        }
                    }
                    
                    callback("onComplete") {
                        expression("\$request.body#/callbacks/onComplete") {
                            post {
                                requestBody("Job complete") {
                                    jsonContent {
                                        schema {
                                            type = SchemaType.OBJECT
                                            properties {
                                                "jobId" to schema { type = SchemaType.STRING }
                                                "outputs" to schema {
                                                    type = SchemaType.ARRAY
                                                    items = schema {
                                                        type = SchemaType.OBJECT
                                                        properties {
                                                            "format" to schema { type = SchemaType.STRING }
                                                            "url" to schema { 
                                                                type = SchemaType.STRING 
                                                                format = "uri"
                                                            }
                                                            "size" to schema { 
                                                                type = SchemaType.INTEGER 
                                                                description = "File size in bytes"
                                                            }
                                                            "duration" to schema {
                                                                type = SchemaType.NUMBER
                                                                description = "Duration in seconds"
                                                            }
                                                        }
                                                    }
                                                }
                                                "completedAt" to schema {
                                                    type = SchemaType.STRING
                                                    format = "date-time"
                                                }
                                            }
                                        }
                                    }
                                }
                                response("200", "OK")
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

1. **Webhooks**
   - Always include signature verification
   - Document retry behavior
   - Provide webhook testing endpoints
   - Include event type in headers

2. **Callbacks**
   - Make callbacks optional when possible
   - Document expected callback response times
   - Handle callback failures gracefully
   - Include correlation IDs

3. **Links**
   - Use operationId over operationRef
   - Document the relationship clearly
   - Test runtime expressions thoroughly
   - Keep parameter mappings simple

4. **Extensions**
   - Use consistent naming (x-company-feature)
   - Document custom extensions
   - Don't rely on extensions for core functionality
   - Validate extension data

5. **External Docs**
   - Keep URLs stable
   - Provide context in descriptions
   - Link to specific sections
   - Include examples in external docs

## Next Steps

- [Examples](examples/) - Complete API examples with advanced features
- [Best Practices](best-practices.md) - Design patterns and guidelines
- [API Operations](api-operations.md) - Review operation definitions