package io.github.jysohnn.kopipe.knowledge

import com.fasterxml.jackson.databind.ObjectMapper
import io.github.jysohnn.kopipe.objectmapper.defaultObjectMapper
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

    private val objectMapper: ObjectMapper = defaultObjectMapper

    override fun toEmbeddingVectors(texts: List<String>): List<List<Double>>? {
        if (texts.isEmpty()) return emptyList()

        try {
            val requestObject = OpenAIEmbeddingRequest(
                model = model,
                encodingFormat = "float",
                input = texts
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

            return responseObject.data.map { it.embedding }
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