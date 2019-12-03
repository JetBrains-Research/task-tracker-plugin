package ui

import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.ToolWindow
import com.intellij.openapi.wm.ToolWindowFactory
import com.intellij.openapi.wm.ToolWindowManager
import javafx.application.Platform
import javafx.embed.swing.JFXPanel
import javafx.fxml.FXMLLoader
import javafx.scene.Parent
import javafx.scene.Scene
import javafx.scene.paint.Color
import java.awt.BorderLayout
import java.awt.Dimension
import java.awt.GridBagConstraints
import java.awt.GridLayout
import java.util.logging.Logger
import javax.swing.*
import javax.swing.Box.createVerticalGlue
import javax.swing.Box.createHorizontalGlue
import javax.swing.text.StyleConstants.getComponent
import com.intellij.openapi.wm.ex.ToolWindowEx
import com.intellij.openapi.wm.impl.ToolWindowImpl
import com.intellij.ui.components.JBScrollPane


class PluginToolWindowFactory : ToolWindowFactory {
    private val log: Logger = Logger.getLogger(javaClass.name)

    init {
        log.info("init factory")
    }

    override fun createToolWindowContent(project: Project, toolWindow: ToolWindow) {
        log.info("create tool window")
        val content = createContent()
        toolWindow.component.parent.add(content)
        toolWindow as ToolWindowImpl
    }

    private fun createContent() : JComponent {
        val panel = JPanel()
        val fxPanel = JFXPanel()
        val controller = Controller()
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






