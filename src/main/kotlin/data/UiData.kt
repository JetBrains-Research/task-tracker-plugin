package data

import javafx.scene.control.RadioButton
import javafx.scene.control.Toggle
import ui.ControllerManager
import ui.NotifyEvent
import kotlin.properties.Delegates

class UiData(val tasks: List<String>) {
    val chosenTask =  object : UiField<Int>(NotifyEvent.CHOSEN_TASK_NOTIFY, 0) {
        override val logValue: String
            get() = tasks[uiValue]
    }

    val writtenTask = UiField(NotifyEvent.WRITTEN_TASK_NOTIFY, "")

    val age = UiField(NotifyEvent.AGE_NOTIFY, 0.0)

    val programExperience = UiField<String>(NotifyEvent.PROGRAM_EXPERIENCE_NOTIFY, "null")

    val taskStatus = UiField(NotifyEvent.TASK_STATUS_NOTIFY, "null")

    open class UiField<T : Any?> (val notifyEvent: NotifyEvent, val defaultUiValue: T) {
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

    companion object {
        val headers = listOf(
            "chosenTask",
            "writtenTask",
            "age",
            "programExperience"
        )
    }

    fun getData() = listOf(
        chosenTask,
        writtenTask,
        age,
        programExperience
    )
}