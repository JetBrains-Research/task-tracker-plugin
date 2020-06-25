package org.jetbrains.research.ml.codetracker.ui.panes

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
import org.jetbrains.research.ml.codetracker.ui.MainController
import org.jetbrains.research.ml.codetracker.ui.makeTranslatable
import kotlin.reflect.KClass

enum class TaskNotifyEvent : IPaneNotifyEvent {
    LANGUAGE_NOTIFY
}

object TaskControllerManager : PaneControllerManager<TaskNotifyEvent, TaskController>() {
    override val paneControllerClass: KClass<TaskController> = TaskController::class
    override val paneControllers: MutableList<TaskController> = arrayListOf()
    override val fxmlFilename: String = "task-ui-form-2.fxml"
    override val paneUiData: PaneUiData<TaskNotifyEvent> =
        TaskUiData

    override fun notify(notifyEvent: TaskNotifyEvent, new: Any?) {
        when (notifyEvent) {
            TaskNotifyEvent.LANGUAGE_NOTIFY -> switchLanguage(new as Int)
        }
    }
}

object TaskUiData : PaneUiData<TaskNotifyEvent>(
    TaskControllerManager
) {
    override fun getData(): List<UiField<*>> = arrayListOf()
    override val currentLanguage: LanguageUiField = LanguageUiField(
        TaskNotifyEvent.LANGUAGE_NOTIFY
    )
}

class TaskController(override val uiData: TaskUiData, scale: Double, fxPanel: JFXPanel, id: Int) : PaneController<TaskNotifyEvent>(uiData, scale, fxPanel, id) {
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

    @FXML lateinit var sendSolutionButton: Button
    @FXML lateinit var sendSolutionText: Text
    @FXML lateinit var backToTasksButton: Button
    @FXML lateinit var backToTasksText: Text



    override fun initialize() {
        initButtons()
        super.initialize()
    }

    override fun makeTranslatable() {
//        Todo: find out the best way to translate task info because it depends on chosen task
        taskNameText.makeTranslatable("${TaskChooserUiData.chosenTask.uiValue}:${::taskNameText.name}")
        taskDescriptionText.makeTranslatable("${TaskChooserUiData.chosenTask.uiValue}:${::taskDescriptionText.name}")
        taskInputText.makeTranslatable("${TaskChooserUiData.chosenTask.uiValue}:${::taskInputText.name}")
        taskOutputText.makeTranslatable("${TaskChooserUiData.chosenTask.uiValue}:${::taskOutputText.name}")
        inputLabel.makeTranslatable(::inputLabel.name)
        outputLabel.makeTranslatable(::outputLabel.name)
        sendSolutionText.makeTranslatable(::sendSolutionText.name)
        backToTasksText.makeTranslatable(::backToTasksText.name)
    }

    private fun initButtons() {
        sendSolutionButton.addEventHandler(MouseEvent.MOUSE_CLICKED) {
            MainController.visiblePaneControllerManager =
                TaskChooserControllerManager
//            Todo: send data here
        }
        backToTasksButton.addEventHandler(MouseEvent.MOUSE_CLICKED) {
            MainController.visiblePaneControllerManager =
                TaskChooserControllerManager
        }
    }

}