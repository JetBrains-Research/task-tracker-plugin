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


class Logger(initEvent: DocumentEvent) {

    companion object {
        private val folderPath = "${PathManager.getPluginsPath()}/code-tracker/"
    }

    private val document: Document = initEvent.document
    private val logFile: File = createLogFile()
    private val fileWriter: FileWriter = FileWriter(logFile)
    private val csvPrinter: CSVPrinter = CSVPrinter(fileWriter, CSVFormat.DEFAULT)

    init {
        csvPrinter.printRecord(DocumentChange.headers)
        log(initEvent)
    }

    private fun createLogFile(): File {
        val file = FileDocumentManager.getInstance().getFile(document)
        val logFile = File("$folderPath${file?.nameWithoutExtension}_${file.hashCode()}_${document.hashCode()}.csv")
        FileUtil.createIfDoesntExist(logFile)
        return logFile
    }

    fun flush() {
        csvPrinter.flush()
    }

    fun log(event: DocumentEvent) {
        val change = getDocumentChange(event)
        csvPrinter.printRecord(change.getData())
    }

    private fun getDocumentChange(event: DocumentEvent) : DocumentChange {
        val document = event.document
        val file = FileDocumentManager.getInstance().getFile(document)
        val offset = event.offset
        val newLength = event.newLength

        val firstLine = document.getLineNumber(offset)
        val lastLine =
            if (newLength == 0) {
                firstLine
            } else {
                document.getLineNumber(offset + newLength - 1)
            }

        return DocumentChange(
            event.document.modificationStamp,
            SystemProperties.getUserName(),
            file?.name,
            file?.hashCode(),
            document.hashCode(),
            offset,
            newLength,
            firstLine,
            lastLine,
            document.text
        )
    }

}


data class DocumentChange(
    val timestamp: Long,
    val userName: String,
    val fileName: String?,
    val fileHashCode: Int?,
    val documentHashCode: Int,
    val offset: Int,
    val newLength: Int,
    val firstLine: Int,
    val lastLine: Int,
    val fragment: String
) {

    companion object {
        val headers = listOf("timestamp", "userName", "fileName", "fileHashCode", "documentHashCode", "offset",
            "newLength", "firstLine", "lastLine", "fragment")
    }


    fun getData() : List<String>{
        return listOf(
            timestamp,
            userName,
            fileName,
            fileHashCode,
            documentHashCode,
            offset,
            newLength,
            firstLine,
            lastLine,
            fragment
        ).map { it.toString() }
    }
}