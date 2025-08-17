package io.github.jysohnn.kopipe.tool

import com.fasterxml.jackson.databind.ObjectMapper
import io.github.jysohnn.kopipe.objectmapper.defaultObjectMapper

abstract class Tool(
    val name: String,
    val description: String,
    val inputExample: Any,
    val outputExample: String
) {

    protected val objectMapper: ObjectMapper = defaultObjectMapper

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