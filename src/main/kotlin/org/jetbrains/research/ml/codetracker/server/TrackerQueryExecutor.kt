package org.jetbrains.research.ml.codetracker.server

import org.jetbrains.research.ml.codetracker.*
import com.intellij.openapi.application.PathManager
import io.reactivex.internal.operators.maybe.MaybeDoAfterSuccess
import org.jetbrains.research.ml.codetracker.models.Extension
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.Request
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import java.io.File
import java.lang.IllegalStateException
import java.net.URL

object TrackerQueryExecutor : QueryExecutor() {

    private val ACTIVITY_TRACKER_FILE = "ide-events${Extension.CSV.ext}"
    private const val CODE_TRACKER_FILE_FIELD = "codetracker"
    private const val ACTIVITY_TRACKER_FILE_FIELD = "activitytracker"
    private val activityTrackerPath = "${PathManager.getPluginsPath()}/activity-tracker/" + ACTIVITY_TRACKER_FILE

    var activityTrackerKey: String? = null

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

    private fun sendTrackerData(file: File,
                                fileFieldName: String,
                                baseUrlSuffix: String,
                                method: String,
                                toAddActivityTrackerKey: Boolean = false,
                                afterSuccessfulResponse: () -> Unit = { }) {
        if (file.exists()) {
            val currentUrl = baseUrl + baseUrlSuffix
            logger.info("${Plugin.PLUGIN_ID}: ...sending file ${file.name}")
            val requestBody = MultipartBody.Builder().setType(MultipartBody.FORM)
            requestBody.addFormDataPart(fileFieldName, file.name, file.asRequestBody("text/csv".toMediaType()))
            if (toAddActivityTrackerKey) {
                activityTrackerKey?.let { requestBody.addFormDataPart("activityTrackerKey", it) }
            }
            val response =  executeQuery(Request.Builder().url(currentUrl).method(method, requestBody.build()).build())
            if (response.isSuccessful()) {
                afterSuccessfulResponse()
            } else {
                logger.info("${Plugin.PLUGIN_ID}: cannot send $fileFieldName data")
                throw IllegalStateException("Unsuccessful server response")
            }
        } else {
            logger.info("${Plugin.PLUGIN_ID}: file ${file.name} for $fileFieldName doesn't exist")
        }
    }

    fun sendCodeTrackerData(file: File) {
        val isKeyNull = activityTrackerKey == null
        sendTrackerData(file, CODE_TRACKER_FILE_FIELD, "data-item", "POST", !isKeyNull) {
            if (!isKeyNull) {
                sendTrackerData(File(activityTrackerPath), ACTIVITY_TRACKER_FILE_FIELD,
                    "activity-tracker-item/$activityTrackerKey", "PUT")
            }
        }
    }
}
