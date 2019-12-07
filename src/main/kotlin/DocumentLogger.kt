import com.intellij.openapi.application.PathManager
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.editor.Document
import com.intellij.openapi.fileEditor.FileDocumentManager
import com.intellij.openapi.util.io.FileUtil
import data.DocumentChangeData
import org.apache.commons.csv.CSVFormat
import org.apache.commons.csv.CSVPrinter
import org.joda.time.DateTime
import ui.ControllerManager
import java.io.File
import java.io.FileWriter
import kotlin.math.abs


object DocumentLogger {
    private val diagnosticLogger: Logger = Logger.getInstance(javaClass)

    val documentsToPrinters: HashMap<Document, Printer> = HashMap()

    private val folderPath = "${PathManager.getPluginsPath()}/code-tracker/"
    private const val MAX_FILE_SIZE = 50 * 1024 * 1024
    private const val MAX_DIF_SIZE = 300

    data class Printer(val csvPrinter: CSVPrinter, val fileWriter: FileWriter, val file: File)

    fun log(document: Document) {
        var printer = documentsToPrinters.getOrPut(document, { initPrinter(document) })
        if (isFull(printer.file.length())) {
            diagnosticLogger.info("${Plugin.PLUGIN_ID}: File ${printer.file.name} is full")
            sendFile(printer.file)
            printer = initPrinter(document)
            diagnosticLogger.info("${Plugin.PLUGIN_ID}: File ${printer.file.name} was cleared")
        }
        val change = document.getChange()
        printer.csvPrinter.printRecord(change.getData() + ControllerManager.uiData.getData().map { it.logValue })
    }

    fun logCurrentDocuments() {
        diagnosticLogger.info("${Plugin.PLUGIN_ID}: log current documents: ${documentsToPrinters.keys.size}")
        documentsToPrinters.keys.forEach { log(it) }
    }


    private fun isFull(fileSize: Long): Boolean = abs(MAX_FILE_SIZE - fileSize) < MAX_DIF_SIZE

    private fun sendFile(file: File) {
        Plugin.server.sendTrackingData(file, false)
    }

    fun getFiles() : List<File> = documentsToPrinters.values.map { it.file }

    fun flush() {
        diagnosticLogger.info("${Plugin.PLUGIN_ID}: flush loggers")
        documentsToPrinters.values.forEach { it.csvPrinter.flush() }
    }

    fun close(document: Document, printer: Printer) : Boolean {
        printer.csvPrinter.close()
        printer.fileWriter.close()
        val closed = documentsToPrinters.remove(document, printer)
        if (closed) {
            diagnosticLogger.info("${Plugin.PLUGIN_ID}: close ${printer.file.name}")
        } else {
            diagnosticLogger.info("${Plugin.PLUGIN_ID}: cannot close ${printer.file.name}")
        }
        return closed
    }

    fun close() {
        diagnosticLogger.info("${Plugin.PLUGIN_ID}: close loggers")
        documentsToPrinters.values.forEach { it.csvPrinter.close(); it.fileWriter.close() }
        documentsToPrinters.clear()
    }

    private fun initPrinter(document: Document) : Printer {
        diagnosticLogger.info("${Plugin.PLUGIN_ID}: init printer")
        val file = createLogFile(document)
        val fileWriter = FileWriter(file)
        val csvPrinter = CSVPrinter(fileWriter, CSVFormat.DEFAULT)
        csvPrinter.printRecord(DocumentChangeData.headers + ControllerManager.uiData.getData().map { it.header })
        return Printer(csvPrinter, fileWriter, file)
    }


    private fun createLogFile(document: Document): File {
        File(folderPath).mkdirs()
        val file = FileDocumentManager.getInstance().getFile(document)
        diagnosticLogger.info("${Plugin.PLUGIN_ID}: create log file for file ${file?.name}")
        val logFile = File("$folderPath${file?.nameWithoutExtension}_${file.hashCode()}_${document.hashCode()}.csv")
        FileUtil.createIfDoesntExist(logFile)
        return logFile
    }

    private fun Document.getChange() : DocumentChangeData {
        val time = DateTime.now()
        val file = FileDocumentManager.getInstance().getFile(this)

        return DocumentChangeData(
            time,
            this.modificationStamp,
            file?.name,
            file?.hashCode(),
            this.hashCode(),
            this.text
        )
    }

}