package io.github.jysohnn.kopipe.pipe.languagemodel

import io.github.jysohnn.kopipe.objectmapper.objectMapper
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.util.concurrent.TimeUnit

class OpenAILanguageModel(
    val model: String = "gpt-4.1",
    private val apiKey: String = System.getenv("OPENAI_API_KEY")
        ?: throw IllegalArgumentException("OPENAI_API_KEY not exist.")
) : LanguageModel() {

    private val httpClient = OkHttpClient.Builder()
        .readTimeout(1, TimeUnit.MINUTES)
        .build()

    override fun execute(input: String): String {
        try {
            val requestObject = OpenAILanguageModelRequest(
                model = model,
                input = input
            )
            val requestBody = objectMapper.writeValueAsString(requestObject).toRequestBody()

            val request = Request.Builder()
                .url("https://api.openai.com/v1/responses")
                .addHeader("Content-Type", "application/json")
                .addHeader("Authorization", "Bearer $apiKey")
                .post(requestBody).build()
            val response = httpClient.newCall(request).execute()
            val responseObject = objectMapper.readValue(
                response.body.string(),
                OpenAILanguageModelResponse::class.java
            )

            return responseObject.output[0].content[0].text
        } catch (throwable: Throwable) {
            return "Error: ${throwable.message}"
        }
    }

    private data class OpenAILanguageModelRequest(
        val model: String,
        val input: String
    )

    private data class OpenAILanguageModelResponse(
        val output: List<Output>
    ) {
        data class Output(
            val content: List<Content>
        ) {
            data class Content(
                val text: String
            )
        }
    }
}