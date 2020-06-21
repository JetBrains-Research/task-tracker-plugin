package server

import kotlinx.serialization.builtins.serializer
import models.Task

object PluginServer {

    var trackerQueryExecutor: TrackerQueryExecutor = TrackerQueryExecutor
    private var collectionsQueryExecutor: CollectionsQueryExecutor = CollectionsQueryExecutor

    // Todo: get genders, countries and experiences

    fun getAvailableLanguages(): List<String> =
        collectionsQueryExecutor.getCollection("language/all", String.serializer())

    fun getTasks(): List<Task> =
        collectionsQueryExecutor.getCollection("task/all", Task.serializer())

}
