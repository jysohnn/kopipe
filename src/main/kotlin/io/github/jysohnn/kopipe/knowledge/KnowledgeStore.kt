package io.github.jysohnn.kopipe.knowledge

interface KnowledgeStore {
    fun addAll(knowledge: List<String>)
    fun retrieve(query: String, minSimilarity: Double = -1.0): String?
}