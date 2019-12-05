import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.editor.EditorFactory
import com.intellij.openapi.editor.event.DocumentEvent
import com.intellij.openapi.editor.event.DocumentListener
import com.intellij.openapi.fileEditor.FileDocumentManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.project.ProjectManager
import com.intellij.openapi.project.ProjectManagerListener


object Plugin {
    const val PLUGIN_ID = "codetracker"
    private val diagnosticLogger: Logger = Logger.getInstance(javaClass)
    private val projectsToListeners: MutableMap<Project, PluginDocumentListener> = HashMap()
    val server: Server = PluginServer


    init {
        diagnosticLogger.info("${PLUGIN_ID}: init plugin")
    }

    private class PluginDocumentListener(private val project: Project) : DocumentListener {
        private val diagnosticLogger: Logger = Logger.getInstance(javaClass)
        val logger = DocumentLogger(project)

        init {
            diagnosticLogger.info("${PLUGIN_ID}: init document listener")
        }

        // Tracking documents changes before to be consistent with activity-tracker plugin
        override fun beforeDocumentChange(event: DocumentEvent) {
            if (isValidChange(event)) {
                logger.log(event.document)
            }
        }

        private fun isValidChange(event: DocumentEvent) : Boolean {
            return EditorFactory.getInstance().getEditors(event.document).isNotEmpty() && FileDocumentManager.getInstance().getFile(event.document) != null
        }

        fun add() {
            projectsToListeners[project] = this
            EditorFactory.getInstance().eventMulticaster.addDocumentListener(this)
        }

        fun remove() {
            projectsToListeners.remove(project, this)
            EditorFactory.getInstance().eventMulticaster.removeDocumentListener(this)
        }
    }

    fun startTracking(project: Project) {
        PluginDocumentListener(project).add()
    }

    // check disposable?
    fun addProjectManagerListener(project: Project) {
        ProjectManager.getInstance().addProjectManagerListener (project, object : ProjectManagerListener {
            override fun projectClosing(project: Project) {
                diagnosticLogger.info("${PLUGIN_ID}: close project")
                val logger = projectsToListeners[project]?.logger
                if (logger != null) {
                    diagnosticLogger.info("${PLUGIN_ID}: prepare for sending ${logger.getFiles().joinToString { it.name } }")
                    logger.logCurrentDocuments()
                    logger.flush()
                    logger.close()
                    logger.getFiles().forEach { server.sendTrackingData(it); it.deleteOnExit() }
                }

                projectsToListeners[project]?.remove()

                super.projectClosing(project)
            }
        } )
    }

}
