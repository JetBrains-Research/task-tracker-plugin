package org.jetbrains.research.ml.tasktracker.server

import com.intellij.openapi.application.PathManager
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.Request
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import org.jetbrains.research.ml.tasktracker.Plugin
import org.jetbrains.research.ml.tasktracker.models.Extension
import org.jetbrains.research.ml.tasktracker.tracking.ActivityTrackerFileHandler
import org.jetbrains.research.ml.tasktracker.tracking.StoredInfoWrapper
import java.io.File
import java.io.PrintWriter
import java.net.URL


object TrackerQueryExecutor : QueryExecutor() {

    private val ACTIVITY_TRACKER_FILE = "ide-events${Extension.CSV.ext}"
    private const val TASK_TRACKER_FILE_FIELD = "tasktracker"
    private const val ACTIVITY_TRACKER_FILE_FIELD = "activitytracker"
    private val activityTrackerPath = "${PathManager.getPluginsPath()}/activity-tracker/$ACTIVITY_TRACKER_FILE"
    private const val DEFAULT_ACTIVITY_TRACKER_ID = "-1"

    var userId: String? = null

    init {
        StoredInfoWrapper.info.userId?.let { userId = it } ?: run {
            initUserId()
            StoredInfoWrapper.updateStoredInfo(userId = userId)
        }
    }

    private fun initUserId() {
        val currentUrl = URL("${baseUrl}user")
        logger.info("${Plugin.PLUGIN_NAME}: ...generating user id")
        val requestBody = ByteArray(0).toRequestBody(null, 0, 0)
        val request = Request.Builder().url(currentUrl).post(requestBody).build()
        userId = executeQuery(request)?.let { it.body?.string() }
    }

    private fun getRequestForUserQuery(
        urlSuffix: String,
        taskTrackerKey: String,
        activityTrackerKey: String
    ): Request {
        val json = "{\"diId\":$taskTrackerKey,\"atiId\":\"$activityTrackerKey\"}"
        val body = json.toRequestBody("application/json".toMediaTypeOrNull())
        return Request.Builder().url(baseUrl + urlSuffix).method("PUT", body).build()
    }

    private fun getRequestForSendingDataQuery(
        urlSuffix: String,
        file: File,
        fileFieldName: String,
        activityTrackerKey: String? = null
    ): Request {
        if (file.exists()) {
            logger.info("${Plugin.PLUGIN_NAME}: ...sending file ${file.name}")
            val requestBody = MultipartBody.Builder().setType(MultipartBody.FORM)
            requestBody.addFormDataPart(fileFieldName, file.name, file.asRequestBody("text/csv".toMediaType()))
            activityTrackerKey?.let {
                requestBody.addFormDataPart("activityTrackerKey", it)
            }
            return Request.Builder().url(baseUrl + urlSuffix).method("POST", requestBody.build()).build()
        } else {
            throw IllegalStateException("File ${file.name} for $fileFieldName doesn't exist")
        }
    }

    private fun executeTrackerQuery(request: Request): String? {
        val response = executeQuery(request)
        if (response.isSuccessful()) {
            return response?.let { it.body?.string() }
        }
        throw IllegalStateException("Unsuccessful server response")
    }

    private fun sendTaskTrackerData(files: List<File>, activityTrackerKey: String): List<String?> {
        return files.map {
            val request = getRequestForSendingDataQuery("data-item", it, TASK_TRACKER_FILE_FIELD, activityTrackerKey)
            executeTrackerQuery(request)
        }
    }

    private fun sendActivityTrackerData(): String? {
        return try {
            ActivityTrackerFileHandler.filterActivityTrackerData(activityTrackerPath)?.let {
                executeTrackerQuery(
                    getRequestForSendingDataQuery(
                        "activity-tracker-item",
                        File(it),
                        ACTIVITY_TRACKER_FILE_FIELD
                    )
                )
            }
        } catch (e: IllegalStateException) {
            // We catch it because we want to update the user anyway
            DEFAULT_ACTIVITY_TRACKER_ID
        }
    }

    private fun updateUserData(taskTrackerKey: String, activityTrackerKey: String) {
        executeTrackerQuery(
            getRequestForUserQuery(
                "user/$userId",
                taskTrackerKey, activityTrackerKey
            )
        )
    }

    private fun clearActivityTrackerFile() {
        val file = File(activityTrackerPath)
        val writer = PrintWriter(file)
        writer.print("")
        writer.close()
    }


    fun sendData(taskTrackerFiles: List<File>) {
        sendActivityTrackerData()?.let { activityTrackerKey ->
            sendTaskTrackerData(taskTrackerFiles, activityTrackerKey).forEach {
                // Todo: should we send all successful responses??
                it?.let{
                    updateUserData(it, activityTrackerKey)
                } ?: error("Unsuccessful server response")
            }
            clearActivityTrackerFile()
        }
    }
}
