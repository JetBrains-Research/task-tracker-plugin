package ui

import Plugin
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.ToolWindow
import com.intellij.openapi.wm.ToolWindowFactory
import com.intellij.openapi.wm.impl.ToolWindowImpl
import com.intellij.ui.components.JBScrollPane
import javafx.application.Platform
import javafx.embed.swing.JFXPanel
import javafx.fxml.FXMLLoader
import javafx.scene.Parent
import javafx.scene.Scene
import javafx.scene.paint.Color
import javax.swing.JComponent
import javax.swing.JPanel


class PluginToolWindowFactory : ToolWindowFactory {
    private val diagnosticLogger: Logger = Logger.getInstance(javaClass)

    init {
        diagnosticLogger.info("${Plugin.PLUGIN_ID}: init factory")
    }

    override fun createToolWindowContent(project: Project, toolWindow: ToolWindow) {
        diagnosticLogger.info("${Plugin.PLUGIN_ID}: create tool window")
        val content = createContent(project)
        toolWindow.component.parent.add(content)
        toolWindow as ToolWindowImpl
    }

    private fun createContent(project: Project) : JComponent {
        val panel = JPanel()
        val fxPanel = JFXPanel()
        val controller = Controller(project)
        Platform.setImplicitExit(false);
        Platform.runLater {
            val loader = FXMLLoader(javaClass.classLoader.getResource("ui-form.fxml"))
            loader.setController(controller)
            val root = loader.load<Parent>()
            val scene = Scene(root, Color.WHITE)
            fxPanel.scene = scene
            fxPanel.background = java.awt.Color.white
        }

        panel.background = java.awt.Color.white
        panel.add(fxPanel)
        return JBScrollPane(panel)
    }
}






