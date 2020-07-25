package org.jetbrains.research.ml.codetracker.models

import kotlinx.serialization.Serializable


@Serializable
class TaskSolvingErrorText(
    val header: String,
    val description: String
)

@Serializable
class SuccessfulSubmitText(
    val header: String,
    val description: String
)


@Serializable
data class TaskSolvingErrorDialogText(
    val translation: Map<PaneLanguage, TaskSolvingErrorText>
)

@Serializable
data class SuccessfulSubmitDialogText(
    val translation: Map<PaneLanguage, SuccessfulSubmitText>
)

