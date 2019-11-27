import com.google.gson.GsonBuilder
import java.io.File
import java.util.logging.Logger
import java.net.URL
import okhttp3.Request
import okhttp3.OkHttpClient
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody
import okhttp3.MultipartBody


interface Server {
    fun getTasks() : List<Task>

    fun sendTrackingData(file: File)
}

// todo: replace with the real one
object DummyServer : Server {
    private val log: Logger = Logger.getLogger(javaClass.name)
    private val client = OkHttpClient()
    private val MEDIA_TYPE_CSV = "text/csv".toMediaType()
    private val baseUrl: String = "https://damp-taiga-50950.herokuapp.com/api/"
    //private val baseUrl: String = "http://localhost:3000/api/"

    init {
        log.info("init server")
    }

    override fun sendTrackingData(file: File) {
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

        client.newCall(request).execute().use { response ->
            if (response.code == 200) {
                log.info("Tracking data successfully received")
            } else {
                log.info("Error sending tracking data")
            }
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
