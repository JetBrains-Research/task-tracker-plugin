package org.jetbrains.research.ml.tasktracker.ui.panes

import com.intellij.openapi.project.Project
import javafx.beans.binding.Bindings
import javafx.embed.swing.JFXPanel
import javafx.fxml.FXML
import javafx.scene.control.Button
import javafx.scene.input.MouseEvent
import javafx.scene.layout.Pane
import javafx.scene.shape.Polygon
import org.jetbrains.research.ml.tasktracker.ui.panes.util.PaneController
import org.jetbrains.research.ml.tasktracker.ui.panes.util.PaneControllerManager
import java.net.URL
import java.util.*
import kotlin.reflect.KClass


object ErrorControllerManager : PaneControllerManager<ErrorController>() {
    override val canCreateContent: Boolean = true
    override val paneControllerClass: KClass<ErrorController> = ErrorController::class
    override val fxmlFilename: String = "error-ui-form.fxml"

    fun setRefreshAction(action: (Project) -> Unit) {
        paneControllers.forEach { it.refreshAction = action }
    }
}

class ErrorController(project: Project, scale: Double, fxPanel: JFXPanel, id: Int) : PaneController(project, scale, fxPanel, id) {
    @FXML private lateinit var refreshButton: Button
    @FXML private lateinit var mainPane: Pane

    @FXML private lateinit var orangePolygon: Polygon
    @FXML private lateinit var bluePolygon: Polygon
    @FXML private lateinit var yellowPolygon: Polygon

    var refreshAction: (Project) -> Unit = { }


    override fun initialize(url: URL?, resource: ResourceBundle?) {
        mainPane.styleProperty().bind(Bindings.concat("-fx-font-size: ${scale}px;"))
        scalePolygons(arrayListOf(orangePolygon, bluePolygon, yellowPolygon))
        refreshButton.addEventHandler(MouseEvent.MOUSE_CLICKED) {
            refreshAction(project)
        }
    }
}