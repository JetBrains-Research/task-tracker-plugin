package org.jetbrains.research.ml.codetracker.tracking

import org.joda.time.DateTime

data class DocumentChangeData(
    val date: DateTime,
    val timestamp: Long,
    val fileName: String?,
    val fileHashCode: Int?,
    val documentHashCode: Int,
    val fragment: String
) {

    companion object {
        val headers = listOf(
            "date",
            "timestamp",
            "fileName",
            "fileHashCode",
            "documentHashCode",
            "fragment"
        )
    }

    fun getData(): List<String> {
        return listOf(
            date,
            timestamp,
            fileName,
            fileHashCode,
            documentHashCode,
            fragment
        ).map { it.toString() }
    }
}