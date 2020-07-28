package org.jetbrains.research.ml.codetracker.tracking

import com.intellij.openapi.editor.Document
import com.intellij.openapi.fileEditor.FileDocumentManager
import org.jetbrains.research.ml.codetracker.server.TrackerQueryExecutor
import org.jetbrains.research.ml.codetracker.ui.panes.SurveyUiData
import org.jetbrains.research.ml.codetracker.ui.panes.TaskChoosingUiData
import org.joda.time.DateTime

data class LoggedDataGetter<T, S>(val header: String, val getData: (T) -> S)

abstract class LoggedData<T, S> {
    protected abstract val loggedDataGetters: List<LoggedDataGetter<T, S>>

    val headers: List<String>
        get() = loggedDataGetters.map { it.header }

    fun getData(t: T): List<S> {
        return loggedDataGetters.map { it.getData(t) }
    }
}

object UiLoggedData : LoggedData<Unit, String>() {

    override val loggedDataGetters: List<LoggedDataGetter<Unit, String>> = arrayListOf(
        LoggedDataGetter("age") { SurveyUiData.age.uiValue.toString() },
        LoggedDataGetter("gender") {
            // Todo: make it better: delete duplicates of code
            val currentValue = SurveyUiData.gender.uiValue
            if (!isDefaultValue(currentValue)) {
                SurveyUiData.gender.dataList[currentValue].key
            } else {
                currentValue.toString()
            }
        },
        LoggedDataGetter("programExperienceYears") { SurveyUiData.peYears.uiValue.toString() },
        LoggedDataGetter("programExperienceMonths") { SurveyUiData.peMonths.uiValue.toString() },
        LoggedDataGetter("country") {
            val currentValue = SurveyUiData.country.uiValue
            if (!isDefaultValue(currentValue)) {
                SurveyUiData.country.dataList[currentValue].key
            } else {
                currentValue.toString()
            }
        },
        LoggedDataGetter("chosenTask") {
            val currentValue = TaskChoosingUiData.chosenTask.uiValue
            if (!isDefaultValue(currentValue)) {
                TaskChoosingUiData.chosenTask.dataList[currentValue].key
            } else {
                currentValue.toString()
            }
        }
    )

    private fun isDefaultValue(value: Int, defaultValue: Int = -1): Boolean {
        return value == defaultValue
    }
}

object DocumentLoggedData : LoggedData<Document, String?>() {
    override val loggedDataGetters: List<LoggedDataGetter<Document, String?>> = arrayListOf(
        LoggedDataGetter("date") { DateTime.now().toString() },
        LoggedDataGetter("timestamp") { it.modificationStamp.toString() },
        LoggedDataGetter("fileName") { FileDocumentManager.getInstance().getFile(it)?.name },
        LoggedDataGetter("fileHashCode") { FileDocumentManager.getInstance().getFile(it)?.hashCode().toString() },
        LoggedDataGetter("documentHashCode") { it.hashCode().toString() },
        LoggedDataGetter("fragment") { it.text },
        LoggedDataGetter("activityTrackerKey") { TrackerQueryExecutor.activityTrackerKey }
    )
}