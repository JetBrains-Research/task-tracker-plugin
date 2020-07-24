package org.jetbrains.research.ml.codetracker

import com.intellij.openapi.diagnostic.Logger
import org.jetbrains.research.ml.codetracker.server.TrackerQueryExecutor
import org.jetbrains.research.ml.codetracker.tracking.DocumentLogger
import org.jetbrains.research.ml.codetracker.tracking.TaskFileHandler


object Plugin {
    const val PLUGIN_ID = "codetracker"

    private val logger: Logger = Logger.getInstance(javaClass)

    init {
        logger.info("$PLUGIN_ID: init plugin")
    }

    fun stopTracking(): Boolean {
        logger.info("$PLUGIN_ID: close IDE")
        logger.info("$PLUGIN_ID: prepare fo sending ${DocumentLogger.getFiles().size} files")
        if (DocumentLogger.getFiles().isNotEmpty()) {
            DocumentLogger.logCurrentDocuments()
            DocumentLogger.flush()
            DocumentLogger.documentsToPrinters.forEach { (d, p) ->
                TrackerQueryExecutor.sendCodeTrackerData(
                    p.file,
                    { TrackerQueryExecutor.isLastSuccessful }
                ) { DocumentLogger.close(d, p) }
            }
        }
//        TaskFileHandler.stopTracking()
        return TrackerQueryExecutor.isLastSuccessful
    }

}
