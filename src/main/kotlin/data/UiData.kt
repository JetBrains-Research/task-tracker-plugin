package data

import ui.ControllerManager
import kotlin.properties.Delegates

class UiData(val tasks: List<String>) {
    private val controllerManager = ControllerManager

    var chosenTask: String by Delegates.observable(tasks[0]) { _, _, new ->
        val newSelectedIndex = tasks.indexOf(new)
        if (newSelectedIndex != -1) {
            controllerManager.chosenTaskNotify(newSelectedIndex)
        }
    }

    var writtenTask: String by Delegates.observable("") { _, _, new ->
        controllerManager.writtenTaskNotify(new)
    }

    var age: Int by Delegates.observable(0) { _, _, new ->
        controllerManager.ageNotify(new)

    }

    var programExperience: ProgramExperience by Delegates.observable(ProgramExperience.LESS_THAN_HALF_YEAR) { _, _, new ->
        controllerManager.programExperienceNotify(new.number)
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