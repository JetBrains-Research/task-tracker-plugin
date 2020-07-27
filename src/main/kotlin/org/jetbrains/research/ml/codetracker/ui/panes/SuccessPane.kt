package org.jetbrains.research.ml.codetracker.ui.panes

import com.intellij.openapi.project.Project
import javafx.embed.swing.JFXPanel
import javafx.fxml.FXML
import javafx.scene.control.Button
import javafx.scene.text.Text
import org.jetbrains.research.ml.codetracker.Plugin
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
    @FXML lateinit var backToTasksText: FormattedText
    @FXML lateinit var successText: Text

    override fun initialize(url: URL?, resource: ResourceBundle?) {
        logger.info("${Plugin.PLUGIN_ID}:${this::class.simpleName} init controller")
        initSuccessText()
        initButtons()
        makeTranslatable()
        super.initialize(url, resource)
    }

    private fun initSuccessText() {
        //        todo: get text from server according to LanguagePaneUiData.language.currentValue
        successText.text = "The data for the %s task has been submitted successfully."
    }

    private fun initButtons() {
//        todo: get text from server according to LanguagePaneUiData.language.currentValue
        backToTasksText.text = "back to tasks"
        backToTasksButton.onMouseClicked { changeVisiblePane(TaskChoosingControllerManager) }
    }

    private fun makeTranslatable() {
        subscribe(LanguageNotifier.LANGUAGE_TOPIC, object : LanguageNotifier {
            override fun accept(newLanguageIndex: Int) {
                val newLanguage = paneUiData.language.dataList[newLanguageIndex]
//                    todo: add translation
                    successText.text = "The data for the %s task has been submitted successfully."
                    backToTasksText.text = "back to tasks"
            }
        })

    }
}