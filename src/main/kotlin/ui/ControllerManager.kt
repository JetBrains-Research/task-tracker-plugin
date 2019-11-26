package ui

import data.UiData

object ControllerManager {
    private val controllers : MutableList<Controller> = arrayListOf()

    val uiData = UiData(Plugin.server.getTasks())

    fun addController(controller: Controller) = controllers.add(controller)

    fun removeController(controller: Controller) = controllers.remove(controller)

    fun notify(event: NotifyEvent, new: Any) {
        when(event) {
            NotifyEvent.CHOSEN_TASK_NOTIFY -> controllers.forEach { it.taskChoiceBox.selectionModel.select(new as Int) }
            NotifyEvent.WRITTEN_TASK_NOTIFY -> controllers.forEach { it.taskTextField.text = new as String }
            NotifyEvent.AGE_NOTIFY -> controllers.forEach { it.ageSlider.value = (new as Int).toDouble() }
            NotifyEvent.PROGRAM_EXPERIENCE_NOTIFY ->  controllers.forEach { it.programExperienceButtons[new as Int].isSelected = true }
        }
    }
}

enum class NotifyEvent {
    CHOSEN_TASK_NOTIFY,
    WRITTEN_TASK_NOTIFY,
    AGE_NOTIFY,
    PROGRAM_EXPERIENCE_NOTIFY
}
