package org.jetbrains.research.ml.codetracker.ui.panes

import com.intellij.openapi.project.Project
import javafx.beans.binding.Bindings
import javafx.embed.swing.JFXPanel
import javafx.fxml.FXML
import javafx.scene.layout.Pane
import javafx.scene.shape.Polygon
import javafx.scene.text.Text
import org.jetbrains.research.ml.codetracker.ui.panes.util.PaneController
import org.jetbrains.research.ml.codetracker.ui.panes.util.PaneControllerManager
import java.net.URL
import java.util.*
import kotlin.reflect.KClass


object LoadingControllerManager : PaneControllerManager<LoadingController>() {
    override val canCreateContent: Boolean = true
    override val paneControllerClass: KClass<LoadingController> = LoadingController::class
    override val fxmlFilename: String = "loading-ui-form.fxml"
}

class LoadingController(project: Project, scale: Double, fxPanel: JFXPanel, id: Int) : PaneController(project, scale, fxPanel, id) {
    @FXML private lateinit var loadingText: Text
    @FXML private lateinit var mainPane: Pane

    @FXML private lateinit var orangePolygon: Polygon
    @FXML private lateinit var bluePolygon: Polygon
    @FXML private lateinit var yellowPolygon: Polygon

    override fun initialize(url: URL?, resource: ResourceBundle?) {
        mainPane.styleProperty().bind(Bindings.concat("-fx-font-size: ${scale}px;"))
        scalePolygons(arrayListOf(orangePolygon, bluePolygon, yellowPolygon))
    }
}