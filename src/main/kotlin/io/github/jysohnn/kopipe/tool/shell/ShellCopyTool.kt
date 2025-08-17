package io.github.jysohnn.kopipe.tool.shell

import io.github.jysohnn.kopipe.objectmapper.objectMapper
import io.github.jysohnn.kopipe.tool.Tool

class ShellCopyTool : Tool(
    name = "shell_copy",
    description = "Copy the **fileName** file and create a new file named **copyFileName**.",
    inputExample = Input(fileName = "file.txt", copyFileName = "file_copy.txt"),
    outputExample = "Copied **fileName** and created a new file named **copyFileName**."
) {
    class Input(val fileName: String, val copyFileName: String)

    override fun invoke(input: String): String {
        try {
            val inputObject = objectMapper.readValue(
                input,
                Input::class.java
            )

            val process = ProcessBuilder("zsh", "-c", "cp ${inputObject.fileName} ${inputObject.copyFileName}")
                .redirectErrorStream(true)
                .start()

            process.waitFor()

            return "Copied ${inputObject.fileName} and created a new file named ${inputObject.copyFileName}."
        } catch (throwable: Throwable) {
            return "Error: ${throwable.message}"
        }
    }
}