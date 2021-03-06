package org.jetbrains.research.ml.tasktracker.models

import kotlinx.serialization.Serializable


@Serializable
class TaskSolvingErrorText(
    val header: String,
    val description: String
)

@Serializable
data class TaskSolvingErrorDialogText(
    val translation: Map<PaneLanguage, TaskSolvingErrorText>
)

