import com.intellij.openapi.application.PathManager
import com.intellij.openapi.editor.Document
import com.intellij.openapi.editor.event.DocumentEvent
import com.intellij.openapi.fileEditor.FileDocumentManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.io.FileUtil
import com.intellij.util.SystemProperties
import data.DocumentChangeData
import org.apache.commons.csv.CSVFormat
import org.apache.commons.csv.CSVPrinter
import org.joda.time.DateTime
import ui.ControllerManager
import java.io.File
import java.io.FileWriter
import java.util.logging.Logger


class DocumentLogger(project: Project) {
    private val log = Logger.getLogger(javaClass.name)
    private val documentsToPrinters: HashMap<Document, Printer> = HashMap()

    companion object {
        private val folderPath = "${PathManager.getPluginsPath()}/code-tracker/"
        private const val MAX_FILE_SIZE = 50 * 1024 * 1024
        private const val MAX_DIF_SIZE = 300
    }

    data class Printer(val csvPrinter: CSVPrinter, val fileWriter: FileWriter, val file: File)

    fun log(event: DocumentEvent) {
        val document = event.document
        var printer = documentsToPrinters.getOrPut(document, { initPrinter(document) })
        if (isFull(printer.file.length())) {
            log.info("File ${printer.file.name} is full")
            sendFile(printer.file)
            printer = initPrinter(document)
            log.info("File ${printer.file.name} was cleared")
        }
        val change = getDocumentChange(event)
        printer.csvPrinter.printRecord(change.getData() + ControllerManager.uiData.getData().map { it.logValue })
    }

    private fun isFull(fileSize: Long): Boolean = MAX_FILE_SIZE - fileSize < MAX_DIF_SIZE

    private fun sendFile(file: File) = Plugin.server.sendTrackingData(file)

    fun getFiles() : List<File> = documentsToPrinters.values.map { it.file }

    fun flush() {
        log.info("flush loggers")
        documentsToPrinters.values.forEach { it.csvPrinter.flush() }
    }

    fun close() {
        log.info("close loggers")
        documentsToPrinters.values.forEach { it.csvPrinter.close(); it.fileWriter.close() }
    }

    private fun initPrinter(document: Document) : Printer {
        val file = createLogFile(document)
        val fileWriter = FileWriter(file)
        val csvPrinter = CSVPrinter(fileWriter, CSVFormat.DEFAULT)
        csvPrinter.printRecord(DocumentChangeData.headers + ControllerManager.uiData.getData().map { it.header })
        return Printer(csvPrinter, fileWriter, file)
    }


    private fun createLogFile(document: Document): File {
        File(folderPath).mkdirs()
        val file = FileDocumentManager.getInstance().getFile(document)
        val logFile = File("$folderPath${file?.nameWithoutExtension}_${file.hashCode()}_${document.hashCode()}.csv")
        FileUtil.createIfDoesntExist(logFile)
        return logFile
    }

    private fun getDocumentChange(event: DocumentEvent) : DocumentChangeData {
        val time = DateTime.now()
        val document = event.document
        val file = FileDocumentManager.getInstance().getFile(document)

        return DocumentChangeData(
            time,
            event.document.modificationStamp,
            file?.name,
            file?.hashCode(),
            document.hashCode(),
            document.text
        )
    }
}