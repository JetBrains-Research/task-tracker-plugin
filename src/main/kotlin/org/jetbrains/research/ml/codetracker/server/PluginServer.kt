package org.jetbrains.research.ml.codetracker.server

import kotlinx.serialization.builtins.serializer
import org.jetbrains.research.ml.codetracker.models.*

object PluginServer {
    val paneText: PaneText by lazy { findPaneText() }
    val availableLanguages: List<PaneLanguage> by lazy { findAvailableLanguages() }
    val tasks: List<Task> by lazy { findTasks() }
    val genders: List<Gender> by lazy { findGenders() }
    val countries: List<Country> by lazy { findCountries() }



    private fun findAvailableLanguages(): List<PaneLanguage> {
        return CollectionsQueryExecutor.getCollection("language/all", PaneLanguage.serializer())
    }

    private fun findTasks(): List<Task> {
        return CollectionsQueryExecutor.getCollection("task/all", Task.serializer())
    }

    private fun findGenders(): List<Gender> {
        return CollectionsQueryExecutor.getCollection("gender/all", Gender.serializer())
    }

    private fun findCountries(): List<Country> {
        return CollectionsQueryExecutor.getCollection("country/all", Country.serializer())
    }

    private fun findPaneText(): PaneText {
        val paneTextList = CollectionsQueryExecutor.getCollection("settings", PaneText.serializer())
        if (paneTextList.size == 1) {
            return paneTextList[0]
        }
        //  show the message about internet connection?
        throw IllegalArgumentException("Got incorrect data from server")
    }

}
