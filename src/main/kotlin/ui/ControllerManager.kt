package ui

import Task
import data.UiData
import javafx.collections.FXCollections
import kotlin.properties.Delegates

object ControllerManager {

    private val writeTask = Task("-1", "Написать вручную")

    private val controllers : MutableList<Controller> = arrayListOf()

    var activePane: String by Delegates.observable("infoFormPane") { _, old, new ->
        controllers.forEach { it.setActive(new) }
    }

    val uiData = UiData(Plugin.server.getTasks() + writeTask)

    fun addController(controller: Controller){
        controllers.add(controller)
        controller.taskChoiceBox.items = FXCollections.observableList(uiData.tasks.map { it.name })
        uiData.getData().forEach { notify(it.notifyEvent, it.uiValue) }
    }

    fun removeController(controller: Controller) = controllers.remove(controller)

    fun notify(event: NotifyEvent, new: Any?) {
        when(event) {
            NotifyEvent.CHOSEN_TASK_NOTIFY -> controllers.forEach {
                it.taskChoiceBox.selectionModel.select(new as Int)
                it.setWrittenTaskVisibility(uiData.tasks[new].name == writeTask.name)
                it.setStartSolvingButtonDisability(uiData.tasks[new].name == writeTask.name && uiData.writtenTask.isDefault() )
            }

            NotifyEvent.WRITTEN_TASK_NOTIFY -> controllers.forEach {
                it.taskTextField.text = new as String
                it.setStartSolvingButtonDisability(uiData.tasks[uiData.chosenTask.uiValue].name == writeTask.name && uiData.writtenTask.isDefault(new) )
            }

            NotifyEvent.AGE_NOTIFY -> controllers.forEach {
                it.ageSlider.value = (new as Double)
                it.ageLabel.text = new.toInt().toString()
                it.setInfoFormButtonsDisability(uiData.age.isDefault(new) || uiData.programExperience.isDefault())
            }

            NotifyEvent.PROGRAM_EXPERIENCE_NOTIFY ->  controllers.forEach {
                it.selectExperienceButton(new as String)
                it.setInfoFormButtonsDisability(uiData.age.isDefault() || uiData.programExperience.isDefault(new))
            }

            NotifyEvent.TASK_STATUS_NOTIFY -> controllers.forEach {
                it.selectTaskStatusButton(new as String)
                it.setStatusButtonsDisability(uiData.taskStatus.isDefault())
            }
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
