package org.jetbrains.research.ml.tasktracker

import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.project.Project
import com.intellij.openapi.startup.StartupActivity
import org.jetbrains.research.ml.tasktracker.tracking.TaskFileHandler


class InitActivity : StartupActivity {
    private val logger: Logger = Logger.getInstance(javaClass)

    init {
        logger.info("${Plugin.PLUGIN_NAME}: startup activity")
    }

    override fun runActivity(project: Project) {
        Plugin.installRequiredPlugins(project)
        logger.info("${Plugin.PLUGIN_NAME}: run activity")
        TaskFileHandler.addProject(project)
    }
}
