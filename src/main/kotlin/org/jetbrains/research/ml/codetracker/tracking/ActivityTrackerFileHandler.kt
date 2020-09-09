package org.jetbrains.research.ml.codetracker.tracking

import com.intellij.openapi.diagnostic.Logger
import krangl.*
import org.apache.commons.csv.CSVFormat
import org.jetbrains.research.ml.codetracker.Plugin.PLUGIN_NAME
import org.jetbrains.research.ml.codetracker.models.Language
import org.jetbrains.research.ml.codetracker.server.PluginServer
import java.io.File
import java.io.FileNotFoundException

enum class ActivityTrackerColumn {
    TIMESTAMP, USERNAME, EVENT_TYPE, EVENT_DATA, PROJECT_NAME, FOCUSED_COMPONENT,
    CURRENT_FILE, PSI_PATH, EDITOR_LINE, EDITOR_COLUMN
}

object ActivityTrackerFileHandler {

    private const val ACTIVITY_TRACKER_FILE_NAME = "ide-events"
    private const val DEFAULT_PATH_SYMBOL = "*"
    private val logger: Logger = Logger.getInstance(javaClass)

    // TODO: get the current language instead of the argument??
    fun filterActivityTrackerData(filePath: String, language: Language = Language.PYTHON): String? {
        return try {
            val df = DataFrame.readCSV(
                filePath,
                format = CSVFormat.DEFAULT.withHeader(ActivityTrackerColumn::class.java)
            )
            val filteredDf = filterDataFrame(df, language)
            val resultPath = filePath.replace(ACTIVITY_TRACKER_FILE_NAME, "${ACTIVITY_TRACKER_FILE_NAME}_filtered")
            filteredDf.writeCSV(File(resultPath), format = CSVFormat.DEFAULT.withIgnoreHeaderCase(true))
            resultPath
        } catch (e: FileNotFoundException) {
            logger.info("${PLUGIN_NAME}: The activity tracker file $filePath does not exist")
            null
        }
    }

    private fun filterDataFrame(df: DataFrame, language: Language): DataFrame {
        // Remove columns, which can contain private information
        val anonymousDf = df.remove(
            ActivityTrackerColumn.USERNAME.name, ActivityTrackerColumn.PROJECT_NAME.name,
            ActivityTrackerColumn.PSI_PATH.name
        )
        return clearFilesPaths(anonymousDf, language)
    }

    // Return the default symbol, if the file is not from the plugin files and only filename from the path otherwise
    private fun replaceAbsoluteFilePath(path: String, pluginFilesRegex: Regex): String {
        // Try to find plugin's tasks files
        val found = pluginFilesRegex.find(path)
        return if (found != null) {
            // Get name from path
            path.split("/").last()
        } else {
            DEFAULT_PATH_SYMBOL
        }
    }

    private fun clearFilesPaths(df: DataFrame, language: Language): DataFrame {
        val tasks = PluginServer.tasks.joinToString(separator = "|") { it.key }
        val tasksMatchCondition = ".*/$PLUGIN_NAME/($tasks)${language.extension.ext}".toRegex(
            RegexOption.IGNORE_CASE
        )
        return df.addColumn(ActivityTrackerColumn.CURRENT_FILE.name) { filePath ->
            filePath[ActivityTrackerColumn.CURRENT_FILE.name].map<String> {
                replaceAbsoluteFilePath(it, tasksMatchCondition)
            }
        }
    }
}