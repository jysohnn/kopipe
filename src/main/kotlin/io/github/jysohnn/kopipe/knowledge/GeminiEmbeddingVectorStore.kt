package io.github.jysohnn.kopipe.knowledge

import io.github.jysohnn.kopipe.objectmapper.objectMapper
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.util.concurrent.TimeUnit

class GeminiEmbeddingVectorStore(
    val model: String = "gemini-embedding-001",
    private val apiKey: String = System.getenv("GEMINI_API_KEY")
        ?: throw IllegalArgumentException("GEMINI_API_KEY not exist.")
) : EmbeddingVectorStore() {

    private val httpClient = OkHttpClient.Builder()
        .readTimeout(1, TimeUnit.MINUTES)
        .build()

    override fun toEmbeddingVectors(texts: List<String>): List<List<Double>>? {
        if (texts.isEmpty()) return emptyList()

        try {
            val vectors: MutableList<List<Double>> = mutableListOf()

            texts.chunked(100).forEach { chunk ->
                val requestObject = GeminiEmbeddingRequest(
                    requests = chunk.map {
                        GeminiEmbeddingRequest.Request(
                            model = "models/$model",
                            content = GeminiEmbeddingRequest.Request.Content(
                                parts = listOf(
                                    GeminiEmbeddingRequest.Request.Content.Part(
                                        text = it
                                    )
                                )
                            )
                        )
                    }
                )
                val requestBody = objectMapper.writeValueAsString(requestObject).toRequestBody()

                val request = Request.Builder()
                    .url("https://generativelanguage.googleapis.com/v1beta/models/$model:batchEmbedContents")
                    .addHeader("Content-Type", "application/json")
                    .addHeader("x-goog-api-key", apiKey)
                    .post(requestBody).build()

                val response = httpClient.newCall(request).execute()
                val responseObject = objectMapper.readValue(
                    response.body.string(),
                    GeminiEmbeddingResponse::class.java
                )

                vectors.addAll(responseObject.embeddings.map { it.values })
            }

            return vectors
        } catch (_: Throwable) {
            return null
        }
    }

    private data class GeminiEmbeddingRequest(
        val requests: List<Request>
    ) {
        data class Request(
            val model: String,
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

    private data class GeminiEmbeddingResponse(
        val embeddings: List<Embedding>
    ) {
        data class Embedding(
            val values: List<Double>
        )
    }
}