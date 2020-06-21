package server

import com.intellij.openapi.application.PathManager
import models.Extension
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.Request
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File
import java.net.URL

object TrackerQueryExecutor {

    private val ACTIVITY_TRACKER_FILE = "ide-events${Extension.CSV.ext}"
    private const val CODE_TRACKER_FILE_FIELD = "codetracker"
    private const val ACTIVITY_TRACKER_FILE_FIELD = "activitytracker"
    private val activityTrackerPath = "${PathManager.getPluginsPath()}/activity-tracker/" + ACTIVITY_TRACKER_FILE
    private var activityTrackerKey: String? = null

    init {
        initActivityTrackerInfo()
    }

    private fun initActivityTrackerInfo() {
        val currentUrl = URL(QueryExecutor.baseUrl + "activity-tracker-item")
        QueryExecutor.logger.info("${Plugin.PLUGIN_ID}: ...generating activity tracker key")

        val request = Request.Builder().url(currentUrl).post(
            ByteArray(0)
                .toRequestBody(null, 0, 0)
        ).build()
        activityTrackerKey = QueryExecutor.executeQuery(request).get()?.let { it.body?.string() }
    }

    private fun createTrackerRequestBody(
        fileFieldName: String, file: File,
        toAddActivityTrackerKey: Boolean = false
    ): MultipartBody.Builder {
        val requestBody = MultipartBody.Builder()
            .setType(MultipartBody.FORM)
            .addFormDataPart(
                fileFieldName, file.name,
                file.asRequestBody("text/csv".toMediaType())
            )
        if (toAddActivityTrackerKey) {
            activityTrackerKey?.let { requestBody.addFormDataPart("activityTrackerKey", it) }
        }
        return requestBody
    }

    private fun sendActivityTrackerData() {
        val file = File(activityTrackerPath)
        if (file.exists()) {
            val currentUrl = QueryExecutor.baseUrl + "activity-tracker-item/" + activityTrackerKey
            QueryExecutor.logger.info("${Plugin.PLUGIN_ID}: ...sending file ${file.name}")
            QueryExecutor.executeQuery(
                Request.Builder()
                    .url(currentUrl)
                    .put(
                        createTrackerRequestBody(
                            ACTIVITY_TRACKER_FILE_FIELD,
                            file
                        ).build()
                    )
                    .build()
            )
        } else {
            QueryExecutor.logger.info("${Plugin.PLUGIN_ID}: activity-tracker file doesn't exist")
        }
    }

    fun sendCodeTrackerData(file: File, deleteAfter: () -> Boolean, postActivity: () -> Unit) {
        val currentUrl = QueryExecutor.baseUrl + "data-item"
        QueryExecutor.logger.info("${Plugin.PLUGIN_ID}: ...sending file ${file.name}")
        val future = QueryExecutor.executeQuery(
            Request.Builder()
                .url(currentUrl)
                .post(
                    createTrackerRequestBody(
                        CODE_TRACKER_FILE_FIELD,
                        file,
                        activityTrackerKey != null
                    ).build()
                )
                .build()
        )
        if (future.get()?.isSuccessful == true) {
            if (deleteAfter()) {
                QueryExecutor.logger.info("${Plugin.PLUGIN_ID}: delete file ${file.name}")
                file.delete()
            }
            if (activityTrackerKey != null) {
                sendActivityTrackerData()
            }
        }
        postActivity()
    }

    fun sendCodeTrackerData(file: File) {
        return sendCodeTrackerData(file, { false }, {})
    }
}