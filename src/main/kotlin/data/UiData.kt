package data

import ui.ControllerManager
import ui.NotifyEvent
import kotlin.properties.Delegates

class UiData(val tasks: List<String>) {
    private val controllerManager = ControllerManager

    var chosenTask: String by Delegates.observable(tasks[0]) { _, _, new ->
        val newSelectedIndex = tasks.indexOf(new)
        if (newSelectedIndex != -1) {
            controllerManager.notify(NotifyEvent.CHOSEN_TASK_NOTIFY, newSelectedIndex)
        }
    }

    var writtenTask: String by Delegates.observable("") { _, _, new ->
        controllerManager.notify(NotifyEvent.WRITTEN_TASK_NOTIFY, new)
    }

    var age: Int by Delegates.observable(0) { _, _, new ->
        controllerManager.notify(NotifyEvent.AGE_NOTIFY, new)

    }

    var programExperience: ProgramExperience by Delegates.observable(ProgramExperience.LESS_THAN_HALF_YEAR) { _, _, new ->
        controllerManager.notify(NotifyEvent.PROGRAM_EXPERIENCE_NOTIFY, new.number)
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
    ).map { it.toString() }
}

enum class ProgramExperience(val number: Int) {
    LESS_THAN_HALF_YEAR(0),
    FROM_HALF_TO_ONE_YEAR(1),
    FROM_ONE_TO_TWO_YEARS(2),
    FROM_TWO_TO_FOUR_YEARS(3),
    FROM_FOUR_TO_SIX_YEARS(4),
    MORE_THAN_SIX_YEARS(5)
}