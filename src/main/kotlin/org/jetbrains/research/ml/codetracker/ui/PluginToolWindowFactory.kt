package org.jetbrains.research.ml.codetracker.ui

import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.ToolWindow
import com.intellij.openapi.wm.ToolWindowFactory
import com.intellij.openapi.wm.impl.ToolWindowImpl
import org.jetbrains.research.ml.codetracker.Plugin


class PluginToolWindowFactory : ToolWindowFactory {

    private val diagnosticLogger: Logger = Logger.getInstance(javaClass)

    init {
        diagnosticLogger.info("${Plugin.PLUGIN_ID}: init factory")
    }

    override fun createToolWindowContent(project: Project, toolWindow: ToolWindow) {
        diagnosticLogger.info("${Plugin.PLUGIN_ID}: create tool window")
        val content = MainController.createContent(project)
        toolWindow.component.parent.add(content)
        toolWindow as ToolWindowImpl
    }
}






