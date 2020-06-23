package ui

import com.intellij.openapi.Disposable
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Disposer
import com.intellij.ui.components.JBScrollPane
import javafx.application.Platform
import javafx.collections.FXCollections
import javafx.embed.swing.JFXPanel
import javafx.fxml.FXML
import javafx.fxml.FXMLLoader
import javafx.scene.Parent
import javafx.scene.Scene
import javafx.scene.control.ComboBox
import javafx.scene.paint.Color
import java.awt.Toolkit
import javax.swing.JComponent
import javax.swing.JPanel
import kotlin.properties.Delegates
import kotlin.reflect.KClass



/**
 * Todo:
 *  * sort out what to do with notifyEvent everywhere and the whole architecture
 *  * add buttons disability
 *  * add autocomplete country box
 *  * fix all todos
 *  * add 'send successfully' signes
 *  * add translatable
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
    //    todo: get from server
    val languages = listOf(Language("ru"), Language("eng"))
    abstract val currentLanguage: LanguageUiField

    /**
     * Represents pane data with [uiValue], that triggers [notifyEvent] when it changes.
     */
    open inner class UiField <T : Any?> (val notifyEvent: E, val defaultUiValue: T, val header: String) {

        open var uiValue: T by Delegates.observable(defaultUiValue) { _, old, new ->
            if (old != new) {
                controllerManager.notify(notifyEvent, new)
            }
        }

        fun anyNonDefault(new: T): Boolean {
            return new == defaultUiValue || getData().filter { it != this }.any { it.uiValue == it.defaultUiValue }
        }

        fun setDefault() {
            uiValue = defaultUiValue
        }
        fun isDefault(): Boolean {
            return uiValue == defaultUiValue
        }
        fun isDefault(new: T): Boolean {
            return new == defaultUiValue
        }
    }

//    Todo: add some sorting? (alphabetically or by solved status)
//    Todo: add dataList from here
    /**
     * Represents pane data, which [uiValue] is one of the [dataList] items,
     * so it can be thought of as an item index with type [Int].
     */
    open inner class ListedUiField<T: Any?> (private val dataList: List<T>, notifyEvent: E, defaultValue: Int, header: String) : UiField<Int>(notifyEvent, defaultValue, header) {
        override var uiValue: Int by Delegates.observable(defaultUiValue) { _, old, new ->
            if (old != new && new in dataList.indices) {
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

    inner class LanguageUiField(notifyEvent: E) : ListedUiField<Language>(languages, notifyEvent, 0,"language")

    abstract fun getData(): List<UiField<*>>

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
    }

    fun selectLanguage(newLanguageIndex: Int) {
        languageComboBox.selectionModel.select(newLanguageIndex)
    }

    private fun initLanguageComboBox() {
        languageComboBox.items = FXCollections.observableList(uiData.languages.map { it.key })
        languageComboBox.selectionModel.selectedItemProperty().addListener { _, old, new ->
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

            //        Todo: maybe create some other way of data updating?
            paneUiData.getData().forEach { notify(it.notifyEvent, it.uiValue) }

        }

        return fxPanel
    }

    //    todo: call MainController here?
    fun setVisible(visible: Boolean) {
        println("${this::class}: set visible $visible")
        paneControllers.forEach { it.fxPanel.isVisible = visible }
    }

    protected fun switchLanguage(newLanguageIndex: Int) =
        MainController.paneControllerManagers.forEach { controllerManager ->
            controllerManager.paneControllers.forEach { it.selectLanguage(newLanguageIndex) }
        }

    private fun removeController(controller: T) {
        paneControllers.remove(controller)
    }

}


typealias Pane = PaneControllerManager<out IPaneNotifyEvent, out PaneController<out IPaneNotifyEvent>>

//Todo: rename
internal object MainController {
    //    Todo: move to Scalable?
    private const val SCREEN_HEIGHT = 1080.0

    private val diagnosticLogger: Logger = Logger.getInstance(javaClass)
    //    Todo: do something with list
    val paneControllerManagers = arrayListOf(ProfileControllerManager, TaskChooserControllerManager, TaskControllerManager, FinishControllerManager)
    internal var visiblePaneControllerManager: Pane = ProfileControllerManager
        set(value) {
            paneControllerManagers.forEach { it.setVisible(it == value) }
            field = value
        }

    fun createContent(project: Project): JComponent {
        val screenSize = Toolkit.getDefaultToolkit().screenSize
        diagnosticLogger.info("Screen size: $screenSize")
        val scale = screenSize.height / SCREEN_HEIGHT
        val panel = JPanel()
        panel.background = java.awt.Color.WHITE
        println("${this::class}: $paneControllerManagers")
        paneControllerManagers.map { it.createContent(project, scale) }.forEach { panel.add(it) }
        return JBScrollPane(panel)
    }
}