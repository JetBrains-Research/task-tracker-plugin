import com.intellij.openapi.editor.Document
import com.intellij.openapi.editor.EditorFactory
import com.intellij.openapi.editor.event.DocumentEvent
import com.intellij.openapi.editor.event.DocumentListener
import com.intellij.openapi.fileEditor.FileDocumentManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.project.ProjectManager
import com.intellij.openapi.project.ProjectManagerListener
import ui.PluginStatusBarWidget


class CodeTrackerPlugin(private val widget: PluginStatusBarWidget) {
    var trackingState : TrackingState = TrackingState.OFF
        set(trackingState : TrackingState) {
            if (trackingState == TrackingState.ON) {
                addMultiDocumentListener()
            } else {
                removeMultiDocumentListener()
            }
            field = trackingState
            widget.updateState()
        }

    private val documentsToLoggers: MutableMap<Document, Logger> = HashMap()

    private val documentListener = object : DocumentListener {

        /*
         Tracking documents changes before to be consistent with activity-tracker plugin
         */
        override fun beforeDocumentChange(event: DocumentEvent) {
            if (isValidChange(event)) {
                documentsToLoggers.getOrPut(event.document, { Logger(event.document) } ).log(event)
            }
        }
    }

    fun addProjectManagerListener(project: Project) {
        ProjectManager.getInstance().addProjectManagerListener (project, object : ProjectManagerListener {
            override fun projectClosing(project: Project) {
                flushLoggers()
                documentsToLoggers.values.forEach { it.close() }
                super.projectClosing(project)
            }
        } )

    }

    private fun addMultiDocumentListener() {
        EditorFactory.getInstance().eventMulticaster.addDocumentListener(documentListener)
    }

    private fun removeMultiDocumentListener() {
        EditorFactory.getInstance().eventMulticaster.removeDocumentListener(documentListener)
        flushLoggers()
    }


    private fun isValidChange(event: DocumentEvent) : Boolean {
        return EditorFactory.getInstance().getEditors(event.document).isNotEmpty() && FileDocumentManager.getInstance().getFile(event.document) != null
    }

    private fun flushLoggers() {
        documentsToLoggers.values.forEach { it.flush() }
    }


    fun switchedState() : TrackingState {
        return when(trackingState) {
            TrackingState.OFF -> TrackingState.ON
            TrackingState.ON -> TrackingState.OFF
        }
    }
}

enum class TrackingState {
    OFF,
    ON
}