package org.jetbrains.research.ml.codetracker.ui

import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.project.Project
import com.intellij.ui.components.JBScrollPane
import org.jetbrains.research.ml.codetracker.ui.panes.*
import java.awt.Toolkit
import javax.swing.JComponent
import javax.swing.JPanel

typealias Pane = PaneControllerManager<out IPaneNotifyEvent, out PaneController<out IPaneNotifyEvent>>

//Todo: rename
internal object MainController {
    //    Todo: move to Scalable?
    private const val SCREEN_HEIGHT = 1080.0

    private val diagnosticLogger: Logger = Logger.getInstance(javaClass)
    //    Todo: automatically collect ControllerManagers
    val paneControllerManagers = arrayListOf(
        ProfileControllerManager,
        TaskChooserControllerManager,
        TaskControllerManager,
        FinishControllerManager
    )
    internal var visiblePaneControllerManager: Pane =
        ProfileControllerManager
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