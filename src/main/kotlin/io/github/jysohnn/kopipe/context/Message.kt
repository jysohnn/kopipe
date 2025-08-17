package io.github.jysohnn.kopipe.context

class Message(
    val role: Role,
    val text: String
) {

    fun copy(): Message {
        return Message(
            role = this.role,
            text = this.text
        )
    }

    override fun toString(): String {
        return "[$role]\n$text\n"
    }
}