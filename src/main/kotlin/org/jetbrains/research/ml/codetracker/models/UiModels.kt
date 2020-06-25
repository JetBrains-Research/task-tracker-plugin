package org.jetbrains.research.ml.codetracker.models

import kotlinx.serialization.Serializable

@Serializable
data class PaneLanguage(val key: String)

@Serializable
data class Gender(
    val key: String,
    val info: Map<String, String>? = null
)