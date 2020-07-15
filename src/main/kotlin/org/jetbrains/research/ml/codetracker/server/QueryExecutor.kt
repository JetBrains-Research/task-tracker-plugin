package org.jetbrains.research.ml.codetracker.server

import org.jetbrains.research.ml.codetracker.*
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.util.registry.Registry
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import java.lang.IllegalStateException
import java.net.UnknownHostException
import java.util.concurrent.Callable
import java.util.concurrent.Executors
import java.util.concurrent.Future
import java.util.concurrent.TimeUnit

abstract class QueryExecutor {
    companion object {
        private const val SLEEP_TIME = 5_000L
        private const val MAX_COUNT_ATTEMPTS = 5

        private val daemon = Executors.newSingleThreadScheduledExecutor()
    }

    protected val logger: Logger = Logger.getInstance(javaClass)

    private val client: OkHttpClient by lazy {
        logger.info("${Plugin.PLUGIN_ID}: init org.jetbrains.research.ml.codetracker.server. API base url is ${baseUrl}. Max count attempt of sending org.jetbrains.research.ml.codetracker.data to org.jetbrains.research.ml.codetracker.server = ${MAX_COUNT_ATTEMPTS}\"")
        OkHttpClient.Builder()
            .connectTimeout(10, TimeUnit.SECONDS)
            .writeTimeout(10, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .build()
    }

    var isLastSuccessful: Boolean = false
//        private set

    protected val baseUrl: String = Registry.get("codetracker.org.jetbrains.research.ml.codetracker.server.url").asString()

    protected fun executeQuery(request: Request): Future<Response?> {
        var curCountAttempts = 0
        val error = "The query ${request.method} ${request.url} was failed"

        fun executeQueryHelper(): Response? {
            logger.info("${Plugin.PLUGIN_ID}: call execute query helper, attempt $curCountAttempts")
            try {
                curCountAttempts++
                logger.info("${Plugin.PLUGIN_ID}: An attempt ${curCountAttempts} of execute the query ${request.method} ${request.url} has been started")
                val response = client.newCall(request).execute()
                logger.info("${Plugin.PLUGIN_ID}: HTTP status code is ${response.code}")

                if (response.isSuccessful) {
                    logger.info("${Plugin.PLUGIN_ID}: The query ${request.method} ${request.url} was successfully received")
                    isLastSuccessful = true
                    return response
                }
            } catch (e: Exception) {
                logger.info("${Plugin.PLUGIN_ID}: ${error}: internet connection exception")
            }
            if (curCountAttempts < MAX_COUNT_ATTEMPTS) {
                logger.info("${Plugin.PLUGIN_ID}: trying again")
                daemon.schedule( { executeQueryHelper() }, SLEEP_TIME, TimeUnit.SECONDS)
            }
            isLastSuccessful = false
            logger.info("${Plugin.PLUGIN_ID}: $error")
            return null
        }

        return daemon.schedule(Callable<Response?> { executeQueryHelper() }, 0, TimeUnit.SECONDS)
    }

    protected fun isSuccess(response: Response?): Boolean {
        return response?.isSuccessful == true
    }
}