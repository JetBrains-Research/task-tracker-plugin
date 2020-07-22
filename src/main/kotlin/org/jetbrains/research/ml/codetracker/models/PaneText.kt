package org.jetbrains.research.ml.codetracker.models

import kotlinx.serialization.Serializable

@Serializable
data class SurveyPaneText(
    val age: String,
    val gender: String,
    val experience: String,
    val country: String,
    val years: String,
    val months: String,
    val startSession: String
)

@Serializable
data class FinishPaneText(
    val praise: String,
    val backToSurvey: String,
    val finalMessage: String,
    val backToTasks: String
)

@Serializable
data class TaskChoosePaneText(
    val chooseTask: String,
    val finishSession: String,
    val startSolving: String
)

@Serializable
data class TaskPaneText(
    val inputData: String,
    val outputData: String,
    val submit: String,
    val backToTasks: String
)

@Serializable
data class PaneText(
    val surveyPane: Map<PaneLanguage, SurveyPaneText>,
    val taskChoosingPane: Map<PaneLanguage, TaskChoosePaneText>,
    val taskSolvingPane: Map<PaneLanguage, TaskPaneText>,
    val finalPane: Map<PaneLanguage, FinishPaneText>
)