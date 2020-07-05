package org.jetbrains.research.ml.codetracker.models

import kotlinx.serialization.Serializable

@Serializable
data class PaneLanguage(val key: String)

@Serializable
data class Gender(
    val key: String,
    val translation: Map<PaneLanguage, String>
)

@Serializable
data class Country(
    val key: String,
    val translation: Map<PaneLanguage, String>
)