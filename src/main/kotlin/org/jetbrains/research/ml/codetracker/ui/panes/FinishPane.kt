package org.jetbrains.research.ml.codetracker.ui.panes

import javafx.embed.swing.JFXPanel
import javafx.fxml.FXML
import javafx.scene.control.Button
import javafx.scene.control.Label
import javafx.scene.input.MouseEvent
import javafx.scene.shape.Polygon
import javafx.scene.shape.Rectangle
import javafx.scene.text.Text
import org.jetbrains.research.ml.codetracker.server.PluginServer
import org.jetbrains.research.ml.codetracker.ui.*
import org.jetbrains.research.ml.codetracker.ui.MainController
import kotlin.reflect.KClass

enum class FinishNotifyEvent : IPaneNotifyEvent {
    LANGUAGE_NOTIFY
}

object FinishControllerManager : PaneControllerManager<FinishNotifyEvent, FinishController>() {
    override val paneControllerClass: KClass<FinishController> = FinishController::class
    override val paneControllers: MutableList<FinishController> = arrayListOf()
    override val fxmlFilename: String = "finish-ui-form-2.fxml"
    override val paneUiData: PaneUiData<FinishNotifyEvent> =
        FinishUiData

    override fun notify(notifyEvent: FinishNotifyEvent, new: Any?) {
        when (notifyEvent) {
            FinishNotifyEvent.LANGUAGE_NOTIFY -> switchLanguage(new as Int)
        }
    }

}

object FinishUiData : PaneUiData<FinishNotifyEvent>(
    FinishControllerManager
) {
    override val currentLanguage: LanguageUiField = LanguageUiField(
        FinishNotifyEvent.LANGUAGE_NOTIFY
    )
    override fun getData(): List<UiField<*>> = arrayListOf()
}

class FinishController(override val uiData: FinishUiData, scale: Double, fxPanel: JFXPanel, id: Int) : PaneController<FinishNotifyEvent>(uiData, scale, fxPanel, id) {
//    @FXML lateinit var finishPane: Pane

    @FXML lateinit var blueRectangle: Rectangle
    @FXML lateinit var orangePolygon: Polygon
    @FXML lateinit var yellowPolygon: Polygon

    @FXML lateinit var backToTasksButton: Button
    @FXML lateinit var backToTasksText: Text
    @FXML lateinit var backToProfileButton: Button
    @FXML lateinit var backToProfileText: Text

    @FXML lateinit var greatWorkLabel: Label
    @FXML lateinit var messageText: Text

    private val translations = PluginServer.paneText.finishPane


    override fun initialize() {
        initButtons()
        super.initialize()
    }

    override fun makeTranslatable() {
        backToTasksText.makeTranslatable { backToTasksText.text = translations[it]?.backToTasks }
        backToProfileText.makeTranslatable { backToProfileText.text = translations[it]?.backToSurvey }
        greatWorkLabel.makeTranslatable { greatWorkLabel.text = translations[it]?.praise }
        messageText.makeTranslatable { messageText.text = translations[it]?.finalMessage }
    }

    private fun initButtons() {
        backToTasksButton.addEventHandler(MouseEvent.MOUSE_CLICKED) {
            MainController.visiblePaneControllerManager =
                TaskChooserControllerManager
        }
        backToProfileButton.addEventHandler(MouseEvent.MOUSE_CLICKED) {
            MainController.visiblePaneControllerManager =
                ProfileControllerManager
        }

    }
}