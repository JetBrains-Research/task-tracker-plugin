package org.jetbrains.research.ml.tasktracker.tracking

import com.intellij.openapi.editor.Document
import com.intellij.openapi.fileEditor.FileDocumentManager
import org.jetbrains.research.ml.tasktracker.Plugin
import org.jetbrains.research.ml.tasktracker.server.TrackerQueryExecutor
import org.jetbrains.research.ml.tasktracker.ui.panes.SurveyUiData
import org.jetbrains.research.ml.tasktracker.ui.panes.TaskChoosingUiData
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

enum class UiLoggedDataHeader(val header: String) {
    AGE("age"),
    GENDER("gender"),
    PROGRAM_EXPERIENCE_YEARS("programExperienceYears"),
    PROGRAM_EXPERIENCE_MONTHS("programExperienceMonths"),
    COUNTRY("country"),
    CHOSEN_TASK("chosenTask"),
    PROGRAMMING_LANGUAGE("programmingLanguage")
}

object UiLoggedData : LoggedData<Unit, String>() {
    override val loggedDataGetters: List<LoggedDataGetter<Unit, String>> = arrayListOf(
        LoggedDataGetter(UiLoggedDataHeader.AGE.header) { SurveyUiData.age.uiValue.toString() },
        LoggedDataGetter(UiLoggedDataHeader.GENDER.header) { SurveyUiData.gender.toString() },
        LoggedDataGetter(UiLoggedDataHeader.PROGRAM_EXPERIENCE_YEARS.header) { SurveyUiData.peYears.uiValue.toString() },
        LoggedDataGetter(UiLoggedDataHeader.PROGRAM_EXPERIENCE_MONTHS.header) { SurveyUiData.peMonths.uiValue.toString() },
        LoggedDataGetter(UiLoggedDataHeader.COUNTRY.header) {SurveyUiData.country.toString() },
        LoggedDataGetter(UiLoggedDataHeader.CHOSEN_TASK.header) { TaskChoosingUiData.chosenTask.toString() },
        LoggedDataGetter(UiLoggedDataHeader.PROGRAMMING_LANGUAGE.header) { SurveyUiData.programmingLanguage.toString() }
    )
}

object DocumentLoggedData : LoggedData<Document, String?>() {
    override val loggedDataGetters: List<LoggedDataGetter<Document, String?>> = arrayListOf(
        LoggedDataGetter("date") { DateTime.now().toString() },
        LoggedDataGetter("timestamp") { it.modificationStamp.toString() },
        LoggedDataGetter("fileName") { FileDocumentManager.getInstance().getFile(it)?.name },
        LoggedDataGetter("fileHashCode") { FileDocumentManager.getInstance().getFile(it)?.hashCode().toString() },
        LoggedDataGetter("documentHashCode") { it.hashCode().toString() },
        LoggedDataGetter("fragment") { it.text },
        LoggedDataGetter("userId") { TrackerQueryExecutor.userId },
        LoggedDataGetter("testMode") { Plugin.testMode.toString() }
    )
}