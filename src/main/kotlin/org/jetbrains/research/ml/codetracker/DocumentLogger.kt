package org.jetbrains.research.ml.codetracker

import org.jetbrains.research.ml.codetracker.*
import com.intellij.openapi.application.PathManager
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.editor.Document
import com.intellij.openapi.fileEditor.FileDocumentManager
import com.intellij.openapi.util.io.FileUtil
import org.jetbrains.research.ml.codetracker.data.DocumentChangeData
import org.apache.commons.csv.CSVFormat
import org.apache.commons.csv.CSVPrinter
import org.joda.time.DateTime
import org.jetbrains.research.ml.codetracker.server.TrackerQueryExecutor
import org.jetbrains.research.ml.codetracker.ui.ControllerManager
import java.io.File
import java.io.FileOutputStream
import java.io.FileWriter
import java.io.OutputStreamWriter
import java.nio.charset.StandardCharsets
import kotlin.math.abs


object DocumentLogger {
    data class Printer(val csvPrinter: CSVPrinter, val fileWriter: OutputStreamWriter, val file: File)

    private val logger: Logger = Logger.getInstance(javaClass)

    private val myDocumentsToPrinters: HashMap<Document, Printer> = HashMap()

    val documentsToPrinters: Map<Document, Printer>
        get() = documentsToPrinters.toMap()

    private val folderPath = "${PathManager.getPluginsPath()}/code-tracker/"
    private const val MAX_FILE_SIZE = 50 * 1024 * 1024
    private const val MAX_DIF_SIZE = 300

    fun log(document: Document) {
        var printer = myDocumentsToPrinters.getOrPut(document, {
            initPrinter(document)
        })
        if (isFull(printer.file.length())) {
            logger.info("${Plugin.PLUGIN_ID}: File ${printer.file.name} is full")
            sendFile(printer.file)
            printer = initPrinter(document)
            logger.info("${Plugin.PLUGIN_ID}: File ${printer.file.name} was cleared")
        }
        val change = document.getChange()
        printer.csvPrinter.printRecord(change.getData() + ControllerManager.uiData.getData().map { it.logValue })
    }

    fun logCurrentDocuments() {
        logger.info("${Plugin.PLUGIN_ID}: log current documents: ${myDocumentsToPrinters.keys.size}")
        myDocumentsToPrinters.keys.forEach {
            log(
                it
            )
        }
    }

    // Todo: change it. Handle a case if we get a fileSize bigger than MAX_FILE_SIZE + MAX_DIF_SIZE
    private fun isFull(fileSize: Long): Boolean = abs(MAX_FILE_SIZE - fileSize) < MAX_DIF_SIZE

    private fun sendFile(file: File) {
        TrackerQueryExecutor.sendCodeTrackerData(file)
    }

    fun getFiles(): List<File> = myDocumentsToPrinters.values.map { it.file }

    fun flush() {
        logger.info("${Plugin.PLUGIN_ID}: flush loggers")
        myDocumentsToPrinters.values.forEach { it.csvPrinter.flush() }
    }

    fun close(document: Document, printer: Printer) {
        printer.csvPrinter.close()
        printer.fileWriter.close()
        logger.info("${Plugin.PLUGIN_ID}: close ${printer.file.name}")
    }

    fun close() {
        logger.info("${Plugin.PLUGIN_ID}: close loggers")
        myDocumentsToPrinters.values.forEach { it.csvPrinter.close(); it.fileWriter.close() }
        myDocumentsToPrinters.clear()
    }

    private fun initPrinter(document: Document): Printer {
        logger.info("${Plugin.PLUGIN_ID}: init printer")
        val file = createLogFile(document)
        val fileWriter = OutputStreamWriter(FileOutputStream(file), StandardCharsets.UTF_8)
        val csvPrinter = CSVPrinter(fileWriter, CSVFormat.DEFAULT)
        csvPrinter.printRecord(DocumentChangeData.headers + ControllerManager.uiData.getData().map { it.header })
        return Printer(
            csvPrinter,
            fileWriter,
            file
        )
    }


    private fun createLogFile(document: Document): File {
        File(folderPath).mkdirs()
        val file = FileDocumentManager.getInstance().getFile(document)
        logger.info("${Plugin.PLUGIN_ID}: create log file for file ${file?.name}")
        val logFile = File("$folderPath${file?.nameWithoutExtension}_${file.hashCode()}_${document.hashCode()}.csv")
        FileUtil.createIfDoesntExist(logFile)
        return logFile
    }

    private fun Document.getChange(): DocumentChangeData {
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