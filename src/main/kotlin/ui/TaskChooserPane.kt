package ui

import data.Task
import javafx.collections.FXCollections
import javafx.embed.swing.JFXPanel
import javafx.fxml.FXML
import javafx.scene.control.Button
import javafx.scene.control.ComboBox
import javafx.scene.control.Label
import javafx.scene.input.MouseEvent
import javafx.scene.layout.Pane
import javafx.scene.shape.Polygon
import javafx.scene.shape.Rectangle
import javafx.scene.text.Text
import kotlin.reflect.KClass

enum class TaskChooserNotifyEvent : PaneNotifyEvent {
    CHOSEN_TASK_NOTIFY
}

object TaskChooserControllerManager : PaneControllerManager<TaskChooserNotifyEvent, TaskChooserController>() {
    override val paneControllerClass: KClass<TaskChooserController> = TaskChooserController::class
    override var paneControllers: MutableList<TaskChooserController> = arrayListOf()
    override val paneUiData: PaneUiData<TaskChooserNotifyEvent> = TaskChooserUiData
    override val fxmlFilename: String = "taskChooser-ui-form-2.fxml"

    override fun notify(
        notifyEvent: TaskChooserNotifyEvent,
        new: Any?,
        controllers: MutableList<TaskChooserController>
    ) {
        when (notifyEvent) {
            TaskChooserNotifyEvent.CHOSEN_TASK_NOTIFY -> controllers.forEach { it.selectTask(new as Int) }
        }
    }
}

object TaskChooserUiData : PaneUiData<TaskChooserNotifyEvent>(TaskChooserControllerManager) {
//    Todo: get tasks from server
    val tasks: List<Task> = arrayListOf(Task("key1", "name1"), Task("key2", "name2"))
    val chosenTask = ListedUiField(tasks, TaskChooserNotifyEvent.CHOSEN_TASK_NOTIFY, "chosenTask")
    override fun getData(): List<UiField<*>> = listOf(chosenTask)
}



class TaskChooserController(override val uiData: TaskChooserUiData, scale: Double, fxPanel: JFXPanel, id: Int) : PaneController<TaskChooserNotifyEvent>(uiData, scale, fxPanel, id) {
    @FXML private lateinit var taskChooserPane: Pane

    @FXML private lateinit var orangePolygon: Polygon
    @FXML private lateinit var yellowRectangle: Rectangle
    @FXML private lateinit var bluePolygon: Polygon

    @FXML private lateinit var choseTaskComboBox: ComboBox<String>
    @FXML private lateinit var choseTaskLabel: Label

    @FXML private lateinit var backToProfileButton: Button
    @FXML private lateinit var startSolvingButton: Button
    @FXML private lateinit var startSolvingText: Text
    @FXML private lateinit var finishWorkButton: Button
    @FXML private lateinit var finishWorkText: Text

    @FXML private lateinit var languageComboBox: ComboBox<String>


    override fun initialize() {
        initChoseTaskComboBox()
        initButtons()
    }

    fun selectTask(newTaskIndex: Int) {
        choseTaskComboBox.selectionModel.select(newTaskIndex)
    }

    private fun initChoseTaskComboBox() {
        choseTaskComboBox.items = FXCollections.observableList(uiData.tasks.map { it.name })
        choseTaskComboBox.selectionModel.selectedItemProperty().addListener { _, old, new ->
//            diagnosticLogger.info("${Plugin.PLUGIN_ID}, controller${id}: choicebox changed from $old to $new")
            uiData.chosenTask.uiValue = choseTaskComboBox.selectionModel.selectedIndex
        }
    }

    private fun initButtons() {
//        Todo: add other buttons
        backToProfileButton.addEventHandler(MouseEvent.MOUSE_CLICKED) {
            MainController.visiblePaneControllerManager = ProfileControllerManager
        }
    }
}