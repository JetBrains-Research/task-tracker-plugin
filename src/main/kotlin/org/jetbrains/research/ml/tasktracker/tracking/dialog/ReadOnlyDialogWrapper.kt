package org.jetbrains.research.ml.tasktracker.tracking.dialog


import org.jetbrains.research.ml.tasktracker.models.Task
import org.jetbrains.research.ml.tasktracker.models.TaskSolvingErrorText
import org.jetbrains.research.ml.tasktracker.server.PluginServer
import org.jetbrains.research.ml.tasktracker.ui.panes.util.LowerCaseFormatter
import java.awt.Dimension


class ReadOnlyDialogWrapper(private val task: Task) : CustomDialogWrapper() {
    private val formatter = LowerCaseFormatter()
    private val defaultTaskSolvingErrorText = TaskSolvingErrorText(
        "Task solving",
        "You cannot edit this file until you choose the task. To start or continue solving the %s task, choose it on the task choosing panel in the TaskTracker plugin and press %s."
    )
    override val customPreferredSize: Dimension? = Dimension(300, 150)

    override fun createMessage(): String {
        val dialogText = PluginServer.taskSolvingErrorDialogText?.translation?.get(currentLanguage)?.description
            ?: defaultTaskSolvingErrorText.description
        return "<html>${java.lang.String.format(
            dialogText,
            task.infoTranslation[currentLanguage]?.name,
            PluginServer.paneText?.taskChoosingPane?.get(currentLanguage)?.startSolving?.let { formatter.format(it) })}</html>"
    }

    init {
        init()
        title = PluginServer.taskSolvingErrorDialogText?.translation?.get(currentLanguage)?.header
            ?: defaultTaskSolvingErrorText.header
    }
}