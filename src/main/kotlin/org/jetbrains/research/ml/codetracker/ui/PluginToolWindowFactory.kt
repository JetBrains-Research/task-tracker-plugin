package org.jetbrains.research.ml.codetracker.ui

import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.ToolWindow
import com.intellij.openapi.wm.ToolWindowFactory
import org.jetbrains.research.ml.codetracker.Plugin
import java.awt.*
import javax.swing.*


class PluginToolWindowFactory : ToolWindowFactory {

    private val logger: Logger = Logger.getInstance(javaClass)

    init {
        logger.info("${Plugin.PLUGIN_ID}: init tool window factory")
    }

    override fun createToolWindowContent(project: Project, toolWindow: ToolWindow) {
        logger.info("${Plugin.PLUGIN_ID}: creating tool window")
        val content = if (Plugin.checkRequiredPlugins()) {
            MainController.createContent(project)
        } else  {
            createContentToRestart(project)
        }
        toolWindow.component.parent.add(content)
    }


    private fun createContentToRestart(project: Project) : JComponent {

        val panel = JPanel(GridBagLayout())
        val gbc = GridBagConstraints()
        gbc.gridwidth = GridBagConstraints.REMAINDER
        gbc.insets = Insets(3, 3, 3, 3)

        val label = JLabel("<html><b>Codetracker</b> installation is incomplete</html>")
        val button = JButton("Complete installation")
        button.addActionListener { Plugin.restartIde(project) }
        panel.add(label, gbc)
        panel.add(button, gbc)
        return panel
    }
}






