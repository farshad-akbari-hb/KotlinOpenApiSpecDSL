package me.farshad.dsl.builder.paths

import me.farshad.dsl.spec.PathItem

class PathsBuilder(
    private val paths: MutableMap<String, PathItem>,
) {
    fun path(
        path: String,
        block: PathItemBuilder.() -> Unit,
    ) {
        paths[path] = PathItemBuilder().apply(block).build()
    }
}
