package me.farshad.dsl.builder

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlinx.serialization.json.*
import me.farshad.dsl.builder.core.openApi
import me.farshad.dsl.builder.core.toJson
import me.farshad.dsl.builder.core.toYaml
import me.farshad.dsl.spec.PropertyType
import me.farshad.dsl.spec.SchemaType

class OpenApiBuilderTest {
    
    @Test
    fun testMinimalOpenApiSpec() {
        val spec = openApi {
            info {
                title = "Test API"
                version = "1.0.0"
            }
        }
        
        assertEquals("3.1.0", spec.openapi)
        assertEquals("Test API", spec.info.title)
        assertEquals("1.0.0", spec.info.version)
        assertEquals(0, spec.servers.size)
        assertEquals(0, spec.paths.size)
        assertNull(spec.components)
    }
    
    @Test
    fun testCustomOpenApiVersion() {
        val spec = openApi {
            openapi = "3.0.0"
            info {
                title = "Test API"
                version = "1.0.0"
            }
        }
        
        assertEquals("3.0.0", spec.openapi)
    }
    
    @Test
    fun testOpenApiWithServers() {
        val spec = openApi {
            info {
                title = "Test API"
                version = "1.0.0"
            }
            server("https://api.example.com")
            server("https://staging.example.com") {
                description = "Staging server"
            }
        }
        
        assertEquals(2, spec.servers.size)
        assertEquals("https://api.example.com", spec.servers[0].url)
        assertNull(spec.servers[0].description)
        assertEquals("https://staging.example.com", spec.servers[1].url)
        assertEquals("Staging server", spec.servers[1].description)
    }
    
    @Test
    fun testOpenApiWithPaths() {
        val spec = openApi {
            info {
                title = "Test API"
                version = "1.0.0"
            }
            paths {
                path("/users") {
                    get {
                        summary = "List users"
                    }
                }
                path("/users/{id}") {
                    get {
                        summary = "Get user by ID"
                    }
                    put {
                        summary = "Update user"
                    }
                }
            }
        }
        
        assertEquals(2, spec.paths.size)
        assertNotNull(spec.paths["/users"])
        assertNotNull(spec.paths["/users"]?.get)
        assertEquals("List users", spec.paths["/users"]?.get?.summary)
        
        assertNotNull(spec.paths["/users/{id}"])
        assertNotNull(spec.paths["/users/{id}"]?.get)
        assertEquals("Get user by ID", spec.paths["/users/{id}"]?.get?.summary)
        assertNotNull(spec.paths["/users/{id}"]?.put)
        assertEquals("Update user", spec.paths["/users/{id}"]?.put?.summary)
    }
    
    @Test
    fun testOpenApiWithComponents() {
        val spec = openApi {
            info {
                title = "Test API"
                version = "1.0.0"
            }
            components {
                schema("User") {
                    type = SchemaType.OBJECT
                    property("id", PropertyType.INTEGER, true)
                    property("name", PropertyType.STRING, true)
                }
                securityScheme("bearerAuth", "http", "bearer", "JWT")
            }
        }
        
        assertNotNull(spec.components)
        assertNotNull(spec.components?.schemas)
        assertEquals(1, spec.components?.schemas?.size)
        assertNotNull(spec.components?.schemas?.get("User"))
        assertNotNull(spec.components?.securitySchemes)
        assertEquals(1, spec.components?.securitySchemes?.size)
    }
    
    @Test
    fun testCompleteOpenApiSpec() {
        val spec = openApi {
            openapi = "3.1.0"
            info {
                title = "Complete API"
                version = "2.0.0"
                description = "A complete API example"
                termsOfService = "https://example.com/terms"
                contact {
                    name = "API Support"
                    email = "support@example.com"
                }
                license("MIT", "https://opensource.org/licenses/MIT")
            }
            server("https://api.example.com") {
                description = "Production server"
            }
            paths {
                path("/users") {
                    get {
                        summary = "List all users"
                        tags("users")
                        response("200", "Successful response") {
                            jsonContent("UserList")
                        }
                    }
                    post {
                        summary = "Create a new user"
                        tags("users")
                        requestBody {
                            required = true
                            jsonContent("User")
                        }
                        response("201", "User created")
                    }
                }
            }
            components {
                schema("User") {
                    type = SchemaType.OBJECT
                    property("id", PropertyType.INTEGER, true)
                    property("name", PropertyType.STRING, true)
                    property("email", PropertyType.STRING, false)
                }
                schema("UserList") {
                    type = SchemaType.ARRAY
                    items {
                        type = SchemaType.OBJECT
                    }
                }
            }
        }
        
        assertEquals("3.1.0", spec.openapi)
        assertEquals("Complete API", spec.info.title)
        assertEquals("2.0.0", spec.info.version)
        assertEquals("A complete API example", spec.info.description)
        assertEquals("https://example.com/terms", spec.info.termsOfService)
        assertNotNull(spec.info.contact)
        assertEquals("API Support", spec.info.contact?.name)
        assertNotNull(spec.info.license)
        assertEquals("MIT", spec.info.license?.name)
        
        assertEquals(1, spec.servers.size)
        assertEquals("https://api.example.com", spec.servers[0].url)
        
        assertEquals(1, spec.paths.size)
        assertNotNull(spec.paths["/users"])
        assertNotNull(spec.paths["/users"]?.get)
        assertNotNull(spec.paths["/users"]?.post)
        
        assertNotNull(spec.components)
        assertEquals(2, spec.components?.schemas?.size)
    }
    
    @Test
    fun testOpenApiJsonSerialization() {
        val spec = openApi {
            info {
                title = "JSON Test API"
                version = "1.0.0"
            }
            paths {
                path("/test") {
                    get {
                        summary = "Test endpoint"
                        response("200", "Success")
                    }
                }
            }
        }
        
        val json = spec.toJson()
        val jsonObject = Json.parseToJsonElement(json).jsonObject
        
        assertEquals("3.1.0", jsonObject["openapi"]?.jsonPrimitive?.content)
        assertEquals("JSON Test API", jsonObject["info"]?.jsonObject?.get("title")?.jsonPrimitive?.content)
        assertNotNull(jsonObject["paths"]?.jsonObject?.get("/test"))
    }
    
    @Test
    fun testOpenApiYamlSerialization() {
        val spec = openApi {
            info {
                title = "YAML Test API"
                version = "1.0.0"
                description = "Testing YAML serialization"
            }
        }
        
        val yaml = spec.toYaml()
        
        assert(yaml.contains("openapi: \"3.1.0\""))
        assert(yaml.contains("title: \"YAML Test API\""))
        assert(yaml.contains("version: \"1.0.0\""))
        assert(yaml.contains("description: \"Testing YAML serialization\""))
    }
    
    @Test
    fun testEmptyPathsAndComponents() {
        val spec = openApi {
            info {
                title = "Empty API"
                version = "1.0.0"
            }
            paths {
                // Empty paths block
            }
            components {
                // Empty components block
            }
        }
        
        assertEquals(0, spec.paths.size)
        assertNotNull(spec.components)
        assertNull(spec.components?.schemas)
        assertNull(spec.components?.securitySchemes)
        assertNull(spec.components?.examples)
    }
    
    @Test
    fun testMultipleServersWithoutDescription() {
        val spec = openApi {
            info {
                title = "Multi-Server API"
                version = "1.0.0"
            }
            server("https://api1.example.com")
            server("https://api2.example.com")
            server("https://api3.example.com")
        }
        
        assertEquals(3, spec.servers.size)
        spec.servers.forEach { server ->
            assertNull(server.description)
        }
    }
}