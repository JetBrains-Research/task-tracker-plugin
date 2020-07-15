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

data class Content(val panel: JPanel, val project: Project, val scale: Double, var isInitialized: Boolean)


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
//        subscribe(ServerConnectionNotifier.SERVER_CONNECTION_TOPIC, object : ServerConnectionNotifier {
//            override fun accept(connection: ServerConnectionResult) {
//                visiblePane = when (connection) {
//                    ServerConnectionResult.SUCCESS -> {
//                        logger.info("Get success connection result")
//                        contents.filter { !it.isInitialized }.forEach {
//                            it.isInitialized = true
//                            addPanesOnPanel(it.panel, { it.dependsOnServerData }, it.project, it.scale)
//                        }
////                      Do we want to unsubscribe after first success?
//                        ProfileControllerManager
//                    }
//                    ServerConnectionResult.FAIL -> {
//                        logger.info("Get fail connection result")
//                        ErrorControllerManager
//                    }
//                    ServerConnectionResult.LOADING -> {
//                        logger.info("Get loading connection result")
//                        LoadingControllerManager
//                    }
//                }
//            }
//        })
    }

//   Run on EDT (ToolWindowFactory takes care of it)
//   We should wait for notification about ConnectionResult?


    fun createContent(project: Project): JComponent {
        val screenSize = Toolkit.getDefaultToolkit().screenSize
        val scale = screenSize.height / SCREEN_HEIGHT
        val panel = JPanel()
        panel.background = java.awt.Color.WHITE
        logger.info("codetracker: server connection result is ${PluginServer.serverConnectionResult}")

//        when (PluginServer.serverConnectionResult) {
//            ServerConnectionResult.LOADING -> {
//                visiblePane = LoadingControllerManager
//                addPanesOnPanel(panel, { !it.dependsOnServerData }, project, scale)
//                contents.add(Content(panel, project, scale, false))
//            }
//            ServerConnectionResult.FAIL -> {
//                visiblePane = ErrorControllerManager
//                addPanesOnPanel(panel, { !it.dependsOnServerData }, project, scale)
//                contents.add(Content(panel, project, scale, false))
//            }
//            ServerConnectionResult.SUCCESS -> {
//                visiblePane = ProfileControllerManager
//                addPanesOnPanel(panel, { true }, project, scale)
//                contents.add(Content(panel, project, scale, true))
//            }
//        }
        return JBScrollPane(panel)
    }

    private fun addPanesOnPanel(panel: JPanel, filter: (Pane) -> Boolean, project: Project, scale: Double) {
        val filteredPanes = panes.filter { filter(it) }
        filteredPanes.map { it.createContent(project, scale) }.forEach { panel.add(it) }
        Platform.runLater {
            filteredPanes.forEach { it.getLastAddedPaneController()?.update() }
        }
    }

//    fun create

//    надо в создании контента проверять, че там с сервером. Если фейл -- то загружать ServerPane,
//    при этом подписаться на обновления и как только будет ок -- то загрузить все, поставить в качестве визибле -- профиль


//    если сразу ок -- то просто загрузить все панели
}