package ui

import data.ProgramExperience
import data.UiData
import javafx.collections.FXCollections
import javafx.collections.ObservableList
import javafx.fxml.FXML
import javafx.scene.control.*
import java.util.logging.Logger
import Task

class Controller {
    private val log: Logger = Logger.getLogger(javaClass.name)

    private val controllerManager = ControllerManager
    private val uiData: UiData = ControllerManager.uiData

    @FXML
    lateinit var taskChoiceBox: ChoiceBox<Task>

    @FXML
    lateinit var taskTextField: TextField

    @FXML
    lateinit var ageSlider: Slider

    @FXML
    lateinit var programExperienceGroup: ToggleGroup

    @FXML
    lateinit var peLessThanHalf: RadioButton
    @FXML
    lateinit var peFromHalfToOne: RadioButton
    @FXML
    lateinit var peFromOneToTwo: RadioButton
    @FXML
    lateinit var peFromTwoToFour: RadioButton
    @FXML
    lateinit var peFromFourToSix: RadioButton
    @FXML
    lateinit var peMoreThanSix: RadioButton

    @FXML
    lateinit var programExperienceButtons: List<RadioButton>

    @FXML
    lateinit var clearTask: Button


    fun initialize() {
        log.info("init controller")
        controllerManager.addController(this)

        initTaskChoiceBox()
        initTaskTextField()
        initAgeSlider()
        initProgramExperienceGroup()
    }

    private fun initTaskTextField() {
        taskTextField.text = uiData.writtenTask
        taskTextField.textProperty().addListener { _, old, new ->
            log.info("textfield changed from $old to $new")
            uiData.writtenTask = new
        }
    }

    private fun initTaskChoiceBox() {
        taskChoiceBox.items = FXCollections.observableList(uiData.tasks)
        taskChoiceBox.selectionModel.select(uiData.chosenTask)
        taskChoiceBox.selectionModel.selectedItemProperty().addListener { _, old, new ->
            log.info("choicebox changed from $old to $new")
            uiData.chosenTask = new
        }
    }

    private fun initAgeSlider() {
        ageSlider.value = uiData.age.toDouble()
        ageSlider.valueProperty().addListener { _, old, new ->
            log.info("slider changed from $old to $new")
            uiData.age = new.toInt()
        }
    }

    private fun initProgramExperienceGroup() {
        programExperienceButtons = listOf(
            peLessThanHalf,
            peFromHalfToOne,
            peFromOneToTwo,
            peFromTwoToFour,
            peFromFourToSix,
            peMoreThanSix
        )
        programExperienceButtons[uiData.programExperience.number].isSelected = true
        programExperienceGroup.selectedToggleProperty().addListener { _, old, new ->
            log.info("program experience changed from $old to $new")
            uiData.programExperience = ProgramExperience.values()[programExperienceButtons.indexOf(new)]
        }

    }

}