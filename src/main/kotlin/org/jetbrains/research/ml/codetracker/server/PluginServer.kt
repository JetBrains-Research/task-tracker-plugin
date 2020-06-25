package org.jetbrains.research.ml.codetracker.server

import kotlinx.serialization.builtins.serializer
import org.jetbrains.research.ml.codetracker.models.PaneLanguage
import org.jetbrains.research.ml.codetracker.models.Task

object PluginServer {
    // Todo: get genders, countries and experiences

    init {
        println("Init server")
        println(getAvailableLanguages())
    }

    fun getAvailableLanguages(): List<PaneLanguage> {
        return CollectionsQueryExecutor.getCollection("language/all", PaneLanguage.serializer())
    }

    fun getTasks(): List<Task> {
        return CollectionsQueryExecutor.getCollection("task/all", Task.serializer())
    }

}
