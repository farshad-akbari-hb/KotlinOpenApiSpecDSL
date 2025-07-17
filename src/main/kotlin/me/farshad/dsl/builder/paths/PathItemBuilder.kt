package me.farshad.dsl.builder.paths

import me.farshad.dsl.spec.Operation
import me.farshad.dsl.spec.PathItem

class PathItemBuilder {
    private var get: Operation? = null
    private var post: Operation? = null
    private var put: Operation? = null
    private var delete: Operation? = null
    private var patch: Operation? = null

    fun get(block: OperationBuilder.() -> Unit) {
        get = OperationBuilder().apply(block).build()
    }

    fun post(block: OperationBuilder.() -> Unit) {
        post = OperationBuilder().apply(block).build()
    }

    fun put(block: OperationBuilder.() -> Unit) {
        put = OperationBuilder().apply(block).build()
    }

    fun delete(block: OperationBuilder.() -> Unit) {
        delete = OperationBuilder().apply(block).build()
    }

    fun patch(block: OperationBuilder.() -> Unit) {
        patch = OperationBuilder().apply(block).build()
    }

    fun build() = PathItem(get, post, put, delete, patch)
}