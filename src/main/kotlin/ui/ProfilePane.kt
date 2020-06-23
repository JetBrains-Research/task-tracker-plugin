package ui

import javafx.collections.FXCollections
import javafx.embed.swing.JFXPanel
import javafx.fxml.FXML
import javafx.scene.control.*
import javafx.scene.input.MouseEvent
import javafx.scene.layout.Pane
import javafx.scene.shape.Polygon
import javafx.scene.shape.Rectangle
import javafx.scene.text.Text
import javafx.util.converter.IntegerStringConverter
import java.util.function.UnaryOperator
import kotlin.reflect.KClass

enum class ProfileNotifyEvent : IPaneNotifyEvent {
    AGE_NOTIFY,
    PROGRAM_EXPERIENCE_NOTIFY,
    GENDER_NOTIFY,
    COUNTRY_NOTIFY,
    LANGUAGE_NOTIFY
}

object ProfileControllerManager : PaneControllerManager<ProfileNotifyEvent, ProfileController>() {
    override val paneControllerClass: KClass<ProfileController> = ProfileController::class
    override val paneUiData: ProfileUiData = ProfileUiData
    override var paneControllers: MutableList<ProfileController> = arrayListOf()
    override val fxmlFilename: String = "profile-ui-form-2.fxml"

    //Todo: get generic type here?
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

inline class Country(val key: String)


object ProfileUiData : PaneUiData<ProfileNotifyEvent>(ProfileControllerManager) {
    //    Todo: get from server
    val countryList: List<Country> = listOf(Country("Россия"), Country("Нидерланды"))

    val age = UiField(ProfileNotifyEvent.AGE_NOTIFY, 0, "age")
    val gender = UiField<Gender?>(ProfileNotifyEvent.GENDER_NOTIFY, null, "gender")
    val programExperience = UiField<PE?>(ProfileNotifyEvent.PROGRAM_EXPERIENCE_NOTIFY, null, "programExperience")
    val country = ListedUiField(countryList, ProfileNotifyEvent.COUNTRY_NOTIFY, -1,"country")
    override val currentLanguage: LanguageUiField = LanguageUiField(ProfileNotifyEvent.LANGUAGE_NOTIFY)


//    fun isAllDataFilled(): Boolean {
//        return getData().all { !it.isDefault() }
//    }

    override fun getData() = listOf(age, gender, programExperience, country)

    enum class PE {
        LESS_THAN_HALF,
        FROM_HALF_TO_ONE,
        FROM_ONE_TO_TWO,
        FROM_TWO_TO_FOUR,
        FROM_FOUR_TO_SIX,
        MORE_THAN_SIX

    }
    enum class Gender {
        FEMALE,
        MALE,
        OTHER

    }
}

class ProfileController(override val uiData: ProfileUiData, scale: Double, fxPanel: JFXPanel, id: Int) : PaneController<ProfileNotifyEvent>(uiData, scale, fxPanel, id) {
    @FXML private lateinit var profilePane: Pane

    // Scalable components:
    @FXML private lateinit var orangePolygon: Polygon
    @FXML private lateinit var yellowRectangle: Rectangle
    @FXML private lateinit var bluePolygon: Polygon

    // Age
    @FXML private lateinit var ageLabel: Label
    //    lateinit var translatableAgeLabel: TranslatableComponent
    @FXML private lateinit var ageTextField: TextField

    // Gender
    @FXML private lateinit var genderLabel: Label
    @FXML private lateinit var genderGroup: ToggleGroup
    @FXML private lateinit var femaleGender: RadioButton
    @FXML private lateinit var maleGender: RadioButton
    @FXML private lateinit var otherGender: RadioButton
    @FXML private lateinit var genderButtonByGender: HashMap<ProfileUiData.Gender, RadioButton>

    // Program Experience
    @FXML private lateinit var experienceLabel: Label
    @FXML private lateinit var programExperienceGroup: ToggleGroup
    @FXML private lateinit var peLessThanHalf: RadioButton
    @FXML private lateinit var peFromHalfToOne: RadioButton
    @FXML private lateinit var peFromOneToTwo: RadioButton
    @FXML private lateinit var peFromTwoToFour: RadioButton
    @FXML private lateinit var peFromFourToSix: RadioButton
    @FXML private lateinit var peMoreThanSix: RadioButton
    @FXML private lateinit var experienceButtonByPE: HashMap<ProfileUiData.PE, RadioButton>

    // Country
    @FXML private lateinit var countryLabel: Label
    @FXML private lateinit var countryComboBox: ComboBox<String>

    // StartWorking
    @FXML private lateinit var startWorkingButton: Button
    @FXML private lateinit var startWorkingText: Text

//    Todo: add language


    override fun initialize() {
        initAge()
        initGender()
        initProgramExperience()
        initCountry()
        initStartWorkingButton()
        super.initialize()

//        Todo: add translatable, add scalable
//        translatableAgeLabel = TranslatableComponent(ageLabel, ::ageLabel.name)
    }

    // Todo: make button not active when something is not set?
    fun setAge(newAge: Int) {
//        Default value shouldn't be showed
//        Todo: include in initAge filter?
//        val strAge = if (ProfileUiData.age.isDefault(newAge)) "" else newAge.toString()
        ageTextField.text = newAge.toString()
    }

    fun selectGender(newGender: ProfileUiData.Gender?) {
        genderGroup.selectToggle(genderButtonByGender.getOrDefault(newGender, null))
    }

    fun selectProgramExperience(newProgramExperience: ProfileUiData.PE?) {
        programExperienceGroup.selectToggle(experienceButtonByPE.getOrDefault(newProgramExperience, null))
    }

    fun selectCountry(newCountryIndex: Int) {
        countryComboBox.selectionModel.select(newCountryIndex)
    }

    fun setStartWorkingButtonDisability(isDisable: Boolean) {
        println("isDisable: $isDisable")
        startWorkingButton.isDisable = isDisable
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
            uiData.age.uiValue = converter.fromString(new) ?: uiData.age.defaultUiValue
        }
    }

    private fun initGender() {
        genderButtonByGender = hashMapOf(
            ProfileUiData.Gender.FEMALE to femaleGender,
            ProfileUiData.Gender.MALE to maleGender,
            ProfileUiData.Gender.OTHER to otherGender
        )
        genderGroup.selectedToggleProperty().addListener { _, old, new ->
            uiData.gender.uiValue = genderButtonByGender.filterValues { it == new }.keys.elementAtOrElse(0) { uiData.gender.defaultUiValue}
        }
    }

    private fun initProgramExperience() {
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
            uiData.programExperience.uiValue = experienceButtonByPE.filterValues { it == new }.keys.elementAtOrElse(0) { uiData.programExperience.defaultUiValue }
        }
    }

    private fun initCountry() {
//        Todo: make it autocomplete https://stackoverflow.com/questions/19924852/autocomplete-combobox-in-javafx
        countryComboBox.items = FXCollections.observableList(uiData.countryList.map { it.key } )
        countryComboBox.selectionModel.selectedItemProperty().addListener { _, old, new ->
            uiData.country.uiValue = countryComboBox.selectionModel.selectedIndex
        }
    }

    private fun initStartWorkingButton() {
        startWorkingButton.addEventHandler(MouseEvent.MOUSE_CLICKED) {
            MainController.visiblePaneControllerManager = TaskChooserControllerManager
        }
    }

}