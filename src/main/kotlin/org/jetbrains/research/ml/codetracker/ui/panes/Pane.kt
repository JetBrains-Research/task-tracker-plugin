package org.jetbrains.research.ml.codetracker.ui.panes

import com.intellij.openapi.Disposable
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Disposer
import javafx.application.Platform
import javafx.collections.FXCollections
import javafx.embed.swing.JFXPanel
import javafx.fxml.FXML
import javafx.fxml.FXMLLoader
import javafx.scene.Parent
import javafx.scene.Scene
import javafx.scene.control.ComboBox
import javafx.scene.paint.Color
import org.jetbrains.research.ml.codetracker.ui.Language
import org.jetbrains.research.ml.codetracker.ui.MainController
import org.jetbrains.research.ml.codetracker.ui.TranslationManager
import java.awt.Panel
import kotlin.properties.Delegates
import kotlin.reflect.KClass



/**
 * Todo:
 *  * fix all todos
 *  * add scalable
 */

/**
 * Each *pane* should have its own PaneNotifyEvent, inherited from [IPaneNotifyEvent]. It's needed to
 * notify different PaneControllers (representing *the pane* in each of the open IDE windows, see [PaneController])
 * about [PaneUiData] changes (for example, if one PaneController has changed, others should be changed too).
 */
interface IPaneNotifyEvent


/**
 * Shown on the *pane* data that may change by user actions (for example, selecting from ComboBoxes or ToggleGroups).
 * By default, each pane has language ComboBox, so corresponding field [currentLanguage] is implemented here.
 */
abstract class PaneUiData <E : IPaneNotifyEvent> (protected val controllerManager: PaneControllerManager<E, out PaneController<E>>) {
    abstract val currentLanguage: LanguageUiField
    /**
     * Represents pane data with [uiValue], that triggers [notifyEvent] when it changes.
     */
    open inner class UiField <T : Any?> (val notifyEvent: E, val defaultUiValue: T, val header: String) {

        var isUiValueDefault: Boolean = true
            protected set


        open var uiValue: T by Delegates.observable(defaultUiValue) { _, old, new ->
            if (old != new) {
                isUiValueDefault = new == defaultUiValue
                controllerManager.notify(notifyEvent, new)
            }
        }
    }

//    Todo: add some sorting? (alphabetically or by solved status) and use it in ComboBoxes
    /**
     * Represents pane data, which [uiValue] is one of the [dataList] items,
     * so it can be thought of as an item index with type [Int].
     */
    open inner class ListedUiField<T: Any?> (val dataList: List<T>, notifyEvent: E, defaultValue: Int, header: String) : UiField<Int>(notifyEvent, defaultValue, header) {
        override var uiValue: Int by Delegates.observable(defaultUiValue) { _, old, new ->
            if (old != new && new in dataList.indices) {
                isUiValueDefault = new == defaultValue
                controllerManager.notify(notifyEvent, new)
            }
        }

        override fun toString(): String {
            return if (uiValue in dataList.indices) {
                dataList[uiValue].toString()
            } else {
                uiValue.toString()
            }
        }
    }

    inner class LanguageUiField(notifyEvent: E) : ListedUiField<Language>(
        TranslationManager.availableLanguages, notifyEvent,
        TranslationManager.currentLanguageIndex,"language")

    /**
     * Represents ui fields with visibility
     */
    inner class VisibleUiField<T : Any?>(var isVisible: Boolean, notifyEvent: E, defaultUiValue: T, header: String) : UiField<T>(notifyEvent, defaultUiValue, header)

    abstract fun getData(): List<UiField<*>>

    fun anyDataDefault(): Boolean = getData().any { it.isUiValueDefault }

    fun anyDataVisibleAndDefault() : Boolean {
        return getData().any {
//            If field is invisible, doesn't matter whether it's uiValue is default
            if (it is VisibleUiField && !it.isVisible) {
                false
            } else {
                it.isUiValueDefault
            }
        }
    }

}

// Todo: make private constructor?
/**
 * [PaneController] stores all ui components of *the pane* such as buttons, labels, and so on.
 * Its instance is created every time new IDE window opens. By default, all panes has language ComboBox.
 */
abstract class PaneController<E : IPaneNotifyEvent>(open val uiData: PaneUiData<E>, val scale: Double, val fxPanel: JFXPanel, val id: Int) {
    @FXML private lateinit var languageComboBox : ComboBox<String>

    open fun initialize() {
        initLanguageComboBox()
        makeTranslatable()
    }

    abstract fun makeTranslatable()

    fun selectLanguage(newLanguageIndex: Int) {
        languageComboBox.selectionModel.select(newLanguageIndex)
    }

    private fun initLanguageComboBox() {
        languageComboBox.items = FXCollections.observableList(uiData.currentLanguage.dataList.map { it.key })
        languageComboBox.selectionModel.selectedItemProperty().addListener { _, _, _ ->
            uiData.currentLanguage.uiValue = languageComboBox.selectionModel.selectedIndex
        }
    }
}

/**
 * Keeps all [PaneController] instances in consistent state, handling all [IPaneNotifyEvent]'s that are triggered
 * when [paneUiData] is changed. Also, creates [PaneController] content by loading .fxml file.
 */
abstract class PaneControllerManager<E : IPaneNotifyEvent, T : PaneController<E>>  {
    protected abstract val paneControllerClass: KClass<T>
    protected abstract val paneControllers: MutableList<T>
    protected abstract val fxmlFilename: String
    protected abstract val paneUiData: PaneUiData<E>
    private var lastId = 0

    abstract fun notify(notifyEvent: E, new: Any?)

    fun createContent(project: Project, scale: Double): JFXPanel {
        val fxPanel = JFXPanel()
        val controller = paneControllerClass.constructors.first().call(paneUiData, scale, fxPanel, lastId++)
        paneControllers.add(controller)

        Disposer.register(project, Disposable {
            this.removeController(controller)
        })

        Platform.setImplicitExit(false)
        Platform.runLater {
            val loader = FXMLLoader()
            loader.namespace["scale"] = scale
            loader.location = javaClass.getResource(fxmlFilename)
            loader.setController(controller)
            val root = loader.load<Parent>()
            val scene = Scene(root, Color.WHITE)
            fxPanel.scene = scene
            fxPanel.background = java.awt.Color.WHITE
            fxPanel.isVisible = MainController.visiblePaneControllerManager == this
            //  Todo: maybe create some other way of data updating?
            paneUiData.getData().forEach { notify(it.notifyEvent, it.uiValue) }
            //  Todo: Set current language AFTER all controllers created their content, otherwise some comboboxes may be not initialized yet
            notify(paneUiData.currentLanguage.notifyEvent, paneUiData.currentLanguage.uiValue)
        }
        return fxPanel
    }

    //    todo: call MainController here?
    fun setVisible(visible: Boolean) {
        println("${this::class}: set visible $visible")
        paneControllers.forEach { it.fxPanel.isVisible = visible }
    }

    protected fun switchLanguage(newLanguageIndex: Int) {
        TranslationManager.currentLanguageIndex = newLanguageIndex
        MainController.paneControllerManagers.forEach { controllerManager ->
            controllerManager.paneControllers.forEach { it.selectLanguage(newLanguageIndex) }
        }
    }

    private fun removeController(controller: T) {
        paneControllers.remove(controller)
    }
}
