# Blog API Example

This example demonstrates a modern blog/CMS API with advanced features like content management, comments, media handling, and real-time updates via webhooks.

## Complete Blog API Specification

```kotlin
import me.farshad.openapi.*
import kotlinx.serialization.Serializable
import me.farshad.openapi.builder.*

// Enums
@Serializable
enum class PostStatus {
    draft, published, scheduled, archived
}

@Serializable
enum class UserRole {
    admin, editor, author, contributor, subscriber
}

@Serializable
enum class CommentStatus {
    pending, approved, spam, trash
}

@Serializable
enum class MediaType {
    image, video, audio, document
}

// Data Models
@Serializable
data class User(
    val id: String,
    val username: String,
    val email: String,
    val displayName: String,
    val bio: String? = null,
    val avatarUrl: String? = null,
    val role: UserRole,
    val verified: Boolean = false,
    val createdAt: String,
    val updatedAt: String
)

@Serializable
data class Author(
    val id: String,
    val displayName: String,
    val avatarUrl: String? = null,
    val bio: String? = null
)

@Serializable
data class Category(
    val id: String,
    val name: String,
    val slug: String,
    val description: String? = null,
    val parentId: String? = null,
    val postCount: Int = 0
)

@Serializable
data class Tag(
    val id: String,
    val name: String,
    val slug: String,
    val postCount: Int = 0
)

@Serializable
data class Media(
    val id: String,
    val url: String,
    val thumbnailUrl: String? = null,
    val filename: String,
    val mimeType: String,
    val size: Long,
    val width: Int? = null,
    val height: Int? = null,
    val duration: Int? = null,
    val type: MediaType,
    val uploadedBy: String,
    val uploadedAt: String
)

@Serializable
data class Post(
    val id: String,
    val title: String,
    val slug: String,
    val excerpt: String? = null,
    val content: String,
    val featuredImage: Media? = null,
    val author: Author,
    val categories: List<Category> = emptyList(),
    val tags: List<Tag> = emptyList(),
    val status: PostStatus,
    val publishedAt: String? = null,
    val scheduledAt: String? = null,
    val createdAt: String,
    val updatedAt: String,
    val viewCount: Int = 0,
    val likeCount: Int = 0,
    val commentCount: Int = 0,
    val commentsEnabled: Boolean = true,
    val metadata: Map<String, Any> = emptyMap()
)

@Serializable
data class Comment(
    val id: String,
    val postId: String,
    val parentId: String? = null,
    val author: CommentAuthor,
    val content: String,
    val status: CommentStatus,
    val createdAt: String,
    val updatedAt: String,
    val likeCount: Int = 0,
    val replies: List<Comment>? = null
)

@Serializable
data class CommentAuthor(
    val name: String,
    val email: String? = null,
    val url: String? = null,
    val avatarUrl: String? = null,
    val isRegistered: Boolean = false,
    val userId: String? = null
)

// Request/Response DTOs
@Serializable
data class CreatePostRequest(
    val title: String,
    val content: String,
    val excerpt: String? = null,
    val categoryIds: List<String> = emptyList(),
    val tagIds: List<String> = emptyList(),
    val featuredImageId: String? = null,
    val status: PostStatus = PostStatus.draft,
    val scheduledAt: String? = null,
    val commentsEnabled: Boolean = true,
    val metadata: Map<String, Any> = emptyMap()
)

@Serializable
data class UpdatePostRequest(
    val title: String? = null,
    val content: String? = null,
    val excerpt: String? = null,
    val categoryIds: List<String>? = null,
    val tagIds: List<String>? = null,
    val featuredImageId: String? = null,
    val status: PostStatus? = null,
    val scheduledAt: String? = null,
    val commentsEnabled: Boolean? = null,
    val metadata: Map<String, Any>? = null
)

@Serializable
data class CreateCommentRequest(
    val postId: String,
    val parentId: String? = null,
    val content: String,
    val author: CommentAuthor? = null
)

@Serializable
data class PaginatedResponse<T>(
    val items: List<T>,
    val pagination: PaginationInfo
)

@Serializable
data class PaginationInfo(
    val page: Int,
    val pageSize: Int,
    val totalPages: Int,
    val totalItems: Int,
    val hasNext: Boolean,
    val hasPrevious: Boolean
)

@Serializable
data class SearchResults(
    val posts: List<Post> = emptyList(),
    val categories: List<Category> = emptyList(),
    val tags: List<Tag> = emptyList(),
    val users: List<User> = emptyList(),
    val totalResults: Int,
    val searchTime: Double
)

@Serializable
data class Analytics(
    val views: Int,
    val uniqueVisitors: Int,
    val avgReadTime: Double,
    val bounceRate: Double,
    val topPosts: List<PostAnalytics>,
    val topReferrers: List<Referrer>
)

@Serializable
data class PostAnalytics(
    val postId: String,
    val title: String,
    val views: Int,
    val avgReadTime: Double
)

@Serializable
data class Referrer(
    val source: String,
    val count: Int
)

// API Specification
fun createBlogApi() = openApi {
    openapi = "3.1.0"
    
    info {
        title = "Modern Blog API"
        version = "2.0.0"
        description = """
            A comprehensive blog/CMS API with support for:
            - Multiple content types (posts, pages, media)
            - User roles and permissions
            - Comments with threading
            - Categories and tags
            - Search and filtering
            - Analytics
            - Webhooks for real-time updates
            - GraphQL-like field selection
        """.trimIndent()
        
        contact {
            name = "Blog API Support"
            email = "api@blog.example.com"
            url = "https://blog.example.com/api/support"
        }
        
        license {
            name = "MIT"
            url = "https://opensource.org/licenses/MIT"
        }
    }
    
    servers {
        server {
            url = "https://api.blog.example.com/v2"
            description = "Production API"
        }
        
        server {
            url = "https://staging-api.blog.example.com/v2"
            description = "Staging API"
        }
        
        server {
            url = "http://localhost:3000/v2"
            description = "Local development"
        }
    }
    
    // Components
    components {
        // Security schemes
        securityScheme("bearerAuth") {
            type = SecuritySchemeType.HTTP
            scheme = "bearer"
            bearerFormat = "JWT"
            description = "JWT authentication token"
        }
        
        securityScheme("apiKey") {
            type = SecuritySchemeType.API_KEY
            name = "X-API-Key"
            `in` = ApiKeyLocation.HEADER
            description = "API key for server-to-server communication"
        }
        
        securityScheme("oauth2") {
            type = SecuritySchemeType.OAUTH2
            flows {
                authorizationCode {
                    authorizationUrl = "https://auth.blog.example.com/oauth/authorize"
                    tokenUrl = "https://auth.blog.example.com/oauth/token"
                    refreshUrl = "https://auth.blog.example.com/oauth/refresh"
                    scopes {
                        scope("read", "Read access to public content")
                        scope("write", "Write access to own content")
                        scope("admin", "Full administrative access")
                        scope("analytics", "Access to analytics data")
                    }
                }
            }
        }
        
        // Schemas
        schema(User::class)
        schema(Author::class)
        schema(Post::class)
        schema(Category::class)
        schema(Tag::class)
        schema(Media::class)
        schema(Comment::class)
        schema(CommentAuthor::class)
        schema(CreatePostRequest::class)
        schema(UpdatePostRequest::class)
        schema(CreateCommentRequest::class)
        schema(PaginatedResponse::class, Post::class)
        schema(PaginationInfo::class)
        schema(SearchResults::class)
        schema(Analytics::class)
        schema(PostAnalytics::class)
        schema(Referrer::class)
        
        // Common parameters
        parameter("pageParam") {
            name = "page"
            `in` = ParameterLocation.QUERY
            schema {
                type = SchemaType.INTEGER
                minimum = 1
                default = 1
            }
        }
        
        parameter("pageSizeParam") {
            name = "pageSize"
            `in` = ParameterLocation.QUERY
            schema {
                type = SchemaType.INTEGER
                minimum = 1
                maximum = 100
                default = 20
            }
        }
        
        parameter("sortParam") {
            name = "sort"
            `in` = ParameterLocation.QUERY
            schema {
                type = SchemaType.STRING
                enum = listOf(
                    "createdAt", "-createdAt",
                    "updatedAt", "-updatedAt",
                    "publishedAt", "-publishedAt",
                    "title", "-title",
                    "viewCount", "-viewCount",
                    "likeCount", "-likeCount"
                )
                default = "-publishedAt"
            }
        }
        
        parameter("fieldsParam") {
            name = "fields"
            `in` = ParameterLocation.QUERY
            description = "Comma-separated list of fields to include"
            schema {
                type = SchemaType.STRING
                example = "id,title,excerpt,author,publishedAt"
            }
        }
        
        parameter("includeParam") {
            name = "include"
            `in` = ParameterLocation.QUERY
            description = "Related resources to include"
            schema {
                type = SchemaType.ARRAY
                items = schema {
                    type = SchemaType.STRING
                    enum = listOf("author", "categories", "tags", "comments", "media")
                }
            }
            style = "form"
            explode = false
        }
        
        // Common responses
        response("BadRequest") {
            description = "Bad request"
            jsonContent {
                schema {
                    type = SchemaType.OBJECT
                    properties {
                        "error" to schema {
                            type = SchemaType.OBJECT
                            properties {
                                "code" to schema { type = SchemaType.STRING }
                                "message" to schema { type = SchemaType.STRING }
                                "details" to schema {
                                    type = SchemaType.ARRAY
                                    items = schema {
                                        type = SchemaType.OBJECT
                                        properties {
                                            "field" to schema { type = SchemaType.STRING }
                                            "message" to schema { type = SchemaType.STRING }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        
        response("NotFound") {
            description = "Resource not found"
            jsonContent {
                schema {
                    type = SchemaType.OBJECT
                    properties {
                        "error" to schema {
                            type = SchemaType.OBJECT
                            properties {
                                "code" to schema { 
                                    type = SchemaType.STRING 
                                    const = "NOT_FOUND"
                                }
                                "message" to schema { type = SchemaType.STRING }
                            }
                        }
                    }
                }
            }
        }
        
        response("Unauthorized") {
            description = "Unauthorized"
            jsonContent {
                schema {
                    type = SchemaType.OBJECT
                    properties {
                        "error" to schema {
                            type = SchemaType.OBJECT
                            properties {
                                "code" to schema { 
                                    type = SchemaType.STRING 
                                    const = "UNAUTHORIZED"
                                }
                                "message" to schema { type = SchemaType.STRING }
                            }
                        }
                    }
                }
            }
        }
    }
    
    // Tags
    tags {
        tag {
            name = "Posts"
            description = "Blog post operations"
        }
        tag {
            name = "Categories"
            description = "Category management"
        }
        tag {
            name = "Tags"
            description = "Tag management"
        }
        tag {
            name = "Comments"
            description = "Comment operations"
        }
        tag {
            name = "Media"
            description = "Media upload and management"
        }
        tag {
            name = "Users"
            description = "User management"
        }
        tag {
            name = "Search"
            description = "Search operations"
        }
        tag {
            name = "Analytics"
            description = "Analytics and statistics"
        }
    }
    
    // Apply default security
    security {
        requirement("bearerAuth")
    }
    
    // Paths
    paths {
        // Posts
        path("/posts") {
            get {
                summary = "List posts"
                description = "Get a paginated list of blog posts"
                operationId = "listPosts"
                tags = listOf("Posts")
                
                // Override security - public endpoint
                security { }
                
                parameter {
                    name = "status"
                    `in` = ParameterLocation.QUERY
                    schema {
                        type = SchemaType.ARRAY
                        items = schema {
                            ref = "#/components/schemas/PostStatus"
                        }
                    }
                    style = "form"
                    explode = true
                }
                
                parameter {
                    name = "authorId"
                    `in` = ParameterLocation.QUERY
                    schema {
                        type = SchemaType.STRING
                    }
                }
                
                parameter {
                    name = "categoryId"
                    `in` = ParameterLocation.QUERY
                    schema {
                        type = SchemaType.STRING
                    }
                }
                
                parameter {
                    name = "tagIds"
                    `in` = ParameterLocation.QUERY
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
                    name = "search"
                    `in` = ParameterLocation.QUERY
                    description = "Search in title and content"
                    schema {
                        type = SchemaType.STRING
                    }
                }
                
                parameter {
                    name = "publishedAfter"
                    `in` = ParameterLocation.QUERY
                    schema {
                        type = SchemaType.STRING
                        format = "date-time"
                    }
                }
                
                parameter {
                    name = "publishedBefore"
                    `in` = ParameterLocation.QUERY
                    schema {
                        type = SchemaType.STRING
                        format = "date-time"
                    }
                }
                
                parameter { ref = "#/components/parameters/pageParam" }
                parameter { ref = "#/components/parameters/pageSizeParam" }
                parameter { ref = "#/components/parameters/sortParam" }
                parameter { ref = "#/components/parameters/fieldsParam" }
                parameter { ref = "#/components/parameters/includeParam" }
                
                response("200", "Successful response") {
                    jsonContent {
                        schema {
                            ref = "#/components/schemas/PaginatedResponsePost"
                        }
                    }
                    
                    headers {
                        header("X-Total-Count") {
                            schema { type = SchemaType.INTEGER }
                        }
                        header("Link") {
                            description = "Pagination links"
                            schema { type = SchemaType.STRING }
                        }
                    }
                }
            }
            
            post {
                summary = "Create post"
                description = "Create a new blog post"
                operationId = "createPost"
                tags = listOf("Posts")
                
                requestBody("Post data") {
                    required = true
                    jsonContent(CreatePostRequest::class)
                }
                
                response("201", "Post created") {
                    jsonContent(Post::class)
                    headers {
                        header("Location") {
                            schema {
                                type = SchemaType.STRING
                                format = "uri"
                            }
                        }
                    }
                }
                
                response { ref = "#/components/responses/BadRequest" }
                response { ref = "#/components/responses/Unauthorized" }
            }
        }
        
        path("/posts/{postId}") {
            parameter {
                name = "postId"
                `in` = ParameterLocation.PATH
                required = true
                schema {
                    type = SchemaType.STRING
                    format = "uuid"
                }
            }
            
            get {
                summary = "Get post"
                description = "Get a single blog post by ID"
                operationId = "getPost"
                tags = listOf("Posts")
                
                // Public for published posts
                security { }
                
                parameter { ref = "#/components/parameters/fieldsParam" }
                parameter { ref = "#/components/parameters/includeParam" }
                
                response("200", "Post found") {
                    jsonContent(Post::class)
                    
                    headers {
                        header("Cache-Control") {
                            schema { type = SchemaType.STRING }
                        }
                        header("ETag") {
                            schema { type = SchemaType.STRING }
                        }
                    }
                    
                    links {
                        link("author") {
                            operationId = "getUser"
                            parameters = mapOf(
                                "userId" to "\$response.body#/author/id"
                            )
                        }
                        
                        link("comments") {
                            operationId = "listComments"
                            parameters = mapOf(
                                "postId" to "\$response.body#/id"
                            )
                        }
                    }
                }
                
                response { ref = "#/components/responses/NotFound" }
            }
            
            put {
                summary = "Update post"
                description = "Update an existing blog post"
                operationId = "updatePost"
                tags = listOf("Posts")
                
                parameter {
                    name = "If-Match"
                    `in` = ParameterLocation.HEADER
                    description = "ETag for optimistic concurrency"
                    schema { type = SchemaType.STRING }
                }
                
                requestBody("Updated post data") {
                    required = true
                    jsonContent(UpdatePostRequest::class)
                }
                
                response("200", "Post updated") {
                    jsonContent(Post::class)
                }
                
                response { ref = "#/components/responses/BadRequest" }
                response { ref = "#/components/responses/NotFound" }
                response("409", "Conflict - ETag mismatch")
            }
            
            delete {
                summary = "Delete post"
                description = "Delete a blog post"
                operationId = "deletePost"
                tags = listOf("Posts")
                
                response("204", "Post deleted")
                response { ref = "#/components/responses/NotFound" }
            }
        }
        
        path("/posts/{postId}/publish") {
            post {
                summary = "Publish post"
                description = "Publish a draft post"
                operationId = "publishPost"
                tags = listOf("Posts")
                
                parameter {
                    name = "postId"
                    `in` = ParameterLocation.PATH
                    required = true
                    schema { type = SchemaType.STRING }
                }
                
                requestBody("Publish options") {
                    jsonContent {
                        schema {
                            type = SchemaType.OBJECT
                            properties {
                                "publishAt" to schema {
                                    type = SchemaType.STRING
                                    format = "date-time"
                                    description = "Schedule for future publication"
                                }
                                "notifySubscribers" to schema {
                                    type = SchemaType.BOOLEAN
                                    default = true
                                }
                            }
                        }
                    }
                }
                
                response("200", "Post published") {
                    jsonContent(Post::class)
                }
                
                callbacks {
                    callback("postPublished") {
                        expression("{$request.body#/webhookUrl}") {
                            post {
                                requestBody("Post published event") {
                                    jsonContent {
                                        schema {
                                            type = SchemaType.OBJECT
                                            properties {
                                                "event" to schema {
                                                    type = SchemaType.STRING
                                                    const = "post.published"
                                                }
                                                "timestamp" to schema {
                                                    type = SchemaType.STRING
                                                    format = "date-time"
                                                }
                                                "post" to schema {
                                                    ref = "#/components/schemas/Post"
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
            }
        }
        
        // Comments
        path("/posts/{postId}/comments") {
            parameter {
                name = "postId"
                `in` = ParameterLocation.PATH
                required = true
                schema { type = SchemaType.STRING }
            }
            
            get {
                summary = "List comments"
                description = "Get comments for a post"
                operationId = "listComments"
                tags = listOf("Comments")
                
                security { }  // Public
                
                parameter {
                    name = "status"
                    `in` = ParameterLocation.QUERY
                    schema {
                        ref = "#/components/schemas/CommentStatus"
                    }
                }
                
                parameter {
                    name = "parentId"
                    `in` = ParameterLocation.QUERY
                    description = "Filter by parent comment (null for top-level)"
                    schema {
                        type = SchemaType.STRING
                        nullable = true
                    }
                }
                
                parameter { ref = "#/components/parameters/pageParam" }
                parameter { ref = "#/components/parameters/pageSizeParam" }
                
                response("200", "Comments list") {
                    jsonContent(listOf<Comment>())
                }
            }
            
            post {
                summary = "Add comment"
                description = "Add a comment to a post"
                operationId = "createComment"
                tags = listOf("Comments")
                
                // Comments can be anonymous
                security {
                    requirement("bearerAuth")
                    requirement()  // Allow anonymous
                }
                
                requestBody("Comment data") {
                    required = true
                    jsonContent(CreateCommentRequest::class)
                }
                
                response("201", "Comment created") {
                    jsonContent(Comment::class)
                }
                
                response("403", "Comments disabled for this post")
            }
        }
        
        // Media
        path("/media") {
            post {
                summary = "Upload media"
                description = "Upload image, video, or document"
                operationId = "uploadMedia"
                tags = listOf("Media")
                
                requestBody("File upload") {
                    required = true
                    content("multipart/form-data") {
                        schema {
                            type = SchemaType.OBJECT
                            required = listOf("file")
                            properties {
                                "file" to schema {
                                    type = SchemaType.STRING
                                    format = "binary"
                                }
                                "alt" to schema {
                                    type = SchemaType.STRING
                                    description = "Alt text for images"
                                }
                                "caption" to schema {
                                    type = SchemaType.STRING
                                }
                            }
                        }
                    }
                }
                
                response("201", "Media uploaded") {
                    jsonContent(Media::class)
                }
                
                response("413", "File too large") {
                    jsonContent {
                        schema {
                            type = SchemaType.OBJECT
                            properties {
                                "error" to schema {
                                    type = SchemaType.OBJECT
                                    properties {
                                        "code" to schema {
                                            type = SchemaType.STRING
                                            const = "FILE_TOO_LARGE"
                                        }
                                        "message" to schema { type = SchemaType.STRING }
                                        "maxSize" to schema { type = SchemaType.INTEGER }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        
        // Search
        path("/search") {
            get {
                summary = "Search content"
                description = "Search across posts, categories, tags, and users"
                operationId = "search"
                tags = listOf("Search")
                
                security { }  // Public
                
                parameter {
                    name = "q"
                    `in` = ParameterLocation.QUERY
                    required = true
                    description = "Search query"
                    schema {
                        type = SchemaType.STRING
                        minLength = 2
                    }
                }
                
                parameter {
                    name = "types"
                    `in` = ParameterLocation.QUERY
                    description = "Types to search"
                    schema {
                        type = SchemaType.ARRAY
                        items = schema {
                            type = SchemaType.STRING
                            enum = listOf("posts", "categories", "tags", "users")
                        }
                        default = listOf("posts")
                    }
                    style = "form"
                    explode = true
                }
                
                parameter {
                    name = "limit"
                    `in` = ParameterLocation.QUERY
                    schema {
                        type = SchemaType.INTEGER
                        minimum = 1
                        maximum = 50
                        default = 10
                    }
                }
                
                response("200", "Search results") {
                    jsonContent(SearchResults::class)
                    
                    headers {
                        header("X-Search-Time") {
                            description = "Search execution time in ms"
                            schema { type = SchemaType.NUMBER }
                        }
                    }
                }
            }
        }
        
        // Analytics
        path("/analytics") {
            get {
                summary = "Get analytics"
                description = "Get blog analytics and statistics"
                operationId = "getAnalytics"
                tags = listOf("Analytics")
                
                security {
                    requirement("bearerAuth")
                    requirement("oauth2") {
                        scopes = listOf("analytics")
                    }
                }
                
                parameter {
                    name = "period"
                    `in` = ParameterLocation.QUERY
                    schema {
                        type = SchemaType.STRING
                        enum = listOf("day", "week", "month", "year")
                        default = "month"
                    }
                }
                
                parameter {
                    name = "startDate"
                    `in` = ParameterLocation.QUERY
                    schema {
                        type = SchemaType.STRING
                        format = "date"
                    }
                }
                
                parameter {
                    name = "endDate"
                    `in` = ParameterLocation.QUERY
                    schema {
                        type = SchemaType.STRING
                        format = "date"
                    }
                }
                
                response("200", "Analytics data") {
                    jsonContent(Analytics::class)
                }
            }
        }
    }
    
    // Webhooks
    webhooks {
        webhook("postPublished") {
            post {
                summary = "Post published"
                description = "Triggered when a post is published"
                
                requestBody("Event payload") {
                    jsonContent {
                        schema {
                            type = SchemaType.OBJECT
                            properties {
                                "event" to schema {
                                    type = SchemaType.STRING
                                    const = "post.published"
                                }
                                "timestamp" to schema {
                                    type = SchemaType.STRING
                                    format = "date-time"
                                }
                                "post" to schema {
                                    ref = "#/components/schemas/Post"
                                }
                                "previousStatus" to schema {
                                    ref = "#/components/schemas/PostStatus"
                                }
                            }
                        }
                    }
                }
                
                response("200", "Webhook processed")
            }
        }
        
        webhook("commentAdded") {
            post {
                summary = "Comment added"
                description = "Triggered when a new comment is added"
                
                requestBody("Event payload") {
                    jsonContent {
                        schema {
                            type = SchemaType.OBJECT
                            properties {
                                "event" to schema {
                                    type = SchemaType.STRING
                                    const = "comment.added"
                                }
                                "timestamp" to schema {
                                    type = SchemaType.STRING
                                    format = "date-time"
                                }
                                "comment" to schema {
                                    ref = "#/components/schemas/Comment"
                                }
                                "post" to schema {
                                    ref = "#/components/schemas/Post"
                                }
                            }
                        }
                    }
                }
                
                response("200", "Webhook processed")
            }
        }
    }
}

// Generate the specification
fun main() {
    val spec = createBlogApi()
    
    // Save as JSON
    spec.toJsonFile("blog-api.json")
    
    // Save as YAML  
    spec.toYamlFile("blog-api.yaml")
    
    println("Blog API specification generated!")
}
```

## Key Features Demonstrated

### 1. **Content Management**
- Full CRUD for posts with draft/publish workflow
- Categories and tags with hierarchical support
- Media upload and management
- Rich text content with metadata

### 2. **User Interaction**
- Threaded comments with moderation
- Like/view counting
- User roles and permissions
- Anonymous and authenticated comments

### 3. **Advanced Querying**
- Flexible filtering options
- Full-text search across content types
- Field selection (GraphQL-like)
- Include related resources
- Pagination with multiple strategies

### 4. **Real-time Features**
- Webhooks for content events
- Callbacks for async operations
- Event-driven architecture support

### 5. **Security**
- Multiple auth methods (JWT, API Key, OAuth2)
- Role-based permissions
- Public and private endpoints
- Optimistic concurrency control

### 6. **API Features**
- Comprehensive error handling
- HATEOAS links
- Content negotiation
- Rate limiting headers
- Caching support

## Usage Examples

```kotlin
// Client usage examples
val blogClient = BlogApiClient(
    baseUrl = "https://api.blog.example.com/v2",
    token = "your-jwt-token"
)

// Create a draft post
val draft = blogClient.createPost(
    CreatePostRequest(
        title = "Getting Started with Kotlin",
        content = "# Introduction\n\nKotlin is a modern programming language...",
        excerpt = "Learn the basics of Kotlin programming",
        categoryIds = listOf("programming", "kotlin"),
        status = PostStatus.draft
    )
)

// Upload featured image
val image = blogClient.uploadMedia(
    file = File("kotlin-logo.png"),
    alt = "Kotlin Logo"
)

// Update post with image and publish
val published = blogClient.updatePost(
    postId = draft.id,
    UpdatePostRequest(
        featuredImageId = image.id,
        status = PostStatus.published
    )
)

// Search for content
val results = blogClient.search(
    query = "kotlin coroutines",
    types = listOf("posts", "tags"),
    limit = 20
)

// Get analytics
val analytics = blogClient.getAnalytics(
    period = "month",
    startDate = "2023-01-01",
    endDate = "2023-12-31"
)

// Subscribe to webhooks
blogClient.subscribeWebhook(
    url = "https://myapp.com/webhooks",
    events = listOf("post.published", "comment.added"),
    secret = "webhook-secret"
)
```

## Advanced Features

### 1. **Field Selection**
```
GET /posts?fields=id,title,excerpt,author.name,publishedAt
```

### 2. **Resource Inclusion**
```
GET /posts/123?include=author,categories,tags,comments
```

### 3. **Complex Filtering**
```
GET /posts?status=published&authorId=456&categoryId=789&publishedAfter=2023-01-01
```

### 4. **Search with Facets**
```
GET /search?q=kotlin&types=posts,tags&facets=category,author
```

## Next Steps

- Implement GraphQL endpoint for flexible queries
- Add real-time subscriptions via WebSocket
- Implement content versioning
- Add AI-powered content recommendations
- Support for multiple languages/localization
- Advanced SEO metadata handling