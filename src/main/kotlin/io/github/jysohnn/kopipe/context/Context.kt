package io.github.jysohnn.kopipe.context

class Context(
    private val messages: MutableList<Message> = mutableListOf()
) {

    fun append(message: Message) {
        this.messages.add(message)
    }

    fun isNotEmpty(): Boolean {
        return messages.isNotEmpty()
    }

    fun contains(message: Message): Boolean {
        return messages.toSet().contains(message)
    }

    override fun toString(): String {
        return messages.joinToString("\n")
    }
}