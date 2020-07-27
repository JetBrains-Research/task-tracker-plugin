package org.jetbrains.research.ml.codetracker.ui.panes

import com.intellij.openapi.project.Project
import javafx.beans.binding.Bindings
import javafx.embed.swing.JFXPanel
import javafx.fxml.FXML
import javafx.scene.control.Button
import javafx.scene.layout.Pane
import javafx.scene.text.Text
import org.jetbrains.research.ml.codetracker.Plugin
import org.jetbrains.research.ml.codetracker.models.SuccessPaneText
import org.jetbrains.research.ml.codetracker.server.PluginServer
import org.jetbrains.research.ml.codetracker.ui.panes.util.*
import java.net.URL
import java.util.*
import kotlin.reflect.KClass

object SuccessControllerManager : ServerDependentPane<SuccessController>() {
    override val paneControllerClass: KClass<SuccessController> = SuccessController::class
    override val fxmlFilename: String = "success-ui-form-2.fxml"
}

class SuccessController(project: Project, scale: Double, fxPanel: JFXPanel, id: Int) : LanguagePaneController(project, scale, fxPanel, id) {
    @FXML lateinit var backToTasksButton: Button
    @FXML lateinit var backToTasksText: Text
    @FXML lateinit var successText: Text

    @FXML private lateinit var mainPane: Pane

    private val translations = PluginServer.paneText?.successPane
    private val defaultSuccessPaneText = SuccessPaneText("back to tasks",
        "The data for the %s task has been submitted successfully.")

    override fun initialize(url: URL?, resource: ResourceBundle?) {
        logger.info("${Plugin.PLUGIN_ID}:${this::class.simpleName} init controller")
        mainPane.styleProperty().bind(Bindings.concat("-fx-font-size: ${scale}px;"))
        initSuccessText()
        initButtons()
        makeTranslatable()
        super.initialize(url, resource)
    }

    private fun initSuccessText() {
        val text = translations?.get(LanguagePaneUiData.language.currentValue)?.successMessage ?: defaultSuccessPaneText.successMessage
        successText.text = getFormattedText(text)
    }

    private fun getFormattedText(text: String): String {
        // Todo: task always is null. Why?
        val currentTask = TaskChoosingUiData.chosenTask.currentValue
        currentTask?.let {
            return java.lang.String.format(text, currentTask.key)
        }
        return java.lang.String.format(text, "")
    }

    private fun initButtons() {
        backToTasksText.text = translations?.get(LanguagePaneUiData.language.currentValue)?.backToTasks ?: defaultSuccessPaneText.backToTasks
        backToTasksButton.onMouseClicked { changeVisiblePane(TaskChoosingControllerManager) }
    }

    private fun makeTranslatable() {
        subscribe(LanguageNotifier.LANGUAGE_TOPIC, object : LanguageNotifier {
            override fun accept(newLanguageIndex: Int) {
                val newLanguage = LanguagePaneUiData.language.dataList[newLanguageIndex]
                val successPaneText = translations?.get(newLanguage)
                successPaneText?.let {
                    successText.text = getFormattedText(it.successMessage)
                    backToTasksText.text = it.backToTasks
                }
            }
        })

    }
}