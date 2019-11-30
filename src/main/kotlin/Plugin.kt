import com.intellij.openapi.editor.EditorFactory
import com.intellij.openapi.editor.event.DocumentEvent
import com.intellij.openapi.editor.event.DocumentListener
import com.intellij.openapi.fileEditor.FileDocumentManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.project.ProjectManager
import com.intellij.openapi.project.ProjectManagerListener
import java.util.logging.Logger


object Plugin {
    private val log: Logger = Logger.getLogger(javaClass.name)
    private val projectsToListeners: MutableMap<Project, PluginDocumentListener> = HashMap()
    val server: Server = PluginServer


    init {
        log.info("init plugin")
    }

    private class PluginDocumentListener(private val project: Project) : DocumentListener {
        private val log: Logger = Logger.getLogger(javaClass.name)
        val logger = DocumentLogger(project)

        init {
            log.info("init document listener")
        }

        // Tracking documents changes before to be consistent with activity-tracker plugin
        override fun beforeDocumentChange(event: DocumentEvent) {
            if (isValidChange(event)) {
                logger.log(event)
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
                log.info("close project")
                projectsToListeners[project]?.logger?.flush()
                projectsToListeners[project]?.logger?.close()
                projectsToListeners[project]?.logger?.getFiles()?.forEach { server.sendTrackingData(it) }
                projectsToListeners[project]?.remove()

                super.projectClosing(project)
            }
        } )
    }

}
