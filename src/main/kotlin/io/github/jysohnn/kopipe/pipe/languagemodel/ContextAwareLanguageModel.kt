package io.github.jysohnn.kopipe.pipe.languagemodel

import io.github.jysohnn.kopipe.context.Context
import io.github.jysohnn.kopipe.context.Message
import io.github.jysohnn.kopipe.context.Role

class ContextAwareLanguageModel(
    val languageModel: LanguageModel,
    val context: Context = Context(),
    val knowledgeContext: Context? = null,
    val toolContext: Context? = null
) : LanguageModel() {

    override fun execute(input: String): String {
        this.context.append(Message(Role.USER, input))

        val inputWithContext = """
            |# System Instruction
            |- You are the **${Role.ASSISTANT}**, responsible for resolving the user's requests.
            |- Answer the **User's Latest Request** (shown at the very bottom) based on the [Conversation History], [Knowledge History], and [Tool History] provided below.
            |- Always respond in the same language that the user uses.
            |- The roles in the history represent the following:
            |${Role.entries.joinToString("\n") { it.getSpecification() }}
            |
            |# Knowledge History
            |- This section contains additional knowledge or reference information retrieved based on the user's requests.  
            |- It may include factual data, background explanations, or domain-specific knowledge that can assist in answering the request.  
            |${if (knowledgeContext?.isNotEmpty() == true) knowledgeContext else "None."}
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
        this.context.append(Message(Role.ASSISTANT, output))

        return output
    }
}