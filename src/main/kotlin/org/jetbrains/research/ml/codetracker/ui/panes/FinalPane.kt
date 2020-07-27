package org.jetbrains.research.ml.codetracker.ui.panes

import com.intellij.openapi.project.Project
import javafx.beans.binding.Bindings
import javafx.embed.swing.JFXPanel
import javafx.fxml.FXML
import javafx.scene.control.Button
import javafx.scene.control.Label
import javafx.scene.layout.AnchorPane
import javafx.scene.layout.Pane
import javafx.scene.shape.Polygon
import javafx.scene.text.Text
import org.jetbrains.research.ml.codetracker.Plugin
import org.jetbrains.research.ml.codetracker.server.PluginServer
import org.jetbrains.research.ml.codetracker.ui.panes.util.*
import java.net.URL
import java.util.*
import kotlin.reflect.KClass

object FinalControllerManager : ServerDependentPane<FinalController>() {
    override val paneControllerClass: KClass<FinalController> = FinalController::class
    override val fxmlFilename: String = "final-ui-form.fxml"
}

class FinalController(project: Project, scale: Double, fxPanel: JFXPanel, id: Int) : LanguagePaneController(project, scale, fxPanel, id) {
    @FXML lateinit var backToTasksButton: Button
    @FXML lateinit var backToTasksText: Text
    @FXML lateinit var backToProfileButton: Button
    @FXML lateinit var backToProfileText: Text

    @FXML lateinit var greatWorkLabel: Label
    @FXML lateinit var messageText: Text

    @FXML private lateinit var mainPane: AnchorPane

    @FXML private lateinit var orangePolygon: Polygon
    @FXML private lateinit var yellowPolygon: Polygon

    private val translations = PluginServer.paneText?.finalPane

    override fun initialize(url: URL?, resource: ResourceBundle?) {
        logger.info("${Plugin.PLUGIN_ID}:${this::class.simpleName} init controller")
        mainPane.styleProperty().bind(Bindings.concat("-fx-font-size: ${scale}px;"))
        scalePolygons(arrayListOf(orangePolygon, yellowPolygon))
        initButtons()
        makeTranslatable()
        super.initialize(url, resource)
    }

    private fun initButtons() {
        backToProfileButton.onMouseClicked { changeVisiblePane(SurveyControllerManager) }
        backToTasksButton.onMouseClicked { changeVisiblePane(TaskChoosingControllerManager) }
    }

    private fun makeTranslatable() {
        subscribe(LanguageNotifier.LANGUAGE_TOPIC, object : LanguageNotifier {
            override fun accept(newLanguageIndex: Int) {
                val newLanguage = LanguagePaneUiData.language.dataList[newLanguageIndex]
                val finishPaneText = translations?.get(newLanguage)
                finishPaneText?.let {
                    greatWorkLabel.text = it.praise
                    messageText.text = it.finalMessage
                    backToTasksText.text = it.backToTasks
                    backToProfileText.text = it.backToSurvey
                }
            }
        })
    }
}