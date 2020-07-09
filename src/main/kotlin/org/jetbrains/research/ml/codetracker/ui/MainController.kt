package org.jetbrains.research.ml.codetracker.ui

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.project.Project
import com.intellij.ui.components.JBScrollPane
import javafx.application.Platform
import org.jetbrains.research.ml.codetracker.Plugin
import org.jetbrains.research.ml.codetracker.server.ServerConnectionResult
import org.jetbrains.research.ml.codetracker.server.ServerConnectionNotifier
import org.jetbrains.research.ml.codetracker.ui.panes.*
import java.awt.Toolkit
import javax.swing.JComponent
import javax.swing.JPanel




internal object MainController {
    //    Todo: move to Scalable in future
    private const val SCREEN_HEIGHT = 1080.0

    private val logger: Logger = Logger.getInstance(javaClass)
    //    Todo: automatically collect ControllerManagers
    val paneControllerManagers: List<PaneControllerManager<out PaneController>> = arrayListOf(
        ErrorControllerManager,
        ProfileControllerManager,
        TaskChooserControllerManager,
        TaskControllerManager,
        FinishControllerManager)

//    Set LoadingPane instead
    internal var visiblePaneControllerManager: PaneControllerManager<out PaneController>? = ProfileControllerManager
        set(value) {
            paneControllerManagers.forEach { it.setVisible(it == value) }
            field = value
        }

    init {
        subscribe(ServerConnectionNotifier.SERVER_CONNECTION_TOPIC, object : ServerConnectionNotifier {
            override fun accept(connection: ServerConnectionResult) {
                visiblePaneControllerManager = when (connection) {
                    ServerConnectionResult.SUCCESS ->
//                      Do we want to unsubscribe after first success?
                        ProfileControllerManager

                    ServerConnectionResult.FAIL -> ErrorControllerManager
                }
            }
        })
    }

//   Run on EDT (ToolWindowFactory takes care of it)
//   We should wait for notification about ConnectionResult?
    fun createContent(project: Project): JComponent {
        logger.info("${this::class.simpleName}:createContent ${Thread.currentThread().name}")
        val screenSize = Toolkit.getDefaultToolkit().screenSize
        logger.info("${Plugin.PLUGIN_ID}: screen size: $screenSize")
        val scale = screenSize.height / SCREEN_HEIGHT
        val panel = JPanel()
        panel.background = java.awt.Color.WHITE
        logger.info("${this::class}: $paneControllerManagers")
        paneControllerManagers.map { it.createContent(project, scale) }.forEach {
            logger.info("${this::class.simpleName}:createContent forEach ${Thread.currentThread().name}")
            panel.add(it)
        }

//        Run on JavaFX thread because it triggers fx components + they need to be updated after their initialization
        Platform.runLater {
            paneControllerManagers.forEach { it.getLastAddedPaneController()?.update() }
        }

        return JBScrollPane(panel)
    }
}