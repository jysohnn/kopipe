package io.github.jysohnn.kopipe.tool

import com.fasterxml.jackson.databind.ObjectMapper
import io.github.jysohnn.kopipe.context.Context
import io.github.jysohnn.kopipe.context.Role
import io.github.jysohnn.kopipe.objectmapper.defaultObjectMapper
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

    private val objectMapper: ObjectMapper = defaultObjectMapper

    fun select(
        input: String,
        context: Context,
        toolContext: Context? = null,
        knowledgeContext: Context? = null
    ): Output {
        val inputWithContext = """
            |# System Instruction
            |- You are a **Tool Usage Planner**.
            |- Your task is to decide whether to use a tool from the [Tool List] in order to handle the user's latest request.
            |- To make this decision, carefully analyze the following inputs: [Conversation History], [Knowledge History], [Tool History], [User's Latest Request]
            |
            |- If you decide to use a tool:
            |- Select the most appropriate tool from the [Tool List].
            |- Plan the exact input parameters for that tool.
            |- Output your decision strictly in the JSON schema provided.
            |- If you decide **not** to use any tool, output exactly "null"
            |- Below is an output example:
            |${
            objectMapper.writeValueAsString(
                LanguageModelOutput(
                    name = "tool_name",
                    input = mapOf("key1" to "value1", "key2" to "value2")
                )
            )
        }
            |
            |- Always respond in the same language that the user uses.
            |- The roles in the history represent the following:
            |${Role.entries.joinToString("\n") { it.getSpecification() }}
            |
            |- The available [Tool List] is as follows. Refer to the **Input Example** and write the JSON.
            |- Do not translate the JSON arbitrarily—write it in English exactly as in the input example.
            |- Never output free text, additional explanations, or Markdown. Any characters other than JSON are prohibited.
            |
            |# Tool List
            |${tools.joinToString("\n") { it.getSpecification() }}
            |
            |# Knowledge History
            |- This section contains additional knowledge or reference information retrieved based on the user's requests.  
            |- It may include factual data, background explanations, or domain-specific knowledge that can assist in answering the request.  
            |${if (knowledgeContext?.isNotEmpty() == true) knowledgeContext.distinct() else "None."}
            |
            |# Conversation History
            |- This section records the dialogue between the USER and the ASSISTANT up to this point.  
            |- It provides the conversational context, including the user’s previous questions and the assistant’s answers.  
            |$context
            |
            |# Tool History
            |- This section lists the tools that have been invoked to fulfill the user's request, along with their execution results.  
            |- It shows what external actions were taken (e.g., database query, API call, or command execution) and their outcomes.  
            |${if (toolContext?.isNotEmpty() == true) toolContext else "None."}
            |
            |# User's Latest Request
            |- This is the **most recent user request** that you must answer, using all of the context provided above.
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