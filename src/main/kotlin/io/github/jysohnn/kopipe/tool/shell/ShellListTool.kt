package io.github.jysohnn.kopipe.tool.shell

import io.github.jysohnn.kopipe.tool.Tool

class ShellListTool : Tool(
    name = "shell_list",
    description = "List the files in the current directory.",
    inputExample = Input(),
    outputExample = "The files in the current directory are as follows:\nfile_1.txt file_2.txt file_3.txt.",
    isUserConsentRequired = false
) {
    class Input

    override fun invoke(input: String): String {
        try {
            val process = ProcessBuilder("zsh", "-c", "ls")
                .redirectErrorStream(true)
                .start()

            val result = process.inputStream.bufferedReader().readText()
            process.waitFor()

            return "The files in the current directory are as follows:\n$result"
        } catch (throwable: Throwable) {
            return "Error: ${throwable.message}"
        }
    }
}