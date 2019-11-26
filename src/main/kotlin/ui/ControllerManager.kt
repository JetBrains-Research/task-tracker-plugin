package ui

import data.UiData

object ControllerManager {
    private val controllers : MutableList<Controller> = arrayListOf()
    val uiData = UiData(listOf("a", "b"))

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