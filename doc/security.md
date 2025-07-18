# Security

This guide covers implementing authentication and authorization in your OpenAPI specifications, including various security schemes and their applications.

## Table of Contents
- [Security Overview](#security-overview)
- [API Key Authentication](#api-key-authentication)
- [HTTP Authentication](#http-authentication)
- [OAuth2](#oauth2)
- [OpenID Connect](#openid-connect)
- [Multiple Security Schemes](#multiple-security-schemes)
- [Security Requirements](#security-requirements)
- [Complete Examples](#complete-examples)
- [Best Practices](#best-practices)

## Security Overview

OpenAPI 3.1 supports several authentication methods:
- **API Key**: Simple key-based authentication
- **HTTP**: Basic, Bearer, and other HTTP authentication schemes
- **OAuth2**: Various OAuth2 flows
- **OpenID Connect**: OpenID Connect Discovery

### Defining Security Schemes

```kotlin
openApi {
    components {
        // Define security schemes here
        securityScheme("apiKey") {
            type = SecuritySchemeType.API_KEY
            name = "X-API-Key"
            `in` = ApiKeyLocation.HEADER
            description = "API key for authentication"
        }
        
        securityScheme("bearerAuth") {
            type = SecuritySchemeType.HTTP
            scheme = "bearer"
            bearerFormat = "JWT"
            description = "JWT bearer token authentication"
        }
    }
    
    // Apply globally
    security {
        requirement("apiKey")
    }
}
```

## API Key Authentication

### Header-based API Key

```kotlin
components {
    securityScheme("apiKeyHeader") {
        type = SecuritySchemeType.API_KEY
        name = "X-API-Key"
        `in` = ApiKeyLocation.HEADER
        description = """
            API key must be provided in the X-API-Key header.
            
            Example: X-API-Key: your-api-key-here
            
            To obtain an API key, visit https://api.example.com/developers
        """.trimIndent()
    }
}

// Usage in operation
paths {
    path("/secure-data") {
        get {
            summary = "Get secure data"
            security {
                requirement("apiKeyHeader")
            }
            response("200", "Success") {
                jsonContent(SecureData::class)
            }
            response("401", "Missing or invalid API key") {
                jsonContent(Error::class)
            }
        }
    }
}
```

### Query Parameter API Key

```kotlin
components {
    securityScheme("apiKeyQuery") {
        type = SecuritySchemeType.API_KEY
        name = "api_key"
        `in` = ApiKeyLocation.QUERY
        description = "API key as query parameter (not recommended for production)"
    }
}

// Usage
get {
    summary = "Legacy endpoint with query API key"
    deprecated = true  // Query parameters can leak in logs
    security {
        requirement("apiKeyQuery")
    }
}
```

### Cookie-based API Key

```kotlin
components {
    securityScheme("apiKeyCookie") {
        type = SecuritySchemeType.API_KEY
        name = "api_session"
        `in` = ApiKeyLocation.COOKIE
        description = "Session cookie for API access"
    }
}
```

### Multiple API Keys

```kotlin
components {
    // Primary API key
    securityScheme("primaryKey") {
        type = SecuritySchemeType.API_KEY
        name = "X-API-Key"
        `in` = ApiKeyLocation.HEADER
    }
    
    // Secondary key for additional validation
    securityScheme("secretKey") {
        type = SecuritySchemeType.API_KEY
        name = "X-API-Secret"
        `in` = ApiKeyLocation.HEADER
    }
}

// Require both keys
paths {
    path("/highly-secure") {
        post {
            security {
                requirement {
                    schemes = listOf("primaryKey", "secretKey")
                }
            }
        }
    }
}
```

## HTTP Authentication

### Basic Authentication

```kotlin
components {
    securityScheme("basicAuth") {
        type = SecuritySchemeType.HTTP
        scheme = "basic"
        description = """
            Basic HTTP authentication using username and password.
            
            Credentials should be base64 encoded in the format: username:password
            
            Example: Authorization: Basic dXNlcm5hbWU6cGFzc3dvcmQ=
        """.trimIndent()
    }
}

// Usage
paths {
    path("/admin") {
        get {
            summary = "Admin endpoint"
            security {
                requirement("basicAuth")
            }
            response("200", "Admin data") {
                jsonContent(AdminData::class)
            }
            response("401", "Unauthorized") {
                headers {
                    header("WWW-Authenticate") {
                        schema {
                            type = SchemaType.STRING
                            example = "Basic realm=\"Admin Area\""
                        }
                    }
                }
            }
        }
    }
}
```

### Bearer Token (JWT)

```kotlin
components {
    securityScheme("bearerAuth") {
        type = SecuritySchemeType.HTTP
        scheme = "bearer"
        bearerFormat = "JWT"
        description = """
            JWT Bearer token authentication.
            
            Include the JWT token in the Authorization header:
            Authorization: Bearer <token>
            
            Tokens expire after 1 hour. Refresh using /auth/refresh endpoint.
        """.trimIndent()
    }
}

// Define token schemas
components {
    schema("LoginRequest") {
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
    
    schema("TokenResponse") {
        type = SchemaType.OBJECT
        properties {
            "accessToken" to schema { type = SchemaType.STRING }
            "refreshToken" to schema { type = SchemaType.STRING }
            "tokenType" to schema { 
                type = SchemaType.STRING 
                const = "Bearer"
            }
            "expiresIn" to schema { 
                type = SchemaType.INTEGER 
                description = "Token lifetime in seconds"
            }
        }
    }
}

// Authentication endpoints
paths {
    path("/auth/login") {
        post {
            summary = "Login to get access token"
            tags = listOf("Authentication")
            
            requestBody("Credentials") {
                jsonContent(LoginRequest::class)
            }
            
            response("200", "Login successful") {
                jsonContent(TokenResponse::class)
            }
            
            response("401", "Invalid credentials") {
                jsonContent(Error::class)
            }
        }
    }
    
    path("/auth/refresh") {
        post {
            summary = "Refresh access token"
            security {
                requirement("bearerAuth")  // Requires refresh token
            }
            
            response("200", "Token refreshed") {
                jsonContent(TokenResponse::class)
            }
        }
    }
    
    path("/auth/logout") {
        post {
            summary = "Logout and invalidate token"
            security {
                requirement("bearerAuth")
            }
            
            response("204", "Logged out successfully")
        }
    }
}
```

### Custom HTTP Schemes

```kotlin
components {
    securityScheme("digestAuth") {
        type = SecuritySchemeType.HTTP
        scheme = "digest"
        description = "Digest authentication"
    }
    
    securityScheme("customAuth") {
        type = SecuritySchemeType.HTTP
        scheme = "Custom"
        description = "Custom authentication scheme: Custom base64(apiKey:timestamp:signature)"
    }
}
```

## OAuth2

### Authorization Code Flow

```kotlin
components {
    securityScheme("oauth2AuthCode") {
        type = SecuritySchemeType.OAUTH2
        description = "OAuth2 authorization code flow"
        flows {
            authorizationCode {
                authorizationUrl = "https://auth.example.com/oauth/authorize"
                tokenUrl = "https://auth.example.com/oauth/token"
                refreshUrl = "https://auth.example.com/oauth/refresh"
                scopes {
                    scope("read:users", "Read user information")
                    scope("write:users", "Create and update users")
                    scope("delete:users", "Delete users")
                    scope("admin", "Full administrative access")
                }
            }
        }
    }
}

// Usage with scopes
paths {
    path("/users") {
        get {
            summary = "List users"
            security {
                requirement("oauth2AuthCode") {
                    scopes = listOf("read:users")
                }
            }
        }
        
        post {
            summary = "Create user"
            security {
                requirement("oauth2AuthCode") {
                    scopes = listOf("write:users")
                }
            }
        }
    }
    
    path("/users/{id}") {
        delete {
            summary = "Delete user"
            security {
                requirement("oauth2AuthCode") {
                    scopes = listOf("delete:users", "admin")  // Requires either scope
                }
            }
        }
    }
}
```

### Implicit Flow (Deprecated)

```kotlin
components {
    securityScheme("oauth2Implicit") {
        type = SecuritySchemeType.OAUTH2
        description = "OAuth2 implicit flow (deprecated, use authorization code flow instead)"
        flows {
            implicit {
                authorizationUrl = "https://auth.example.com/oauth/authorize"
                scopes {
                    scope("read", "Read access")
                    scope("write", "Write access")
                }
            }
        }
    }
}
```

### Client Credentials Flow

```kotlin
components {
    securityScheme("oauth2ClientCredentials") {
        type = SecuritySchemeType.OAUTH2
        description = "OAuth2 client credentials flow for machine-to-machine authentication"
        flows {
            clientCredentials {
                tokenUrl = "https://auth.example.com/oauth/token"
                refreshUrl = "https://auth.example.com/oauth/refresh"
                scopes {
                    scope("system:read", "Read system data")
                    scope("system:write", "Modify system data")
                    scope("system:admin", "System administration")
                }
            }
        }
    }
}

// Service-to-service endpoints
paths {
    path("/system/health") {
        get {
            summary = "System health check"
            security {
                requirement("oauth2ClientCredentials") {
                    scopes = listOf("system:read")
                }
            }
        }
    }
}
```

### Password Flow (Resource Owner)

```kotlin
components {
    securityScheme("oauth2Password") {
        type = SecuritySchemeType.OAUTH2
        description = "OAuth2 password flow (only for highly trusted applications)"
        flows {
            password {
                tokenUrl = "https://auth.example.com/oauth/token"
                refreshUrl = "https://auth.example.com/oauth/refresh"
                scopes {
                    scope("profile", "User profile access")
                    scope("email", "Email access")
                    scope("offline_access", "Offline access for refresh tokens")
                }
            }
        }
    }
}
```

### Multiple OAuth2 Flows

```kotlin
components {
    securityScheme("oauth2Combined") {
        type = SecuritySchemeType.OAUTH2
        description = "OAuth2 with multiple flows supported"
        flows {
            // For web applications
            authorizationCode {
                authorizationUrl = "https://auth.example.com/oauth/authorize"
                tokenUrl = "https://auth.example.com/oauth/token"
                scopes {
                    scope("user:read", "Read user data")
                    scope("user:write", "Write user data")
                }
            }
            
            // For trusted mobile/desktop apps
            password {
                tokenUrl = "https://auth.example.com/oauth/token"
                scopes {
                    scope("user:read", "Read user data")
                    scope("user:write", "Write user data")
                    scope("offline_access", "Refresh token access")
                }
            }
            
            // For machine-to-machine
            clientCredentials {
                tokenUrl = "https://auth.example.com/oauth/token"
                scopes {
                    scope("api:access", "General API access")
                }
            }
        }
    }
}
```

## OpenID Connect

```kotlin
components {
    securityScheme("openIdConnect") {
        type = SecuritySchemeType.OPENID_CONNECT
        openIdConnectUrl = "https://auth.example.com/.well-known/openid-configuration"
        description = """
            OpenID Connect authentication.
            
            Supported scopes:
            - openid: OpenID Connect authentication
            - profile: User profile information
            - email: User email address
            - phone: User phone number
            - address: User address
            - offline_access: Refresh token access
            
            Discovery document available at the URL above.
        """.trimIndent()
    }
}

// Usage
paths {
    path("/user/profile") {
        get {
            summary = "Get user profile"
            security {
                requirement("openIdConnect")
            }
            
            response("200", "User profile") {
                jsonContent {
                    schema {
                        type = SchemaType.OBJECT
                        properties {
                            "sub" to schema { 
                                type = SchemaType.STRING 
                                description = "Subject identifier"
                            }
                            "name" to schema { type = SchemaType.STRING }
                            "email" to schema { 
                                type = SchemaType.STRING 
                                format = "email"
                            }
                            "email_verified" to schema { type = SchemaType.BOOLEAN }
                            "picture" to schema { 
                                type = SchemaType.STRING 
                                format = "uri"
                            }
                        }
                    }
                }
            }
        }
    }
}
```

## Multiple Security Schemes

### OR Logic (Any One Scheme)

```kotlin
paths {
    path("/flexible-auth") {
        get {
            summary = "Endpoint accepting multiple auth methods"
            // User can authenticate with ANY of these
            security {
                requirement("bearerAuth")
                requirement("apiKey")
                requirement("basicAuth")
            }
            
            response("200", "Success") {
                jsonContent(Data::class)
            }
        }
    }
}
```

### AND Logic (Multiple Schemes Required)

```kotlin
paths {
    path("/high-security") {
        post {
            summary = "Requires multiple authentication methods"
            // User must provide ALL of these
            security {
                requirement {
                    schemes = listOf("apiKey", "bearerAuth")
                }
            }
            
            response("200", "Success") {
                jsonContent(SecureData::class)
            }
        }
    }
}
```

### Complex Security Requirements

```kotlin
paths {
    path("/complex-security") {
        post {
            summary = "Complex security requirements"
            // (apiKey AND bearerAuth) OR (oauth2 with admin scope)
            security {
                requirement {
                    schemes = listOf("apiKey", "bearerAuth")
                }
                requirement("oauth2AuthCode") {
                    scopes = listOf("admin")
                }
            }
        }
    }
}
```

### Optional Security

```kotlin
paths {
    path("/public-with-benefits") {
        get {
            summary = "Public endpoint with additional features for authenticated users"
            // Empty security array means optional authentication
            security {
                requirement("bearerAuth")
                requirement()  // Empty requirement = no auth needed
            }
            
            response("200", "Success") {
                description = "Returns basic data for anonymous users, extended data for authenticated users"
                jsonContent {
                    schema {
                        oneOf = listOf(
                            SchemaReference.ReferenceTo("#/components/schemas/PublicData"),
                            SchemaReference.ReferenceTo("#/components/schemas/ExtendedData")
                        )
                    }
                }
            }
        }
    }
}
```

## Security Requirements

### Global Security

```kotlin
openApi {
    // Apply security globally to all operations
    security {
        requirement("bearerAuth")
    }
    
    paths {
        path("/users") {
            get {
                // Inherits global security
                summary = "List users (requires auth)"
            }
        }
        
        path("/public") {
            get {
                summary = "Public endpoint"
                // Override global security with empty array
                security { }
            }
        }
    }
}
```

### Path-level Security

```kotlin
paths {
    path("/admin/{resource}") {
        // Apply to all operations on this path
        parameters {
            parameter {
                name = "resource"
                `in` = ParameterLocation.PATH
                required = true
                schema { type = SchemaType.STRING }
            }
        }
        
        // These security requirements apply to all operations
        get {
            security {
                requirement("oauth2AuthCode") {
                    scopes = listOf("admin:read")
                }
            }
        }
        
        post {
            security {
                requirement("oauth2AuthCode") {
                    scopes = listOf("admin:write")
                }
            }
        }
        
        delete {
            security {
                requirement("oauth2AuthCode") {
                    scopes = listOf("admin:delete")
                }
            }
        }
    }
}
```

### Conditional Security

```kotlin
// Different security for different operations
paths {
    path("/resources/{id}") {
        // Read operations
        get {
            summary = "Get resource"
            security {
                requirement("apiKey")  // Simple API key for reads
            }
        }
        
        // Write operations
        put {
            summary = "Update resource"
            security {
                requirement("oauth2AuthCode") {  // OAuth for writes
                    scopes = listOf("write:resources")
                }
            }
        }
        
        // Delete operations
        delete {
            summary = "Delete resource"
            security {
                requirement {  // Multiple auth methods for deletes
                    schemes = listOf("oauth2AuthCode", "apiKey")
                }
            }
        }
    }
}
```

## Complete Examples

### Multi-tenant API with API Keys

```kotlin
val multiTenantApi = openApi {
    openapi = "3.1.0"
    info {
        title = "Multi-tenant API"
        version = "1.0.0"
    }
    
    components {
        // Tenant API key
        securityScheme("tenantKey") {
            type = SecuritySchemeType.API_KEY
            name = "X-Tenant-Key"
            `in` = ApiKeyLocation.HEADER
            description = "Tenant-specific API key"
        }
        
        // User API key
        securityScheme("userKey") {
            type = SecuritySchemeType.API_KEY
            name = "X-User-Key"
            `in` = ApiKeyLocation.HEADER
            description = "User-specific API key"
        }
        
        // Admin override key
        securityScheme("adminKey") {
            type = SecuritySchemeType.API_KEY
            name = "X-Admin-Key"
            `in` = ApiKeyLocation.HEADER
            description = "Administrative override key"
        }
    }
    
    paths {
        path("/tenants/{tenantId}/data") {
            parameter {
                name = "tenantId"
                `in` = ParameterLocation.PATH
                required = true
                schema { type = SchemaType.STRING }
            }
            
            get {
                summary = "Get tenant data"
                security {
                    // Tenant key OR admin key
                    requirement("tenantKey")
                    requirement("adminKey")
                }
            }
        }
        
        path("/users/{userId}/profile") {
            get {
                summary = "Get user profile"
                security {
                    // (Tenant key AND user key) OR admin key
                    requirement {
                        schemes = listOf("tenantKey", "userKey")
                    }
                    requirement("adminKey")
                }
            }
        }
    }
}
```

### OAuth2 with Role-based Access

```kotlin
val roleBasedApi = openApi {
    openapi = "3.1.0"
    info {
        title = "Role-based API"
        version = "1.0.0"
    }
    
    components {
        securityScheme("oauth2") {
            type = SecuritySchemeType.OAUTH2
            flows {
                authorizationCode {
                    authorizationUrl = "https://auth.example.com/authorize"
                    tokenUrl = "https://auth.example.com/token"
                    scopes {
                        // Role-based scopes
                        scope("role:user", "Basic user access")
                        scope("role:moderator", "Moderator access")
                        scope("role:admin", "Administrator access")
                        
                        // Resource-based scopes
                        scope("read:posts", "Read posts")
                        scope("write:posts", "Create/update posts")
                        scope("delete:posts", "Delete posts")
                        scope("read:users", "Read user data")
                        scope("write:users", "Modify user data")
                    }
                }
            }
        }
    }
    
    paths {
        // Public endpoints
        path("/posts") {
            get {
                summary = "List public posts"
                // No security required
            }
        }
        
        // User endpoints
        path("/my/posts") {
            get {
                summary = "Get my posts"
                security {
                    requirement("oauth2") {
                        scopes = listOf("role:user", "read:posts")
                    }
                }
            }
            
            post {
                summary = "Create my post"
                security {
                    requirement("oauth2") {
                        scopes = listOf("role:user", "write:posts")
                    }
                }
            }
        }
        
        // Moderator endpoints
        path("/moderate/posts/{postId}") {
            put {
                summary = "Moderate post"
                security {
                    requirement("oauth2") {
                        scopes = listOf("role:moderator", "write:posts")
                    }
                }
            }
            
            delete {
                summary = "Remove post"
                security {
                    requirement("oauth2") {
                        scopes = listOf("role:moderator", "delete:posts")
                    }
                }
            }
        }
        
        // Admin endpoints
        path("/admin/users") {
            get {
                summary = "List all users"
                security {
                    requirement("oauth2") {
                        scopes = listOf("role:admin", "read:users")
                    }
                }
            }
            
            delete {
                summary = "Delete user"
                security {
                    requirement("oauth2") {
                        scopes = listOf("role:admin", "write:users")
                    }
                }
            }
        }
    }
}
```

### API with Security Headers

```kotlin
components {
    // Define security-related headers as parameters
    parameter("rateLimitHeader") {
        name = "X-RateLimit-Remaining"
        `in` = ParameterLocation.HEADER
        schema {
            type = SchemaType.INTEGER
        }
    }
    
    parameter("apiVersionHeader") {
        name = "X-API-Version"
        `in` = ParameterLocation.HEADER
        required = true
        schema {
            type = SchemaType.STRING
            enum = listOf("v1", "v2", "v3")
        }
    }
}

// Security-enhanced responses
paths {
    path("/secure-endpoint") {
        get {
            security {
                requirement("bearerAuth")
            }
            
            response("200", "Success") {
                jsonContent(Data::class)
                
                headers {
                    // Security headers
                    header("X-Content-Type-Options") {
                        schema {
                            type = SchemaType.STRING
                            const = "nosniff"
                        }
                    }
                    
                    header("X-Frame-Options") {
                        schema {
                            type = SchemaType.STRING
                            const = "DENY"
                        }
                    }
                    
                    header("Strict-Transport-Security") {
                        schema {
                            type = SchemaType.STRING
                            const = "max-age=31536000; includeSubDomains"
                        }
                    }
                    
                    header("X-RateLimit-Limit") {
                        schema { type = SchemaType.INTEGER }
                    }
                    
                    header("X-RateLimit-Remaining") {
                        schema { type = SchemaType.INTEGER }
                    }
                    
                    header("X-RateLimit-Reset") {
                        schema { 
                            type = SchemaType.INTEGER 
                            description = "Unix timestamp"
                        }
                    }
                }
            }
            
            response("429", "Too Many Requests") {
                headers {
                    header("Retry-After") {
                        schema { type = SchemaType.INTEGER }
                    }
                }
            }
        }
    }
}
```

## Best Practices

### 1. Security Scheme Design
- Use HTTPS for all authenticated endpoints
- Prefer header-based authentication over query parameters
- Use appropriate OAuth2 flows for your use case
- Document token expiration and refresh procedures

### 2. API Key Management
- Provide clear instructions for obtaining API keys
- Implement key rotation policies
- Use different keys for different environments
- Never expose API keys in examples

### 3. OAuth2 Implementation
- Use PKCE for public clients
- Implement proper scope granularity
- Support token refresh for long-lived access
- Validate redirect URIs strictly

### 4. Error Handling
```kotlin
response("401", "Unauthorized") {
    jsonContent {
        schema {
            type = SchemaType.OBJECT
            properties {
                "error" to schema {
                    type = SchemaType.STRING
                    enum = listOf(
                        "invalid_token",
                        "expired_token",
                        "insufficient_scope",
                        "invalid_api_key"
                    )
                }
                "error_description" to schema {
                    type = SchemaType.STRING
                }
                "required_scopes" to schema {
                    type = SchemaType.ARRAY
                    items = schema { type = SchemaType.STRING }
                }
            }
        }
    }
    
    headers {
        header("WWW-Authenticate") {
            schema { type = SchemaType.STRING }
            examples {
                example("bearer") {
                    value = "Bearer realm=\"api\", error=\"invalid_token\""
                }
                example("basic") {
                    value = "Basic realm=\"Admin Area\""
                }
            }
        }
    }
}
```

### 5. Security Documentation
- Provide authentication flow diagrams
- Include example requests with auth headers
- Document rate limiting policies
- Explain token lifecycle and refresh

### 6. Testing Security
- Include test credentials in documentation (for sandbox only)
- Provide a sandbox environment
- Document security-related error codes
- Show examples of successful and failed auth

## Next Steps

- [Reusable Components](reusable-components.md) - Building reusable security schemes
- [Advanced Features](advanced-features.md) - Security with webhooks and callbacks
- [Best Practices](best-practices.md) - Security best practices and patterns