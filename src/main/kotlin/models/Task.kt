package models

import kotlinx.serialization.Serializable

@Serializable
data class Example(val input: String, val output: String)

@Serializable
data class TaskInfo(
    val name: String,
    val description: String,
    val input: String,
    val output: String
)

@Serializable
data class Task(
    val key: String,
    val id: Int = -1,
    val info: Map<String, TaskInfo>? = null,
    val examples: List<Example> = emptyList()
)
