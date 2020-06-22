package ui

import DocumentLogger
import data.Example
import Plugin
import com.intellij.openapi.Disposable
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Disposer
import data.PE
import data.Task
import data.UiData
import javafx.beans.binding.Bindings
import javafx.fxml.FXML
import javafx.scene.control.*
import javafx.scene.input.MouseEvent
import javafx.scene.layout.Pane
import javafx.scene.shape.Polygon
import javafx.scene.text.Text
import javafx.scene.text.TextFlow
import javafx.scene.transform.Scale;
import javafx.util.converter.IntegerStringConverter
import java.util.function.UnaryOperator


//todo test csv writing

class Controller(val project: Project, val scale: Double) {
    // todo: separate task and info form logic?

    abstract class TranslatedComponent<C>(component: C, translations: HashMap<String, String>) {
        abstract fun translate(language: String)
    }


    private val diagnosticLogger: Logger = Logger.getInstance(javaClass)

    private val uiData: UiData = ControllerManager.uiData


    @FXML
    lateinit var mainPane: Pane

    @FXML
    lateinit var paneByName: HashMap<String, Pane>

    /*
    ############################## task chooser pane ########################################
     */
    @FXML
    lateinit var taskChooserPane: Pane

    @FXML
    lateinit var taskComboBox: ComboBox<String>

    @FXML
    lateinit var startSolvingButton: Button

    /*
    ############################## task status pane ########################################
     */
    @FXML
    lateinit var taskStatusPane: Pane

    @FXML
    lateinit var taskFlow: TextFlow
    @FXML
    lateinit var taskNameText: Text
    @FXML
    lateinit var taskDescriptionText: Text
    @FXML
    lateinit var taskInputText: Text
    @FXML
    lateinit var taskOutputText: Text

    @FXML
    lateinit var firstExampleInput: TextArea
    @FXML
    lateinit var secondExampleInput: TextArea
    @FXML
    lateinit var thirdExampleInput: TextArea
    @FXML
    lateinit var firstExampleOutput: TextArea
    @FXML
    lateinit var secondExampleOutput: TextArea
    @FXML
    lateinit var thirdExampleOutput: TextArea

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
    lateinit var goToInfoForm: Button

    @FXML
    lateinit var goToTaskChooser: Button

    /*
    ############################## info form pane ########################################
     */

    @FXML
    lateinit var infoFormPane: Pane

    @FXML
    lateinit var ageField: TextField

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
    lateinit var startInfoFormButton: Button

    internal var id: Int = -1


    @FXML
    lateinit var pol_1: Polygon
    @FXML
    lateinit var pol_2: Polygon
    @FXML
    lateinit var pol_3: Polygon
    @FXML
    lateinit var pol_4: Polygon
    @FXML
    lateinit var pol_5: Polygon
    @FXML
    lateinit var pol_6: Polygon
    @FXML
    lateinit var pol_7: Polygon
    @FXML
    lateinit var pol_8: Polygon
    @FXML
    lateinit var pol_9: Polygon





    fun initialize() {
        mainPane.styleProperty().bind(Bindings.concat("-fx-font-size: ${scale}px;"))
        val polygons = arrayListOf(pol_1, pol_2, pol_3, pol_4, pol_5, pol_6, pol_7, pol_8, pol_9)

        val polygonScale = Scale()
        polygonScale.x = scale
        polygonScale.y = scale
        polygonScale.pivotX = 0.0
        polygonScale.pivotY = 0.0
        polygons.forEach { it.transforms.addAll(polygonScale)}

        diagnosticLogger.info("${Plugin.PLUGIN_ID}, controller${id}: init controller")
        Disposer.register(project, Disposable {
            ControllerManager.removeController(this)
        })

        initMaps()

        ControllerManager.addController(this)

        initInfoFormPane()
        initTaskChooserPane()
        initTaskStatusPane()
        initTaskFinishPane()
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

    }


    fun selectExperienceButton(experience: PE) {
        diagnosticLogger.info("${Plugin.PLUGIN_ID}, controller${id}: select experience button: $experience")
        val selectedButton = experienceButtonByPE[experience]
        programExperienceGroup.selectToggle(selectedButton)
    }

    fun setActive(name: String) {
        val pane = paneByName[name]
        if (pane != null) {
            diagnosticLogger.info("${Plugin.PLUGIN_ID}, controller${id}: set active panel: $name")
            paneByName.values.forEach { it.isVisible = false }
            pane.isVisible = true
        }
    }

    fun setStatusButtonsDisability(isDisable: Boolean) {
        diagnosticLogger.info("${Plugin.PLUGIN_ID}, controller${id}: set status buttons disability: $isDisable")
        endSolvingButton.isDisable = isDisable
        continueSolvingButton.isDisable = isDisable
    }

    fun setStartSolvingButtonDisability(isDisable: Boolean) {
        diagnosticLogger.info("${Plugin.PLUGIN_ID}, controller${id}: set solving button disability: $isDisable")
        startSolvingButton.isDisable = isDisable
    }

    fun setInfoFormButtonsDisability(isDisable: Boolean) {
        diagnosticLogger.info("${Plugin.PLUGIN_ID}, controller${id}: set info form buttons disability: $isDisable")
        startInfoFormButton.isDisable = isDisable
    }

    fun setTaskInfo(task: Task) {
        taskNameText.text = task.name
        taskDescriptionText.text = task.description
        taskInputText.text = task.input
        taskOutputText.text = task.output

        setExample(task.example_1, firstExampleInput, firstExampleOutput)
        setExample(task.example_2, secondExampleInput, secondExampleOutput)
        setExample(task.example_3, thirdExampleInput, thirdExampleOutput)
    }

    private fun setExample(example: Example, exampleInput: TextArea, exampleOutput: TextArea) {
        exampleInput.text = example.input
        exampleOutput.text = example.output
    }

    private fun initInfoFormPane() {
        initAgeField()
        initProgramExperienceGroup()
        initStartInfoFormButton()
    }

    private fun initTaskChooserPane() {
        initTaskChoiceBox()
        initStartSolvingButton()
    }

    private fun initTaskStatusPane() {
        initEndSolvingButton()
        initContinueSolvingButton()
    }

    private fun initTaskFinishPane() {
        initGoToInfoFormButton()
        initGoToTaskChooserButton()
    }

    private fun initStartSolvingButton() {
        startSolvingButton.addEventHandler(MouseEvent.MOUSE_CLICKED) {
            // log current state to store uiData change
            DocumentLogger.logCurrentDocuments()

            uiData.activePane.uiValue = taskStatusPane.id

            // log current state to store uiData change
            DocumentLogger.logCurrentDocuments()
        }
    }

    private fun initContinueSolvingButton() {
        continueSolvingButton.addEventHandler(MouseEvent.MOUSE_CLICKED) {
            // log current state to store uiData change
            DocumentLogger.logCurrentDocuments()

            uiData.activePane.uiValue = taskChooserPane.id
            setDefaultTaskData()

            // log current state to store uiData change
            DocumentLogger.logCurrentDocuments()
        }
    }

    private fun initEndSolvingButton() {
        endSolvingButton.addEventHandler(MouseEvent.MOUSE_CLICKED) {
            // log current state to store uiData change
            DocumentLogger.logCurrentDocuments()

            uiData.activePane.uiValue = taskFinishPane.id

            // log current state to store uiData change
            DocumentLogger.logCurrentDocuments()
        }
    }


    private fun initGoToInfoFormButton() {
        goToInfoForm.addEventHandler(MouseEvent.MOUSE_CLICKED) {
            // log current state to store uiData change
            DocumentLogger.logCurrentDocuments()

            uiData.activePane.uiValue = infoFormPane.id
            setDefaultTaskData()

            // log current state to store uiData change
            DocumentLogger.logCurrentDocuments()
        }
    }

    private fun initGoToTaskChooserButton() {
        goToTaskChooser.addEventHandler(MouseEvent.MOUSE_CLICKED) {
            // log current state to store uiData change
            DocumentLogger.logCurrentDocuments()

            uiData.activePane.uiValue = taskChooserPane.id
            setDefaultTaskData()

            // log current state to store uiData change
            DocumentLogger.logCurrentDocuments()
        }
    }


    private fun initTaskChoiceBox() {
        taskComboBox.selectionModel.selectedItemProperty().addListener { _, old, new ->
            diagnosticLogger.info("${Plugin.PLUGIN_ID}, controller${id}: choicebox changed from $old to $new")
            uiData.chosenTask.uiValue = taskComboBox.selectionModel.selectedIndex
        }
    }

    private fun initAgeField() {
        val filter: UnaryOperator<TextFormatter.Change?> = UnaryOperator label@ { change: TextFormatter.Change? ->
            val text: String? = change?.controlNewText
            if (text != null && (text.length < 3) &&(text.isEmpty() || text.matches(Regex("[1-9]+[0-9]*")))) {
                return@label change
            }
            null
        }

        val converter = IntegerStringConverter()
        ageField.textFormatter = TextFormatter(TextFormatter.IDENTITY_STRING_CONVERTER, "", filter)

        ageField.textProperty().addListener { _, old, new ->
            diagnosticLogger.info("${Plugin.PLUGIN_ID}, controller${id}: age has changed from $old to $new")
            uiData.age.uiValue = converter.fromString(new) ?: uiData.age.defaultUiValue
        }
    }

    private fun initProgramExperienceGroup() {
        programExperienceGroup.selectedToggleProperty().addListener { _, old, new ->
            diagnosticLogger.info("${Plugin.PLUGIN_ID}, controller${id}: program experience changed from $old to $new")
            uiData.programExperience.uiValue = experienceButtonByPE.filterValues { it == new }.keys.elementAtOrElse(0) { PE.NULL }
        }
    }

    private fun initStartInfoFormButton() {
        startInfoFormButton.addEventHandler(MouseEvent.MOUSE_CLICKED) {
            // log current state to store uiData change
            DocumentLogger.logCurrentDocuments()

            uiData.activePane.uiValue = taskChooserPane.id

            // log current state to store uiData change
            DocumentLogger.logCurrentDocuments()
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