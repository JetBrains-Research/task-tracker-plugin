package org.jetbrains.research.ml.codetracker.server

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.diagnostic.Logger
import com.intellij.util.messages.Topic
import org.jetbrains.research.ml.codetracker.Plugin
import org.jetbrains.research.ml.codetracker.models.*
import java.util.function.Consumer

enum class ServerConnectionResult {
    SUCCESS,
    FAIL
}

interface ServerConnectionNotifier : Consumer<ServerConnectionResult> {
    companion object {
        val SERVER_CONNECTION_TOPIC = Topic.create("server connection result", ServerConnectionNotifier::class.java)
    }
}

// What do we want to do on reconnection? reload all UI? update only depending on server uiData (like countries or genders?
interface ServerReconnectionNotifier  {
    companion object {
        val SERVER_RECONNECTION_TOPIC = Topic.create("server reconnection", ServerReconnectionNotifier::class.java)
    }
    fun onNotification()
}

object PluginServer {

    var paneText: PaneText? = null
    var availableLanguages: List<PaneLanguage> = emptyList()
    var tasks: List<Task> = emptyList()
    var genders: List<Gender> = emptyList()
    var countries: List<Country> = emptyList()

    private val logger: Logger = Logger.getInstance(javaClass)

    init {
        safeFind { findData() }
    }

    /**
     * Tries to find all data again, sends connection results, and after that notifies all subscribers about reconnection
     */
    fun reconnect() {
        safeFind { findData() }
        val publisher = ApplicationManager.getApplication().messageBus.syncPublisher(ServerReconnectionNotifier.SERVER_RECONNECTION_TOPIC)
        publisher.onNotification()
    }

    /**
     * Tries to find all data and sends the result to all subscribers
     */
    private fun safeFind(find: () -> Unit) {
        val publisher = ApplicationManager.getApplication().messageBus.syncPublisher(ServerConnectionNotifier.SERVER_CONNECTION_TOPIC)
        try {
            find()
            publisher.accept(ServerConnectionResult.SUCCESS)
        } catch (e: java.lang.IllegalStateException) {
            publisher.accept(ServerConnectionResult.FAIL)
        }
    }

    private fun findData() {
        paneText = findPaneText()
        availableLanguages = findAvailableLanguages()
        tasks = findTasks()
        genders = findGenders()
        countries = findCountries()
    }

    private fun findAvailableLanguages(): List<PaneLanguage> {
        logger.info("${Plugin.PLUGIN_ID}: Getting available languages from server, current thread is ${Thread.currentThread().name}")
        return CollectionsQueryExecutor.getCollection("language/all", PaneLanguage.serializer())
    }

    private fun findTasks(): List<Task> {
        logger.info("${Plugin.PLUGIN_ID}: Getting tasks from server, current thread is ${Thread.currentThread().name}")
        return CollectionsQueryExecutor.getCollection("task/all", Task.serializer())
    }

    private fun findGenders(): List<Gender> {
        logger.info("${Plugin.PLUGIN_ID}: Getting genders from server, current thread is ${Thread.currentThread().name}")
        return CollectionsQueryExecutor.getCollection("gender/all", Gender.serializer())
    }

    private fun findCountries(): List<Country> {
        logger.info("${Plugin.PLUGIN_ID}: Getting countries from server, current thread is ${Thread.currentThread().name}")
        return CollectionsQueryExecutor.getCollection("country/all", Country.serializer())
    }

    private fun findPaneText(): PaneText {
        logger.info("${Plugin.PLUGIN_ID}: Getting pane text from server, current thread is ${Thread.currentThread().name}")
        val paneTextList = CollectionsQueryExecutor.getCollection("settings", PaneText.serializer())
        if (paneTextList.size == 1) {
            return paneTextList[0]
        }
        //  show the message about internet connection?
        throw IllegalStateException("Got incorrect data from server")
    }

}
