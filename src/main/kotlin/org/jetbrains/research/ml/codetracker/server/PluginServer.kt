package org.jetbrains.research.ml.codetracker.server

import com.intellij.openapi.diagnostic.Logger
import kotlinx.serialization.builtins.serializer
import org.jetbrains.research.ml.codetracker.Plugin
import org.jetbrains.research.ml.codetracker.models.*

object PluginServer {
    val paneText: PaneText by lazy { findPaneText() }
    val availableLanguages: List<PaneLanguage> by lazy { findAvailableLanguages() }
    val tasks: List<Task> by lazy { findTasks() }
    val genders: List<Gender> by lazy { findGenders() }
    val countries: List<Country> by lazy { findCountries() }
    private val logger: Logger = Logger.getInstance(javaClass)

    private fun findAvailableLanguages(): List<PaneLanguage> {
        logger.info("${Plugin.PLUGIN_ID}: Getting available languages from server")
        return CollectionsQueryExecutor.getCollection("language/all", PaneLanguage.serializer())
    }

    private fun findTasks(): List<Task> {
        logger.info("${Plugin.PLUGIN_ID}: Getting tasks from server")
        return CollectionsQueryExecutor.getCollection("task/all", Task.serializer())
    }

    private fun findGenders(): List<Gender> {
        logger.info("${Plugin.PLUGIN_ID}: Getting genders from server")
        return CollectionsQueryExecutor.getCollection("gender/all", Gender.serializer())
    }

    private fun findCountries(): List<Country> {
        logger.info("${Plugin.PLUGIN_ID}: Getting countries from server")
        return CollectionsQueryExecutor.getCollection("country/all", Country.serializer())
    }

    private fun findPaneText(): PaneText {
        logger.info("${Plugin.PLUGIN_ID}: Getting pane text from server")
        val paneTextList = CollectionsQueryExecutor.getCollection("settings", PaneText.serializer())
        if (paneTextList.size == 1) {
            return paneTextList[0]
        }
        //  show the message about internet connection?
        throw IllegalArgumentException("Got incorrect data from server")
    }

}
