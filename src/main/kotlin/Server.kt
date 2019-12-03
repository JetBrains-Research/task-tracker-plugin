import com.google.gson.GsonBuilder
import com.intellij.openapi.application.PathManager
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.Response
import java.io.File
import java.net.URL
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.logging.Logger


interface Server {
    fun getTasks() : List<Task>

    fun sendTrackingData(file: File)
}

object PluginServer : Server {
    private val daemon = Executors.newSingleThreadExecutor()

    private val log: Logger = Logger.getLogger(javaClass.name)
    private val client = OkHttpClient()
    private val media_type_csv = "text/csv".toMediaType()
    private const val BASE_URL: String = "http://coding-assistant-helper.ru/api/"
    private const val MAX_COUNT_ATTEMPTS = 5
    private const val ACTIVITY_TRACKER_FILE = "ide-events.csv"
    private val activityTrackerPath = "${PathManager.getPluginsPath()}/activity-tracker/" + ACTIVITY_TRACKER_FILE
   // private val activityTrackerPath = "/Users/macbook/Library/Application Support/IntelliJIdea2019.2/activity-tracker/" + ACTIVITY_TRACKER_FILE
    private var activityTrackerKey: String? = null

    init {
        log.info("init server")
        log.info("Max count attempt of sending data to server = ${MAX_COUNT_ATTEMPTS}")
        initActivityTrackerInfo()
    }

    private fun sendData(request: Request): Response {
        return client.newCall(request).execute()
    }

    private fun initActivityTrackerInfo() {
        log.info("Activity tracker is working...")
        generateActivityTrackerKey()
    }

    private fun generateActivityTrackerKey() {
        val currentUrl = URL(BASE_URL + "activity-tracker-item")

        log.info("...generating activity tracker key")

        val request = Request.Builder()
            .url(currentUrl)
            .post(RequestBody.create(null, ByteArray(0)))
            .build()

        client.newCall(request).execute().use { response ->
            if (response.code == 200) {
                log.info("Activity tracker key was generated successfully")
                activityTrackerKey = response.body!!.string()
            } else {
                log.info("Generating activity tracker key error")
            }
        }
    }

    var sendNextTime = true

    private fun sendDataToServer(request: Request) {
        if (!sendNextTime) return

        daemon.execute {
            var curCountAttempts = 0

            while (curCountAttempts < MAX_COUNT_ATTEMPTS) {
                log.info("An attempt of sending data to server is number ${curCountAttempts + 1}")
                val code = sendData(request).code
                curCountAttempts++;
                if (code == 200) {
                    log.info("Tracking data successfully received")
                    sendNextTime = true
                    break
                }
                log.info("Error sending tracking data")
                // wait for 5 seconds
                Thread.sleep(5_000)
            }
            if (!sendNextTime) {
                sendNextTime = false
            }
        }
    }

    private fun sendActivityTrackerData() {
        val file = File(activityTrackerPath)
        val fileExists = file.exists()
        if (fileExists) {
            val currentUrl = BASE_URL + "activity-tracker-item/" + activityTrackerKey

            log.info("...sending file ${file.name}")

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

            sendDataToServer(request)
        }
    }

    override fun sendTrackingData(file: File) {
        val currentUrl = BASE_URL + "data-item"
        log.info("...sending file ${file.name}")

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

        sendDataToServer(request)
        if (sendNextTime) {
            sendActivityTrackerData()
        }

        sendNextTime = true
    }

    override fun getTasks(): List<Task> {
        val currentUrl = URL(BASE_URL + "task/all")

        val request = Request.Builder().url(currentUrl).build()

        client.newCall(request).execute().use { response ->
            return if (response.code == 200) {
                log.info("All tasks successfully received")
                val gson = GsonBuilder().create()
                gson.fromJson(response.body!!.string(), Array<Task>::class.java).toList()
            } else {
                log.info("Error getting tasks")
                emptyList()
            }
        }
    }

}
