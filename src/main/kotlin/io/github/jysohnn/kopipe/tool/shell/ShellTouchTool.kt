package io.github.jysohnn.kopipe.tool.shell

import io.github.jysohnn.kopipe.tool.Tool

class ShellTouchTool : Tool(
    name = "shell_touch",
    description = "Create an empty file with the given **fileName**.",
    inputExample = Input(fileName = "new_file.txt"),
    outputExample = "Created an empty file named new_file.txt."
) {
    class Input(val fileName: String)

    override fun invoke(input: String): String {
        try {
            val inputObject = objectMapper.readValue(
                input,
                Input::class.java
            )

            val process = ProcessBuilder("zsh", "-c", "touch ${inputObject.fileName}")
                .redirectErrorStream(true)
                .start()

            process.waitFor()

            return "Created an empty file named ${inputObject.fileName}."
        } catch (throwable: Throwable) {
            return "Error: ${throwable.message}"
        }
    }
}