package org.jetbrains.research.ml.codetracker.server

import kotlinx.serialization.KSerializer
import kotlinx.serialization.builtins.list
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonConfiguration
import okhttp3.Request
import okhttp3.Response
import java.net.URL

object CollectionsQueryExecutor : QueryExecutor() {

    fun <T> getCollection(url: String, serializer: KSerializer<T>): List<T> {
        return parseResponse(getResponse(url), serializer) { it.list }
    }

    fun <T> getItemFromCollection(url: String, serializer: KSerializer<T>): T {
        return parseResponse(getResponse(url), serializer) { it }
    }

    private fun <T, R> parseResponse(
        response: Response?,
        serializer: KSerializer<T>,
        transform: (KSerializer<T>) -> KSerializer<R>
    ): R {
        if (isSuccess(response)) {
            return Json(JsonConfiguration.Stable).parse(transform(serializer), response?.body?.string() ?: "")
        }
        throw IllegalStateException("Unsuccessful server response")
    }

    private fun getResponse(url: String): Response? {
        return executeQuery(Request.Builder().url(URL("${baseUrl}${url}")).build())
    }

}