package io.github.jysohnn.kopipe.context

class Message(
    val role: Role,
    val text: String
) {
    override fun toString(): String {
        return "[$role]\n$text\n"
    }
}