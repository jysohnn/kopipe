package io.github.jysohnn.kopipe.knowledge

interface KnowledgeStore {
    fun store(knowledge: List<String>)
    fun retrieve(query: String, minSimilarity: Double = -1.0): String?
}