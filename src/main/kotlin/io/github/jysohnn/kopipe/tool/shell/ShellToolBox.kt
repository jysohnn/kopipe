package io.github.jysohnn.kopipe.tool.shell

import io.github.jysohnn.kopipe.tool.Tool

class ShellToolBox {
    companion object {
        fun getAll(): List<Tool> = listOf(
            ShellCopyTool(),
            ShellListTool(),
            ShellMoveTool(),
            ShellReadTool(),
            ShellTouchTool(),
            ShellWriteTool()
        )
    }
}