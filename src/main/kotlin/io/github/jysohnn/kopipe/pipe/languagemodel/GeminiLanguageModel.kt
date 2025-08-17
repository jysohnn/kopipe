package io.github.jysohnn.kopipe.pipe.languagemodel

import com.fasterxml.jackson.databind.ObjectMapper
import io.github.jysohnn.kopipe.objectmapper.defaultObjectMapper
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.util.concurrent.TimeUnit

class GeminiLanguageModel(
    val model: String = "gemini-2.5-flash",
    private val apiKey: String = System.getenv("GEMINI_API_KEY")
        ?: throw IllegalArgumentException("GEMINI_API_KEY not exist.")
) : LanguageModel() {

    private val httpClient = OkHttpClient.Builder()
        .readTimeout(1, TimeUnit.MINUTES)
        .build()

    private val objectMapper: ObjectMapper = defaultObjectMapper

    override fun execute(input: String): String {
        try {
            val requestObject = GeminiLanguageModelRequest(
                contents = listOf(
                    GeminiLanguageModelRequest.Content(
                        parts = listOf(
                            GeminiLanguageModelRequest.Content.Part(
                                text = input
                            )
                        )
                    )
                )
            )
            val requestBody = objectMapper.writeValueAsString(requestObject).toRequestBody()

            val request = Request.Builder()
                .url("https://generativelanguage.googleapis.com/v1beta/models/$model:generateContent")
                .addHeader("Content-Type", "application/json")
                .addHeader("x-goog-api-key", apiKey)
                .post(requestBody).build()
            val response = httpClient.newCall(request).execute()
            val responseObject = objectMapper.readValue(
                response.body.string(),
                GeminiLanguageModelResponse::class.java
            )

            return responseObject.candidates[0].content.parts[0].text
        } catch (throwable: Throwable) {
            return "Error: ${throwable.message}"
        }
    }

    private data class GeminiLanguageModelRequest(
        val contents: List<Content>
    ) {
        data class Content(
            val parts: List<Part>
        ) {
            data class Part(
                val text: String
            )
        }
    }

    private data class GeminiLanguageModelResponse(
        val candidates: List<Candidate>
    ) {
        data class Candidate(
            val content: Content
        ) {
            data class Content(
                val parts: List<Part>
            ) {
                data class Part(
                    val text: String
                )
            }
        }
    }
}