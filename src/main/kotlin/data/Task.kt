package data

import kotlinx.serialization.Serializable


@Serializable
data class Example(val input: String = "", val output: String = "")

@Serializable
data class Task(val key: String,
                val name: String,
                val description: String = "Нет описания",
                val input: String = "",
                val output: String = "",
                val id: Int = -1,
                @Serializable val example_1: Example = Example(),
                @Serializable val example_2: Example = Example(),
                @Serializable val example_3: Example = Example()
)
