package data

import javafx.scene.control.RadioButton
import javafx.scene.control.Toggle
import ui.ControllerManager
import ui.NotifyEvent
import kotlin.properties.Delegates
import Task

class UiData(val tasks: List<Task>) {
    val chosenTask =  object : UiField<Int>(NotifyEvent.CHOSEN_TASK_NOTIFY, 0, "chosenTask") {
        override val logValue: String
            get() = tasks[uiValue].key
    }

    val writtenTask = UiField(NotifyEvent.WRITTEN_TASK_NOTIFY, "", "writtenTask")

    val age = UiField(NotifyEvent.AGE_NOTIFY, 0.0, "age")

    val programExperience = UiField(NotifyEvent.PROGRAM_EXPERIENCE_NOTIFY, PE.NULL, "programExperience")

    val taskStatus = UiField(NotifyEvent.TASK_STATUS_NOTIFY, TaskStatus.NULL, "taskStatus")

    open class UiField<T : Any?> (val notifyEvent: NotifyEvent, val defaultUiValue: T, val header: String) {
        private val controllerManager = ControllerManager
        var uiValue: T by Delegates.observable(defaultUiValue) { _, _, new ->
            controllerManager.notify(notifyEvent, new)
        }
        // to be able to set specific ways of logging like logging a task id in case of chosenTask
        open val logValue: String
            get() = uiValue.toString()

        fun setDefault() {
            uiValue = defaultUiValue
        }

        fun isDefault() : Boolean {
            return uiValue == defaultUiValue
        }

        fun isDefault(new: T) : Boolean {
            return new == defaultUiValue
        }
    }

    fun getData(notifyEvent: NotifyEvent) : List<UiField<out Any>> {
        return getData().filter { it.notifyEvent == notifyEvent }
    }

    fun getData() = listOf(
        chosenTask,
        writtenTask,
        age,
        programExperience,
        taskStatus
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