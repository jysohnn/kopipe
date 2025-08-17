# Kopipe

A Kotlin-based pipeline library for developing LLM agents. Features intuitive input/output connections through the `-`
operator overloading of the `Pipe` class.

## Key Features

- **Pipeline Connection**: Easily connect multiple components using the `-` operator
- **Language Model Integration**: Support for various LLMs including OpenAI, Gemini API
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

To use Gemini API, set the environment variable:

```bash
export GEMINI_API_KEY="your-api-key-here"
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

The abstract base class for all pipeline components that enables type-safe chaining through the `-` operator:

```kotlin
abstract class Pipe<I, O> {
    // Pipe connection through - operator overloading
    operator fun <T> minus(pipe: Pipe<O, T>): Pipe<I, T>

    // Actual processing logic
    abstract fun execute(input: I): O
}
```

### ContextAwareLanguageModel

A wrapper for language models that maintains conversation context and provides rich prompt templates with system instructions:

```kotlin
class ContextAwareLanguageModel(
    val languageModel: LanguageModel,
    val context: Context,
    val knowledgeContext: Context?,
    val toolContext: Context?
) : LanguageModel() {

    override fun execute(input: String): String
}
```

### EmbeddingVectorStore

An abstract base class for knowledge stores that use embedding vectors for semantic similarity search:

```kotlin
abstract class EmbeddingVectorStore : KnowledgeStore {
    override fun store(knowledge: List<String>)
    override fun retrieve(query: String, minSimilarity: Double): String?
    protected abstract fun toEmbeddingVectors(texts: List<String>): List<List<Double>>?
}
```

### Tool & ToolSelector

A sophisticated tool system with user consent management and intelligent selection:

```kotlin
abstract class Tool(
    val name: String,
    val description: String,
    val inputExample: Any,
    val outputExample: String,
    val isUserConsentRequired: Boolean
) {
    abstract fun invoke(input: String): String
}

class ToolSelector(
    val languageModel: LanguageModel,
    private val tools: List<Tool>
) {
    data class Output(val tool: Tool?, val input: String = "")
    
    fun select(
        input: String,
        context: Context,
        knowledgeContext: Context?,
        toolContext: Context?
    ): Output
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

## License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.