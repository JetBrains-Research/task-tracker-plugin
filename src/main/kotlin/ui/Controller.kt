package ui

import data.UiData
import javafx.collections.FXCollections
import javafx.event.EventHandler
import javafx.fxml.FXML
import javafx.scene.control.*
import javafx.scene.input.MouseEvent
import java.util.logging.Logger

class Controller {
    private val log: Logger = Logger.getLogger(javaClass.name)

    private val controllerManager = ControllerManager
    private val uiData: UiData = ControllerManager.uiData

    @FXML
    lateinit var taskChoiceBox: ChoiceBox<String>

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
    lateinit var experienceButtonByText: Map<String, RadioButton?>

    @FXML
    lateinit var clearInfoForm: Button


    fun initialize() {
        log.info("init controller")
        controllerManager.addController(this)

        initTaskChoiceBox()
        initTaskTextField()
        initAgeSlider()
        initProgramExperienceGroup()

        initClearInfoForm()
    }


    private fun initTaskChoiceBox() {
        taskChoiceBox.items = FXCollections.observableList(uiData.tasks)
        taskChoiceBox.selectionModel.select(uiData.chosenTask.uiValue)
        taskChoiceBox.selectionModel.selectedItemProperty().addListener { _, old, new ->
            log.info("choicebox changed from $old to $new")
            uiData.chosenTask.uiValue = taskChoiceBox.selectionModel.selectedIndex
        }
    }

    private fun initTaskTextField() {
        taskTextField.text = uiData.writtenTask.uiValue
        taskTextField.textProperty().addListener { _, old, new ->
            log.info("textfield changed from $old to $new")
            uiData.writtenTask.uiValue = new
        }
    }

    private fun initAgeSlider() {
        ageSlider.value = uiData.age.uiValue
        ageSlider.valueProperty().addListener { _, old, new ->
            log.info("slider changed from $old to $new")
            uiData.age.uiValue = new.toDouble()
        }
    }

    private fun initProgramExperienceGroup() {
        experienceButtonByText = hashMapOf(
            "null" to null,
            peLessThanHalf.text to peLessThanHalf,
            peFromHalfToOne.text to peFromHalfToOne,
            peFromOneToTwo.text to peFromOneToTwo,
            peFromTwoToFour.text to peFromTwoToFour,
            peFromFourToSix.text to peFromFourToSix,
            peMoreThanSix.text to peMoreThanSix
        )

        selectExperienceButton(uiData.programExperience.uiValue)
        programExperienceGroup.selectedToggleProperty().addListener { _, old, new ->
            log.info("program experience changed from $old to $new")
            uiData.programExperience.uiValue = (new as? RadioButton)?.text ?: "null"
        }
    }

    fun selectExperienceButton(text: String) {
        val selectedButton = experienceButtonByText[text]
        programExperienceGroup.selectToggle(selectedButton)
    }

    private fun initClearInfoForm() {
        clearInfoForm.addEventHandler(MouseEvent.MOUSE_CLICKED, EventHandler { event ->
            uiData.age.setDefault()
            uiData.programExperience.setDefault()
        })
    }

}