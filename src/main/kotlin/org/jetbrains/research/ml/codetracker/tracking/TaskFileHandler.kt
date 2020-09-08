package org.jetbrains.research.ml.codetracker.tracking

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.application.WriteAction
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.editor.Document
import com.intellij.openapi.fileEditor.FileDocumentManager
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.project.ProjectLocator
import com.intellij.openapi.roots.ContentEntry
import com.intellij.openapi.roots.ModifiableRootModel
import com.intellij.openapi.roots.ModuleRootManager
import com.intellij.openapi.module.Module
import com.intellij.openapi.module.ModuleManager
import com.intellij.openapi.util.io.FileUtil
import com.intellij.openapi.vfs.LocalFileSystem
import com.intellij.openapi.vfs.VfsUtilCore.isEqualOrAncestor
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.util.io.ReadOnlyAttributeUtil
import org.jetbrains.jps.model.serialization.PathMacroUtil
import org.jetbrains.research.ml.codetracker.Plugin
import org.jetbrains.research.ml.codetracker.models.Language
import org.jetbrains.research.ml.codetracker.models.Task
import org.jetbrains.research.ml.codetracker.server.PluginServer
import org.jetbrains.research.ml.codetracker.server.ServerConnectionNotifier
import org.jetbrains.research.ml.codetracker.server.ServerConnectionResult
import org.jetbrains.research.ml.codetracker.ui.MainController
import org.jetbrains.research.ml.codetracker.ui.panes.TaskChoosingUiData
import org.jetbrains.research.ml.codetracker.ui.panes.TaskSolvingControllerManager
import org.jetbrains.research.ml.codetracker.ui.panes.util.subscribe
import java.io.File
import java.io.IOException


object TaskFileHandler {
    private const val PLUGIN_FOLDER = Plugin.PLUGIN_NAME

    private val logger: Logger = Logger.getInstance(javaClass)
    private val documentToTask: HashMap<Document, Task> = HashMap()
    private val projectToTaskToFiles: HashMap<Project, HashMap<Task, VirtualFile>> = HashMap()
    private val projectsToInit = arrayListOf<Project>()

    private val listener by lazy {
        TaskDocumentListener()
    }

    init {
        if (PluginServer.serverConnectionResult != ServerConnectionResult.SUCCESS) {
            subscribe(ServerConnectionNotifier.SERVER_CONNECTION_TOPIC, object : ServerConnectionNotifier {
                override fun accept(connection: ServerConnectionResult) {
                    // Todo: call it if the current language is chosen
                    if (connection == ServerConnectionResult.SUCCESS) {
                        projectsToInit.forEach { initProject(it) }
                        projectsToInit.clear()
                    }
                }
            })
        }
    }

    /**
     * Call if you sure that ServerConnectionResult was successful and therefore all tasks are received
     */
    private fun initProject(project: Project) {
        projectToTaskToFiles[project] = hashMapOf()
        PluginServer.tasks.forEach { task ->
            val virtualFile = getOrCreateFile(project, task, Plugin.currentLanguage)
            virtualFile?.let {
                addTaskFile(it, task, project)
                ApplicationManager.getApplication().invokeAndWait {
                    if (task.isItsFileWritable()) {
                        openFile(project, virtualFile)
                    } else {
                        closeFile(project, virtualFile)
                    }
                }
            }
        }
    }

    fun addProject(project: Project) {
        if (projectsToInit.contains(project) || projectToTaskToFiles.keys.contains(project)) {
            logger.info("Project $project is already added or set to be added")
            return
        }
        if (PluginServer.serverConnectionResult == ServerConnectionResult.SUCCESS) {
            initProject(project)
        } else {
            projectsToInit.add(project)
        }
    }

    private fun addSourceFolder(relativePath: String, module: Module) {
        val directory = File(PathMacroUtil.getModuleDir(module.moduleFilePath), relativePath)
        if (!directory.exists()) {
            directory.mkdirs()
        }
        val virtualFile = LocalFileSystem.getInstance().refreshAndFindFileByIoFile(directory)
        virtualFile?.let {
            val rootModel = ModuleRootManager.getInstance(module).modifiableModel
            getContentEntry(virtualFile, rootModel)?.addSourceFolder(it.url, false)
            rootModel.commit()
        }

    }

    private fun getContentEntry(url: VirtualFile?, rootModel: ModifiableRootModel): ContentEntry? {
        rootModel.contentEntries.forEach { e ->
            url?.let {
                if (isEqualOrAncestor(e.url, url.url)) return e
            }
        }
        return null
    }

    private fun getOrCreateFile(project: Project, task: Task, language: Language): VirtualFile? {
        val relativeFilePath = TaskFileInitContentProvider.getLanguageFolderRelativePath(language)
        ApplicationManager.getApplication().runWriteAction {
            addSourceFolder(relativeFilePath, ModuleManager.getInstance(project).modules.last())
        }
        val file =
            File("${project.basePath}/$relativeFilePath/${TaskFileInitContentProvider.getTaskFileName(task, language)}")
        if (!file.exists()) {
            ApplicationManager.getApplication().runWriteAction {
                FileUtil.createIfDoesntExist(file)
                file.writeText(TaskFileInitContentProvider.getInitFileContent(task, language))
            }
        }
        return LocalFileSystem.getInstance().refreshAndFindFileByIoFile(file)
    }

    private fun removeAllListeners() {
        projectToTaskToFiles.forEach { (_, taskToFiles) ->
            taskToFiles.forEach { (_, file) ->
                run {
                    ApplicationManager.getApplication().invokeAndWait {
                        val document = FileDocumentManager.getInstance().getDocument(file)
                        document?.removeDocumentListener(listener)
                    }
                }
            }
        }
    }

    /**
     *  If [documentToTask] doesn't have this [task] then we didn't track the document. Once document is added,
     *  DocumentListener is connected and tracks all document changes
     */
    private fun addTaskFile(virtualFile: VirtualFile, task: Task, project: Project) {
        val oldVirtualFile = projectToTaskToFiles[project]?.get(task)
        if (oldVirtualFile == null) {
            projectToTaskToFiles[project]?.set(task, virtualFile)
//          need to RUN ON EDT cause of read and write actions
            ApplicationManager.getApplication().invokeAndWait {
                val document = FileDocumentManager.getInstance().getDocument(virtualFile)
                document?.let {
                    it.addDocumentListener(listener)
                    // Log the first state
                    DocumentLogger.log(it)
                }
            }

        } else {
            // If the old document is not equal to the old document, we should raise an error
            if (virtualFile != oldVirtualFile) {
                val message = "${Plugin.PLUGIN_NAME}: an attempt to assign another virtualFile to the task $task in " +
                        "the project ${project}."
                logger.error(message)
                throw IllegalArgumentException(message)
            }
        }
    }

    fun openTaskFiles(task: Task, language: Language = Language.PYTHON) {
        projectToTaskToFiles.forEach { (project, taskFiles) -> openFile(project, taskFiles[task]) }
    }

    /*
     * Opens file and makes it writable
     */
    private fun openFile(project: Project, virtualFile: VirtualFile?) {
        virtualFile?.let {
            setReadOnly(it, false)
            FileEditorManager.getInstance(project).openFile(it, true, true)
        }
    }

    fun getDocument(project: Project, task: Task): Document {
        val virtualFile = projectToTaskToFiles[project]?.get(task)
            ?: throw IllegalStateException("A file for the task ${task.key} in the project ${project.name} does not exist")
        return FileDocumentManager.getInstance().getDocument(virtualFile)
            ?: throw IllegalStateException("A document for the file ${virtualFile.name} in the project ${project.name} does not exist")
    }

    fun closeTaskFiles(task: Task, language: Language = Language.PYTHON) {
        projectToTaskToFiles.forEach { (project, taskFiles) -> closeFile(project, taskFiles[task]) }
    }

    /**
     * Makes file read-only and closes it
     */
    private fun closeFile(project: Project, virtualFile: VirtualFile?) {
        virtualFile?.let {
            setReadOnly(it, true)
            FileEditorManager.getInstance(project).closeFile(virtualFile)
        }
    }

    /**
     * File is writable if it's task is null or its task is currently chosen on the TaskSolvingPane
     */
    fun Task?.isItsFileWritable(): Boolean {
        return (this == null || (this == TaskChoosingUiData.chosenTask.currentValue &&
                MainController.visiblePane == TaskSolvingControllerManager))
    }


    private fun setReadOnly(vFile: VirtualFile, readOnlyStatus: Boolean) {
        try {
            WriteAction.runAndWait<IOException> {
                ReadOnlyAttributeUtil.setReadOnlyAttribute(vFile, readOnlyStatus)
            }
        } catch (e: IOException) {
            logger.info("Exception was raised in attempt to set read only status")
        }
    }

    fun getTaskByVirtualFile(virtualFile: VirtualFile?): Task? {
//        Due to the lazy evaluation of sequences in kotlin it not so terribly complex as you may think.
//        Even if it is, we have only 3 tasks and only a couple of projects open at the same time, so it's not so bad.
        return virtualFile?.let {
            ProjectLocator.getInstance().getProjectsForFile(virtualFile).asSequence().map { project ->
                projectToTaskToFiles[project]?.entries?.firstOrNull { it.value == virtualFile }?.key
            }.firstOrNull()
        }
    }
}