package io.github.jysohnn.kopipe.pipe

abstract class Pipe<I, O> {

    class CompositePipe<I, T, O>(
        private val fromPipe: Pipe<I, T>,
        private val toPipe: Pipe<T, O>
    ) : Pipe<I, O>() {

        override fun execute(input: I): O {
            return this.toPipe.execute(input = this.fromPipe.execute(input = input))
        }
    }

    operator fun <T> minus(pipe: Pipe<O, T>): Pipe<I, T> {
        return CompositePipe(
            fromPipe = this,
            toPipe = pipe
        )
    }

    abstract fun execute(input: I): O
}