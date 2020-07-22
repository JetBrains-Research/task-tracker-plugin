package org.jetbrains.research.ml.codetracker.tracking

import com.intellij.openapi.editor.Document
import com.intellij.openapi.fileEditor.FileDocumentManager
import org.jetbrains.research.ml.codetracker.ui.panes.SurveyUiData
import org.jetbrains.research.ml.codetracker.ui.panes.TaskChoosingUiData
import org.joda.time.DateTime

data class LoggedDataGetter<T, S>(val header: String, val getData: (T) -> S)

abstract class LoggedData<T, S> {
    protected abstract val loggedDataGetters: List<LoggedDataGetter<T, S>>

    val headers: List<String>
        get() = loggedDataGetters.map { it.header }

    abstract fun getData(t: T): List<S>
}

object UiLoggedData : LoggedData<Unit, String>() {
    override val loggedDataGetters: List<LoggedDataGetter<Unit, String>> = arrayListOf(
        LoggedDataGetter("age") { SurveyUiData.age.uiValue.toString() },
        LoggedDataGetter("gender") { SurveyUiData.gender.uiValue.toString() },
        LoggedDataGetter("programExperienceYears") { SurveyUiData.peYears.toString() },
        LoggedDataGetter("programExperienceMonths") { SurveyUiData.peMonths.toString() },
        LoggedDataGetter("country") { SurveyUiData.country.uiValue.toString() },
        LoggedDataGetter("chosenTask") { TaskChoosingUiData.chosenTask.uiValue.toString() }
    )

    override fun getData(t: Unit): List<String> {
        return loggedDataGetters.map { it.getData(t) }
    }
}

object DocumentLoggedData : LoggedData<Document, String?>() {
    override val loggedDataGetters: List<LoggedDataGetter<Document, String?>> = arrayListOf(
        LoggedDataGetter("date") { DateTime.now().toString() },
        LoggedDataGetter("timestamp") { it.modificationStamp.toString() },
        LoggedDataGetter("fileName") { FileDocumentManager.getInstance().getFile(it)?.name },
        LoggedDataGetter("fileHashCode") { FileDocumentManager.getInstance().getFile(it)?.hashCode().toString() },
        LoggedDataGetter("documentHashCode") { it.hashCode().toString() },
        LoggedDataGetter("fragment") { it.text }
    )

    override fun getData(t: Document): List<String?> {
        return loggedDataGetters.map { it.getData(t) }
    }
}