package org.jetbrains.research.ml.codetracker.server

import org.jetbrains.research.ml.codetracker.*
import com.intellij.openapi.application.PathManager
import org.jetbrains.research.ml.codetracker.models.Extension
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.Request
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File
import java.lang.IllegalStateException
import java.net.URL

object TrackerQueryExecutor : QueryExecutor() {

    private val ACTIVITY_TRACKER_FILE = "ide-events${Extension.CSV.ext}"
    private const val CODE_TRACKER_FILE_FIELD = "codetracker"
    private const val ACTIVITY_TRACKER_FILE_FIELD = "activitytracker"
    private val activityTrackerPath = "${PathManager.getPluginsPath()}/activity-tracker/" + ACTIVITY_TRACKER_FILE

    private var activityTrackerKey: String? = null

    init {
        initActivityTrackerInfo()
    }

    private fun initActivityTrackerInfo() {
        val currentUrl = URL(baseUrl + "activity-tracker-item")
        logger.info("${Plugin.PLUGIN_ID}: ...generating activity tracker key")
        val requestBody = ByteArray(0).toRequestBody(null, 0, 0)
        val request = Request.Builder().url(currentUrl).post(requestBody).build()
        activityTrackerKey = executeQuery(request)?.let { it.body?.string() }
    }

    private fun createTrackerRequestBody(
        fileFieldName: String, file: File,
        toAddActivityTrackerKey: Boolean = false
    ): MultipartBody.Builder {
        val requestBody = MultipartBody.Builder().setType(MultipartBody.FORM)
        requestBody.addFormDataPart(fileFieldName, file.name, file.asRequestBody("text/csv".toMediaType()))
        if (toAddActivityTrackerKey) {
            activityTrackerKey?.let { requestBody.addFormDataPart("activityTrackerKey", it) }
        }
        return requestBody
    }

    private fun sendActivityTrackerData() {
        val file = File(activityTrackerPath)
        if (file.exists()) {
            val currentUrl = baseUrl + "activity-tracker-item/" + activityTrackerKey
            logger.info("${Plugin.PLUGIN_ID}: ...sending file ${file.name}")
            val requestBody = createTrackerRequestBody(ACTIVITY_TRACKER_FILE_FIELD, file).build()
            val response = executeQuery(Request.Builder().url(currentUrl).put(requestBody).build())
            if (!response.isSuccessful()) {
                logger.info("${Plugin.PLUGIN_ID}: cannot send activity tracker data")
                throw IllegalStateException("Unsuccessful server response")
            }
        } else {
            logger.info("${Plugin.PLUGIN_ID}: activity-tracker file doesn't exist")
        }
    }

    fun sendCodeTrackerData(file: File) {
        val currentUrl = baseUrl + "data-item"
        logger.info("${Plugin.PLUGIN_ID}: ...sending file ${file.name}")
        val requestBody = createTrackerRequestBody(CODE_TRACKER_FILE_FIELD, file,activityTrackerKey != null).build()
        val response = executeQuery(Request.Builder().url(currentUrl).post(requestBody).build())
        if (response.isSuccessful()) {
            if (activityTrackerKey != null) {
                sendActivityTrackerData()
            }
        } else {
            logger.info("${Plugin.PLUGIN_ID}: cannot send codetracker data")
            throw IllegalStateException("Unsuccessful server response")
        }
    }
}
