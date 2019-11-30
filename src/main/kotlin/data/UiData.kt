package data

import javafx.scene.control.RadioButton
import javafx.scene.control.Toggle
import ui.ControllerManager
import ui.NotifyEvent
import kotlin.properties.Delegates

class UiData(val tasks: List<String>) {
    val chosenTask =  object : UiField<Int>(NotifyEvent.CHOSEN_TASK_NOTIFY, 0, "chosenTask") {
        override val logValue: String
            get() = tasks[uiValue]
    }

    val writtenTask = UiField(NotifyEvent.WRITTEN_TASK_NOTIFY, "", "writtenTask")

    val age = UiField(NotifyEvent.AGE_NOTIFY, 0.0, "age")

    val programExperience = UiField<String>(NotifyEvent.PROGRAM_EXPERIENCE_NOTIFY, "null", "programExperience")

    val taskStatus = UiField(NotifyEvent.TASK_STATUS_NOTIFY, "null", "taskStatus")

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
    }

    fun getData() = listOf(
        chosenTask,
        writtenTask,
        age,
        programExperience,
        taskStatus
    )
}