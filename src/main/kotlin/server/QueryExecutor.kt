package server

import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.util.registry.Registry
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import java.net.UnknownHostException
import java.util.concurrent.Executors
import java.util.concurrent.Future
import java.util.concurrent.Callable
import java.util.concurrent.TimeUnit

abstract class QueryExecutor {

    companion object {
        private const val SLEEP_TIME = 5_000L
        private const val MAX_COUNT_ATTEMPTS = 5
    }

    protected val logger: Logger = Logger.getInstance(javaClass)
    private val daemon = Executors.newSingleThreadScheduledExecutor()

    private var client: OkHttpClient
    private var isLastSuccessful: Boolean = false
    protected val baseUrl: String = Registry.get("codetracker.server.url").asString()

    init {
        logger.info("${Plugin.PLUGIN_ID}: init server. API base url is ${baseUrl}. Max count attempt of sending data to server = ${MAX_COUNT_ATTEMPTS}\"")
        client = OkHttpClient.Builder()
            .connectTimeout(10, TimeUnit.SECONDS)
            .writeTimeout(10, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .build()
    }

    fun checkSuccessful(): Boolean = isLastSuccessful

    protected fun executeQuery(request: Request): Future<Response?> {
        var curCountAttempts = 0
        val errorMessageTemplate = "The query ${request.method} ${request.url} was failed"
        fun executeQueryHelper(): Response? {
            try {
                logger.info("${Plugin.PLUGIN_ID}: An attempt ${curCountAttempts + 1} of execute the query ${request.method} ${request.url} has been started")
                val response = client.newCall(request).execute()
                logger.info("${Plugin.PLUGIN_ID}: HTTP status code is ${response.code}")

                if (response.isSuccessful) {
                    logger.info("${Plugin.PLUGIN_ID}: The query ${request.method} ${request.url} was successfully")
                    isLastSuccessful = true
                    return response
                }
                curCountAttempts++
                if (curCountAttempts < MAX_COUNT_ATTEMPTS) {
                    daemon.schedule(
                        { executeQueryHelper() },
                        SLEEP_TIME, TimeUnit.SECONDS
                    )
                }
            } catch (ex: Exception) {
                when (ex) {
                    is UnknownHostException ->
                        logger.info("${Plugin.PLUGIN_ID}: ${errorMessageTemplate}: no internet connection")
                    else -> logger.info("${Plugin.PLUGIN_ID}: ${errorMessageTemplate}: internet connection exception")
                }
            }
            isLastSuccessful = false
            logger.info("${Plugin.PLUGIN_ID}: $errorMessageTemplate")
            return null
        }

        return daemon.schedule(Callable<Response?> {
            executeQueryHelper()
        }, 0, TimeUnit.SECONDS)
    }
}