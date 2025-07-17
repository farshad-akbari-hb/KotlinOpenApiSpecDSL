package me.farshad.dsl.builder.paths

import me.farshad.dsl.builder.paths.OperationBuilder
import me.farshad.dsl.spec.ParameterLocation
import me.farshad.dsl.spec.PropertyType
import me.farshad.dsl.spec.SchemaFormat
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class OperationBuilderTest {
    @Test
    fun testMinimalOperation() {
        val operationBuilder = OperationBuilder()
        val operation = operationBuilder.build()

        assertNull(operation.summary)
        assertNull(operation.description)
        assertNull(operation.operationId)
        assertNull(operation.tags)
        assertNull(operation.parameters)
        assertNull(operation.requestBody)
        assertEquals(0, operation.responses.size)
        assertNull(operation.security)
    }

    @Test
    fun testOperationWithSummaryAndDescription() {
        val operationBuilder = OperationBuilder()
        operationBuilder.summary = "List users"
        operationBuilder.description = "Returns a list of all users in the system"
        operationBuilder.operationId = "listUsers"

        val operation = operationBuilder.build()

        assertEquals("List users", operation.summary)
        assertEquals("Returns a list of all users in the system", operation.description)
        assertEquals("listUsers", operation.operationId)
    }

    @Test
    fun testOperationWithSingleTag() {
        val operationBuilder = OperationBuilder()
        operationBuilder.tags("users")

        val operation = operationBuilder.build()

        assertNotNull(operation.tags)
        assertEquals(1, operation.tags?.size)
        assertEquals("users", operation.tags?.get(0))
    }

    @Test
    fun testOperationWithMultipleTags() {
        val operationBuilder = OperationBuilder()
        operationBuilder.tags("users", "admin", "api-v1")

        val operation = operationBuilder.build()

        assertNotNull(operation.tags)
        assertEquals(3, operation.tags?.size)
        assertEquals(listOf("users", "admin", "api-v1"), operation.tags)
    }

    @Test
    fun testOperationWithParameters() {
        val operationBuilder = OperationBuilder()
        operationBuilder.parameter("id", ParameterLocation.PATH, PropertyType.INTEGER, true, "User ID")
        operationBuilder.parameter(
            "includeDeleted",
            ParameterLocation.QUERY,
            PropertyType.BOOLEAN,
            false,
            "Include deleted users",
        )
        operationBuilder.parameter("X-API-Key", ParameterLocation.HEADER, PropertyType.STRING, true, "API Key")

        val operation = operationBuilder.build()

        assertNotNull(operation.parameters)
        assertEquals(3, operation.parameters?.size)

        val pathParam = operation.parameters?.find { it.name == "id" }
        assertNotNull(pathParam)
        assertEquals(ParameterLocation.PATH, pathParam.location)
        assertEquals(true, pathParam.required)
        assertEquals("User ID", pathParam.description)

        val queryParam = operation.parameters?.find { it.name == "includeDeleted" }
        assertNotNull(queryParam)
        assertEquals(ParameterLocation.QUERY, queryParam.location)
        assertEquals(false, queryParam.required)

        val headerParam = operation.parameters?.find { it.name == "X-API-Key" }
        assertNotNull(headerParam)
        assertEquals(ParameterLocation.HEADER, headerParam.location)
    }

    @Test
    fun testOperationWithParameterFormats() {
        val operationBuilder = OperationBuilder()
        operationBuilder.parameter(
            "date",
            ParameterLocation.QUERY,
            PropertyType.STRING,
            false,
            "Date parameter",
            SchemaFormat.DATE_TIME,
        )
        operationBuilder.parameter(
            "timestamp",
            ParameterLocation.QUERY,
            PropertyType.STRING,
            false,
            "Timestamp",
            SchemaFormat.DATE_TIME,
        )
        operationBuilder.parameter(
            "userId",
            ParameterLocation.QUERY,
            PropertyType.INTEGER,
            false,
            "User ID",
            SchemaFormat.INT64,
        )

        val operation = operationBuilder.build()

        assertNotNull(operation.parameters)
        assertEquals(3, operation.parameters?.size)

        assertEquals(
            SchemaFormat.DATE_TIME,
            operation.parameters
                ?.find { it.name == "date" }
                ?.schema
                ?.format,
        )
        assertEquals(
            SchemaFormat.DATE_TIME,
            operation.parameters
                ?.find { it.name == "timestamp" }
                ?.schema
                ?.format,
        )
        assertEquals(
            SchemaFormat.INT64,
            operation.parameters
                ?.find { it.name == "userId" }
                ?.schema
                ?.format,
        )
    }

    @Test
    fun testOperationWithRequestBody() {
        val operationBuilder = OperationBuilder()
        operationBuilder.requestBody {
            required = true
            description = "User data"
            jsonContent("User")
        }

        val operation = operationBuilder.build()

        assertNotNull(operation.requestBody)
        assertEquals(true, operation.requestBody?.required)
        assertEquals("User data", operation.requestBody?.description)
    }

    @Test
    fun testOperationWithResponses() {
        val operationBuilder = OperationBuilder()
        operationBuilder.response("200", "Success") {
            jsonContent("UserList")
        }
        operationBuilder.response("400", "Bad Request")
        operationBuilder.response("401", "Unauthorized")
        operationBuilder.response("404", "Not Found")

        val operation = operationBuilder.build()

        assertEquals(4, operation.responses.size)
        assertNotNull(operation.responses["200"])
        assertEquals("Success", operation.responses["200"]?.description)
        assertNotNull(operation.responses["400"])
        assertEquals("Bad Request", operation.responses["400"]?.description)
    }

    @Test
    fun testOperationWithSecurity() {
        val operationBuilder = OperationBuilder()
        operationBuilder.security("bearerAuth")
        operationBuilder.security("oauth2", "read:users", "write:users")

        val operation = operationBuilder.build()

        assertNotNull(operation.security)
        assertEquals(2, operation.security?.size)

        val bearerAuth = operation.security?.find { it.containsKey("bearerAuth") }
        assertNotNull(bearerAuth)
        assertEquals(0, bearerAuth["bearerAuth"]?.size)

        val oauth2 = operation.security?.find { it.containsKey("oauth2") }
        assertNotNull(oauth2)
        assertEquals(listOf("read:users", "write:users"), oauth2["oauth2"])
    }

    @Test
    fun testCompleteOperation() {
        val operationBuilder = OperationBuilder()
        operationBuilder.summary = "Update user"
        operationBuilder.description = "Updates an existing user by ID"
        operationBuilder.operationId = "updateUser"
        operationBuilder.tags("users", "admin")
        operationBuilder.parameter("id", ParameterLocation.PATH, PropertyType.INTEGER, true, "User ID")
        operationBuilder.requestBody {
            required = true
            jsonContent("User")
        }
        operationBuilder.response("200", "User updated") {
            jsonContent("User")
        }
        operationBuilder.response("404", "User not found")
        operationBuilder.security("bearerAuth")

        val operation = operationBuilder.build()

        assertEquals("Update user", operation.summary)
        assertEquals("Updates an existing user by ID", operation.description)
        assertEquals("updateUser", operation.operationId)
        assertEquals(2, operation.tags?.size)
        assertEquals(1, operation.parameters?.size)
        assertNotNull(operation.requestBody)
        assertEquals(2, operation.responses.size)
        assertEquals(1, operation.security?.size)
    }

    @Test
    fun testOperationBuilderChaining() {
        val operation =
            OperationBuilder()
                .apply {
                    summary = "Create user"
                    operationId = "createUser"
                    tags("users")
                    requestBody {
                        required = true
                        jsonContent("User")
                    }
                    response("201", "Created")
                    security("apiKey")
                }.build()

        assertEquals("Create user", operation.summary)
        assertEquals("createUser", operation.operationId)
        assertNotNull(operation.tags)
        assertNotNull(operation.requestBody)
        assertEquals(1, operation.responses.size)
        assertNotNull(operation.security)
    }

    @Test
    fun testOperationWithEmptyCollections() {
        val operationBuilder = OperationBuilder()
        // Add and then check that empty collections are not included

        val operation = operationBuilder.build()

        assertNull(operation.tags)
        assertNull(operation.parameters)
        assertNull(operation.security)
    }

    @Test
    fun testOperationTagsAccumulation() {
        val operationBuilder = OperationBuilder()
        operationBuilder.tags("tag1")
        operationBuilder.tags("tag2", "tag3")
        operationBuilder.tags("tag4")

        val operation = operationBuilder.build()

        assertNotNull(operation.tags)
        assertEquals(4, operation.tags?.size)
        assertEquals(listOf("tag1", "tag2", "tag3", "tag4"), operation.tags)
    }

    @Test
    fun testOperationParametersAccumulation() {
        val operationBuilder = OperationBuilder()
        operationBuilder.parameter("param1", ParameterLocation.QUERY, PropertyType.STRING)
        operationBuilder.parameter("param2", ParameterLocation.QUERY, PropertyType.INTEGER)
        operationBuilder.parameter("param3", ParameterLocation.HEADER, PropertyType.STRING)

        val operation = operationBuilder.build()

        assertNotNull(operation.parameters)
        assertEquals(3, operation.parameters?.size)
    }

    @Test
    fun testOperationSecurityAccumulation() {
        val operationBuilder = OperationBuilder()
        operationBuilder.security("scheme1")
        operationBuilder.security("scheme2", "scope1")
        operationBuilder.security("scheme3", "scope1", "scope2", "scope3")

        val operation = operationBuilder.build()

        assertNotNull(operation.security)
        assertEquals(3, operation.security?.size)
    }
}
