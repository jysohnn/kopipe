package io.github.jysohnn.kopipe.tool.shell

import io.github.jysohnn.kopipe.tool.Tool

class ShellMoveTool : Tool(
    name = "shell_move",
    description = "Rename the **fileName** file to **newFileName**.",
    inputExample = Input(fileName = "file.txt", newFileName = "new_name_file.txt"),
    outputExample = "Renamed **fileName** to **newFileName**."
) {
    class Input(val fileName: String, val newFileName: String)

    override fun invoke(input: String): String {
        try {
            val inputObject = objectMapper.readValue(
                input,
                Input::class.java
            )

            val process = ProcessBuilder("zsh", "-c", "mv ${inputObject.fileName} ${inputObject.newFileName}")
                .redirectErrorStream(true)
                .start()

            process.waitFor()

            return "Renamed ${inputObject.fileName} to ${inputObject.newFileName}."
        } catch (throwable: Throwable) {
            return "Error: ${throwable.message}"
        }
    }
}