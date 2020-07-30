package org.jetbrains.research.ml.codetracker.models

import kotlinx.serialization.Serializable

@Serializable
data class PaneLanguage(override val key: String) : Keyed

@Serializable
data class Gender(
    override val key: String,
    val translation: Map<PaneLanguage, String>
) : Keyed

@Serializable
data class Country(
    override val key: String,
    val translation: Map<PaneLanguage, String>
) : Keyed