package server

import kotlinx.serialization.KSerializer
import kotlinx.serialization.builtins.list
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonConfiguration
import okhttp3.Request
import okhttp3.Response
import java.net.URL

class CollectionsQueryExecutor : QueryExecutor() {

    inline fun <reified T : Any> parseResponse(response: Response?, serializer: KSerializer<T>): List<T> {
        if (response?.isSuccessful == true) {

            return Json(JsonConfiguration.Stable).parse(serializer.list, response.body?.string() ?: "")
        }
        return emptyList()
    }

    inline fun <reified T : Any> getCollection(url: String, serializer: KSerializer<T>): List<T> {
        return parseResponse(
            `access$executeQuery`(
                Request.Builder().url(URL("${`access$baseUrl`}${url}")).build()
            ).get(), serializer
        )
    }

    @PublishedApi
    internal fun `access$executeQuery`(request: Request) = executeQuery(request)

    @PublishedApi
    internal val `access$baseUrl`: String
        get() = baseUrl


}