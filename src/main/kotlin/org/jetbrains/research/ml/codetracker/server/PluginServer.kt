package org.jetbrains.research.ml.codetracker.server

import kotlinx.serialization.builtins.serializer
import org.jetbrains.research.ml.codetracker.models.*

object PluginServer {
    // Todo: get genders, countries and experiences

    fun getAvailableLanguages(): List<PaneLanguage> {
        return CollectionsQueryExecutor.getCollection("language/all", PaneLanguage.serializer())
    }

    fun getTasks(): List<Task> {
        return CollectionsQueryExecutor.getCollection("task/all", Task.serializer())
    }

    fun getGenders(): List<Gender> {
        return CollectionsQueryExecutor.getCollection("gender/all", Gender.serializer())
    }

    fun getCountries(): List<Country> {
        return CollectionsQueryExecutor.getCollection("country/all", Country.serializer())
    }

    fun getPaneText(): PaneText? {
        val paneTextList = CollectionsQueryExecutor.getCollection("settings", PaneText.serializer())
        if (paneTextList.size == 1) {
            return paneTextList[0]
        }
        return null
    }

}
