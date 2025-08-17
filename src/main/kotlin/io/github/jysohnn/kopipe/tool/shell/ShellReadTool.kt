package io.github.jysohnn.kopipe.tool.shell

import io.github.jysohnn.kopipe.objectmapper.objectMapper
import io.github.jysohnn.kopipe.tool.Tool

class ShellReadTool : Tool(
    name = "shell_read",
    description = "Read the contents of the file with the given **fileName**.",
    inputExample = Input(fileName = "file.txt"),
    outputExample = "The contents of file.txt are as follows:\nThis is the file contents.",
    isUserConsentRequired = false
) {
    class Input(val fileName: String)

    override fun invoke(input: String): String {
        try {
            val inputObject = objectMapper.readValue(
                input,
                Input::class.java
            )

            val process = ProcessBuilder("zsh", "-c", "cat ${inputObject.fileName}")
                .redirectErrorStream(true)
                .start()

            val result = process.inputStream.bufferedReader().readText()
            process.waitFor()

            return "The contents of ${inputObject.fileName} are as follows:\n$result"
        } catch (throwable: Throwable) {
            return "Error: ${throwable.message}"
        }
    }
}