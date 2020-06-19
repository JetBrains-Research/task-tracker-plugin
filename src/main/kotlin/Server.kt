import com.google.gson.Gson
import com.intellij.openapi.application.Application
import com.intellij.openapi.application.ApplicationManager
import kotlinx.serialization.json.*
import com.intellij.openapi.application.PathManager
import com.intellij.openapi.diagnostic.Logger
import kotlinx.serialization.ImplicitReflectionSerializer
import kotlinx.serialization.builtins.list
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.parse
import kotlinx.serialization.serializer
import models.Extension
import models.Task
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import java.io.File
import java.net.URL
import java.net.UnknownHostException
import java.util.concurrent.CompletableFuture
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit


interface Server {
    fun getTasks(): List<Task>

    fun getAvailableLanguages(): List<String>

    fun sendTrackingData(file: File, deleteAfter: () -> Boolean, postActivity: () -> Unit = { })

    fun checkSuccessful(): Boolean
}

enum class Model(val model: String) {
    TASK("task"),
    LANGUAGE("language");
}

object PluginServer : Server {
    private enum class FileSendingState {
        NOT_SENT, SENT
    }

    private enum class FileTypes {
        CODE_TRACKER, ACTIVITY_TRACKER
    }

    private const val SLEEP_TIME = 5_000L
    private const val MAX_COUNT_ATTEMPTS = 5

    private var ACTIVITY_TRACKER_FILE = "ide-events${Extension.CSV.ext}"
    private var BASE_URL: String = System.getenv("BASE_URL") ?: "http://localhost:3000/api/"

    private val daemon = Executors.newSingleThreadExecutor()
    private val diagnosticLogger: Logger = Logger.getInstance(javaClass)

    private var activityTrackerKey: String? = null
    private var isLastSuccessful: Boolean = false

    private var client: OkHttpClient
    private val media_type_csv = "text/csv".toMediaType()
    private val activityTrackerPath = "${PathManager.getPluginsPath()}/activity-tracker/" + ACTIVITY_TRACKER_FILE

    init {
        diagnosticLogger.info("${Plugin.PLUGIN_ID}: init server")
        diagnosticLogger.info("${Plugin.PLUGIN_ID}: API base url is $BASE_URL")
        diagnosticLogger.info("${Plugin.PLUGIN_ID}: Max count attempt of sending data to server = $MAX_COUNT_ATTEMPTS")
        client = OkHttpClient.Builder()
            .connectTimeout(10, TimeUnit.SECONDS)
            .writeTimeout(10, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .build()
        initActivityTrackerInfo()
    }

    override fun checkSuccessful(): Boolean = isLastSuccessful

    private fun setIsLastSuccessful(value: Boolean) {
        isLastSuccessful = value
    }

    private fun sendData(request: Request): Response {
        return client.newCall(request).execute()
    }

    private fun initActivityTrackerInfo() {
        diagnosticLogger.info("${Plugin.PLUGIN_ID}: Activity tracker is working...")
        generateActivityTrackerKey()
    }


    private var currentState = FileSendingState.NOT_SENT

    private fun sendDataToServer(request: Request, currentFileType: FileTypes): CompletableFuture<Void> {

        if (currentState == FileSendingState.NOT_SENT && currentFileType == FileTypes.ACTIVITY_TRACKER) {
            return CompletableFuture.completedFuture(null)
        }

        return CompletableFuture.runAsync(Runnable {
            try {
                setIsLastSuccessful(false)
                var curCountAttempts = 0
                while (curCountAttempts < MAX_COUNT_ATTEMPTS) {
                    diagnosticLogger.info("${Plugin.PLUGIN_ID}: An attempt of sending data to server is number ${curCountAttempts + 1}")
                    val response = sendData(request)
                    diagnosticLogger.info("${Plugin.PLUGIN_ID}: HTTP status code is ${response.code}")

                    if (response.isSuccessful) {
                        diagnosticLogger.info("${Plugin.PLUGIN_ID}: Tracking data successfully received")
                        setIsLastSuccessful(true)
                        currentState = FileSendingState.SENT
                        break
                    }
                    curCountAttempts++
                    diagnosticLogger.info("${Plugin.PLUGIN_ID}: Error sending tracking data")
                    Thread.sleep(SLEEP_TIME)
                }
                if (curCountAttempts == MAX_COUNT_ATTEMPTS) {
                    currentState = FileSendingState.NOT_SENT
                }
            } catch (ex: Exception) {
                when (ex) {
                    is UnknownHostException ->
                        diagnosticLogger.info("${Plugin.PLUGIN_ID}: Error sending tracking data: no internet connection")
                    else -> diagnosticLogger.info("${Plugin.PLUGIN_ID}: Error sending tracking data: internet connection exception")
                }
            }
        }, daemon)
    }

    private fun sendActivityTrackerData() {
        val file = File(activityTrackerPath)
        val fileExists = file.exists()
        if (fileExists) {
            val currentUrl = BASE_URL + "activity-tracker-item/" + activityTrackerKey

            diagnosticLogger.info("${Plugin.PLUGIN_ID}: ...sending file ${file.name}")

            val requestBody = MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart(
                    "code", file.name,
                    RequestBody.create(media_type_csv, file)
                )

            val request = Request.Builder()
                .url(currentUrl)
                .put(requestBody.build())
                .build()

            sendDataToServer(request, FileTypes.ACTIVITY_TRACKER)
        } else {
            diagnosticLogger.info("${Plugin.PLUGIN_ID}: activity-tracker file doesn't exist")
        }
    }

    // if deleteAfter is true, the file will be deleted
    override fun sendTrackingData(file: File, deleteAfter: () -> Boolean, postActivity: () -> Unit) {
        val currentUrl = BASE_URL + "data-item"
        diagnosticLogger.info("${Plugin.PLUGIN_ID}: ...sending file ${file.name}")

        val requestBody = MultipartBody.Builder()
            .setType(MultipartBody.FORM)
            .addFormDataPart(
                "code", file.name,
                RequestBody.create(media_type_csv, file)
            )

        if (activityTrackerKey != null) {
            requestBody.addFormDataPart("activityTrackerKey", activityTrackerKey!!)
        }

        val request = Request.Builder()
            .url(currentUrl)
            .post(requestBody.build())
            .build()

        val future = sendDataToServer(request, FileTypes.CODE_TRACKER)

        future.thenRun {
            if (deleteAfter()) {
                diagnosticLogger.info("${Plugin.PLUGIN_ID}: delete file ${file.name}")
                file.delete()
            }
            if (currentState == FileSendingState.SENT) {
                sendActivityTrackerData()
            }
            postActivity()
        }
        future.get()
    }

    @ImplicitReflectionSerializer
    private inline fun <reified T : Any> getCollection(url: String, collection: Model): List<T> {
        val currentUrl = URL("${BASE_URL}${url}")
        val request = Request.Builder().url(currentUrl).build()
        try {
            setIsLastSuccessful(false)
            client.newCall(request).execute().use { response ->
                return if (response.isSuccessful) {
                    diagnosticLogger.info("${Plugin.PLUGIN_ID}: All ${collection.model}s successfully received")
                    setIsLastSuccessful(true)
                    val json = Json(JsonConfiguration.Stable)
                    json.parse(T::class.serializer().list, response.body!!.string())
                } else {
                    diagnosticLogger.info("${Plugin.PLUGIN_ID}: Error getting ${collection.model}s")
                    emptyList()
                }
            }
        } catch (ex: Exception) {
            when (ex) {
                is UnknownHostException ->
                    diagnosticLogger.info("${Plugin.PLUGIN_ID}: Error getting ${collection.model}s: no internet connection")
                else -> diagnosticLogger.info("${Plugin.PLUGIN_ID}: Error getting ${collection.model}s: internet connection exception")
            }
            return emptyList()
        }
    }

    @ImplicitReflectionSerializer
    override fun getAvailableLanguages(): List<String> {
        return getCollection("language/all", Model.LANGUAGE)
    }

    @ImplicitReflectionSerializer
    override fun getTasks(): List<Task> {
        return getCollection("task/all", Model.TASK)
    }

    private fun generateActivityTrackerKey() {
        val currentUrl = URL(BASE_URL + "activity-tracker-item")

        diagnosticLogger.info("${Plugin.PLUGIN_ID}: ...generating activity tracker key")

        val request = Request.Builder()
            .url(currentUrl)
            .post(RequestBody.create(null, ByteArray(0)))
            .build()
        setIsLastSuccessful(false)

        CompletableFuture.runAsync(Runnable {
            ApplicationManager.getApplication().executeOnPooledThread({
                try {
                    var curCountAttempts = 0
                    while (curCountAttempts < MAX_COUNT_ATTEMPTS) {
                        val response = client.newCall(request).execute()
                        if (response.isSuccessful) {
                            diagnosticLogger.info("${Plugin.PLUGIN_ID}: Activity tracker key was generated successfully")
                            setIsLastSuccessful(true)
                            response.body!!.string()
                        } else {
                            diagnosticLogger.info("${Plugin.PLUGIN_ID}: Generating activity tracker key error")
                            curCountAttempts++
                            Thread.sleep(SLEEP_TIME)
                        }
                    }
                } catch (ex: Exception) {
                    when (ex) {
                        is UnknownHostException ->
                            diagnosticLogger.info("${Plugin.PLUGIN_ID}: Generating activity tracker key error: no internet connection")
                        else -> diagnosticLogger.info("${Plugin.PLUGIN_ID}: Error sending tracking data: internet connection exception")
                    }
                }
            })
        }, daemon)
    }

//    private fun executeQuery(request: Request): String {
//        val failedMessageError = "${Plugin.PLUGIN_ID}: The query ${request.method} ${request.url} was failed"
//        try {
//            var curCountAttempts = 0
//            while (curCountAttempts < MAX_COUNT_ATTEMPTS) {
//                val response = client.newCall(request).execute()
//                if (response.isSuccessful) {
//                    diagnosticLogger.info("${Plugin.PLUGIN_ID}: The query ${request.method} ${request.url} was executed successfully")
//                    setIsLastSuccessful(true)
//                    // Todo: return string
//                    return response.body!!.string()
//                } else {
//                    diagnosticLogger.info(failedMessageError)
//                    curCountAttempts++
//                    Thread.sleep(SLEEP_TIME)
//                }
//            }
//        } catch (ex: Exception) {
//            when (ex) {
//                is UnknownHostException ->
//                    diagnosticLogger.info("${failedMessageError}: no internet connection")
//                else -> diagnosticLogger.info("${failedMessageError}: internet connection exception")
//            }
//        }
//    }

}
