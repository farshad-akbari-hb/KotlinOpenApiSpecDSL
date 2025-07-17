package me.farshad.dsl.builder.info

import me.farshad.dsl.spec.Contact
import me.farshad.dsl.spec.Info
import me.farshad.dsl.spec.License

class InfoBuilder {
    lateinit var title: String
    lateinit var version: String
    var description: String? = null
    var termsOfService: String? = null
    private var contact: Contact? = null
    private var license: License? = null

    fun contact(block: ContactBuilder.() -> Unit) {
        contact = ContactBuilder().apply(block).build()
    }

    fun license(name: String, url: String? = null) {
        license = License(name, url)
    }

    fun build() = Info(title, version, description, termsOfService, contact, license)
}