package io.github.jysohnn.kopipe.tool.shell

import io.github.jysohnn.kopipe.tool.Tool

class ShellWriteTool : Tool(
    name = "shell_write",
    description = "Write **fileContent** to the file with the given **fileName**",
    inputExample = Input(fileName = "file.txt", fileContent = "This is the file content."),
    outputExample = "The contents of file.txt have been updated."
) {
    class Input(val fileName: String, val fileContent: String)

    override fun invoke(input: String): String {
        try {
            val inputObject = objectMapper.readValue(
                input,
                Input::class.java
            )

            val process = ProcessBuilder("zsh", "-c",
                """|cat <<'EOF' > ${inputObject.fileName}
                   |${inputObject.fileContent}
                   |EOF
                """.trimMargin()
            ).redirectErrorStream(true).start()

            process.waitFor()

            return "The contents of ${inputObject.fileName} have been updated."
        } catch (throwable: Throwable) {
            return "Error: ${throwable.message}"
        }
    }
}