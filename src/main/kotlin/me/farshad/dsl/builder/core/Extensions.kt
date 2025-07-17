package me.farshad.dsl.builder.core

import com.charleskorn.kaml.Yaml
import com.charleskorn.kaml.YamlConfiguration
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import me.farshad.dsl.serializer.yamlSerializersModule
import me.farshad.dsl.spec.OpenApiSpec

// Extension function to convert to JSON using kotlinx.serialization
fun OpenApiSpec.toJson(): String {
    val json = Json {
        prettyPrint = true
        encodeDefaults = false
        explicitNulls = false
    }
    return json.encodeToString(this)
}

// Extension function to convert to YAML using kaml
fun OpenApiSpec.toYaml(): String {
    val yaml = Yaml(
        configuration = YamlConfiguration(
            encodeDefaults = false,
            strictMode = false,
            breakScalarsAt = 80
        ),
        serializersModule = yamlSerializersModule
    )
    return yaml.encodeToString(this)
}