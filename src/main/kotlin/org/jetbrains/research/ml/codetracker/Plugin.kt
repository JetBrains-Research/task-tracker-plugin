package org.jetbrains.research.ml.codetracker

import com.intellij.openapi.application.PathManager
import com.intellij.openapi.diagnostic.Logger
import org.jetbrains.research.ml.codetracker.models.Language

enum class TestMode {
    ON,
    OFF
}

object Plugin {
    const val PLUGIN_ID = "codetracker"
    val testMode = TestMode.ON
    val codeTrackerFolderPath = "${PathManager.getPluginsPath()}/${PLUGIN_ID}"
    // TODO: How will we change it?
    val currentLanguage = Language.PYTHON

    private val logger: Logger = Logger.getInstance(javaClass)

    init {
        logger.info("$PLUGIN_ID: init plugin, test mode is $testMode")
    }
}
