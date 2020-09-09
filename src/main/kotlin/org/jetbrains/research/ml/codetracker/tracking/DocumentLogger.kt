package org.jetbrains.research.ml.codetracker.tracking

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.editor.Document
import com.intellij.openapi.fileEditor.FileDocumentManager
import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.io.FileUtil
import com.intellij.util.containers.toArray
import com.intellij.util.messages.Topic
import org.apache.commons.csv.CSVFormat
import org.apache.commons.csv.CSVPrinter
import org.jetbrains.research.ml.codetracker.Plugin
import org.jetbrains.research.ml.codetracker.Plugin.codeTrackerFolderPath
import org.jetbrains.research.ml.codetracker.models.Task
import com.intellij.openapi.progress.Task as IntellijTask
import org.jetbrains.research.ml.codetracker.server.TrackerQueryExecutor
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStreamWriter
import java.nio.charset.StandardCharsets
import java.util.function.Consumer
import kotlin.math.log

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

/**
 * Prints logs to the [logFile] in .csv format using [csvPrinter]
 */
data class LogPrinter(val csvPrinter: CSVPrinter, val fileWriter: OutputStreamWriter, val logFile: File) {
    companion object {
        private const val MAX_DIF_SIZE = 300
        private const val MAX_FILE_SIZE = 50 * 1024 * 1024
    }

    fun isFull(): Boolean {
        return logFile.length() > MAX_FILE_SIZE - MAX_DIF_SIZE
    }
}

/**
 * Takes care of all [logPrinters] (there may be several in case of log file overflowing) created to log a tracked Document
 */
class DocumentLogPrinter {
    var logPrinters = mutableListOf<LogPrinter>()
        private set

    companion object {
        private val logger: Logger = Logger.getInstance(this::class.java)
    }

    /**
     * Gets the active logPrinter or creates a new one if there was none or the active one was full
     */
    fun getActiveLogPrinter(document: Document) : LogPrinter {
        val activePrinter = getLastPrinter(document)
        return if (activePrinter.isFull()) {
            addLogPrinter(document)
        } else {
            activePrinter
        }
    }

    private fun getLastPrinter(document: Document) : LogPrinter {
        return if (logPrinters.size == 0) {
            addLogPrinter(document)
        } else {
            logPrinters[-1]
        }
    }

    private fun addLogPrinter(document: Document) : LogPrinter {
        logger.info("${Plugin.PLUGIN_NAME}: init printer")
        val logFile = createLogFile(document)
        val fileWriter = OutputStreamWriter(FileOutputStream(logFile), StandardCharsets.UTF_8)
        val csvPrinter = CSVPrinter(fileWriter, CSVFormat.DEFAULT)
        csvPrinter.printRecord(DocumentLoggedData.headers + UiLoggedData.headers)
        logPrinters.add(LogPrinter(csvPrinter, fileWriter, logFile))
        return logPrinters[-1]
    }

    private fun createLogFile(document: Document) : File {
        File(codeTrackerFolderPath).mkdirs()
        val trackedFile = FileDocumentManager.getInstance().getFile(document)
        logger.info("${Plugin.PLUGIN_NAME}: create log file for tracked file ${trackedFile?.name}")
        val logFilesNumber = logPrinters.size
        val logFile = File("$codeTrackerFolderPath/${trackedFile?.nameWithoutExtension}_${trackedFile.hashCode()}_${document.hashCode()}_$logFilesNumber.csv")
        FileUtil.createIfDoesntExist(logFile)
        return logFile
    }

    fun removeInActivePrinters() {
        if (logPrinters.size > 1) {
            logPrinters = logPrinters.drop(logPrinters.size - 1).toMutableList()
        }
    }
}

object DocumentLogger {

    private var dataSendingResult: DataSendingResult = DataSendingResult.SUCCESS
        private set

    private val logger: Logger = Logger.getInstance(javaClass)
    private val myDocumentsToPrinters: HashMap<Document, DocumentLogPrinter> = HashMap()


    fun log(document: Document) {
        val docPrinter = myDocumentsToPrinters.getOrPut(document, { DocumentLogPrinter() })
        val logPrinter = docPrinter.getActiveLogPrinter(document)
        logPrinter.csvPrinter.printRecord(DocumentLoggedData.getData(document) + UiLoggedData.getData(Unit))
    }

    private fun sendFile(file: File) {
        TrackerQueryExecutor.sendData(file)
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
                val docPrinter = myDocumentsToPrinters[document]
                    ?: throw IllegalStateException("No printer for the document $document exists")
//                todo: remove old logPrinters
                docPrinter.logPrinters.forEach {
                    it.csvPrinter.flush()
                    sendFile(it.logFile)
                }
                DataSendingResult.SUCCESS
            } catch (e: java.lang.IllegalStateException) {
                DataSendingResult.FAIL
            }
            publisher.accept(dataSendingResult)
        }
    }
}





