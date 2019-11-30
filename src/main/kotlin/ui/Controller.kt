package ui

import data.UiData
import javafx.collections.FXCollections
import javafx.fxml.FXML
import javafx.scene.control.*
import javafx.scene.input.MouseEvent
import javafx.scene.layout.Pane
import java.util.logging.Logger

class Controller {
    private val log: Logger = Logger.getLogger(javaClass.name)

    private val controllerManager = ControllerManager
    private val uiData: UiData = ControllerManager.uiData

    @FXML
    lateinit var taskPaneByName: HashMap<String, Pane>

    /*
    ############################## task chooser pane ########################################
     */
    @FXML
    lateinit var taskChooserPane: Pane

    @FXML
    lateinit var taskChoiceBox: ChoiceBox<String>

    @FXML
    lateinit var taskTextField: TextField

    @FXML
    lateinit var startSolvingButton: Button

    /*
    ############################## task status pane ########################################
     */
    @FXML
    lateinit var taskStatusPane: Pane

    @FXML
    lateinit var taskStatusGroup: ToggleGroup
    @FXML
    lateinit var taskNotSolved: RadioButton
    @FXML
    lateinit var taskSolved: RadioButton
    @FXML
    lateinit var taskStatusButtonByText: HashMap<String, RadioButton?>

    @FXML
    lateinit var endSolvingButton: Button

    @FXML
    lateinit var continueSolvingButton: Button

    /*
    ############################## task finish pane ########################################
    */
    @FXML
    lateinit var taskFinishPane: Pane

    @FXML
    lateinit var startSolvingAgainButton: Button

    /*
    ############################## info form pane ########################################
     */

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

        taskPaneByName = hashMapOf(
            taskChooserPane.id to taskChooserPane,
            taskStatusPane.id to taskStatusPane,
            taskFinishPane.id to taskFinishPane
        )

        initTaskChooserPane()
        initTaskStatusPane()
        initTaskFinishPane()
        initInfoFormPane()

        setActive(ControllerManager.activeTaskPane)
    }


    fun selectExperienceButton(text: String) {
        val selectedButton = experienceButtonByText[text]
        programExperienceGroup.selectToggle(selectedButton)
    }

    fun selectTaskStatusButton(text: String) {
        val selectedButton = taskStatusButtonByText[text]
        taskStatusGroup.selectToggle(selectedButton)
    }

    fun setActive(name: String) {
        val pane = taskPaneByName[name]
        if (pane != null) {
            taskPaneByName.values.forEach { it.isVisible = false }
            pane.isVisible = true
        }
        taskPaneByName.values.forEach { println(it.isVisible) }
    }

    private fun initTaskChooserPane() {
        initTaskChoiceBox()
        initTaskTextField()
        initStartSolvingButton()
    }

    private fun initTaskStatusPane() {
        initTaskStatusGroup()
        initEndSolvingButton()
        initContinueSolvingButton()
    }

    private fun initTaskFinishPane() {
        initStartSolvingAgainButton()
    }

    private fun initInfoFormPane() {
        initAgeSlider()
        initProgramExperienceGroup()
        initClearInfoForm()
    }

    private fun initStartSolvingButton() {
        startSolvingButton.addEventHandler(MouseEvent.MOUSE_CLICKED) {
            ControllerManager.activeTaskPane = taskStatusPane.id
        }
    }

    private fun initContinueSolvingButton() {
        continueSolvingButton.addEventHandler(MouseEvent.MOUSE_CLICKED) {
            ControllerManager.activeTaskPane = taskChooserPane.id
            setTaskDataDefault()
        }
    }

    private fun initEndSolvingButton() {
        endSolvingButton.addEventHandler(MouseEvent.MOUSE_CLICKED) {
            ControllerManager.activeTaskPane = taskFinishPane.id
        }
    }

    private fun initTaskStatusGroup() {
        taskStatusButtonByText = hashMapOf(
            "null" to null,
            taskNotSolved.text to taskNotSolved,
            taskSolved.text to taskSolved
        )
        selectTaskStatusButton(uiData.taskStatus.uiValue)
        taskStatusGroup.selectedToggleProperty().addListener { _, old, new ->
            log.info("task status changed from $old to $new")
            uiData.taskStatus.uiValue = (new as? RadioButton)?.text ?: "null"
        }
    }

    private fun initStartSolvingAgainButton() {
        startSolvingAgainButton.addEventHandler(MouseEvent.MOUSE_CLICKED) {
            ControllerManager.activeTaskPane = taskChooserPane.id
            setTaskDataDefault()
        }
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

    private fun initClearInfoForm() {
        clearInfoForm.addEventHandler(MouseEvent.MOUSE_CLICKED) {
            uiData.age.setDefault()
            uiData.programExperience.setDefault()
        }
    }

    private fun setTaskDataDefault() {
        uiData.chosenTask.setDefault()
        uiData.writtenTask.setDefault()
        uiData.taskStatus.setDefault()
    }

}