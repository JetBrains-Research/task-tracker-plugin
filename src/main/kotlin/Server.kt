import com.google.gson.GsonBuilder
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import java.io.File
import java.io.IOException
import java.net.URL
import java.util.logging.Logger


interface Server {
    fun getTasks() : List<Task>

    fun sendTrackingData(file: File)

    fun sendData(request: Request): Response
}

object PluginServer : Server {
    private val log: Logger = Logger.getLogger(javaClass.name)
    private val client = OkHttpClient()
    private val MEDIA_TYPE_CSV = "text/csv".toMediaType()
    private const val baseUrl: String = "http://coding-assistant-helper.ru/api/"
    private const val MAX_COUNT_ATTEMPTS = 5


    init {
        log.info("init server")
        log.info("Max count attempt of sending data to server = ${MAX_COUNT_ATTEMPTS}")
    }

    override fun sendData(request: Request): Response {
        return client.newCall(request).execute()
    }

    override fun sendTrackingData(file: File) {
        var curCountAttempts = 0
        val currentUrl = baseUrl + "data-item"
        log.info("...sending file ${file.name}")

        val requestBody = MultipartBody.Builder()
            .setType(MultipartBody.FORM)
            .addFormDataPart(
                "code", file.name,
                RequestBody.create(MEDIA_TYPE_CSV, file)
            ).build()

        val request = Request.Builder()
            .url(currentUrl)
            .post(requestBody)
            .build()

        while (curCountAttempts < MAX_COUNT_ATTEMPTS) {
            log.info("An attempt of sending data to server is number ${curCountAttempts + 1}")
            val code = sendData(request).code
            curCountAttempts++;
            if (code == 200) {
                log.info("Tracking data successfully received")
                break
            }
            log.info("Error sending tracking data")
            // wait for 5 seconds
            Thread.sleep(5_000)
        }
    }

    override fun getTasks(): List<Task> {
        val currentUrl = URL(baseUrl + "task/all")

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
