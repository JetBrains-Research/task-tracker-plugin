package ui

import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.ToolWindow
import com.intellij.openapi.wm.ToolWindowFactory
import javafx.application.Platform
import javafx.embed.swing.JFXPanel
import javafx.fxml.FXMLLoader
import javafx.scene.Parent
import javafx.scene.Scene
import javafx.scene.paint.Color
import java.util.logging.Logger
import javax.swing.JComponent


class PluginToolWindowFactory : ToolWindowFactory {
    private val log: Logger = Logger.getLogger(javaClass.name)

    init {
        log.info("init factory")
    }

    override fun createToolWindowContent(project: Project, toolWindow: ToolWindow) {
        log.info("create tool window")
        val content = createContent()
        toolWindow.component.add(content)
    }

    private fun createContent() : JComponent {
        val fxPanel = JFXPanel()
        val controller = Controller()
        Platform.setImplicitExit(false);
        Platform.runLater {
            val loader = FXMLLoader(javaClass.classLoader.getResource("simple-form.fxml"))
            loader.setController(controller)
            val root = loader.load<Parent>()
            val scene = Scene(root, Color.ALICEBLUE)
            fxPanel.scene = scene
        }
        return fxPanel
    }
}






