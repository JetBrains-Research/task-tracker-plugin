package org.jetbrains.research.ml.codetracker.tracking


import com.intellij.openapi.ui.DialogWrapper
import org.jetbrains.research.ml.codetracker.models.Task
import org.jetbrains.research.ml.codetracker.models.TaskChoosePaneText
import org.jetbrains.research.ml.codetracker.server.PluginServer
import org.jetbrains.research.ml.codetracker.ui.panes.SurveyUiData
import org.jetbrains.research.ml.codetracker.ui.panes.util.LanguagePaneUiData
import org.jetbrains.research.ml.codetracker.ui.panes.util.LowerCaseFormatter
import java.awt.BorderLayout
import java.awt.Dimension
import javax.swing.JComponent
import javax.swing.JLabel
import javax.swing.JPanel


class ReadOnlyDialogWrapper(private val task: Task) : DialogWrapper(true) {
    private val formatter = LowerCaseFormatter()

    override fun createCenterPanel(): JComponent? {
        val dialogPanel = JPanel(BorderLayout())
        val label = JLabel(createMessage())
        dialogPanel.add(label, BorderLayout.CENTER)
        return dialogPanel
    }

    private fun createMessage(): String {
//        todo: get from server
        val currentLanguage = SurveyUiData.language.currentValue
        return "<html>Для начала решения задачи <b>${task.infoTranslation[currentLanguage]?.name}</b>" +
                "выберите эту задачу на панели codetracker и нажмите " +
                "<b>${PluginServer.paneText?.taskChoosePane?.get(currentLanguage)?.startSolving?.let { formatter.format(it)} }</b>"
    }

    init {
        init()
        title = "Задача не выбрана"
    }
}