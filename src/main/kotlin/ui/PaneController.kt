package ui

import com.intellij.openapi.Disposable
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Disposer
import com.intellij.ui.components.JBScrollPane
import data.UiData
import javafx.application.Platform
import javafx.embed.swing.JFXPanel
import javafx.fxml.FXMLLoader
import javafx.scene.Parent
import javafx.scene.Scene
import javafx.scene.paint.Color
import java.awt.Toolkit
import javax.swing.JComponent
import javax.swing.JPanel
import kotlin.properties.Delegates
import kotlin.reflect.KClass

// Todo: make a sealed class
interface PaneNotifyEvent

abstract class PaneUiData <E : PaneNotifyEvent> (protected val controllerManager: PaneControllerManager<E, out PaneController<E>>) {

    open inner class UiField <T : Any?> (val notifyEvent: E, val defaultUiValue: T, val header: String) {

        open var uiValue: T by Delegates.observable(defaultUiValue) { _, old, new ->
            if (old != new) {
                controllerManager.notify(notifyEvent, new)
            }
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

//  For ui fields, represented as element at some index from dataList
//    Todo: add some sorting? (alphabetically or by solved status)
    inner class ListedUiField<T: Any?> (private val dataList: List<T>, notifyEvent: E, header: String) : UiField<Int>(notifyEvent, 0, header) {
        override var uiValue: Int by Delegates.observable(defaultUiValue) {_, old, new ->
            if (old != new && new < dataList.size) {
                controllerManager.notify(notifyEvent, new)
            }
        }
        override fun toString(): String {
            return dataList[uiValue].toString()
        }
    }


    abstract fun getData(): List<UiField<*>>

}

abstract class PaneController<E : PaneNotifyEvent>(open val uiData: PaneUiData<E>, val scale: Double, val fxPanel: JFXPanel, val id: Int) {
    companion object {
        private val diagnosticLogger: Logger = Logger.getInstance(javaClass)
    }

    abstract fun initialize()
}




// PaneController, PaneNotifyEvent, PaneControllerManager, PaneUiData


abstract class PaneControllerManager<E : PaneNotifyEvent, T : PaneController<E>>  {
    protected abstract val paneControllerClass: KClass<T>
    protected abstract var paneControllers: MutableList<T>
    protected abstract val fxmlFilename: String
    protected abstract val paneUiData: PaneUiData<E>
    private var lastId = 0

    init {
        MainController.paneControllerManagers.add(this)
    }

    abstract fun notify(notifyEvent: E, new: Any?, controllers: MutableList<T> = paneControllers)

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
//        Todo: rename height
            loader.namespace["scale"] = scale
            loader.location = javaClass.getResource(fxmlFilename)
            loader.setController(controller)
            val root = loader.load<Parent>()
            val scene = Scene(root, Color.WHITE)
            fxPanel.scene = scene
            fxPanel.background = java.awt.Color.white
            fxPanel.isVisible = MainController.visiblePaneControllerManager == this
        }
        return fxPanel
    }

    fun setVisible(visible: Boolean) {
        println("${this::class}: set visible $visible")
        paneControllers.forEach { it.fxPanel.isVisible = visible }
    }

    private fun removeController(controller: T) {
        paneControllers.remove(controller)
    }

}


typealias Pane = PaneControllerManager<out PaneNotifyEvent, out PaneController<out PaneNotifyEvent>>

//Todo: rename
internal object MainController {
//    Todo: move to Scalable?
    private const val SCREEN_HEIGHT = 1080.0

    private val diagnosticLogger: Logger = Logger.getInstance(javaClass)
    val paneControllerManagers = arrayListOf<Pane>()
    internal var visiblePaneControllerManager: Pane = ProfileControllerManager
        set(value) {
            println(value)
            println(paneControllerManagers)
            println(paneControllerManagers.size)
            paneControllerManagers.forEach { it.setVisible(it == value) }
            field = value
        }

    fun createContent(project: Project): JComponent {
        val screenSize = Toolkit.getDefaultToolkit().screenSize
        diagnosticLogger.info("Screen size: $screenSize")
        val scale = screenSize.height / SCREEN_HEIGHT

        val panel = JPanel()
        panel.background = java.awt.Color.white
        paneControllerManagers.map { it.createContent(project, scale) }.forEach { panel.add(it) }
        val scrollPane = JBScrollPane(panel)
        return scrollPane
    }
}