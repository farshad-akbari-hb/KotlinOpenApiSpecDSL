package me.farshad

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import me.farshad.dsl.builder.PathsBuilder
import me.farshad.dsl.builder.PathItemBuilder

class PathsBuilderTest {
    
    @Test
    fun testEmptyPaths() {
        val paths = mutableMapOf<String, me.farshad.dsl.spec.PathItem>()
        val pathsBuilder = PathsBuilder(paths)
        
        // No paths added
        assertEquals(0, paths.size)
    }
    
    @Test
    fun testSinglePath() {
        val paths = mutableMapOf<String, me.farshad.dsl.spec.PathItem>()
        val pathsBuilder = PathsBuilder(paths)
        
        pathsBuilder.path("/users") {
            get {
                summary = "List users"
            }
        }
        
        assertEquals(1, paths.size)
        assertNotNull(paths["/users"])
        assertNotNull(paths["/users"]?.get)
        assertEquals("List users", paths["/users"]?.get?.summary)
    }
    
    @Test
    fun testMultiplePaths() {
        val paths = mutableMapOf<String, me.farshad.dsl.spec.PathItem>()
        val pathsBuilder = PathsBuilder(paths)
        
        pathsBuilder.path("/users") {
            get {
                summary = "List users"
            }
        }
        
        pathsBuilder.path("/users/{id}") {
            get {
                summary = "Get user by ID"
            }
        }
        
        pathsBuilder.path("/products") {
            get {
                summary = "List products"
            }
        }
        
        assertEquals(3, paths.size)
        assertNotNull(paths["/users"])
        assertNotNull(paths["/users/{id}"])
        assertNotNull(paths["/products"])
    }
    
    @Test
    fun testPathOverwrite() {
        val paths = mutableMapOf<String, me.farshad.dsl.spec.PathItem>()
        val pathsBuilder = PathsBuilder(paths)
        
        pathsBuilder.path("/users") {
            get {
                summary = "First version"
            }
        }
        
        pathsBuilder.path("/users") {
            get {
                summary = "Second version"
            }
            post {
                summary = "Create user"
            }
        }
        
        assertEquals(1, paths.size)
        assertNotNull(paths["/users"])
        assertEquals("Second version", paths["/users"]?.get?.summary)
        assertNotNull(paths["/users"]?.post)
    }
    
    @Test
    fun testPathsWithVariousPatterns() {
        val paths = mutableMapOf<String, me.farshad.dsl.spec.PathItem>()
        val pathsBuilder = PathsBuilder(paths)
        
        val pathPatterns = listOf(
            "/",
            "/api/v1/users",
            "/users/{id}",
            "/users/{userId}/posts/{postId}",
            "/files/*",
            "/search?q={query}",
            "/users/{id}/profile.json"
        )
        
        pathPatterns.forEach { pattern ->
            pathsBuilder.path(pattern) {
                get {
                    summary = "Operation for $pattern"
                }
            }
        }
        
        assertEquals(pathPatterns.size, paths.size)
        pathPatterns.forEach { pattern ->
            assertNotNull(paths[pattern])
            assertEquals("Operation for $pattern", paths[pattern]?.get?.summary)
        }
    }
}

class PathItemBuilderTest {
    
    @Test
    fun testEmptyPathItem() {
        val pathItemBuilder = PathItemBuilder()
        val pathItem = pathItemBuilder.build()
        
        assertNull(pathItem.get)
        assertNull(pathItem.post)
        assertNull(pathItem.put)
        assertNull(pathItem.delete)
        assertNull(pathItem.patch)
    }
    
    @Test
    fun testPathItemWithGet() {
        val pathItemBuilder = PathItemBuilder()
        pathItemBuilder.get {
            summary = "GET operation"
        }
        
        val pathItem = pathItemBuilder.build()
        
        assertNotNull(pathItem.get)
        assertEquals("GET operation", pathItem.get?.summary)
        assertNull(pathItem.post)
        assertNull(pathItem.put)
        assertNull(pathItem.delete)
        assertNull(pathItem.patch)
    }
    
    @Test
    fun testPathItemWithAllMethods() {
        val pathItemBuilder = PathItemBuilder()
        
        pathItemBuilder.get {
            summary = "GET operation"
        }
        pathItemBuilder.post {
            summary = "POST operation"
        }
        pathItemBuilder.put {
            summary = "PUT operation"
        }
        pathItemBuilder.delete {
            summary = "DELETE operation"
        }
        pathItemBuilder.patch {
            summary = "PATCH operation"
        }
        
        val pathItem = pathItemBuilder.build()
        
        assertNotNull(pathItem.get)
        assertEquals("GET operation", pathItem.get?.summary)
        assertNotNull(pathItem.post)
        assertEquals("POST operation", pathItem.post?.summary)
        assertNotNull(pathItem.put)
        assertEquals("PUT operation", pathItem.put?.summary)
        assertNotNull(pathItem.delete)
        assertEquals("DELETE operation", pathItem.delete?.summary)
        assertNotNull(pathItem.patch)
        assertEquals("PATCH operation", pathItem.patch?.summary)
    }
    
    @Test
    fun testPathItemMethodOverwrite() {
        val pathItemBuilder = PathItemBuilder()
        
        pathItemBuilder.get {
            summary = "First GET"
            description = "First description"
        }
        
        pathItemBuilder.get {
            summary = "Second GET"
        }
        
        val pathItem = pathItemBuilder.build()
        
        assertNotNull(pathItem.get)
        assertEquals("Second GET", pathItem.get?.summary)
        assertNull(pathItem.get?.description) // Second definition doesn't have description
    }
    
    @Test
    fun testPathItemWithComplexOperations() {
        val pathItemBuilder = PathItemBuilder()
        
        pathItemBuilder.get {
            summary = "List resources"
            description = "Returns a paginated list of resources"
            operationId = "listResources"
            tags("resources")
            parameter("page", me.farshad.dsl.spec.ParameterLocation.QUERY, me.farshad.dsl.spec.PropertyType.INTEGER)
            parameter("limit", me.farshad.dsl.spec.ParameterLocation.QUERY, me.farshad.dsl.spec.PropertyType.INTEGER)
            response("200", "Success")
            response("400", "Bad Request")
        }
        
        pathItemBuilder.post {
            summary = "Create resource"
            operationId = "createResource"
            tags("resources")
            requestBody {
                required = true
                jsonContent {
                    type = me.farshad.dsl.spec.SchemaType.OBJECT
                }
            }
            response("201", "Created")
            response("400", "Bad Request")
        }
        
        val pathItem = pathItemBuilder.build()
        
        assertNotNull(pathItem.get)
        assertEquals("listResources", pathItem.get?.operationId)
        assertEquals(2, pathItem.get?.parameters?.size)
        assertEquals(2, pathItem.get?.responses?.size)
        
        assertNotNull(pathItem.post)
        assertEquals("createResource", pathItem.post?.operationId)
        assertNotNull(pathItem.post?.requestBody)
        assertEquals(2, pathItem.post?.responses?.size)
    }
    
    @Test
    fun testPathItemBuilderChaining() {
        val pathItem = PathItemBuilder().apply {
            get {
                summary = "GET"
            }
            post {
                summary = "POST"
            }
            put {
                summary = "PUT"
            }
        }.build()
        
        assertNotNull(pathItem.get)
        assertNotNull(pathItem.post)
        assertNotNull(pathItem.put)
        assertNull(pathItem.delete)
        assertNull(pathItem.patch)
    }
}