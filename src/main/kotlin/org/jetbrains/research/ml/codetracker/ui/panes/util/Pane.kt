package org.jetbrains.research.ml.codetracker.ui.panes.util

import com.intellij.openapi.Disposable
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Disposer
import javafx.application.Platform
import javafx.embed.swing.JFXPanel
import javafx.fxml.FXMLLoader
import javafx.fxml.Initializable
import javafx.scene.Parent
import javafx.scene.Scene
import javafx.scene.paint.Color
import org.jetbrains.research.ml.codetracker.Plugin
import org.jetbrains.research.ml.codetracker.ui.MainController
import kotlin.reflect.KClass

interface Updatable {
    /**
     * Updates Ui elements; it's separated from initializing because some updates depend on
     * other panes, which may be not initialized yet. So it should be called after all necessary panes initializing.
     */
    fun update()
}

/**
 * [PaneController] stores all UI components of *the pane* such as buttons, labels, and so on.
 * Its instance is created every time new IDE window opens.
 */
abstract class PaneController(val project: Project, val scale: Double, val fxPanel: JFXPanel, val id: Int) :
    Initializable

/**
 * Creates [PaneController] content by loading .fxml file.
 */
abstract class PaneControllerManager<T : PaneController>  {
    abstract val canCreateContent: Boolean
    protected abstract val paneControllerClass: KClass<T>
    private val paneControllers: MutableList<T> = arrayListOf()
    protected abstract val fxmlFilename: String
    protected val logger = Logger.getInstance(javaClass)
    private var lastId = 0

    fun createContent(project: Project, scale: Double): JFXPanel {
        logger.info("${Plugin.PLUGIN_ID}:${this::class.simpleName} create content")
        val fxPanel = JFXPanel()

        Platform.setImplicitExit(false)
        Platform.runLater {
            // Need to run on Fx thread, because controller initialization includes javaFx elements
            val controller = paneControllerClass.constructors.first().call(project, scale, fxPanel, lastId++)
            logger.info("${Plugin.PLUGIN_ID}:${this::class.simpleName} create controller")
            paneControllers.add(controller)
            Disposer.register(project, Disposable {
                this.removeController(controller)
            })

            val loader = FXMLLoader()
            loader.namespace["scale"] = scale
            loader.location = javaClass.getResource(fxmlFilename)
            loader.setController(controller)
            loader.classLoader = this::class.java.classLoader
            logger.info("${Plugin.PLUGIN_ID}:${this::class.simpleName} set controller")
            val root = loader.load<Parent>()
            val scene = Scene(root, Color.WHITE)
            fxPanel.scene = scene
            fxPanel.background = java.awt.Color.WHITE
            fxPanel.isVisible = MainController.visiblePane == this
        }

        return fxPanel
    }

    fun setVisible(visible: Boolean) {
        paneControllers.forEach { it.fxPanel.isVisible = visible }
    }

    fun getLastAddedPaneController() : PaneController? {
        return paneControllers.lastOrNull()
    }

    private fun removeController(controller: T) {
        paneControllers.remove(controller)
    }
}
