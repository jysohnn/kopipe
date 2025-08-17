package io.github.jysohnn.kopipe.knowledge

import kotlin.math.sqrt

abstract class EmbeddingVectorStore : KnowledgeStore {
    class EmbeddingVector(
        val vector: List<Double>,
        val text: String
    )

    private val embeddingVectors: MutableList<EmbeddingVector> = mutableListOf()

    override fun store(knowledge: List<String>) {
        val embeddingVectors = toEmbeddingVectors(texts = knowledge)?.mapIndexed { index, vector ->
            EmbeddingVector(
                vector = vector,
                text = knowledge[index]
            )
        } ?: emptyList()

        this.embeddingVectors.addAll(embeddingVectors)
    }

    override fun retrieve(query: String, minSimilarity: Double): String? {
        val queryVector = toEmbeddingVector(text = query) ?: return null
        val mostSimilarVector = this.embeddingVectors.maxBy {
            calculateCosineSimilarity(queryVector, it.vector)
        }

        if (calculateCosineSimilarity(queryVector, mostSimilarVector.vector) < minSimilarity) return null

        return mostSimilarVector.text
    }

    private fun toEmbeddingVector(text: String): List<Double>? {
        return toEmbeddingVectors(listOf(text))?.get(0)
    }

    protected abstract fun toEmbeddingVectors(texts: List<String>): List<List<Double>>?

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
}