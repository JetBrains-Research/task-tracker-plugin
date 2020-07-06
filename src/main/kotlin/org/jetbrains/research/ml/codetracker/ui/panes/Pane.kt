package org.jetbrains.research.ml.codetracker.ui.panes

import com.intellij.openapi.Disposable
import com.intellij.openapi.diagnostic.Logger
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
import org.jetbrains.research.ml.codetracker.Plugin
import org.jetbrains.research.ml.codetracker.models.PaneLanguage
import org.jetbrains.research.ml.codetracker.ui.MainController
import org.jetbrains.research.ml.codetracker.ui.TranslationManager
import java.lang.Thread.currentThread
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
                println("${this::class.simpleName}:setUiValueInUiField ${currentThread().name}")
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
                println("${this::class.simpleName}:setUiValueInListedField ${currentThread().name}")
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

    inner class LanguageUiField(notifyEvent: E) : ListedUiField<PaneLanguage>(
        TranslationManager.availableLanguages, notifyEvent,
        TranslationManager.currentLanguageIndex,"language")

    /**
     * Represents ui fields that may be required or not, like months text filed in ProfilePane
     */
    inner class RequiredUiField<T : Any?>(var isRequired: Boolean, notifyEvent: E, defaultUiValue: T, header: String) : UiField<T>(notifyEvent, defaultUiValue, header)

    abstract fun getData(): List<UiField<*>>

    fun anyDataDefault(): Boolean = getData().any { it.isUiValueDefault }

    fun anyDataRequiredAndDefault() : Boolean {
        println("${this::class.simpleName}:anyDataRequiredAndDefault ${currentThread().name}")
        return getData().any {
//            If field is optional (not required), doesn't matter whether it's uiValue is default
            if (it is RequiredUiField && !it.isRequired) {
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
    protected val logger = Logger.getInstance(javaClass)


    open fun initialize() {
        println("${this::class.simpleName}:PCinitialize ${currentThread().name}")
        initLanguageComboBox()
    }

    fun selectLanguage(newLanguageIndex: Int) {
        println("${this::class.simpleName}:selectLanguage ${currentThread().name}")
        languageComboBox.selectionModel.select(newLanguageIndex)
    }

    private fun initLanguageComboBox() {
        println("${this::class.simpleName}:initLanguageComboBox ${currentThread().name}")
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
    protected val logger = Logger.getInstance(javaClass)
    private var lastId = 0

    abstract fun notify(notifyEvent: E, new: Any?)

    fun createContent(project: Project, scale: Double): JFXPanel {
        logger.info("${this::class.simpleName}:createContent ${currentThread().name}")
        logger.info("${Plugin.PLUGIN_ID}:${this::class.simpleName} create content")
        val fxPanel = JFXPanel()

        Platform.setImplicitExit(false)
        Platform.runLater {
            logger.info("${this::class.simpleName}:createContent in platfrom block ${currentThread().name}")
            // Need to run on Fx thread, because controller initialization includes javaFx elements
            val controller = paneControllerClass.constructors.first().call(paneUiData, scale, fxPanel, lastId++)
            logger.info("${Plugin.PLUGIN_ID}:${this::class.simpleName} create controller")
            paneControllers.add(controller)
            Disposer.register(project, Disposable {
                this.removeController(controller)
            })

            val loader = FXMLLoader()
            loader.namespace["scale"] = scale
            loader.location = javaClass.getResource(fxmlFilename)
            loader.setController(controller)
            logger.info("${Plugin.PLUGIN_ID}:${this::class.simpleName} set controller")
            val root = loader.load<Parent>()
            val scene = Scene(root, Color.WHITE)
            fxPanel.scene = scene
        }

        logger.info("${this::class.simpleName}:createContent after platfrom block ${currentThread().name}")

        fxPanel.background = java.awt.Color.WHITE
        fxPanel.isVisible = MainController.visiblePaneControllerManager == this

        //  Todo: maybe create some other way of data updating? something wrong here :(
        paneUiData.getData().forEach { notify(it.notifyEvent, it.uiValue) }
        // Note: don't call TranslationManager or other PaneControllerManagers here because some elements may be not initialized yet
        switchUILanguage(TranslationManager.currentLanguageIndex)

        return fxPanel
    }

    //    todo: call MainController here?
    fun setVisible(visible: Boolean) {
        logger.info("${this::class.simpleName}:setVisible ${currentThread().name}")
        logger.info("${Plugin.PLUGIN_ID}:${this::class.simpleName} set visible")
        paneControllers.forEach { it.fxPanel.isVisible = visible }
    }

    /**
     * Just to switch the combobox language on this pane.
     * For switching the language on all ui elements use TranslationManager.
     */
    fun switchUILanguage(newLanguageIndex: Int) {
        logger.info("${this::class.simpleName}:switchUILanguage ${currentThread().name}")
        logger.info("${Plugin.PLUGIN_ID}:${this::class.simpleName} switch ui language")
        paneControllers.forEach { it.selectLanguage(newLanguageIndex) }
    }

    private fun removeController(controller: T) {
        logger.info("${this::class.simpleName}:removeController ${currentThread().name}")
        paneControllers.remove(controller)
    }
}
