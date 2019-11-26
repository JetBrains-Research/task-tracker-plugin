package ui

import data.UiData
import Server
import DumbServer

object ControllerManager {
    private val controllers : MutableList<Controller> = arrayListOf()

    val server: Server = DumbServer
    val uiData = UiData(server.getTasks())

    fun addController(controller: Controller) = controllers.add(controller)

    fun removeController(controller: Controller) = controllers.remove(controller)

    fun chosenTaskNotify(newChosenTask: Int) {
        controllers.forEach { it.taskChoiceBox.selectionModel.select(newChosenTask) }
    }

    fun writtenTaskNotify(newWrittenTask: String) {
        controllers.forEach { it.taskTextField.text = newWrittenTask }
    }

    fun ageNotify(newAge: Int) {
        controllers.forEach { it.ageSlider.value = newAge.toDouble() }
    }
}
