package org.jetbrains.research.ml.codetracker

import com.intellij.ide.IdeBundle
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.application.ModalityState
import com.intellij.openapi.application.PathManager
import com.intellij.openapi.application.ex.ApplicationEx
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.project.Project
import com.intellij.openapi.project.ProjectManager
import com.intellij.openapi.ui.Messages
import net.lingala.zip4j.ZipFile
import java.io.File
import java.io.InputStream
import java.nio.file.Files
import java.nio.file.Paths
import java.nio.file.StandardCopyOption


enum class TestMode {
    ON,
    OFF
}

/**
 * Represents a plugin required by Codetracker
 */
data class RequiredPlugin(val name: String, val zipFile: String) {
    private val resourceFolder = "requiredplugins"
    private val logger: Logger = Logger.getInstance(javaClass)

    /**
     * Perhaps there is a better way of doing this
     */
    fun isInstalled() : Boolean {
        val isAlreadyInstalled =  File("${PathManager.getPluginsPath()}/${name}").exists()
        logger.info("${Plugin.PLUGIN_ID}: plugin ${name} is already installed: $isAlreadyInstalled")
        return isAlreadyInstalled
    }

    /**
     * Not the best way for plugins installation but it works.
     * Sure there is a way to install plugins properly (for example via PluginInstaller) but I have no time to find it out.
     * Gets plugin .zip from resources, moves it to plugins dir and extracts.
     */
    fun install() : Boolean {
        return if (!this.isInstalled()) {
            val input: InputStream = javaClass.getResourceAsStream("$resourceFolder/$zipFile")
            val targetPath = "${PathManager.getPluginsPath()}/${zipFile}"
            Files.copy(input, Paths.get(targetPath), StandardCopyOption.REPLACE_EXISTING)
            val zipFile = ZipFile(targetPath)
            zipFile.extractAll(PathManager.getPluginsPath())
            true
        } else {
            false
        }
    }
}

object Plugin {
    const val PLUGIN_ID = "codetracker"
    val testMode = TestMode.ON
    val codeTrackerFolderPath = "${PathManager.getPluginsPath()}/${PLUGIN_ID}"

    private val requiredPlugins = arrayListOf(
        RequiredPlugin("activity-tracker-plugin", "activity-tracker-plugin.zip"),
        RequiredPlugin("JavaFX plugin", "JavaFX_plugin.zip")
    )

    private val logger: Logger = Logger.getInstance(javaClass)

    init {
        logger.info("$PLUGIN_ID: init plugin, test mode is $testMode")
    }


    fun checkRequiredPlugins(requiredPlugins: List<RequiredPlugin> = this.requiredPlugins) : Boolean {
        return requiredPlugins.all { it.isInstalled() }
    }

    fun installRequiredPlugins(project: Project, requiredPlugins: List<RequiredPlugin> = this.requiredPlugins) {
        logger.info("$PLUGIN_ID: starting installing plugins $requiredPlugins")
        if (requiredPlugins.map { it.install() }.any{ it }) {
            restartIde(project)
        }
    }

    fun restartIde(project: Project) {
        logger.info("$PLUGIN_ID: restarting IDE")

        ApplicationManager.getApplication().invokeLater(
            {
                val result: Int = Messages.showDialog(
                    project,
                    "Restart is needed to complete Codetracker installation",
                    "Complete Codetracker installation",
                    arrayOf("Restart"),
                    0,
                    Messages.getQuestionIcon()
                )
                if (result == 0) {
                    val app = ApplicationManager.getApplication() as ApplicationEx
                    app.restart(true)
                }
            },
            ModalityState.NON_MODAL
        )
    }
}
