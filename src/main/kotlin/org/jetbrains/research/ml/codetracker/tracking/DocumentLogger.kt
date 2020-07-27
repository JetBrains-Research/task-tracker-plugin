package org.jetbrains.research.ml.codetracker.tracking

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.application.PathManager
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.editor.Document
import com.intellij.openapi.fileEditor.FileDocumentManager
import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.io.FileUtil
import com.intellij.util.messages.Topic
import org.apache.commons.csv.CSVFormat
import org.apache.commons.csv.CSVPrinter
import org.jetbrains.research.ml.codetracker.Plugin
import org.jetbrains.research.ml.codetracker.models.Task
import com.intellij.openapi.progress.Task as IntellijTask
import org.jetbrains.research.ml.codetracker.server.TrackerQueryExecutor
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStreamWriter
import java.nio.charset.StandardCharsets
import java.util.function.Consumer

enum class DataSendingResult {
    LOADING,
    SUCCESS,
    FAIL
}

interface DataSendingNotifier : Consumer<DataSendingResult> {
    companion object {
        val DATA_SENDING_TOPIC = Topic.create("data sending result", DataSendingNotifier::class.java)
    }
}


object DocumentLogger {
    data class Printer(val csvPrinter: CSVPrinter, val fileWriter: OutputStreamWriter, val file: File)

    var dataSendingResult: DataSendingResult = DataSendingResult.SUCCESS
        private set

    private val logger: Logger = Logger.getInstance(javaClass)
    private val myDocumentsToPrinters: HashMap<Document, Printer> = HashMap()

    private val folderPath = "${PathManager.getPluginsPath()}/codetracker/"
    private const val MAX_FILE_SIZE = 50 * 1024 * 1024
    private const val MAX_DIF_SIZE = 300

    fun log(document: Document) {
        var printer = myDocumentsToPrinters.getOrPut(document, { initPrinter(document) })
        if (isFull(printer.file.length())) {
            logger.info("${Plugin.PLUGIN_ID}: File ${printer.file.name} is full")
//            Todo: don't send it without user permission
            sendFile(printer.file)
            printer = initPrinter(document)
            logger.info("${Plugin.PLUGIN_ID}: File ${printer.file.name} was cleared")
        }
        printer.csvPrinter.printRecord(DocumentLoggedData.getData(document) + UiLoggedData.getData(Unit))
    }

    private fun isFull(fileSize: Long): Boolean = fileSize > MAX_FILE_SIZE - MAX_DIF_SIZE

    private fun initPrinter(document: Document): Printer {
        logger.info("${Plugin.PLUGIN_ID}: init printer")
        val file = createLogFile(document)
        val fileWriter = OutputStreamWriter(FileOutputStream(file), StandardCharsets.UTF_8)
        val csvPrinter = CSVPrinter(fileWriter, CSVFormat.DEFAULT)
        csvPrinter.printRecord(DocumentLoggedData.headers + UiLoggedData.headers)
        return Printer(csvPrinter, fileWriter, file)
    }


    private fun createLogFile(document: Document): File {
        File(folderPath).mkdirs()
        val file = FileDocumentManager.getInstance().getFile(document)
        logger.info("${Plugin.PLUGIN_ID}: create log file for file ${file?.name}")
        val logFile = File("$folderPath${file?.nameWithoutExtension}_${file.hashCode()}_${document.hashCode()}.csv")
        FileUtil.createIfDoesntExist(logFile)
        return logFile
    }

    private fun sendFile(file: File) {
        TrackerQueryExecutor.sendCodeTrackerData(file)
    }

    fun sendTaskFile(task: Task, project: Project) {
        ApplicationManager.getApplication().invokeAndWait {
            val document = TaskFileHandler.getDocument(project, task)
            ProgressManager.getInstance().run(
                object : IntellijTask.Backgroundable(project,"Sending task ${task.key} solution", false) {
                    override fun run(indicator: ProgressIndicator) {
                        sendFileByDocument(document)
                    }
                })
        }
    }

    private fun sendFileByDocument(document: Document) {
        if (dataSendingResult != DataSendingResult.LOADING) {
            val publisher =
                ApplicationManager.getApplication().messageBus.syncPublisher(DataSendingNotifier.DATA_SENDING_TOPIC)
            dataSendingResult = DataSendingResult.LOADING
            publisher.accept(dataSendingResult)
            dataSendingResult = try {
                // Log the last state (need to RUN ON EDT)
                ApplicationManager.getApplication().invokeAndWait { log(document) }
                val printer = myDocumentsToPrinters[document]
                    ?: throw IllegalStateException("No printer for the document $document exists")
                printer.csvPrinter.flush()
                sendFile(printer.file)
                DataSendingResult.SUCCESS
            } catch (e: java.lang.IllegalStateException) {
                DataSendingResult.FAIL
            }
            publisher.accept(dataSendingResult)
        }
    }
}