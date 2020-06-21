package server

import kotlinx.serialization.builtins.serializer
import models.Task

object PluginServer {
    // Todo: get genders, countries and experiences

    fun getAvailableLanguages(): List<String> {
        return CollectionsQueryExecutor.getCollection("language/all", String.serializer())
    }

    fun getTasks(): List<Task> {
        return CollectionsQueryExecutor.getCollection("task/all", Task.serializer())
    }

}
