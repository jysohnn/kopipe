# Kopipe

A Kotlin-based pipeline library for developing LLM agents. Features intuitive input/output connections through the `-`
operator overloading of the `Pipe` class.

## Key Features

- **Pipeline Connection**: Easily connect multiple components using the `-` operator
- **Language Model Integration**: Support for various LLMs including OpenAI API
- **Tool System**: Various tools for file system operations, knowledge stores, and more
- **Type Safety**: Type-safe pipelines leveraging Kotlin's generics

## Installation

### Gradle (Kotlin DSL)

```kotlin
dependencies {
    implementation("io.github.jysohnn:kopipe:0.0.1-SNAPSHOT")
}
```

## Environment Setup

To use OpenAI API, set the environment variable:

```bash
export OPENAI_API_KEY="your-api-key-here"
```

## Basic Usage

### 1. Simple Pipeline

```kotlin
import io.github.jysohnn.kopipe.pipe.languagemodel.OpenAILanguageModel

// Create OpenAI language model
val llm = OpenAILanguageModel(model = "gpt-4")

// Execute pipeline
val result = llm.execute("Hello, please explain Kotlin programming language.")
println(result)
```

### 2. Pipeline Chaining (- operator)

```kotlin
// Connect multiple pipes to create complex workflows
val pipeline = preprocessor - llm - postprocessor

// Execute connected pipeline
val result = pipeline.execute(input)
```

### 3. Using Tools

```kotlin
import io.github.jysohnn.kopipe.tool.shell.*

// File system tools
val toolBox = ShellToolBox.getAll()

// File reading tool
val readTool = ShellReadTool()
val fileContent = readTool.invoke("""{"path": "/path/to/file.txt"}""")

// File writing tool
val writeTool = ShellWriteTool()
writeTool.invoke("""{"path": "/path/to/output.txt", "content": "Hello, World!"}""")
```

### 4. Context Management

```kotlin
import io.github.jysohnn.kopipe.context.*

// Create message context
val context = Context()
context.append(Message(Role.USER, "Question content"))
context.append(Message(Role.ASSISTANT, "Answer content"))

println(context.toString()) // Print entire conversation
```

## Core Components

### Pipe

The abstract base class for all components:

```kotlin
abstract class Pipe<I, O> {
    // Pipe connection through - operator overloading
    operator fun <T> minus(pipe: Pipe<O, T>): Pipe<I, T>

    // Actual processing logic
    abstract fun execute(input: I): O
}
```

### ContextAwareLanguageModel

A wrapper for language models that maintains conversation context:

```kotlin
class ContextAwareLanguageModel(
    val languageModel: LanguageModel
) : LanguageModel() {
    val context: Context = Context()

    override fun execute(input: String): String
}
```

### OpenAIEmbeddingVectorStore

A knowledge store that uses OpenAI embeddings for semantic similarity:

```kotlin
class OpenAIEmbeddingVectorStore(
    val model: String,
    private val apiKey: String,
    private val isPossibleToRepeatRetrieving: Boolean
) : KnowledgeStore
```

### ToolSelector

An intelligent tool selection system that uses language models to choose appropriate tools:

```kotlin
class ToolSelector(
    val languageModel: LanguageModel,
    private val tools: List<Tool>
) {
    fun select(context: Context, input: String): Output
}
```

### Available Tools

- **ShellReadTool**
    - Reads the contents of a specified file and returns it as a string
    - Input: `{"fileName": "file.txt"}`
    - Executes: `cat <filename>`

- **ShellWriteTool**
    - Writes the specified content to a file, overwriting existing content
    - Input: `{"fileName": "file.txt", "fileContent": "Hello World"}`
    - Executes: `echo <content> > <filename>`

- **ShellCopyTool**
    - Creates a copy of a file with a new name
    - Input: `{"fileName": "file.txt", "copyFileName": "file_copy.txt"}`
    - Executes: `cp <source> <destination>`

- **ShellMoveTool**
    - Renames or moves a file to a new location/name
    - Input: `{"fileName": "file.txt", "newFileName": "new_file.txt"}`
    - Executes: `mv <oldname> <newname>`

- **ShellListTool**
    - Lists all files and directories in the current directory
    - Input: `{}`
    - Executes: `ls`

- **ShellTouchTool**
    - Creates a new empty file with the specified name
    - Input: `{"fileName": "new_file.txt"}`
    - Executes: `touch <filename>`

## Requirements

- Kotlin 2.1.20+
- JVM 21+
- OpenAI API Key (when using OpenAI language model)

## License

This project is currently under development.