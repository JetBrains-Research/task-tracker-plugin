package org.jetbrains.research.ml.codetracker.ui.panes

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.project.Project
import javafx.embed.swing.JFXPanel
import javafx.fxml.FXML
import javafx.scene.control.Button
import javafx.scene.control.TextArea
import javafx.scene.text.Text
import org.jetbrains.research.ml.codetracker.Plugin
import org.jetbrains.research.ml.codetracker.server.PluginServer
import org.jetbrains.research.ml.codetracker.tracking.TaskFileHandler
import org.jetbrains.research.ml.codetracker.ui.panes.util.FormattedLabel
import org.jetbrains.research.ml.codetracker.ui.panes.util.FormattedText

import org.jetbrains.research.ml.codetracker.ui.panes.util.*
import java.net.URL
import java.util.*
import kotlin.reflect.KClass

object TaskControllerManager : ServerDependentPane<TaskController>() {
    override val paneControllerClass: KClass<TaskController> = TaskController::class
    override val fxmlFilename: String = "task-ui-form.fxml"
}

class TaskController(project: Project, scale: Double, fxPanel: JFXPanel, id: Int) : LanguagePaneController(project, scale, fxPanel, id) {
    //    Task info
    @FXML lateinit var taskNameText: FormattedText
    @FXML lateinit var taskDescriptionText: Text
    @FXML lateinit var taskInputHeaderText: FormattedText
    @FXML lateinit var taskInputText: Text
    @FXML lateinit var taskOutputHeaderText: FormattedText
    @FXML lateinit var taskOutputText: Text

    //    Examples
    @FXML lateinit var inputLabel: FormattedLabel
    @FXML lateinit var outputLabel: FormattedLabel
    @FXML lateinit var firstExampleInput: TextArea
    @FXML lateinit var firstExampleOutput: TextArea
    @FXML lateinit var secondExampleInput: TextArea
    @FXML lateinit var secondExampleOutput: TextArea
    @FXML lateinit var thirdExampleInput: TextArea
    @FXML lateinit var thirdExampleOutput: TextArea
    private lateinit var exampleTexts: List<ExampleText>
    private data class ExampleText(val input: TextArea, val output: TextArea)

    @FXML lateinit var sendSolutionButton: Button
    @FXML lateinit var sendSolutionText: FormattedText
    @FXML lateinit var backToTasksButton: Button
    @FXML lateinit var backToTasksText: FormattedText

    private val translations = PluginServer.paneText?.taskSolvingPane

    override fun initialize(url: URL?, resource: ResourceBundle?) {
        logger.info("${Plugin.PLUGIN_ID}:${this::class.simpleName} init controller")
        initTaskInfo()
        initButtons()
        makeTranslatable()
        super.initialize(url, resource)
    }

    private fun initTaskInfo() {
        exampleTexts = listOf(
            ExampleText(firstExampleInput, firstExampleOutput),
            ExampleText(secondExampleInput, secondExampleOutput),
            ExampleText(thirdExampleInput, thirdExampleOutput)
        )
        subscribe(ChosenTaskNotifier.CHOSEN_TASK_TOPIC, object : ChosenTaskNotifier {
            override fun accept(newTaskIndex: Int) {
                val newTask = TaskChoosingUiData.chosenTask.currentValue
                newTask?.let {
                    val newTaskInfo = newTask.infoTranslation[paneUiData.language.currentValue]
                    newTaskInfo?.let {
                        taskNameText.text = it.name
                        taskDescriptionText.text = it.description
                        taskInputText.text = it.input
                        taskOutputText.text = it.output
                    }
                    exampleTexts.zip(newTask.examples) { t, e ->
                        t.input.text = e.input
                        t.output.text = e.output
                    }
                }
            }
        })
    }

    private fun initButtons() {
        sendSolutionButton.onMouseClicked {
            val currentTask = TaskChoosingUiData.chosenTask.currentValue
            currentTask?.let {
                ApplicationManager.getApplication().invokeLater {
//                    TODO: send data here
                    TaskFileHandler.closeTaskFiles(it)
                }
            }
            changeVisiblePane(TaskChoosingControllerManager)

        }
        backToTasksButton.onMouseClicked { changeVisiblePane(TaskChoosingControllerManager) }
    }

    private fun makeTranslatable() {
        subscribe(LanguageNotifier.LANGUAGE_TOPIC, object : LanguageNotifier {
            override fun accept(newLanguageIndex: Int) {
                val newLanguage = paneUiData.language.dataList[newLanguageIndex]
                val taskPaneText = translations?.get(newLanguage)
                taskPaneText?.let {
                    taskInputHeaderText.text = it.inputData
                    taskOutputHeaderText.text = it.outputData
                    inputLabel.text = it.inputData
                    outputLabel.text = it.outputData
                    sendSolutionText.text = it.submit
                    backToTasksText.text = it.backToTasks
                }

                val taskInfo = TaskChoosingUiData.chosenTask.currentValue?.infoTranslation?.get(newLanguage)
                taskInfo?.let {
                    taskNameText.text = it.name
                    taskDescriptionText.text = it.description
                    taskInputText.text = it.input
                    taskOutputText.text = it.output
                }
            }
        })
    }
}