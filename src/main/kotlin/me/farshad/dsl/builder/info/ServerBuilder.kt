package me.farshad.dsl.builder.info

import me.farshad.dsl.spec.Server

class ServerBuilder(
    private val url: String,
) {
    var description: String? = null

    fun build() = Server(url, description)
}
