import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.editor.EditorFactory
import com.intellij.openapi.editor.event.DocumentEvent
import com.intellij.openapi.editor.event.DocumentListener
import com.intellij.openapi.fileEditor.FileDocumentManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.project.ProjectManager
import com.intellij.openapi.project.ProjectManagerListener
import com.intellij.openapi.util.io.FileUtil
import models.Language
import models.Task
import server.PluginServer
import java.io.File


object Plugin {
    const val PLUGIN_ID = "codetracker"
    private val diagnosticLogger: Logger = Logger.getInstance(javaClass)
    private lateinit var listener: PluginDocumentListener
    val server: PluginServer = PluginServer
    val logger = DocumentLogger

    init {
        diagnosticLogger.info("${PLUGIN_ID}: init plugin")
    }

    private class PluginDocumentListener : DocumentListener {
        private val diagnosticLogger: Logger = Logger.getInstance(javaClass)

        init {
            diagnosticLogger.info("${PLUGIN_ID}: init documents listener")
        }

        // Tracking documents changes before to be consistent with activity-tracker plugin
        override fun beforeDocumentChange(event: DocumentEvent) {
            if (isValidChange(event)) {
                logger.log(event.document)
            }
        }

        // To avoid completion events with IntellijIdeaRulezzz sign
        private fun isValidChange(event: DocumentEvent) : Boolean {
            return EditorFactory.getInstance().getEditors(event.document).isNotEmpty() && FileDocumentManager.getInstance().getFile(event.document) != null
        }

        // EditorFactory.eventMulticaster sets listeners to all documents in all open projects
        fun add() {
            EditorFactory.getInstance().eventMulticaster.addDocumentListener(this)
        }

        fun remove() {
            EditorFactory.getInstance().eventMulticaster.removeDocumentListener(this)
        }
    }

    fun startTracking() {
        listener = PluginDocumentListener()
        listener.add()
    }

    fun stopTracking(): Boolean{
        diagnosticLogger.info("${PLUGIN_ID}: close IDE")
        diagnosticLogger.info("${PLUGIN_ID}: prepare fo sending ${logger.getFiles().size} files")
        if (logger.getFiles().isNotEmpty()) {
            logger.logCurrentDocuments()
            logger.flush()
            logger.documentsToPrinters.forEach { (d, p) ->
                server.trackerQueryExecutor.sendCodeTrackerData(p.file, { server.trackerQueryExecutor.checkSuccessful()}) { logger.close(d, p) }
            }
        }
        listener.remove()
        return server.trackerQueryExecutor.checkSuccessful()
    }

    // todo: find the other way for capturing the last project closing
    fun addProjectManagerListener(project: Project) {
        ProjectManager.getInstance().addProjectManagerListener (project, object : ProjectManagerListener {
            override fun projectClosing(project: Project) {
                if (ProjectManager.getInstance().openProjects.size == 1) {
                    stopTracking()
                }

                super.projectClosing(project)
            }
        } )
    }

}
