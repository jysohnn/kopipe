package io.github.jysohnn.kopipe.tool

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import io.github.jysohnn.kopipe.context.Context
import io.github.jysohnn.kopipe.context.Role
import io.github.jysohnn.kopipe.pipe.languagemodel.LanguageModel

class ToolSelector(
    val languageModel: LanguageModel,
    private val tools: List<Tool>
) {

    data class Output(
        val tool: Tool?,
        val input: String = ""
    )

    data class LanguageModelOutput(
        val name: String,
        val input: Any
    )

    private val objectMapper: ObjectMapper = ObjectMapper()
        .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
        .configure(DeserializationFeature.FAIL_ON_NULL_FOR_PRIMITIVES, true)
        .configure(DeserializationFeature.READ_UNKNOWN_ENUM_VALUES_USING_DEFAULT_VALUE, true)
        .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
        .registerModule(JavaTimeModule())
        .registerKotlinModule()
        .setPropertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE)

    fun select(context: Context, input: String): Output {
        val inputWithContext = """
            |# System Instruction
            |- You are a **Tool Usage Planner**. Analyze the given [Conversation History] and [User's Latest Request],
            |- decide whether to use a tool from the [Tool List],
            |- if using, plan which tool to call and with what arguments(input),
            |- and output the result **only** in the JSON schema below. If you will not use any tool, return "null" only.
            |   {"name": "**Tool Name**", input: {// JSON Object in the same format as the input example }}
            |
            |- Always respond in the same language that the user uses.
            |- The roles in the [Conversation History] represent the following:
            |${Role.entries.joinToString("\n") { "\t- ${it.name}: ${it.description}" }}
            |
            |- The available [Tool List] is as follows. Refer to the **Input Example** and write the JSON.
            |- Do not translate the JSON arbitrarily—write it in English exactly as in the input example.
            |- Never output free text, additional explanations, or Markdown. Any characters other than JSON are prohibited.
            |
            |# Tool List
            |${tools.joinToString("\n\n") { it.getSpecification() }}
            |
            |# Conversation History
            |$context
            |
            |# User's Latest Request
            |$input
        """.trimMargin()

        val output = languageModel.execute(input = inputWithContext)

        try {
            val outputObject = objectMapper.readValue(
                output,
                LanguageModelOutput::class.java
            )

            val selectedTool = tools.find { it.name == outputObject.name }!!

            return Output(
                tool = selectedTool,
                input = objectMapper.writeValueAsString(outputObject.input)
            )
        } catch (_: Throwable) {
            return Output(tool = null)
        }
    }
}