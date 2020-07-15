package org.jetbrains.research.ml.codetracker.ui.panes

import com.intellij.openapi.project.Project
import javafx.embed.swing.JFXPanel
import javafx.fxml.FXML
import javafx.scene.text.Text
import kotlin.reflect.KClass


object LoadingControllerManager : PaneControllerManager<LoadingController>() {
    override val dependsOnServerData: Boolean = false
    override val paneControllerClass: KClass<LoadingController> = LoadingController::class
    override val fxmlFilename: String = "loading-ui-form-2.fxml"
}


class LoadingController(project: Project, scale: Double, fxPanel: JFXPanel, id: Int) : PaneController(project, scale, fxPanel, id) {
    @FXML private lateinit var loadingText: Text

    override fun initialize() {}

    override fun update() {}
}