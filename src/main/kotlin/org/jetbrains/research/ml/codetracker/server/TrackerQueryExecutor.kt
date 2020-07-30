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

    var studentId: String? = null

    init {
        initStudentId()
    }

    private fun initStudentId() {
        val currentUrl = URL(baseUrl + "student")
        logger.info("${Plugin.PLUGIN_ID}: ...generating student id")
        val requestBody = ByteArray(0).toRequestBody(null, 0, 0)
        val request = Request.Builder().url(currentUrl).post(requestBody).build()
        studentId = executeQuery(request)?.let { it.body?.string() }
    }

    private fun getRequestBodyForStudentQuery(
        codeTrackerKey: String,
        activityTrackerKey: String
    ): MultipartBody.Builder {
        val requestBody = MultipartBody.Builder().setType(MultipartBody.FORM)
        requestBody.addFormDataPart("diId", codeTrackerKey)
        requestBody.addFormDataPart("atiId", activityTrackerKey)
        return requestBody
    }

    private fun getRequestBodyForDataQuery(file: File, fileFieldName: String): MultipartBody.Builder {
        if (file.exists()) {
            logger.info("${Plugin.PLUGIN_ID}: ...sending file ${file.name}")
            val requestBody = MultipartBody.Builder().setType(MultipartBody.FORM)
            requestBody.addFormDataPart(fileFieldName, file.name, file.asRequestBody("text/csv".toMediaType()))
            return requestBody
        } else {
            throw IllegalStateException("File ${file.name} for $fileFieldName doesn't exist")
        }
    }

    private fun executeTrackerQuery(
        requestBody: MultipartBody.Builder,
        baseUrlSuffix: String,
        method: String = "POST"
    ): String? {
        val currentUrl = baseUrl + baseUrlSuffix
        val response = executeQuery(Request.Builder().url(currentUrl).method(method, requestBody.build()).build())
        if (response.isSuccessful()) {
            return response?.let { it.body?.string() }
        }
        throw IllegalStateException("Unsuccessful server response")
    }

    fun sendCodeTrackerData(file: File) {
        val codeTrackerKey = executeTrackerQuery(getRequestBodyForDataQuery(file, CODE_TRACKER_FILE_FIELD), "data-item")
        codeTrackerKey?.let {
            val activityTrackerKey = executeTrackerQuery(
                getRequestBodyForDataQuery(File(activityTrackerPath), ACTIVITY_TRACKER_FILE_FIELD),
                "activity-tracker-item"
            )
            activityTrackerKey?.let {
                executeTrackerQuery(
                    getRequestBodyForStudentQuery(codeTrackerKey, activityTrackerKey),
                    "student/$studentId",
                    "PUT"
                )
            }

        }
    }
}
