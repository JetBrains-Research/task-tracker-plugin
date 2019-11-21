import com.intellij.openapi.application.PathManager
import com.intellij.openapi.editor.Document
import com.opencsv.CSVWriter
import org.joda.time.DateTime
import java.io.File
import com.intellij.openapi.editor.event.DocumentEvent
import com.intellij.openapi.fileEditor.FileDocumentManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.io.FileUtil
import com.intellij.util.SystemProperties
import org.apache.commons.csv.CSVFormat
import org.apache.commons.csv.CSVPrinter
import java.io.FileWriter
import java.util.*


class Logger(private val document: Document) {
    companion object {
        private val folderPath = "${PathManager.getPluginsPath()}/code-tracker/"
    }

    private val logFile: File = createLogFile()
    private val fileWriter: FileWriter = FileWriter(logFile)
    private val csvPrinter: CSVPrinter = CSVPrinter(fileWriter, CSVFormat.DEFAULT)

    init {
        csvPrinter.printRecord(DocumentChange.headers)
    }

    private fun createLogFile(): File {
        File(folderPath).mkdirs()
        val file = FileDocumentManager.getInstance().getFile(document)
        val logFile = File("$folderPath${file?.nameWithoutExtension}_${file.hashCode()}_${document.hashCode()}.csv")
        FileUtil.createIfDoesntExist(logFile)
        return logFile
    }

    fun flush() {
        csvPrinter.flush()
    }

    fun close() {
        csvPrinter.close()
        fileWriter.close()
    }

    fun log(event: DocumentEvent) {
        val change = getDocumentChange(event)
        csvPrinter.printRecord(change.getData())
    }

    private fun getDocumentChange(event: DocumentEvent) : DocumentChange {
        val time = DateTime.now()
        val document = event.document
        val file = FileDocumentManager.getInstance().getFile(document)

        return DocumentChange(
            time,
            event.document.modificationStamp,
            SystemProperties.getUserName(),
            file?.name,
            file?.hashCode(),
            document.hashCode(),
            document.text
        )
    }

}


data class DocumentChange(
    val date: DateTime,
    val timestamp: Long,
    val userName: String,
    val fileName: String?,
    val fileHashCode: Int?,
    val documentHashCode: Int,
    val fragment: String
) {

    companion object {
        val headers = listOf("date", "timestamp", "userName", "fileName", "fileHashCode", "documentHashCode", "fragment")
    }


    fun getData() : List<String>{
        return listOf(
            date,
            timestamp,
            userName,
            fileName,
            fileHashCode,
            documentHashCode,
            fragment
        ).map { it.toString() }
    }
}