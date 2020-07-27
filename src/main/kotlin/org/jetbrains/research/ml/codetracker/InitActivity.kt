package org.jetbrains.research.ml.codetracker

import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.project.Project
import com.intellij.openapi.startup.StartupActivity
import org.jetbrains.research.ml.codetracker.tracking.TaskFileHandler


class InitActivity : StartupActivity {
    private val logger: Logger = Logger.getInstance(javaClass)

    init {
        logger.info("${Plugin.PLUGIN_ID}: startup activity")
    }

    override fun runActivity(project: Project) {
        logger.info("${Plugin.PLUGIN_ID}: run activity")
        TaskFileHandler.addProject(project)
    }
}
