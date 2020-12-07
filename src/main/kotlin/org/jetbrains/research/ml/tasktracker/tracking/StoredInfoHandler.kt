package org.jetbrains.research.ml.tasktracker.tracking

import com.intellij.openapi.diagnostic.Logger
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonConfiguration
import org.jetbrains.research.ml.tasktracker.Plugin.taskTrackerFolderPath
import org.jetbrains.research.ml.tasktracker.models.Keyed
import org.jetbrains.research.ml.tasktracker.models.StoredInfo
import java.io.File
import java.io.PrintWriter


object StoredInfoHandler{

    val logger: Logger = Logger.getInstance(javaClass)

    fun getIntStoredField(field: UiLoggedDataHeader, defaultValue: Int): Int {
        return run{
            val storedField = StoredInfoWrapper.info.loggedUIData[field.header]?.toIntOrNull()
            logger.info("Stored field $storedField for the ${field.header} value has been received successfully")
            storedField
        } ?: run {
            logger.info("Default value $defaultValue for the ${field.header} value has been received successfully")
            defaultValue
        }
    }

    fun <T : Keyed> getIndexByStoredKey(field: UiLoggedDataHeader, list: List<T>, defaultValue: Int): Int {
        StoredInfoWrapper.info.loggedUIData[field.header]?.let { storedKey ->
            val storedKeyIndex = list.indexOfFirst { it.key == storedKey }
            logger.info("Stored index $storedKeyIndex for the ${field.header} value has been received successfully")
            return storedKeyIndex
        }
        logger.info("Default value $defaultValue for the ${field.header} value has been received successfully")
        return defaultValue
    }
}

/*
    This class provides storing survey info and activity tracker key
 */
object StoredInfoWrapper {

    private const val storedInfoFileName = "storedInfo.txt"
    private val storedInfoFilePath = "${taskTrackerFolderPath}/$storedInfoFileName"
    private val json by lazy {
        Json(JsonConfiguration.Stable)
    }
    private val serializer = StoredInfo.serializer()

    var info: StoredInfo = readStoredInfo()

    private fun readStoredInfo(): StoredInfo {
        val file = File(storedInfoFilePath)
        if (!file.exists()) {
            return StoredInfo()
        }
        return json.parse(serializer, file.readText())
    }

    fun updateStoredInfo(surveyInfo: Map<String, String>? = null,
                         userId: String? = null) {
        surveyInfo?.let{ info.loggedUIData = it }
        userId?.let{ info.userId = it }
        writeStoredInfo()
    }
    
    private fun writeStoredInfo() {
        val file = File(storedInfoFilePath)
        val writer = PrintWriter(file)
        writer.print(json.stringify(serializer, info))
        writer.close()
    }
}