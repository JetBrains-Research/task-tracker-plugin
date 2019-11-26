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

    // todo handle it
    var programExperience: ProgramExperience = ProgramExperience.LESS_THAN_HALF_YEAR
}

enum class ProgramExperience(val toString: String) {
    LESS_THAN_HALF_YEAR("less than 0.5 year"),
    FROM_HALF_TO_ONE_YEAR("from 0.5 to 1 year"),
    FROM_ONE_TO_TWO_YEARS("from 1 to 2 years"),
    FROM_TWO_TO_FOUR_YEARS("from 2 to 4 years"),
    FROM_FOUR_TO_SIX_YEARS("from 4 to 6 years"),
    MORE_THAN_SIX_YEARS("more than 6 years")
}