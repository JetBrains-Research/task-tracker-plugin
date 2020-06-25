package org.jetbrains.research.ml.codetracker

import com.intellij.openapi.diagnostic.Logger
import org.jetbrains.research.ml.codetracker.server.PluginServer
import org.jetbrains.research.ml.codetracker.server.TrackerQueryExecutor


object Plugin {
    const val PLUGIN_ID = "codetracker"

    private val logger: Logger = Logger.getInstance(javaClass)
//    private val pluginServer: PluginServer

    init {
        logger.info("$PLUGIN_ID: init plugin")
//        pluginServer = PluginServer
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
        TaskFileHandler.stopTracking()
        return TrackerQueryExecutor.isLastSuccessful
    }

}
