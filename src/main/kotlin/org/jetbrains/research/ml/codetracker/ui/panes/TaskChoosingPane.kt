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
import javafx.scene.text.Text
import org.jetbrains.research.ml.codetracker.Plugin
import org.jetbrains.research.ml.codetracker.tracking.TaskFileHandler
import org.jetbrains.research.ml.codetracker.server.PluginServer
import org.jetbrains.research.ml.codetracker.tracking.TaskDocumentListener
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

    @FXML private lateinit var mainPane: Pane

    override val paneUiData = TaskChoosingUiData
    private val translations = PluginServer.paneText?.taskChoosingPane

    override fun initialize(url: URL?, resource: ResourceBundle?) {
        logger.info("${Plugin.PLUGIN_ID}:${this::class.simpleName} init controller")
        mainPane.styleProperty().bind(Bindings.concat("-fx-font-size: ${scale}px;"))
        initChoseTaskComboBox()
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