package org.jetbrains.research.ml.codetracker

import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.editor.EditorFactory
import com.intellij.openapi.editor.event.DocumentEvent
import com.intellij.openapi.editor.event.DocumentListener
import com.intellij.openapi.fileEditor.FileDocumentManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.project.ProjectManager
import com.intellij.openapi.project.ProjectManagerListener
import org.jetbrains.research.ml.codetracker.server.TrackerQueryExecutor


object Plugin {
    const val PLUGIN_ID = "codetracker"

    private val logger: Logger = Logger.getInstance(javaClass)
    private lateinit var listener: MyDocumentListener

    init {
        logger.info("$PLUGIN_ID: init plugin")
    }

    private class MyDocumentListener : DocumentListener {
        private val logger: Logger = Logger.getInstance(javaClass)

        init {
            logger.info("$PLUGIN_ID: init documents listener")
        }

        // Tracking documents changes before to be consistent with activity-tracker plugin
        override fun beforeDocumentChange(event: DocumentEvent) {
            if (isValidChange(event)) DocumentLogger.log(event.document)
        }

        // To avoid completion events with IntellijIdeaRulezzz sign
        private fun isValidChange(event: DocumentEvent): Boolean {
            return EditorFactory.getInstance().getEditors(event.document).isNotEmpty()
                    && FileDocumentManager.getInstance().getFile(event.document) != null
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
        listener =
            MyDocumentListener()
        listener.add()
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
        listener.remove()
        return TrackerQueryExecutor.isLastSuccessful
    }

    // todo: find the other way for capturing the last project closing
    fun addProjectManagerListener(project: Project) {
        ProjectManager.getInstance().addProjectManagerListener(project, object : ProjectManagerListener {
            override fun projectClosing(project: Project) {
                if (ProjectManager.getInstance().openProjects.size == 1) {
                    stopTracking()
                }

                super.projectClosing(project)
            }
        })
    }
}
