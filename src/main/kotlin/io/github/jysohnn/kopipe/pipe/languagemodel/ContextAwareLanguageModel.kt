package io.github.jysohnn.kopipe.pipe.languagemodel

import io.github.jysohnn.kopipe.context.Context
import io.github.jysohnn.kopipe.context.Role

class ContextAwareLanguageModel(
    val languageModel: LanguageModel
) : LanguageModel() {
    val context: Context = Context()

    override fun execute(input: String): String {
        val inputWithContext = """
            |# System Instruction
            |- You are the **${Role.ASSISTANT}**, responsible for resolving the user's requests.
            |- Answer the most recent user request based on the [Conversation History] below.
            |- Always respond in the same language that the user uses.
            |- The roles in the [Conversation History] represent the following:
            |${Role.entries.joinToString("\n") { "\t- ${it.name}: ${it.description}" }}
            |
            |# Conversation History
            |$context
        """.trimMargin()

        val output = languageModel.execute(input = inputWithContext)

        return output
    }
}