package org.jetbrains.research.ml.codetracker.tracking.dialog

import com.intellij.openapi.ui.DialogWrapper
import org.jetbrains.research.ml.codetracker.models.PaneLanguage
import org.jetbrains.research.ml.codetracker.ui.panes.SurveyUiData
import java.awt.BorderLayout
import java.awt.Dimension
import javax.swing.JComponent
import javax.swing.JLabel
import javax.swing.JPanel


abstract class CustomDialogWrapper : DialogWrapper(true) {

    protected val currentLanguage: PaneLanguage? by lazy {
        SurveyUiData.language.currentValue
    }
    abstract val customPreferredSize: Dimension?

    override fun createCenterPanel(): JComponent? {
        val dialogPanel = JPanel(BorderLayout())
        val label = JLabel(createMessage())
        // Todo: should I set it?
        label.preferredSize = preferredSize
        dialogPanel.add(label, BorderLayout.CENTER)
        return dialogPanel
    }

    abstract fun createMessage(): String
}