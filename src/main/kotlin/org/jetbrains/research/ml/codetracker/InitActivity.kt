package org.jetbrains.research.ml.codetracker

import org.jetbrains.research.ml.codetracker.*
import com.intellij.openapi.Disposable
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.project.Project
import com.intellij.openapi.startup.StartupActivity
import com.intellij.openapi.util.Disposer
import org.jetbrains.research.ml.codetracker.server.PluginServer
import org.jetbrains.research.ml.codetracker.ui.ServerDialogWrapper


class InitActivity : StartupActivity {
    private val logger: Logger = Logger.getInstance(javaClass)

    val pluginServer: PluginServer = PluginServer

    init {
        logger.info("${Plugin.PLUGIN_ID}: startup activity")
        println(pluginServer.getCountries())

        Disposer.register(
            ApplicationManager.getApplication(),
            Disposable {
                logger.info("${Plugin.PLUGIN_ID}: dispose startup activity")
                if (!Plugin.stopTracking()) {
//                    Todo: don't run it there....
                    ApplicationManager.getApplication().invokeAndWait {
                        ServerDialogWrapper().show()
                    }
                }
            })
    }

    override fun runActivity(project: Project) {
        logger.info("${Plugin.PLUGIN_ID}: run activity")
    }
}
