package org.jetbrains.research.ml.codetracker.ui

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.project.Project
import com.intellij.ui.components.JBScrollPane
import javafx.application.Platform
import org.jetbrains.research.ml.codetracker.server.PluginServer
import org.jetbrains.research.ml.codetracker.server.ServerConnectionResult
import org.jetbrains.research.ml.codetracker.server.ServerConnectionNotifier
import org.jetbrains.research.ml.codetracker.ui.panes.*
import org.jetbrains.research.ml.codetracker.ui.panes.util.PaneController
import org.jetbrains.research.ml.codetracker.ui.panes.util.PaneControllerManager
import org.jetbrains.research.ml.codetracker.ui.panes.util.Updatable
import org.jetbrains.research.ml.codetracker.ui.panes.util.subscribe
import java.awt.Toolkit
import javax.swing.JComponent
import javax.swing.JPanel


typealias Pane = PaneControllerManager<out PaneController>

internal object MainController {
    //    Todo: move to Scalable in future
    private const val SCREEN_HEIGHT = 1080.0

    private val logger: Logger = Logger.getInstance(javaClass)
    private val contents: MutableList<Content> = arrayListOf()

    private val panes: List<Pane> = arrayListOf(
        ErrorControllerManager,
        LoadingControllerManager,
        SurveyControllerManager,
        TaskChoosingControllerManager,
        TaskControllerManager,
        FinalControllerManager)

    internal var visiblePane: Pane? = LoadingControllerManager
        set(value) {
                logger.info("$value set visible")
                panes.forEach { it.setVisible(it == value) }
                field = value
        }

    init {
        /* Subscribes to notifications about server connection result to update visible panes */
        subscribe(ServerConnectionNotifier.SERVER_CONNECTION_TOPIC, object : ServerConnectionNotifier {
            override fun accept(connection: ServerConnectionResult) {
                Platform.runLater {
                    visiblePane = when (connection) {
                        ServerConnectionResult.UNINITIALIZED -> LoadingControllerManager
                        ServerConnectionResult.LOADING -> LoadingControllerManager
                        ServerConnectionResult.FAIL -> ErrorControllerManager
                        ServerConnectionResult.SUCCESS -> {
                            contents.forEach { it.updatePanesToCreate() }
                            SurveyControllerManager
                        }
                    }
                }
            }
        })
    }

    /*   Run on EDT (ToolWindowFactory takes care of it) */
    fun createContent(project: Project): JComponent {
        val screenSize = Toolkit.getDefaultToolkit().screenSize
        val scale = screenSize.height / SCREEN_HEIGHT
        val panel = JPanel()
        panel.background = java.awt.Color.WHITE

        contents.add(Content(panel, project, scale, panes))

        if (PluginServer.serverConnectionResult == ServerConnectionResult.UNINITIALIZED) {
            PluginServer.reconnect(project)
        }
        return JBScrollPane(panel)
    }

    /**
     * Represents ui content that needs to be created. It contains [panel] to which all [panesToCreateContent] should
     * add their contents.
     */
    data class Content(val panel: JPanel, val project: Project, val scale: Double, var panesToCreateContent: List<Pane>) {
        init {
            updatePanesToCreate()
        }

        /**
         * Looks to all [panesToCreateContent] and checks if any can create content. If so, creates pane contents,
         * adds them to the [panel], and removes created panes from [panesToCreateContent]
         */
        fun updatePanesToCreate() {
            ApplicationManager.getApplication().invokeLater {
                val (canCreateContentPanes, cantCreateContentPanes) = panes.partition { it.canCreateContent }
                if (canCreateContentPanes.isNotEmpty()) {
                    canCreateContentPanes.map { it.createContent(project, scale) }.forEach { panel.add(it) }
                    Platform.runLater {
                        canCreateContentPanes.map { it.getLastAddedPaneController() }.forEach {
                            if (it is Updatable) {
                                it.update()
                            }
                        }
                    }
                    panesToCreateContent = cantCreateContentPanes
                }
            }
        }
    }
}