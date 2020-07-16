package org.jetbrains.research.ml.codetracker.ui

import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.project.Project
import com.intellij.ui.components.JBScrollPane
import javafx.application.Platform
import org.jetbrains.research.ml.codetracker.server.PluginServer
import org.jetbrains.research.ml.codetracker.server.ServerConnectionResult
import org.jetbrains.research.ml.codetracker.server.ServerConnectionNotifier
import org.jetbrains.research.ml.codetracker.ui.panes.*
import java.awt.Toolkit
import javax.swing.JComponent
import javax.swing.JPanel


typealias Pane = PaneControllerManager<out PaneController>

data class Content(val panel: JPanel, val project: Project, val scale: Double, var isFullyInitialized: Boolean)


internal object MainController {
    //    Todo: move to Scalable in future
    private const val SCREEN_HEIGHT = 1080.0

    private val logger: Logger = Logger.getInstance(javaClass)
    private val contents: MutableList<Content> = arrayListOf()

    //    Todo: automatically collect ControllerManagers
    private val panes: List<Pane> = arrayListOf(
        ErrorControllerManager,
        LoadingControllerManager,
        ProfileControllerManager,
        TaskChooserControllerManager,
        TaskControllerManager,
        FinishControllerManager)

//    Set LoadingPane instead
    internal var visiblePane: Pane? = LoadingControllerManager
        set(value) {
            logger.info("$value set visible")
            panes.forEach { it.setVisible(it == value) }
            field = value
        }

    init {
        subscribe(ServerConnectionNotifier.SERVER_CONNECTION_TOPIC, object : ServerConnectionNotifier {
            override fun accept(connection: ServerConnectionResult) {
                when (connection) {
                    ServerConnectionResult.UNINITIALIZED -> {
                        visiblePane = LoadingControllerManager
                    }
                    ServerConnectionResult.SUCCESS -> {
                        logger.info("codetracker: get success, set success")
//                      Do we want to unsubscribe after first success?
                        finishInitWithServer()
                    }
                    ServerConnectionResult.FAIL -> {
                        logger.info("codetracker: get failed, set failed")
                        visiblePane = ErrorControllerManager
                    }
                    ServerConnectionResult.LOADING -> {
                        logger.info("codetracker: get loading, set loading")
                        visiblePane = LoadingControllerManager
                    }
                }
            }
        })
    }

//   Run on EDT (ToolWindowFactory takes care of it)
//   We should wait for notification about ConnectionResult?


    fun createContent(project: Project): JComponent {
        val screenSize = Toolkit.getDefaultToolkit().screenSize
        val scale = screenSize.height / SCREEN_HEIGHT
        val panel = JPanel()
        panel.background = java.awt.Color.WHITE

        when (PluginServer.serverConnectionResult) {
            ServerConnectionResult.UNINITIALIZED -> {
                initWithoutServer(panel, project, scale)
                PluginServer.reconnect()
            }
            ServerConnectionResult.LOADING -> {
                initWithoutServer(panel, project, scale)
            }
            ServerConnectionResult.FAIL -> {
                initWithoutServer(panel, project, scale)
            }
            ServerConnectionResult.SUCCESS -> {
                logger.info("codetracker: create content, set success")
                initWithServer(panel, project, scale)
            }
        }
        return JBScrollPane(panel)
    }

    private fun initWithoutServer(panel: JPanel, project: Project, scale: Double) {
        addPanesOnPanel(panel, { !it.dependsOnServerData }, project, scale)
        contents.add(Content(panel, project, scale, false))
    }

    private fun initWithServer(panel: JPanel, project: Project, scale: Double) {
        addPanesOnPanel(panel, { true }, project, scale)
        contents.add(Content(panel, project, scale, false))
    }

    private fun finishInitWithServer() {
        contents.filter { !it.isFullyInitialized }.forEach {
            it.isFullyInitialized = true
            addPanesOnPanel(it.panel, { it.dependsOnServerData }, it.project, it.scale)
        }
        visiblePane = ProfileControllerManager

    }

    private fun addPanesOnPanel(panel: JPanel, filter: (Pane) -> Boolean, project: Project, scale: Double) {
        val filteredPanes = panes.filter { filter(it) }
        filteredPanes.map { it.createContent(project, scale) }.forEach { panel.add(it) }
        Platform.runLater {
            filteredPanes.forEach { it.getLastAddedPaneController()?.update() }
        }
    }
}