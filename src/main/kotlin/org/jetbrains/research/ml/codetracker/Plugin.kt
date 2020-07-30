package org.jetbrains.research.ml.codetracker

import com.intellij.openapi.application.PathManager
import com.intellij.openapi.diagnostic.Logger

enum class TestMode {
    ON,
    OFF
}

object Plugin {
    const val PLUGIN_ID = "codetracker"
    val testMode = TestMode.ON
    val codeTrackerFolderPath = "${PathManager.getPluginsPath()}/${PLUGIN_ID}"

    private val logger: Logger = Logger.getInstance(javaClass)

    init {
        logger.info("$PLUGIN_ID: init plugin")
    }
}
