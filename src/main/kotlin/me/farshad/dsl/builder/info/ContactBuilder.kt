package me.farshad.dsl.builder.info

import me.farshad.dsl.spec.Contact

class ContactBuilder {
    var name: String? = null
    var url: String? = null
    var email: String? = null

    fun build() = Contact(name, url, email)
}
