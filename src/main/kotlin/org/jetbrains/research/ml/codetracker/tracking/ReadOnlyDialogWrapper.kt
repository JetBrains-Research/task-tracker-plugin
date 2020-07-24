package org.jetbrains.research.ml.codetracker.tracking


import com.intellij.openapi.ui.DialogWrapper
import org.jetbrains.research.ml.codetracker.models.Task
import org.jetbrains.research.ml.codetracker.models.TaskSolvingErrorText
import org.jetbrains.research.ml.codetracker.server.PluginServer
import org.jetbrains.research.ml.codetracker.ui.panes.SurveyUiData
import org.jetbrains.research.ml.codetracker.ui.panes.util.LowerCaseFormatter
import java.awt.BorderLayout
import java.awt.Dimension
import javax.swing.JComponent
import javax.swing.JLabel
import javax.swing.JPanel


class ReadOnlyDialogWrapper(private val task: Task) : DialogWrapper(true) {
    private val formatter = LowerCaseFormatter()
    private val defaultTaskSolvingErrorText = TaskSolvingErrorText(
        "Task solving",
        "You cannot edit this file until you choose the task. To start or continue solving the %s task, choose it on the task choosing panel in the codetracker plugin and press %s."
    )

    override fun createCenterPanel(): JComponent? {
        val dialogPanel = JPanel(BorderLayout())
        val label = JLabel(createMessage())
        dialogPanel.add(label, BorderLayout.CENTER)
        return dialogPanel
    }

    private fun createMessage(): String {
        val currentLanguage = SurveyUiData.language.currentValue
        val dialogText = PluginServer.taskSolvingErrorDialogText?.translation?.get(currentLanguage)?.description
            ?: defaultTaskSolvingErrorText.description
        return "<html>${java.lang.String.format(
            dialogText,
            task.infoTranslation[currentLanguage]?.name,
            PluginServer.paneText?.taskChoosingPane?.get(currentLanguage)?.startSolving?.let { formatter.format(it) })}</html>"
    }

    init {
        init()
        val currentLanguage = SurveyUiData.language.currentValue
        title = PluginServer.taskSolvingErrorDialogText?.translation?.get(currentLanguage)?.header
            ?: defaultTaskSolvingErrorText.header
    }
}