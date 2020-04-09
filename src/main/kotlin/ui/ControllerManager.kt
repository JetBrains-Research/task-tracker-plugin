package ui

import Task
import com.intellij.openapi.diagnostic.Logger
import data.PE
import data.UiData
import javafx.collections.FXCollections

internal object ControllerManager {
    private val diagnosticLogger: Logger = Logger.getInstance(javaClass)

    private val selectTask = Task("NULL", "--Не выбрано--")
    private val controllers : MutableList<Controller> = arrayListOf()
    private var lastId: Int = 0


    //controllers.forEach { it.setActive(new) }


    // todo: check for unique keys
    val uiData = UiData(listOf(selectTask) + Plugin.server.getTasks())

    fun addController(controller: Controller) {
        controllers.add(controller)
        controller.id = lastId++

        diagnosticLogger.info("${Plugin.PLUGIN_ID}: add new controller${controller.id} for project \"${controller.project.name}\", total sum is ${controllers.size}")

        controller.taskComboBox.items = FXCollections.observableList(uiData.tasksNames())
        uiData.getData().forEach { notify(it.notifyEvent, it.uiValue, arrayListOf(controller)) }
    }

    fun removeController(controller: Controller) {
        controllers.remove(controller)

        diagnosticLogger.info("${Plugin.PLUGIN_ID}: remove controller${controller.id} for project \"${controller.project.name}\", total sum is ${controllers.size}")
    }

    fun notify(event: NotifyEvent, new: Any?, controllers: MutableList<Controller> = this.controllers) {
        when(event) {
            NotifyEvent.CHOSEN_TASK_NOTIFY -> controllers.forEach {
                it.taskComboBox.selectionModel.select(new as Int)
                val task = uiData.tasks[new]

                it.setStartSolvingButtonDisability(uiData.chosenTask.isDefault())
                it.setTaskInfo(task)
            }

            NotifyEvent.WRITTEN_TASK_NOTIFY -> controllers.forEach {
//                Do nothing since written task was removed
            }

            NotifyEvent.AGE_NOTIFY -> controllers.forEach {
                it.setInfoFormButtonsDisability(uiData.age.isDefault(new as Int) || uiData.programExperience.isDefault())

                if (uiData.age.isDefault(new as Int)) {
                    it.ageField.text = ""
                } else {
                    it.ageField.text = new.toString()
                }
            }

            NotifyEvent.PROGRAM_EXPERIENCE_NOTIFY ->  controllers.forEach {
                it.selectExperienceButton(new as PE)
                it.setInfoFormButtonsDisability(uiData.age.isDefault() || uiData.programExperience.isDefault(new))
            }

            NotifyEvent.TASK_STATUS_NOTIFY -> controllers.forEach {
//                Do nothing since task status buttons were removed
            }

            NotifyEvent.ACTIVE_PANE_NOTIFY -> controllers.forEach {
                it.setActive(new as String)
            }
        }
    }
}

enum class NotifyEvent {
    CHOSEN_TASK_NOTIFY,
    WRITTEN_TASK_NOTIFY,
    AGE_NOTIFY,
    PROGRAM_EXPERIENCE_NOTIFY,
    TASK_STATUS_NOTIFY,
    ACTIVE_PANE_NOTIFY
}
