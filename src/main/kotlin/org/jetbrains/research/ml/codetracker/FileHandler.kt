package org.jetbrains.research.ml.codetracker

import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.editor.Document
import com.intellij.openapi.fileEditor.FileDocumentManager
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.io.FileUtil
import com.intellij.openapi.vfs.LocalFileSystem
import com.intellij.openapi.vfs.VirtualFile
import org.jetbrains.research.ml.codetracker.models.Language
import org.jetbrains.research.ml.codetracker.models.Task
import java.io.File
import java.lang.IllegalArgumentException

object FileHandler {

    private const val PLUGIN_FOLDER = "codetracker"

    private val logger: Logger = Logger.getInstance(javaClass)
    private val documentToTask: HashMap<Document, Task> = HashMap()

    private fun createFile(project: Project, task: Task, language: Language): VirtualFile? {
        val file = File("${project.basePath}/${PLUGIN_FOLDER}/${task.key}${language.extension.ext}")
        FileUtil.createIfDoesntExist(file)
        return LocalFileSystem.getInstance().refreshAndFindFileByIoFile(file)
    }

    // If the documentToTask has task null then we don't track the document
    private fun addDocument(virtualFile: VirtualFile?, task: Task): Document? {
        val document = virtualFile?.let { FileDocumentManager.getInstance().getDocument(it) }
        // It the old task is not equal the new task
        if (documentToTask.containsKey(document) && documentToTask[document] != task) {
            val message = "${Plugin.PLUGIN_ID}: an attempt to assign another task to the document ${document}. " +
                    "The old task is ${documentToTask[document]!!.key}, the new task is ${task.key}"
            logger.error(message)
            throw IllegalArgumentException(message)
        }
        document?.let { documentToTask.getOrPut(it, { task }) }
        return document
    }

    fun createAndOpenFile(project: Project, task: Task, language: Language): VirtualFile? {
        val virtualFile = createFile(project, task, language)
        addDocument(virtualFile, task)
        openFile(project, virtualFile)
        return virtualFile
    }

    fun isTrackedDocument(document: Document): Boolean {
        return documentToTask.containsKey(document)
    }

    private fun openFile(project: Project, virtualFile: VirtualFile?) {
        virtualFile?.let { FileEditorManager.getInstance(project).openFile(it, true, true) }
    }

}