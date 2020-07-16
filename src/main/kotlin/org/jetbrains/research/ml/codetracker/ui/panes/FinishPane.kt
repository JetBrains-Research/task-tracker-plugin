package org.jetbrains.research.ml.codetracker.ui.panes

import com.intellij.openapi.project.Project
import javafx.embed.swing.JFXPanel
import javafx.fxml.FXML
import javafx.scene.control.Button
import javafx.scene.control.Label
import javafx.scene.shape.Polygon
import javafx.scene.shape.Rectangle
import javafx.scene.text.Text
import org.jetbrains.research.ml.codetracker.Plugin
import org.jetbrains.research.ml.codetracker.server.PluginServer
import org.jetbrains.research.ml.codetracker.ui.FormattedLabel
import org.jetbrains.research.ml.codetracker.ui.FormattedText
import java.net.URL
import java.util.*
import kotlin.reflect.KClass

object FinishControllerManager : PaneControllerManager<FinishController>() {
    override val dependsOnServerData: Boolean = true
    override val paneControllerClass: KClass<FinishController> = FinishController::class
    override val fxmlFilename: String = "finish-ui-form.fxml"
}


class FinishController(project: Project, scale: Double, fxPanel: JFXPanel, id: Int) : LanguagePaneController(project, scale, fxPanel, id) {
    //    @FXML lateinit var finishPane: Pane

    @FXML lateinit var blueRectangle: Rectangle
    @FXML lateinit var orangePolygon: Polygon
    @FXML lateinit var yellowPolygon: Polygon

    @FXML lateinit var backToTasksButton: Button
    @FXML lateinit var backToTasksText: FormattedText
    @FXML lateinit var backToProfileButton: Button
    @FXML lateinit var backToProfileText: FormattedText

    @FXML lateinit var greatWorkLabel: FormattedLabel
    @FXML lateinit var messageText: FormattedText

    private val translations = PluginServer.paneText?.finishPane

    override fun initialize(url: URL?, resource: ResourceBundle?) {
        logger.info("${Plugin.PLUGIN_ID}:${this::class.simpleName} init controller")
        initButtons()
        makeTranslatable()
        super.initialize(url, resource)
    }

    private fun initButtons() {
        backToProfileButton.switchPaneOnMouseClicked(ProfileControllerManager)
        backToTasksButton.switchPaneOnMouseClicked(TaskChooserControllerManager)
    }

    private fun makeTranslatable() {
        subscribe(LanguageNotifier.LANGUAGE_TOPIC, object : LanguageNotifier {
            override fun accept(newLanguageIndex: Int) {
                val newLanguage = paneUiData.language.dataList[newLanguageIndex]
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