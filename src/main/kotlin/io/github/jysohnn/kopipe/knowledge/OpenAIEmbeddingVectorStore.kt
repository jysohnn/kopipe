package io.github.jysohnn.kopipe.knowledge

import io.github.jysohnn.kopipe.objectmapper.objectMapper
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.util.concurrent.TimeUnit

class OpenAIEmbeddingVectorStore(
    val model: String = "text-embedding-ada-002",
    private val apiKey: String = System.getenv("OPENAI_API_KEY")
        ?: throw IllegalArgumentException("OPENAI_API_KEY not exist.")
) : EmbeddingVectorStore() {

    private val httpClient = OkHttpClient.Builder()
        .readTimeout(1, TimeUnit.MINUTES)
        .build()

    override fun toEmbeddingVectors(texts: List<String>): List<List<Double>>? {
        if (texts.isEmpty()) return emptyList()

        try {
            val vectors: MutableList<List<Double>> = mutableListOf()

            texts.chunked(100).forEach { chunk ->
                val requestObject = OpenAIEmbeddingRequest(
                    model = model,
                    encodingFormat = "float",
                    input = chunk
                )
                val requestBody = objectMapper.writeValueAsString(requestObject).toRequestBody()

                val request = Request.Builder()
                    .url("https://api.openai.com/v1/embeddings")
                    .addHeader("Content-Type", "application/json")
                    .addHeader("Authorization", "Bearer $apiKey")
                    .post(requestBody).build()

                val response = httpClient.newCall(request).execute()
                val responseObject = objectMapper.readValue(
                    response.body.string(),
                    OpenAIEmbeddingResponse::class.java
                )

                vectors.addAll(responseObject.data.map { it.embedding })
            }

            return vectors
        } catch (_: Throwable) {
            return null
        }
    }

    private data class OpenAIEmbeddingRequest(
        val model: String,
        val encodingFormat: String,
        val input: List<String>
    )

    private data class OpenAIEmbeddingResponse(
        val data: List<Data>
    ) {
        data class Data(
            val embedding: List<Double>
        )
    }
}