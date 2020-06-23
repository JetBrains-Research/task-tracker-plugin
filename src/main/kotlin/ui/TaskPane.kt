package ui

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
import kotlin.reflect.KClass

enum class TaskNotifyEvent : IPaneNotifyEvent {
    LANGUAGE_NOTIFY
}

object TaskControllerManager : PaneControllerManager<TaskNotifyEvent, TaskController>() {
    override val paneControllerClass: KClass<TaskController> = TaskController::class
    override val paneControllers: MutableList<TaskController> = arrayListOf()
    override val fxmlFilename: String = "task-ui-form-2.fxml"
    override val paneUiData: PaneUiData<TaskNotifyEvent> = TaskUiData

    override fun notify(notifyEvent: TaskNotifyEvent, new: Any?) {
        when (notifyEvent) {
            TaskNotifyEvent.LANGUAGE_NOTIFY -> switchLanguage(new as Int)
        }
    }

}

object TaskUiData : PaneUiData<TaskNotifyEvent>(TaskControllerManager) {
    override fun getData(): List<UiField<*>> = arrayListOf()
    override val currentLanguage: LanguageUiField = LanguageUiField(TaskNotifyEvent.LANGUAGE_NOTIFY)
}

class TaskController(override val uiData: TaskUiData, scale: Double, fxPanel: JFXPanel, id: Int) : PaneController<TaskNotifyEvent>(uiData, scale, fxPanel, id) {
//    @FXML lateinit var taskPane: Pane

    @FXML lateinit var greenPolygon: Polygon
    @FXML lateinit var orangePolygon: Polygon
    @FXML lateinit var bluePolygon: Polygon
    @FXML lateinit var yellowRectangle: Rectangle

    @FXML lateinit var taskTextFlow: TextFlow
    @FXML lateinit var taskNameText: Text
    @FXML lateinit var taskDescriptionText: Text
    @FXML lateinit var taskInputText: Text
    @FXML lateinit var taskOutputText: Text

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

    private fun initButtons() {
        sendSolutionButton.addEventHandler(MouseEvent.MOUSE_CLICKED) {
            MainController.visiblePaneControllerManager = TaskChooserControllerManager
//            Todo: send data here?
//            Todo: add send successful sign to the tasks
        }
        backToTasksButton.addEventHandler(MouseEvent.MOUSE_CLICKED) {
            MainController.visiblePaneControllerManager = TaskChooserControllerManager
        }
    }

}