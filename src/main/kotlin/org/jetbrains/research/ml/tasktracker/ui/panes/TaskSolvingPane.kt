package org.jetbrains.research.ml.tasktracker.ui.panes

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.project.Project
import javafx.beans.binding.Bindings
import javafx.embed.swing.JFXPanel
import javafx.fxml.FXML
import javafx.scene.control.Button
import javafx.scene.control.Label
import javafx.scene.control.TextArea
import javafx.scene.layout.Pane
import javafx.scene.shape.Polygon
import javafx.scene.text.Text
import org.jetbrains.research.ml.tasktracker.Plugin
import org.jetbrains.research.ml.tasktracker.server.PluginServer
import org.jetbrains.research.ml.tasktracker.tracking.TaskFileHandler
import org.jetbrains.research.ml.tasktracker.ui.panes.util.*
import java.net.URL
import java.util.*
import kotlin.reflect.KClass

object TaskSolvingControllerManager : ServerDependentPane<TaskSolvingController>() {
    override val paneControllerClass: KClass<TaskSolvingController> = TaskSolvingController::class
    override val fxmlFilename: String = "task-solving-ui-form.fxml"
}

class TaskSolvingController(project: Project, scale: Double, fxPanel: JFXPanel, id: Int) : LanguagePaneController(project, scale, fxPanel, id) {
    //    Task info
    @FXML lateinit var taskNameText: Text
    @FXML lateinit var taskDescriptionText: Text
    @FXML lateinit var taskInputHeaderText: Text
    @FXML lateinit var taskInputText: Text
    @FXML lateinit var taskOutputHeaderText: Text
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

    @FXML private lateinit var mainPane: Pane

    @FXML private lateinit var orangePolygon: Polygon
    @FXML private lateinit var bluePolygon: Polygon
    @FXML private lateinit var greenPolygon: Polygon

    private val translations = PluginServer.paneText?.taskSolvingPane

    override fun initialize(url: URL?, resource: ResourceBundle?) {
        logger.info("${Plugin.PLUGIN_NAME}:${this::class.simpleName} init controller")
        mainPane.styleProperty().bind(Bindings.concat("-fx-font-size: ${scale}px;"))
        scalePolygons(arrayListOf(orangePolygon, bluePolygon, greenPolygon))
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
                    val newTaskInfo = newTask.infoTranslation[LanguagePaneUiData.language.currentValue]
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
                    PluginServer.sendDataForTask(it, project)
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
                val newLanguage = LanguagePaneUiData.language.dataList[newLanguageIndex]
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