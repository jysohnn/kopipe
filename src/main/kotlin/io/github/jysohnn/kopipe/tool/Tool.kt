package io.github.jysohnn.kopipe.tool

import io.github.jysohnn.kopipe.objectmapper.objectMapper

abstract class Tool(
    val name: String,
    val description: String,
    val inputExample: Any,
    val outputExample: String,
    val isUserConsentRequired: Boolean = true
) {

    abstract fun invoke(input: String): String

    fun getSpecification(): String {
        return """
            |Tool Name: $name
            |- Description: $description
            |- Input Example:
            |${objectMapper.writeValueAsString(inputExample)}
            |- Output Example:
            |$outputExample
        """.trimMargin()
    }
}