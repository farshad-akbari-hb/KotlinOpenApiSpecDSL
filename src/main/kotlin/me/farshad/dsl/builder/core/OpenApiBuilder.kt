package me.farshad.dsl.builder.core

import me.farshad.dsl.builder.components.ComponentsBuilder
import me.farshad.dsl.builder.info.InfoBuilder
import me.farshad.dsl.builder.info.ServerBuilder
import me.farshad.dsl.builder.paths.PathsBuilder
import me.farshad.dsl.spec.Components
import me.farshad.dsl.spec.Info
import me.farshad.dsl.spec.OpenApiSpec
import me.farshad.dsl.spec.PathItem
import me.farshad.dsl.spec.Server

class OpenApiBuilder {
    var openapi: String = "3.1.0"
    private lateinit var info: Info
    private val servers = mutableListOf<Server>()
    private val paths = mutableMapOf<String, PathItem>()
    private var components: Components? = null

    fun info(block: InfoBuilder.() -> Unit) {
        info = InfoBuilder().apply(block).build()
    }

    fun server(
        url: String,
        block: ServerBuilder.() -> Unit = {},
    ) {
        servers.add(ServerBuilder(url).apply(block).build())
    }

    fun paths(block: PathsBuilder.() -> Unit) {
        PathsBuilder(paths).apply(block)
    }

    fun components(block: ComponentsBuilder.() -> Unit) {
        components = ComponentsBuilder().apply(block).build()
    }

    fun build() =
        OpenApiSpec(
            openapi = openapi,
            info = info,
            servers = servers,
            paths = paths,
            components = components,
        )
}

// DSL entry point
fun openApi(block: OpenApiBuilder.() -> Unit): OpenApiSpec = OpenApiBuilder().apply(block).build()
