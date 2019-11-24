package ui

import CodeTrackerPlugin
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.application.PathManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.startup.StartupActivity
import com.intellij.openapi.wm.StatusBar
import com.intellij.openapi.wm.StatusBarWidget
import com.intellij.openapi.wm.WindowManager
import com.intellij.openapi.wm.impl.status.EditorBasedWidget
import com.intellij.util.Consumer
import java.awt.Component
import java.awt.event.MouseEvent


class PluginWidgetInitActivity : StartupActivity {
    override fun runActivity(project: Project) {
        val widget = PluginStatusBarWidget(project)
        val disposable = ApplicationManager.getApplication()
        val statusBar = WindowManager.getInstance().getStatusBar(project)
        statusBar?.addWidget(widget, StatusBar.Anchors.DEFAULT_ANCHOR, disposable)
        statusBar?.updateWidget(widget.ID())
        widget.plugin.addProjectManagerListener(project)
    }
}

class PluginStatusBarWidget(project: Project) : EditorBasedWidget(project) {
    val plugin : CodeTrackerPlugin = CodeTrackerPlugin(this)

    companion object {
        const val ID = "code-tracker"
    }

    override fun ID(): String = ID

    override fun getPresentation(type: StatusBarWidget.PlatformType): StatusBarWidget.WidgetPresentation? {
        val presentation = object : StatusBarWidget.TextPresentation {
            override fun getAlignment(): Float = Component.CENTER_ALIGNMENT
            override fun getText() : String = "$ID: ${plugin.trackingState}"
            override fun getTooltipText() : String = "Press to turn code-tracker ${plugin.switchedState()}; path: \"${PathManager.getPluginsPath()}\""
            override fun getClickConsumer(): Consumer<MouseEvent>? = Consumer { mouseEvent ->
                if (mouseEvent.id == MouseEvent.MOUSE_PRESSED) {
                    plugin.trackingState = plugin.switchedState()
                }
            }
        }
        return presentation
    }

    fun updateState() {
        WindowManager.getInstance().allProjectFrames.forEach {
            it.statusBar?.updateWidget(ID)
        }
    }

}