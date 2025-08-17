package io.github.jysohnn.kopipe.context

data class Message(
    val role: Role,
    val text: String
) {

    override fun toString(): String {
        return "[$role]\n$text\n"
    }
}