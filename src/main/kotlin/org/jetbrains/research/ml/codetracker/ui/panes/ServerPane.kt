package org.jetbrains.research.ml.codetracker.ui.panes

import com.intellij.openapi.project.Project
import javafx.embed.swing.JFXPanel
import javafx.fxml.FXML
import javafx.scene.control.Button
import javafx.scene.input.MouseEvent
import org.jetbrains.research.ml.codetracker.Plugin
import org.jetbrains.research.ml.codetracker.server.PluginServer
import kotlin.reflect.KClass



object ServerControllerManager : PaneControllerManager<ServerController>() {
    override val paneControllerClass: KClass<ServerController> = ServerController::class
    override val fxmlFilename: String = "server-ui-form-2.fxml"
}


class ServerController(project: Project, scale: Double, fxPanel: JFXPanel, id: Int) : PaneController(project, scale, fxPanel, id) {
    @FXML private lateinit var refreshButton: Button

    override fun initialize() {
        refreshButton.addEventHandler(MouseEvent.MOUSE_CLICKED) {
            PluginServer.reconnect()
        }
    }

    override fun update() { }

}