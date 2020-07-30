package org.jetbrains.research.ml.codetracker.tracking

import com.intellij.openapi.application.PathManager
import com.intellij.openapi.diagnostic.Logger
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonConfiguration
import org.jetbrains.research.ml.codetracker.models.StoredInfo
import java.io.File
import java.io.PrintWriter


object StoredInfoHandler{

    val logger: Logger = Logger.getInstance(javaClass)

    fun readIntStoredField(field: UiLoggedDataHeader, defaultValue: Int): Int {
        return run{
            val storedField = StoredInfoWrapper.info.loggedUIData[field.header]?.toIntOrNull()
            logger.info("Stored field $storedField for the ${field.header} value has been received successfully")
            storedField
        } ?: run {
            logger.info("Default value $defaultValue for the ${field.header} value has been received successfully")
            defaultValue
        }
    }

    fun <T> readIndexStoredItem(field: UiLoggedDataHeader, collection: List<T>,
                                comparingFunction: (T, String) -> Boolean, defaultValue: Int): Int {
        StoredInfoWrapper.info.loggedUIData[field.header]?.let { storedItem ->
            val storedItemIndex = collection.indexOfFirst { comparingFunction(it, storedItem) }
            logger.info("Stored index $storedItemIndex for the ${field.header} value has been received successfully")
            return storedItemIndex
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
    private val codeTrackerPath = "${PathManager.getPluginsPath()}/codetracker/" + storedInfoFileName
    private val json by lazy {
        Json(JsonConfiguration.Stable)
    }
    private val serializer = StoredInfo.serializer()

    var info: StoredInfo = readStoredInfo()

    private fun readStoredInfo(): StoredInfo {
        val file = File(codeTrackerPath)
        if (!file.exists()) {
            return StoredInfo()
        }
        return json.parse(serializer, file.readText())
    }

    fun updateStoredInfo(surveyInfo: Map<String, String>? = null,
                         activityTrackerKey: String? = null) {
        surveyInfo?.let{ info.loggedUIData = it }
        activityTrackerKey?.let{ info.activityTrackerKey = it }
        writeStoredInfo()
    }
    
    private fun writeStoredInfo() {
        val file = File(codeTrackerPath)
        val writer = PrintWriter(file)
        writer.print(json.stringify(serializer, info))
        writer.close()
    }
}