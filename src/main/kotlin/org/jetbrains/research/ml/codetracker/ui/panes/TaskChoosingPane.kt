package org.jetbrains.research.ml.codetracker.ui.panes

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.project.Project
import com.intellij.util.messages.Topic
import javafx.beans.binding.Bindings
import javafx.collections.FXCollections
import javafx.collections.ObservableList
import javafx.embed.swing.JFXPanel
import javafx.fxml.FXML
import javafx.scene.control.Button
import javafx.scene.control.ComboBox
import javafx.scene.control.Label
import javafx.scene.layout.Pane
import javafx.scene.shape.Polygon
import javafx.scene.text.Text
import org.jetbrains.research.ml.codetracker.Plugin
import org.jetbrains.research.ml.codetracker.tracking.TaskFileHandler
import org.jetbrains.research.ml.codetracker.server.PluginServer
import org.jetbrains.research.ml.codetracker.ui.panes.util.*
import java.net.URL
import java.util.*
import java.util.function.Consumer
import kotlin.reflect.KClass


object TaskChoosingControllerManager : ServerDependentPane<TaskChoosingController>() {
    override val paneControllerClass: KClass<TaskChoosingController> = TaskChoosingController::class
    override val fxmlFilename: String = "task-choosing-ui-form.fxml"
}

interface ChosenTaskNotifier : Consumer<Int> {
    companion object {
        val CHOSEN_TASK_TOPIC = Topic.create("chosen task change", ChosenTaskNotifier::class.java)
    }
}

object TaskChoosingUiData : LanguagePaneUiData() {
    val chosenTask = ListedUiField(PluginServer.tasks, -1, ChosenTaskNotifier.CHOSEN_TASK_TOPIC)
    override fun getData(): List<UiField<*>> = listOf(chosenTask, language)
}


class TaskChoosingController(project: Project, scale: Double, fxPanel: JFXPanel, id: Int) :
    LanguagePaneController(project, scale, fxPanel, id) {
    @FXML
    private lateinit var choseTaskComboBox: ComboBox<String?>
    @FXML
    private lateinit var choseTaskLabel: Label
    private lateinit var choseTaskObservableList: ObservableList<String?>

    //    Todo: maybe we need a text under this button because when user comes back from TaskPane it becomes unclear
    @FXML
    private lateinit var backToProfileButton: Button
    @FXML
    private lateinit var startSolvingButton: Button
    @FXML
    private lateinit var startSolvingText: Text
    @FXML
    private lateinit var finishWorkButton: Button
    @FXML
    private lateinit var finishWorkText: Text
    @FXML
    private lateinit var firstNonBoldText: Text
    @FXML
    private lateinit var secondNonBoldText: Text
    @FXML
    private lateinit var thirdNonBoldText: Text
    @FXML
    private lateinit var fourthNonBoldText: Text
    private lateinit var nonBoldTexts: List<Text>

    @FXML
    private lateinit var firstBoldText: Text
    @FXML
    private lateinit var secondBoldText: Text
    @FXML
    private lateinit var thirdBoldText: Text
    private lateinit var boldTexts: List<Text>

    @FXML private lateinit var mainPane: Pane

    @FXML private lateinit var orangePolygon: Polygon
    @FXML private lateinit var bluePolygon: Polygon

    override val paneUiData = TaskChoosingUiData
    private val translations = PluginServer.paneText?.taskChoosingPane

    override fun initialize(url: URL?, resource: ResourceBundle?) {
        logger.info("${Plugin.PLUGIN_NAME}:${this::class.simpleName} init controller")
        mainPane.styleProperty().bind(Bindings.concat("-fx-font-size: ${scale}px;"))
        scalePolygons(arrayListOf(orangePolygon, bluePolygon))
        initChoseTaskComboBox()
        initInstruction()
        initButtons()
        makeTranslatable()
        super.initialize(url, resource)
    }

    private fun initChoseTaskComboBox() {
        choseTaskObservableList = FXCollections.observableList(paneUiData.chosenTask.dataList.map {
            it.infoTranslation[LanguagePaneUiData.language.currentValue]?.name
        })
        choseTaskComboBox.items = choseTaskObservableList
        choseTaskComboBox.selectionModel.selectedItemProperty().addListener { _ ->
            paneUiData.chosenTask.uiValue = choseTaskComboBox.selectionModel.selectedIndex
        }
        subscribe(ChosenTaskNotifier.CHOSEN_TASK_TOPIC, object : ChosenTaskNotifier {
            override fun accept(newTaskIndex: Int) {
                choseTaskComboBox.selectionModel.select(newTaskIndex)
                startSolvingButton.isDisable = paneUiData.anyRequiredDataDefault()
            }
        })
    }

    private fun initInstruction() {
        boldTexts = arrayListOf(firstBoldText, secondBoldText, thirdBoldText)
        nonBoldTexts = arrayListOf(firstNonBoldText, secondNonBoldText, thirdNonBoldText, fourthNonBoldText)
        setInstruction()
    }

    private fun setInstruction(default: String = "") {
        val language = LanguagePaneUiData.language.currentValue
        val startSolving = translations?.get(language)?.startSolving ?: default
        val submit = PluginServer.paneText?.taskSolvingPane?.get(language)?.submit ?: default
        val instructions = translations?.get(language)?.description ?: ""
        val boldInstructions = arrayListOf(startSolving, submit, submit)
        val nonBoldInstructions = instructions.split("%s")
        boldInstructions.zip(boldTexts).forEach { (s, t) -> t.text = s }
        nonBoldInstructions.zip(nonBoldTexts).forEach { (s, t) -> t.text = s }
    }

    private fun getFormattedText(text: String, default: String = ""): String {
        val language = LanguagePaneUiData.language.currentValue
        val startSolving = translations?.get(language)?.startSolving ?: default
        val submit = PluginServer.paneText?.taskSolvingPane?.get(language)?.submit ?: default
        return java.lang.String.format(text, startSolving, submit, submit)
    }

    private fun initButtons() {
        backToProfileButton.onMouseClicked { changeVisiblePane(SurveyControllerManager) }
        startSolvingButton.onMouseClicked {
            val currentTask = paneUiData.chosenTask.currentValue
            currentTask?.let {
                ApplicationManager.getApplication().invokeLater {
                    TaskFileHandler.openTaskFiles(it)
                }
            }
            changeVisiblePane(TaskSolvingControllerManager)
        }
        finishWorkButton.onMouseClicked { changeVisiblePane(FinalControllerManager) }
    }

    private fun makeTranslatable() {
        subscribe(LanguageNotifier.LANGUAGE_TOPIC, object : LanguageNotifier {
            override fun accept(newLanguageIndex: Int) {
                val newLanguage = LanguagePaneUiData.language.dataList[newLanguageIndex]
                val taskChooserPaneText = translations?.get(newLanguage)
                taskChooserPaneText?.let { it ->
                    choseTaskLabel.text = it.chooseTask
                    startSolvingText.text = it.startSolving
                    finishWorkText.text = it.finishSession
                    val text = translations?.get(newLanguage)?.description ?: ""
                    setInstruction()
                    changeComboBoxItems(
                        choseTaskComboBox,
                        choseTaskObservableList,
                        paneUiData.chosenTask.dataList.map { task ->
                            task.infoTranslation[LanguagePaneUiData.language.currentValue]?.name
                        })
                }
            }
        })
    }
}