package io.github.jysohnn.kopipe.knowledge

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.util.concurrent.TimeUnit
import kotlin.math.sqrt

class OpenAIEmbeddingVectorStore(
    val model: String = "text-embedding-ada-002",
    private val apiKey: String = System.getenv("OPENAI_API_KEY")
        ?: throw IllegalArgumentException("API KEY not exist."),
    private val isPossibleToRepeatRetrieving: Boolean = true
) : KnowledgeStore {
    class EmbeddingVector(
        val vector: List<Double>,
        val text: String
    )

    private val httpClient = OkHttpClient.Builder()
        .readTimeout(1, TimeUnit.MINUTES)
        .build()

    private val objectMapper: ObjectMapper = ObjectMapper()
        .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
        .configure(DeserializationFeature.FAIL_ON_NULL_FOR_PRIMITIVES, true)
        .configure(DeserializationFeature.READ_UNKNOWN_ENUM_VALUES_USING_DEFAULT_VALUE, true)
        .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
        .registerModule(JavaTimeModule())
        .registerKotlinModule()
        .setPropertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE)

    private val embeddingVectors: MutableList<EmbeddingVector> = mutableListOf()
    private val retrievingHistory: MutableSet<String> = mutableSetOf()

    override fun addAll(knowledge: List<String>) {
        val embeddingVectors = toEmbeddingVectors(texts = knowledge).mapIndexed { index, vector ->
            EmbeddingVector(
                vector = vector,
                text = knowledge[index]
            )
        }

        this.embeddingVectors.addAll(embeddingVectors)
    }

    override fun retrieve(query: String, minSimilarity: Double): String? {
        val queryVector = toEmbeddingVector(text = query)
        val mostSimilarVector = this.embeddingVectors.maxBy {
            calculateCosineSimilarity(queryVector, it.vector)
        }

        if (!isPossibleToRepeatRetrieving && retrievingHistory.contains(mostSimilarVector.text)) return null
        if (calculateCosineSimilarity(queryVector, mostSimilarVector.vector) < minSimilarity) return null

        retrievingHistory.add(mostSimilarVector.text)
        return mostSimilarVector.text
    }

    private fun toEmbeddingVector(text: String): List<Double> {
        return toEmbeddingVectors(listOf(text))[0]
    }

    private fun toEmbeddingVectors(texts: List<String>): List<List<Double>> {
        if (texts.isEmpty()) return emptyList()

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
    }

    private fun calculateCosineSimilarity(a: List<Double>, b: List<Double>): Double {
        require(a.size == b.size)

        val dotProduct = a.zip(b).sumOf { (x, y) -> x * y }
        val magnitudeOfA = sqrt(a.sumOf { it * it })
        val magnitudeOfB = sqrt(b.sumOf { it * it })

        return if (magnitudeOfA == 0.0 || magnitudeOfB == 0.0) {
            0.0
        } else {
            dotProduct / (magnitudeOfA * magnitudeOfB)
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