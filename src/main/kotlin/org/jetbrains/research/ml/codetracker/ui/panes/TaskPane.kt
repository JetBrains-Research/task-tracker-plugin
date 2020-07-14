package org.jetbrains.research.ml.codetracker.ui.panes

import com.intellij.openapi.project.Project
import javafx.embed.swing.JFXPanel
import javafx.fxml.FXML
import javafx.scene.control.Button
import javafx.scene.control.Label
import javafx.scene.control.TextArea
import javafx.scene.input.MouseEvent
import javafx.scene.shape.Polygon
import javafx.scene.shape.Rectangle
import javafx.scene.text.Text
import javafx.scene.text.TextFlow
import org.jetbrains.research.ml.codetracker.Plugin
import org.jetbrains.research.ml.codetracker.server.PluginServer
import org.jetbrains.research.ml.codetracker.ui.MainController
import kotlin.reflect.KClass

object TaskControllerManager : PaneControllerManager<TaskController>() {
    override val paneControllerClass: KClass<TaskController> = TaskController::class
    override val fxmlFilename: String = "task-ui-form-2.fxml"
}

class TaskController(project: Project, scale: Double, fxPanel: JFXPanel, id: Int) : LanguagePaneController(project, scale, fxPanel, id) {
    //    Scalable components
    @FXML lateinit var greenPolygon: Polygon
    @FXML lateinit var orangePolygon: Polygon
    @FXML lateinit var bluePolygon: Polygon
    @FXML lateinit var yellowRectangle: Rectangle

    //    Task info
    @FXML lateinit var taskTextFlow: TextFlow
    @FXML lateinit var taskNameText: Text
    @FXML lateinit var taskDescriptionText: Text
    @FXML lateinit var taskInputText: Text
    @FXML lateinit var taskOutputText: Text

    //    Examples
    @FXML lateinit var inputLabel: Label
    @FXML lateinit var outputLabel: Label
    @FXML lateinit var firstExampleInput: TextArea
    @FXML lateinit var firstExampleOutput: TextArea
    @FXML lateinit var secondExampleInput: TextArea
    @FXML lateinit var secondExampleOutput: TextArea
    @FXML lateinit var thirdExampleInput: TextArea
    @FXML lateinit var thirdExampleOutput: TextArea
    private lateinit var exampleTexts: List<ExampleText>
    private data class ExampleText(val input: TextArea, val output: TextArea)


    @FXML lateinit var sendSolutionButton: Button
    @FXML lateinit var sendSolutionText: Text
    @FXML lateinit var backToTasksButton: Button
    @FXML lateinit var backToTasksText: Text

    private val translations = PluginServer.paneText?.taskPane


    override fun initialize() {
        logger.info("${Plugin.PLUGIN_ID}:${this::class.simpleName} init controller")
        initTaskInfo()
        initButtons()
        makeTranslatable()
        super.initialize()
    }

    private fun initTaskInfo() {
        exampleTexts = listOf(
            ExampleText(firstExampleInput, firstExampleOutput),
            ExampleText(secondExampleInput, secondExampleOutput),
            ExampleText(thirdExampleInput, thirdExampleOutput)
        )
        subscribe(ChosenTaskNotifier.CHOSEN_TASK_TOPIC, object : ChosenTaskNotifier {
            override fun accept(newTaskIndex: Int) {
                val newTask = TaskChooserUiData.chosenTask.currentValue
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
//        Todo: add *send successful* pane?
        sendSolutionButton.onMouseClicked { changeVisiblePane(TaskChooserControllerManager) }
        backToTasksButton.onMouseClicked { changeVisiblePane(TaskChooserControllerManager) }
    }

    private fun makeTranslatable() {
        subscribe(LanguageNotifier.LANGUAGE_TOPIC, object : LanguageNotifier {
            override fun accept(newLanguageIndex: Int) {
                val newLanguage = paneUiData.language.dataList[newLanguageIndex]
                val taskPaneText = translations?.get(newLanguage)
                taskPaneText?.let {
                    inputLabel.text = it.inputData
                    outputLabel.text = it.outputData
                    sendSolutionText.text = it.submit
                    backToTasksText.text = it.backToTasks
                }

                val taskInfo = TaskChooserUiData.chosenTask.currentValue?.infoTranslation?.get(newLanguage)
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