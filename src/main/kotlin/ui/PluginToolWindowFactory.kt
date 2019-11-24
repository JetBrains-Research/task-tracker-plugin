package ui

import com.intellij.openapi.project.Project
import com.intellij.openapi.util.IconLoader
import com.intellij.openapi.wm.ToolWindow
import com.intellij.openapi.wm.ToolWindowFactory
import com.intellij.ui.content.ContentFactory
import javafx.application.Platform
import javafx.embed.swing.JFXPanel
import javafx.fxml.FXMLLoader
import javafx.scene.Group
import javafx.scene.Parent
import javafx.scene.Scene
import javafx.scene.paint.Color
import javafx.scene.text.Font
import javafx.scene.text.Text
import javax.swing.GroupLayout


class PluginToolWindowFactory : ToolWindowFactory {

    override fun createToolWindowContent(project: Project, toolWindow: ToolWindow) {

       // val myToolWindow = PluginToolWindow(toolWindow)
       // val infoForm = PersonalInfoForm()
        val contentFactory = ContentFactory.SERVICE.getInstance()
//        val content = contentFactory.createContent(PluginPanel().content(), "", false)
//        toolWindow.contentManager.addContent(content)

        val fxPanel = JFXPanel()
        val component = toolWindow.component

        Platform.setImplicitExit(false);

        Platform.runLater {

            val controller = PluginController()
            val loader = FXMLLoader(javaClass.classLoader.getResource("simple-form.fxml"))
           // loader.setController(controller)
            val root = loader.load<Parent>()
            val scene = Scene(root, Color.ALICEBLUE)
            fxPanel.scene = scene
        }

        component.parent.add(fxPanel)
        component.preferredSize = fxPanel.preferredSize
    }
}






