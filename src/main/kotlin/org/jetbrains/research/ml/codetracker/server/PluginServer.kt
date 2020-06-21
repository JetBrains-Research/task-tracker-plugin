package org.jetbrains.research.ml.codetracker.server

import kotlinx.serialization.builtins.serializer
import org.jetbrains.research.ml.codetracker.models.Task

object PluginServer {
    // Todo: get genders, countries and experiences

    fun getAvailableLanguages(): List<String> {
        return CollectionsQueryExecutor.getCollection("language/all", String.serializer())
    }

    fun getTasks(): List<Task> {
        return CollectionsQueryExecutor.getCollection("task/all", Task.serializer())
    }

}
