package data

import org.joda.time.DateTime

data class DocumentChangeData(
    var date: DateTime,
    var timestamp: Long,
    var fileName: String?,
    var fileHashCode: Int?,
    var documentHashCode: Int,
    var fragment: String
) {

    companion object {
        val headers = listOf(
            "date",
            "timestamp",
            "fileName",
            "fileHashCode",
            "documentHashCode",
            "fragment")
    }

    fun getData() : List<String>{
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