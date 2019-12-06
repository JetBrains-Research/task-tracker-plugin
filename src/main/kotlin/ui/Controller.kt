package ui

import com.intellij.openapi.diagnostic.Logger
import data.PE
import data.TaskStatus
import data.UiData
import javafx.fxml.FXML
import javafx.scene.control.*
import javafx.scene.input.MouseEvent
import javafx.scene.layout.Pane

class Controller {
    // todo: separate task and info form logic?

    private val diagnosticLogger: Logger = Logger.getInstance(javaClass)

    private val controllerManager = ControllerManager
    private val uiData: UiData = ControllerManager.uiData

    @FXML
    lateinit var paneByName: HashMap<String, Pane>

    /*
    ############################## task chooser pane ########################################
     */
    @FXML
    lateinit var taskChooserPane: Pane

    @FXML
    lateinit var taskChoiceBox: ChoiceBox<String>

    @FXML
    lateinit var taskTextLabel: Label

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
    lateinit var taskNameLabel: Label

    @FXML
    lateinit var taskStatusGroup: ToggleGroup
    @FXML
    lateinit var taskNotSolved: RadioButton
    @FXML
    lateinit var taskSolved: RadioButton
    @FXML
    lateinit var taskStatusButtonByTS: HashMap<TaskStatus, RadioButton?>

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
    lateinit var infoFormPane: Pane

    @FXML
    lateinit var ageLabel: Label

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
    lateinit var experienceButtonByPE: Map<PE, RadioButton?>

    @FXML
    lateinit var clearInfoFormButton: Button

    @FXML
    lateinit var startInfoFormButton: Button


    fun initialize() {
        diagnosticLogger.info("${Plugin.PLUGIN_ID}: init controller")
        initMaps()

        controllerManager.addController(this)

        initInfoFormPane()
        initTaskChooserPane()
        initTaskStatusPane()
        initTaskFinishPane()

        setActive(ControllerManager.activePane)
    }

    private fun initMaps() {
        paneByName = hashMapOf(
            infoFormPane.id to infoFormPane,
            taskChooserPane.id to taskChooserPane,
            taskStatusPane.id to taskStatusPane,
            taskFinishPane.id to taskFinishPane
        )

        experienceButtonByPE = hashMapOf(
            PE.NULL to null,
            PE.LESS_THAN_HALF_YEAR to peLessThanHalf,
            PE.FROM_HALF_TO_ONE_YEAR to peFromHalfToOne,
            PE.FROM_ONE_TO_TWO_YEARS to peFromOneToTwo,
            PE.FROM_TWO_TO_FOUR_YEARS to peFromTwoToFour,
            PE.FROM_FOUR_TO_SIX_YEARS to peFromFourToSix,
            PE.MORE_THAN_SIX to peMoreThanSix
        )


        taskStatusButtonByTS = hashMapOf(
            TaskStatus.NULL to null,
            TaskStatus.NOT_SOLVED to taskNotSolved,
            TaskStatus.SOLVED to taskSolved
        )
    }


    fun selectExperienceButton(experience: PE) {
        diagnosticLogger.info("${Plugin.PLUGIN_ID}: select experience button: $experience")
        val selectedButton = experienceButtonByPE[experience]
        programExperienceGroup.selectToggle(selectedButton)
    }

    fun selectTaskStatusButton(status: TaskStatus) {
        diagnosticLogger.info("${Plugin.PLUGIN_ID}: select status button: $status")
        val selectedButton = taskStatusButtonByTS[status]
        taskStatusGroup.selectToggle(selectedButton)
    }

    fun setActive(name: String) {
        val pane = paneByName[name]
        if (pane != null) {
            diagnosticLogger.info("${Plugin.PLUGIN_ID}: set active panel: $name")
            paneByName.values.forEach { it.isVisible = false }
            pane.isVisible = true
        }
    }

    fun setStatusButtonsDisability(isDisable: Boolean) {
        endSolvingButton.isDisable = isDisable
        continueSolvingButton.isDisable = isDisable
    }

    fun setStartSolvingButtonDisability(isDisable: Boolean) {
        startSolvingButton.isDisable = isDisable
    }

    fun setInfoFormButtonsDisability(isDisable: Boolean) {
        clearInfoFormButton.isDisable = isDisable
        startInfoFormButton.isDisable = isDisable
    }

    fun setWrittenTaskVisibility(isVisible: Boolean) {
        taskTextField.isVisible = isVisible
        taskTextLabel.isVisible = isVisible
    }

    fun setTaskNameLabelIf(condition: Boolean, name: String) {
        if (condition) {
            taskNameLabel.text = name
        }
    }

    private fun initInfoFormPane() {
        initAgeSlider()
        initProgramExperienceGroup()
        initStartInfoFormButton()
        initClearInfoForm()
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

    private fun initStartSolvingButton() {
        startSolvingButton.addEventHandler(MouseEvent.MOUSE_CLICKED) {
            ControllerManager.activePane = taskStatusPane.id
        }
    }

    private fun initContinueSolvingButton() {
        continueSolvingButton.addEventHandler(MouseEvent.MOUSE_CLICKED) {
            ControllerManager.activePane = taskChooserPane.id
            setDefaultTaskData()
        }
    }

    private fun initEndSolvingButton() {
        endSolvingButton.addEventHandler(MouseEvent.MOUSE_CLICKED) {
            ControllerManager.activePane = taskFinishPane.id
        }
    }

    private fun initTaskStatusGroup() {
        taskStatusGroup.selectedToggleProperty().addListener { _, old, new ->
            diagnosticLogger.info("${Plugin.PLUGIN_ID}: task status changed from $old to $new")
            uiData.taskStatus.uiValue = taskStatusButtonByTS.filterValues { it == new }.keys.elementAtOrElse(0) { TaskStatus.NULL }
        }
    }

    private fun initStartSolvingAgainButton() {
        startSolvingAgainButton.addEventHandler(MouseEvent.MOUSE_CLICKED) {
            ControllerManager.activePane = infoFormPane.id
            setDefaultInfoData()
            setDefaultTaskData()
        }
    }

    private fun initTaskChoiceBox() {
        taskChoiceBox.selectionModel.selectedItemProperty().addListener { _, old, new ->
            diagnosticLogger.info("${Plugin.PLUGIN_ID}: choicebox changed from $old to $new")
            uiData.chosenTask.uiValue = taskChoiceBox.selectionModel.selectedIndex
        }
    }

    private fun initTaskTextField() {
        taskTextField.textProperty().addListener { _, old, new ->
            diagnosticLogger.info("${Plugin.PLUGIN_ID}: textfield changed from $old to $new")
            uiData.writtenTask.uiValue = new
        }
    }

    private fun initAgeSlider() {
        ageSlider.valueProperty().addListener { _, old, new ->
            diagnosticLogger.info("${Plugin.PLUGIN_ID}: slider changed from $old to $new")
            uiData.age.uiValue = new.toDouble()
        }
    }

    private fun initProgramExperienceGroup() {
        programExperienceGroup.selectedToggleProperty().addListener { _, old, new ->
            diagnosticLogger.info("${Plugin.PLUGIN_ID}: program experience changed from $old to $new")
            uiData.programExperience.uiValue = experienceButtonByPE.filterValues { it == new }.keys.elementAtOrElse(0) { PE.NULL }
        }
    }

    private fun initClearInfoForm() {
        clearInfoFormButton.addEventHandler(MouseEvent.MOUSE_CLICKED) {
            uiData.age.setDefault()
            uiData.programExperience.setDefault()
        }
    }

    private fun initStartInfoFormButton() {
        startInfoFormButton.addEventHandler(MouseEvent.MOUSE_CLICKED) {
            ControllerManager.activePane = taskChooserPane.id
        }
    }

    private fun setDefaultTaskData() {
        uiData.chosenTask.setDefault()
        uiData.writtenTask.setDefault()
        uiData.taskStatus.setDefault()
    }

    private fun setDefaultInfoData() {
        uiData.age.setDefault()
        uiData.programExperience.setDefault()
    }
}