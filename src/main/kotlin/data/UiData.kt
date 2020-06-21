package data

import models.Task
import ui.ControllerManager
import ui.NotifyEvent
import kotlin.properties.Delegates

class UiData(val tasks: List<Task>) {
    val chosenTask = object : UiField<Int>(NotifyEvent.CHOSEN_TASK_NOTIFY, 0, "chosenTask") {
        override val logValue: String
            get() = tasks[uiValue].key
    }

    val writtenTask = UiField(NotifyEvent.WRITTEN_TASK_NOTIFY, "", "writtenTask")

    val age = UiField(NotifyEvent.AGE_NOTIFY, 0, "age")

    val programExperience = UiField(NotifyEvent.PROGRAM_EXPERIENCE_NOTIFY, PE.NULL, "programExperience")

    val taskStatus = UiField(NotifyEvent.TASK_STATUS_NOTIFY, TaskStatus.NULL, "taskStatus")

    val activePane = UiField(NotifyEvent.ACTIVE_PANE_NOTIFY, "infoFormPane", "activePane")


    open class UiField<T : Any?>(val notifyEvent: NotifyEvent, val defaultUiValue: T, val header: String) {

        var uiValue: T by Delegates.observable(defaultUiValue) { _, old, new ->
            if (old != new) {
                ControllerManager.notify(notifyEvent, new)
            }
        }

        // to be able to set specific ways of logging like logging a task id in case of chosenTask
        open val logValue: String
            get() = uiValue.toString()

        fun setDefault() {
            uiValue = defaultUiValue
        }

        fun isDefault(): Boolean {
            return uiValue == defaultUiValue
        }

        fun isDefault(new: T): Boolean {
            return new == defaultUiValue
        }
    }

    // Todo: get real list by language
    fun tasksNames(): List<String> = listOf("pies")

    fun getData() = listOf(
        chosenTask,
        writtenTask,
        age,
        programExperience,
        taskStatus,
        activePane
    )
}

enum class PE {
    NULL,
    LESS_THAN_HALF_YEAR,
    FROM_HALF_TO_ONE_YEAR,
    FROM_ONE_TO_TWO_YEARS,
    FROM_TWO_TO_FOUR_YEARS,
    FROM_FOUR_TO_SIX_YEARS,
    MORE_THAN_SIX
}

enum class TaskStatus {
    NULL,
    NOT_SOLVED,
    SOLVED
}
