package org.jetbrains.research.ml.codetracker.ui

import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.ToolWindow
import com.intellij.openapi.wm.ToolWindowFactory
import com.intellij.openapi.wm.impl.ToolWindowImpl
import org.jetbrains.research.ml.codetracker.Plugin


class PluginToolWindowFactory : ToolWindowFactory {

    private val logger: Logger = Logger.getInstance(javaClass)

    init {
        logger.info("${Plugin.PLUGIN_ID}: init tool window factory")
    }

    override fun createToolWindowContent(project: Project, toolWindow: ToolWindow) {
        logger.info("${Plugin.PLUGIN_ID}: creating tool window")
        val content = MainController.createContent(project)
        toolWindow.component.parent.add(content)
        toolWindow as ToolWindowImpl
    }
}






