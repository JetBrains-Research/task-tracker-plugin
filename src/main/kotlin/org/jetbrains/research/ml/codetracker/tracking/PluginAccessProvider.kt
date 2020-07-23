package org.jetbrains.research.ml.codetracker.tracking

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.fileEditor.FileDocumentManager
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.openapi.vfs.WritingAccessProvider
import org.jetbrains.research.ml.codetracker.models.Task
import org.jetbrains.research.ml.codetracker.ui.MainController
import org.jetbrains.research.ml.codetracker.ui.panes.TaskChoosingUiData
import org.jetbrains.research.ml.codetracker.ui.panes.TaskControllerManager

class PluginAccessProvider : WritingAccessProvider() {
    override fun requestWriting(files: MutableCollection<out VirtualFile>): MutableCollection<VirtualFile> {

//  Надо разрешить всем файлам, которые НЕ соединены с таском или соединены + их таск совпадает с выбранным + текущая панель это Solving
//  если есть какие-то другие файлы, то надо выдать диалог "чтобы изменить этот файл, перейдите на панель с этой задачей"

        val tasks = arrayListOf<Task>()
        val (writableFiles, readOnlyFiles) = files.partition {
            val document = FileDocumentManager.getInstance().getDocument(it)
            val task = TaskFileHandler.getTaskByDocument(document)
            if (task == null || (task == TaskChoosingUiData.chosenTask.currentValue &&
                        MainController.visiblePane == TaskControllerManager)) {
                true
            } else {
                tasks.add(task)
                false
            }
        }

        println("WritableFiles: ${writableFiles.joinToString { it.name }}")
        println("ReadOnlyFiles: ${readOnlyFiles.joinToString { it.name }}")

        if (tasks.isNotEmpty()) {
            println("read only not empty")
            ApplicationManager.getApplication().invokeAndWait {
                ReadOnlyDialogWrapper(tasks.first()).show()
            }
        }

        return readOnlyFiles.toMutableList()
    }
}