package org.jetbrains.research.ml.codetracker.server

import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.util.registry.Registry
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import org.jetbrains.research.ml.codetracker.Plugin
import java.util.concurrent.Callable
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

abstract class QueryExecutor {
    companion object {
        private const val SLEEP_TIME = 5L
        private const val MAX_COUNT_ATTEMPTS = 5

        private val daemon = Executors.newSingleThreadScheduledExecutor()
    }

    protected val logger: Logger = Logger.getInstance(javaClass)

    private val client: OkHttpClient by lazy {
        logger.info("${Plugin.PLUGIN_ID}: init the server. API base url is ${baseUrl}. Max count attempt of server = ${MAX_COUNT_ATTEMPTS}\"")
        OkHttpClient.Builder()
            .connectTimeout(10, TimeUnit.SECONDS)
            .writeTimeout(10, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .build()
    }

    protected val baseUrl: String = Registry.get("codetracker.org.jetbrains.research.ml.codetracker.server.url").asString()

    protected fun executeQuery(request: Request): Response? {
        var curCountAttempts = 0

        fun executeQueryHelper(): Response? {
            logger.info("${Plugin.PLUGIN_ID}: call execute query helper, attempt $curCountAttempts")
            val error = "The query ${request.method} ${request.url} was failed"
            try {
                curCountAttempts++
                logger.info("${Plugin.PLUGIN_ID}: An attempt $curCountAttempts of execute the query ${request.method} ${request.url} has been started")
                val response = client.newCall(request).execute()
                logger.info("${Plugin.PLUGIN_ID}: HTTP status code is ${response.code}")

                if (response.isSuccessful) {
                    logger.info("${Plugin.PLUGIN_ID}: The query ${request.method} ${request.url} was successfully received")
                    return response
                }
            } catch (e: Exception) {
                logger.info("${Plugin.PLUGIN_ID}: ${error}: internet connection exception")
            }
            logger.info("${Plugin.PLUGIN_ID}: $error")
            return null
        }

        var response = daemon.schedule(Callable { executeQueryHelper() }, 0, TimeUnit.SECONDS).get()
        while (curCountAttempts < MAX_COUNT_ATTEMPTS && response == null) {
            response = daemon.schedule(Callable { executeQueryHelper() }, SLEEP_TIME, TimeUnit.SECONDS).get()
        }
        return response
    }

    protected fun Response?.isSuccessful() : Boolean {
        return this?.isSuccessful == true
    }
}