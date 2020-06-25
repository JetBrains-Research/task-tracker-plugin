package org.jetbrains.research.ml.codetracker.server

import kotlinx.serialization.builtins.serializer
import org.jetbrains.research.ml.codetracker.models.Country
import org.jetbrains.research.ml.codetracker.models.Gender
import org.jetbrains.research.ml.codetracker.models.PaneLanguage
import org.jetbrains.research.ml.codetracker.models.Task

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

}
