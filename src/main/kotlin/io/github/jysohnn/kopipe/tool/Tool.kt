package io.github.jysohnn.kopipe.tool

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.registerKotlinModule

abstract class Tool(
    val name: String,
    val description: String,
    val inputExample: Any,
    val outputExample: String
) {

    protected val objectMapper: ObjectMapper = ObjectMapper()
        .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
        .configure(DeserializationFeature.FAIL_ON_NULL_FOR_PRIMITIVES, true)
        .configure(DeserializationFeature.READ_UNKNOWN_ENUM_VALUES_USING_DEFAULT_VALUE, true)
        .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
        .registerModule(JavaTimeModule())
        .registerKotlinModule()
        .setPropertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE)

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