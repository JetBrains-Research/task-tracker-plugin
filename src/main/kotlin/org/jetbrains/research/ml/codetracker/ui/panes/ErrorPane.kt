package org.jetbrains.research.ml.codetracker.ui.panes

import com.intellij.openapi.project.Project
import javafx.embed.swing.JFXPanel
import javafx.fxml.FXML
import javafx.scene.control.Button
import javafx.scene.input.MouseEvent
import javafx.scene.text.Text
import org.jetbrains.research.ml.codetracker.server.PluginServer
import org.jetbrains.research.ml.codetracker.ui.MainController
import java.net.URL
import java.util.*
import kotlin.reflect.KClass



object ErrorControllerManager : PaneControllerManager<ErrorController>() {
    override val dependsOnServerData: Boolean = false
    override val paneControllerClass: KClass<ErrorController> = ErrorController::class
    override val fxmlFilename: String = "error-ui-form.fxml"
}


class ErrorController(project: Project, scale: Double, fxPanel: JFXPanel, id: Int) : PaneController(project, scale, fxPanel, id) {
    @FXML private lateinit var refreshButton: Button
    @FXML private lateinit var errorMessageText: Text

    override fun initialize(url: URL?, resource: ResourceBundle?) {
        refreshButton.addEventHandler(MouseEvent.MOUSE_CLICKED) {
            PluginServer.reconnect()
        }
    }

    override fun update() {
    }
}