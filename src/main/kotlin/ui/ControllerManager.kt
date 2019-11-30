package ui

import Task
import data.UiData
import javafx.scene.control.Toggle
import kotlin.properties.Delegates

object ControllerManager {
    private val writeTask = Task("-1", "Написать вручную")
    private val controllers : MutableList<Controller> = arrayListOf()

    var activeTaskPane: String by Delegates.observable("taskChooserPane") { _, old, new ->
        controllers.forEach { it.setActive(new) }
    }

    val uiData = UiData(listOf(writeTask) + Plugin.server.getTasks())

    fun addController(controller: Controller) = controllers.add(controller)

    fun removeController(controller: Controller) = controllers.remove(controller)

    fun notify(event: NotifyEvent, new: Any?) {
        when(event) {
            NotifyEvent.CHOSEN_TASK_NOTIFY -> controllers.forEach { it.taskChoiceBox.selectionModel.select(new as Int) }
            NotifyEvent.WRITTEN_TASK_NOTIFY -> controllers.forEach { it.taskTextField.text = new as String }
            NotifyEvent.AGE_NOTIFY -> controllers.forEach { it.ageSlider.value = (new as Double) }
            NotifyEvent.PROGRAM_EXPERIENCE_NOTIFY ->  controllers.forEach { it.selectExperienceButton(new as String) }
            NotifyEvent.TASK_STATUS_NOTIFY -> controllers.forEach { it.selectTaskStatusButton(new as String) }
        }
    }

}

enum class NotifyEvent {
    CHOSEN_TASK_NOTIFY,
    WRITTEN_TASK_NOTIFY,
    AGE_NOTIFY,
    PROGRAM_EXPERIENCE_NOTIFY,
    TASK_STATUS_NOTIFY
}
