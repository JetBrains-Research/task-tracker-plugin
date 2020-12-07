package org.jetbrains.research.ml.tasktracker.ui.panes

import com.intellij.openapi.project.Project
import javafx.beans.binding.Bindings
import javafx.embed.swing.JFXPanel
import javafx.fxml.FXML
import javafx.scene.control.Button
import javafx.scene.layout.Pane
import javafx.scene.shape.Polygon
import javafx.scene.text.Text
import org.jetbrains.research.ml.tasktracker.Plugin
import org.jetbrains.research.ml.tasktracker.server.PluginServer
import org.jetbrains.research.ml.tasktracker.ui.panes.util.*
import java.net.URL
import java.util.*
import kotlin.reflect.KClass

object SuccessControllerManager : ServerDependentPane<SuccessController>() {
    override val paneControllerClass: KClass<SuccessController> = SuccessController::class
    override val fxmlFilename: String = "success-ui-form.fxml"
}

class SuccessController(project: Project, scale: Double, fxPanel: JFXPanel, id: Int) : LanguagePaneController(project, scale, fxPanel, id) {
    @FXML lateinit var backToTasksButton: Button
    @FXML lateinit var backToTasksText: Text
    @FXML lateinit var firstNonBoldText: Text
    @FXML lateinit var secondNonBoldText: Text
    lateinit var nonBoldTexts: List<Text>
    @FXML lateinit var firstBoldText: Text

    @FXML private lateinit var mainPane: Pane

    @FXML private lateinit var orangePolygon: Polygon
    @FXML private lateinit var bluePolygon: Polygon
    @FXML private lateinit var yellowPolygon: Polygon

    private val translations = PluginServer.paneText?.successPane

    override fun initialize(url: URL?, resource: ResourceBundle?) {
        logger.info("${Plugin.PLUGIN_NAME}:${this::class.simpleName} init controller")
        mainPane.styleProperty().bind(Bindings.concat("-fx-font-size: ${scale}px;"))
        scalePolygons(arrayListOf(orangePolygon, bluePolygon, yellowPolygon))
        initSuccessText()
        initButtons()
        makeTranslatable()
        super.initialize(url, resource)
    }

    private fun initSuccessText() {
        nonBoldTexts = arrayListOf(firstNonBoldText, secondNonBoldText)
        subscribe(ChosenTaskNotifier.CHOSEN_TASK_TOPIC, object : ChosenTaskNotifier {
            override fun accept(newTaskIndex: Int) {
                setSuccessText()
            }
        })

    }

    private fun setSuccessText(default: String = "") {
        val language = LanguagePaneUiData.language.currentValue
        val currentTask = TaskChoosingUiData.chosenTask.currentValue
        val translatedTask = currentTask?.infoTranslation?.get(language)?.name ?: default
        val successText =  translations?.get(language)?.successMessage ?: ""
        successText.split("%s").zip(nonBoldTexts).forEach { (s, t) -> t.text = s }
        firstBoldText.text = translatedTask
    }


    private fun initButtons() {
        backToTasksText.text = translations?.get(LanguagePaneUiData.language.currentValue)?.backToTasks
        backToTasksButton.onMouseClicked { changeVisiblePane(TaskChoosingControllerManager) }
    }

    private fun makeTranslatable() {
        subscribe(LanguageNotifier.LANGUAGE_TOPIC, object : LanguageNotifier {
            override fun accept(newLanguageIndex: Int) {
                val newLanguage = LanguagePaneUiData.language.dataList[newLanguageIndex]
                val successPaneText = translations?.get(newLanguage)
                successPaneText?.let {
                    setSuccessText()
                    backToTasksText.text = it.backToTasks
                }
            }
        })

    }
}