package org.jetbrains.research.ml.codetracker.ui.panes

import javafx.collections.FXCollections
import javafx.embed.swing.JFXPanel
import javafx.fxml.FXML
import javafx.scene.control.*
import javafx.scene.input.MouseEvent
import javafx.scene.layout.Pane
import javafx.scene.shape.Line
import javafx.scene.shape.Polygon
import javafx.scene.shape.Rectangle
import javafx.scene.text.Text
import javafx.util.converter.IntegerStringConverter
import org.jetbrains.research.ml.codetracker.ui.MainController
import org.jetbrains.research.ml.codetracker.ui.makeTranslatable
import java.util.function.UnaryOperator
import kotlin.reflect.KClass


enum class ProfileNotifyEvent : IPaneNotifyEvent {
    AGE_NOTIFY,
    PE_YEARS_NOTIFY,
    PE_MONTHS_NOTIFY,
    GENDER_NOTIFY,
    COUNTRY_NOTIFY,
    LANGUAGE_NOTIFY
}

object ProfileControllerManager : PaneControllerManager<ProfileNotifyEvent, ProfileController>() {
    override val paneControllerClass: KClass<ProfileController> = ProfileController::class
    override val paneUiData: ProfileUiData =
        ProfileUiData
    override var paneControllers: MutableList<ProfileController> = arrayListOf()
    override val fxmlFilename: String = "profile-ui-form-2.fxml"

    //Todo: get uiFiled type here and cast new automatically?
    override fun notify(notifyEvent: ProfileNotifyEvent, new: Any?) {
        val isProfileUnfilled = paneUiData.anyDataDefault()
        paneControllers.forEach { it.setStartWorkingButtonDisability(isProfileUnfilled) }

        when (notifyEvent) {
            ProfileNotifyEvent.AGE_NOTIFY -> paneControllers.forEach { it.setAge(new as Int) }
            ProfileNotifyEvent.COUNTRY_NOTIFY -> paneControllers.forEach { it.selectCountry(new as Int) }
            ProfileNotifyEvent.GENDER_NOTIFY -> paneControllers.forEach { it.selectGender(new as ProfileUiData.Gender?) }
            ProfileNotifyEvent.PROGRAM_EXPERIENCE_NOTIFY ->  paneControllers.forEach { it.selectProgramExperience(new as ProfileUiData.PE?) }
            ProfileNotifyEvent.LANGUAGE_NOTIFY -> switchLanguage(new as Int)
        }
    }
}

// Todo: move to some other place?
inline class Country(val key: String) {
    override fun toString(): String {
        return key
    }
}

inline class Gender(val key: String) {
    override fun toString(): String {
        return key
    }
}

object ProfileUiData : PaneUiData<ProfileNotifyEvent>(
    ProfileControllerManager
) {
    //    Todo: get from server
    private val countryList: List<Country> = listOf(
        Country("Россия"),
        Country("Нидерланды")
    )
    private val genderList: List<Gender> = listOf(Gender("male"), Gender("female"), Gender("other"))

    val age = UiField(ProfileNotifyEvent.AGE_NOTIFY, 0, "age")
    val gender = ListedUiField(
        genderList,
        ProfileNotifyEvent.GENDER_NOTIFY, -1, "gender")
    val peYears = UiField(ProfileNotifyEvent.PE_YEARS_NOTIFY, -1, "peYears")
    val peMonths = UiField(ProfileNotifyEvent.PE_MONTHS_NOTIFY, -1, "peMonths")
    val country = ListedUiField(
        countryList,
        ProfileNotifyEvent.COUNTRY_NOTIFY, -1,"country")
    override val currentLanguage: LanguageUiField = LanguageUiField(
        ProfileNotifyEvent.LANGUAGE_NOTIFY
    )

    override fun getData() = listOf(
        age,
        gender,
        programExperience,
        country
    )
}

class ProfileController(override val uiData: ProfileUiData, scale: Double, fxPanel: JFXPanel, id: Int) : PaneController<ProfileNotifyEvent>(uiData, scale, fxPanel, id) {
    @FXML private lateinit var profilePane: Pane

    // Scalable components:
    @FXML private lateinit var orangePolygon: Polygon
    @FXML private lateinit var yellowRectangle: Rectangle
    @FXML private lateinit var bluePolygon: Polygon

    // Age
    @FXML private lateinit var ageLabel: Label
    @FXML private lateinit var ageTextField: TextField

    // Gender
    @FXML private lateinit var genderLabel: Label
    @FXML private lateinit var genderGroup: ToggleGroup
    @FXML private lateinit var gender1: RadioButton
    @FXML private lateinit var gender2: RadioButton
    @FXML private lateinit var gender3: RadioButton
    @FXML private lateinit var gender4: RadioButton
    @FXML private lateinit var gender5: RadioButton
    @FXML private lateinit var gender6: RadioButton
    @FXML private lateinit var genderRadioButtons: List<RadioButton>

    // Program Experience
    @FXML private lateinit var experienceLabel: Label
    @FXML private lateinit var peYearsLabel: Label
    @FXML private lateinit var peYearsTextField: TextField
    @FXML private lateinit var peYearsLine: Line
    @FXML private lateinit var peMonthsLabel: Label
    @FXML private lateinit var peMonthsTextField: TextField
    @FXML private lateinit var peMonthsLine: Line

    // Country
    @FXML private lateinit var countryLabel: Label
    @FXML private lateinit var countryComboBox: ComboBox<Country>

    // StartWorking
    @FXML private lateinit var startWorkingButton: Button
    @FXML private lateinit var startWorkingText: Text

    override fun initialize() {
        initAge()
        initGender()
        initPeYears()
        initPeMonths()
        initCountry()
        initStartWorkingButton()
        super.initialize()
    }

    fun setAge(newAge: Int) {
        ageTextField.text = newAge.toString()
    }

    fun selectGender(newGenderIndex: Int) {
//        todo: is newGenderIndex is default?
        genderGroup.selectToggle(genderRadioButtons[newGenderIndex])
    }

    fun setPeYears(newYears: Int) {
        peYearsTextField.text = newYears.toString()
    }

    fun setPeMonths(newMonths: Int) {
        peYearsTextField.text = newMonths.toString()
    }

    fun selectCountry(newCountryIndex: Int) {
        countryComboBox.selectionModel.select(newCountryIndex)
    }

    fun setStartWorkingButtonDisability(isDisable: Boolean) {
        println("isDisable: $isDisable")
        startWorkingButton.isDisable = isDisable
    }

    override fun makeTranslatable() {
        ageLabel.makeTranslatable(::ageLabel.name)
        genderLabel.makeTranslatable(::genderLabel.name)
        experienceLabel.makeTranslatable(::experienceLabel.name)
        countryLabel.makeTranslatable(::countryLabel.name)
        startWorkingText.makeTranslatable(::startWorkingText.name)
    }

    private fun initAge() {
        val filter: UnaryOperator<TextFormatter.Change?> = UnaryOperator label@ { change: TextFormatter.Change? ->
            val text: String? = change?.controlNewText
            if (text != null && (text.length < 3) && (text.isEmpty() || text.matches(Regex("[1-9]+[0-9]*")))) {
                return@label change
            }
            null
        }
        val converter = IntegerStringConverter()
        ageTextField.textFormatter = TextFormatter(TextFormatter.IDENTITY_STRING_CONVERTER, "", filter)
        ageTextField.textProperty().addListener { _, old, new ->
            ProfileUiData.age.uiValue = converter.fromString(new) ?: ProfileUiData.age.defaultUiValue
        }
    }

    private fun initGender() {
        genderRadioButtons = listOf(gender1, gender2, gender3, gender4, gender5, gender6)
        genderGroup.selectedToggleProperty().addListener { _, old, new ->
            ProfileUiData.gender.uiValue = genderButtonByGender.filterValues { it == new }.keys.elementAtOrElse(0) { ProfileUiData.gender.defaultUiValue}
        }
    }

    private fun initPeYears() {
        val yearFilter: UnaryOperator<TextFormatter.Change?> = UnaryOperator label@{ change: TextFormatter.Change? ->
            val text: String? = change?.controlNewText
            if (text != null && (text.length < 3) && (text.isEmpty() || text.matches(Regex("0|[1-9]+[0-9]*")))) {
                return@label change
            }
            null
        }
        val converter = IntegerStringConverter()
        peYearsTextField.textFormatter = TextFormatter(TextFormatter.IDENTITY_STRING_CONVERTER, "", yearFilter)
        peYearsTextField.textProperty().addListener { _, old, new ->
            ProfileUiData.peYears.uiValue = converter.fromString(new) ?: ProfileUiData.peYears.defaultUiValue
        }
    }

    private fun initPeMonths() {
        val monthsFilter: UnaryOperator<TextFormatter.Change?> = UnaryOperator label@{ change: TextFormatter.Change? ->
            val text: String? = change?.controlNewText
            if (text != null && (text.length < 3) && (text.isEmpty() || text.matches(Regex("[0-9]|1[012]]")))) {
                return@label change
            }
            null
        }
        val converter = IntegerStringConverter()
        peMonthsTextField.textFormatter = TextFormatter(TextFormatter.IDENTITY_STRING_CONVERTER, "", monthsFilter)
        peMonthsTextField.textProperty().addListener { _, old, new ->
            ProfileUiData.peMonths.uiValue = converter.fromString(new) ?: ProfileUiData.peMonths.defaultUiValue
        }
    }



//        Todo: make it better somehow?
        experienceButtonByPE = hashMapOf (
            ProfileUiData.PE.LESS_THAN_HALF to peLessThanHalf,
            ProfileUiData.PE.FROM_HALF_TO_ONE to peFromHalfToOne,
            ProfileUiData.PE.FROM_ONE_TO_TWO to peFromOneToTwo,
            ProfileUiData.PE.FROM_TWO_TO_FOUR to peFromTwoToFour,
            ProfileUiData.PE.FROM_FOUR_TO_SIX to peFromFourToSix,
            ProfileUiData.PE.MORE_THAN_SIX to peMoreThanSix
        )
        programExperienceGroup.selectedToggleProperty().addListener { _, old, new ->
            ProfileUiData.programExperience.uiValue = experienceButtonByPE.filterValues { it == new }.keys.elementAtOrElse(0) { ProfileUiData.programExperience.defaultUiValue }
        }
    }

    private fun initCountry() {
//        Todo: make it autocomplete https://stackoverflow.com/questions/19924852/autocomplete-combobox-in-javafx
        countryComboBox.items = FXCollections.observableList(ProfileUiData.country.dataList)
        countryComboBox.selectionModel.selectedItemProperty().addListener { _, old, new ->
            ProfileUiData.country.uiValue = countryComboBox.selectionModel.selectedIndex
        }
    }

    private fun initStartWorkingButton() {
        startWorkingButton.addEventHandler(MouseEvent.MOUSE_CLICKED) {
            MainController.visiblePaneControllerManager =
                TaskChooserControllerManager
        }
    }
}
