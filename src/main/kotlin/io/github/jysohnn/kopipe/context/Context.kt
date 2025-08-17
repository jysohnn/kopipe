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

    fun distinct(): Context {
        val messagesCopy = this.messages.map { it.copy() }
            .distinct()
            .toMutableList()

        return Context(messages = messagesCopy)
    }

    override fun toString(): String {
        return messages.joinToString("\n")
    }
}