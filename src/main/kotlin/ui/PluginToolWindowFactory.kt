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
import java.awt.Toolkit
import javax.swing.JComponent
import javax.swing.JPanel


class PluginToolWindowFactory : ToolWindowFactory {
    private val SCREEN_HEIGHT = 1080.0

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

        val screenSize = Toolkit.getDefaultToolkit().screenSize
        diagnosticLogger.info("Screen size: $screenSize")
        println("Screen size: $screenSize")
        var height = screenSize.height / SCREEN_HEIGHT
        diagnosticLogger.info("Height: $height")

        val controller = Controller(project, height)
        Platform.setImplicitExit(false)


        Platform.runLater {
            val loader = FXMLLoader()
            loader.namespace["height"] = height
            loader.location = javaClass.classLoader.getResource("ui-form-5.fxml")
            loader.setController(controller)
            val root = loader.load<Parent>()
            val scene = Scene(root, Color.WHITE)
            fxPanel.scene = scene
            fxPanel.background = java.awt.Color.white

        }

        panel.background = java.awt.Color.white
        panel.add(fxPanel)
        val scrollPane = JBScrollPane(panel)
        return scrollPane
    }
}






