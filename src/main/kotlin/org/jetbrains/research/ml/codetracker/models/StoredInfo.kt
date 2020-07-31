package org.jetbrains.research.ml.codetracker.models

import kotlinx.serialization.Serializable

@Serializable
data class StoredInfo(
    var loggedUIData: Map<String, String> = mapOf(),
    var userId: String? = null
)