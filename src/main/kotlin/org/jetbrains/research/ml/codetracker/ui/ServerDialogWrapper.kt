package org.jetbrains.research.ml.codetracker.ui


import com.intellij.openapi.ui.DialogWrapper
import java.awt.BorderLayout
import java.awt.Dimension
import javax.swing.JComponent
import javax.swing.JLabel
import javax.swing.JPanel


class ServerDialogWrapper : DialogWrapper(true) {
    private val message =
        "Произошла ошибка отправки данных. Пожалуйста, напишите об этом на почту codetracker.team@gmail.com"

    override fun createCenterPanel(): JComponent? {
        val dialogPanel = JPanel(BorderLayout())
        val label = JLabel(message)
        label.preferredSize = Dimension(100, 100)
        dialogPanel.add(label, BorderLayout.CENTER)
        return dialogPanel
    }

    init {
        init()
        title = "Ошибка"
    }
}