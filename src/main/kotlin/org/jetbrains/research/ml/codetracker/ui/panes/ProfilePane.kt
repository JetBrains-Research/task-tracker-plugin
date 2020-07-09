package org.jetbrains.research.ml.codetracker.ui.panes

import com.intellij.openapi.project.Project
import com.intellij.util.messages.Topic
import javafx.collections.FXCollections
import javafx.embed.swing.JFXPanel
import javafx.fxml.FXML
import javafx.scene.control.*
import javafx.scene.layout.HBox
import javafx.scene.layout.Pane
import javafx.scene.shape.Line
import javafx.scene.shape.Polygon
import javafx.scene.shape.Rectangle
import javafx.scene.text.Text
import org.jetbrains.research.ml.codetracker.Plugin
import org.jetbrains.research.ml.codetracker.models.Country
import org.jetbrains.research.ml.codetracker.models.Gender
import org.jetbrains.research.ml.codetracker.server.PluginServer
import org.jetbrains.research.ml.codetracker.ui.*
import java.util.function.Consumer
import kotlin.reflect.KClass


object ProfileControllerManager : PaneControllerManager<ProfileController>() {
    override val paneControllerClass: KClass<ProfileController> = ProfileController::class
    override var paneControllers: MutableList<ProfileController> = arrayListOf()
    override val fxmlFilename: String = "profile-ui-form-2.fxml"
}

/**
 * [create] fun was added to simplify object creation, passing to [subscribe] method, because there is no SAM conversions for
 * kotlin interfaces (waiting for 1.4 release). Without SAM conversions creation an objects turns into many repeating
 * lines and looks ugly :( There is a way of turning these interfaces into classes and pass *accept* implementation
 * as constructor param, which allows to avoid extra lines with creation object. However, MessageBus requires interfaces,
 * so it's not an option.
 */
interface AgeNotifier : Consumer<Int> {
    companion object {
        val AGE_TOPIC = Topic.create("age change", AgeNotifier::class.java)
    }
}

interface GenderNotifier : Consumer<Int> {
    companion object {
        val GENDER_TOPIC = Topic.create("gender change", GenderNotifier::class.java)
    }
}

interface PeYearsNotifier : Consumer<Int> {
    companion object {
        val PE_YEARS_TOPIC = Topic.create("program experience years change", PeYearsNotifier::class.java)
    }
}

interface PeMonthsNotifier : Consumer<Int> {
    companion object {
        val PE_MONTHS_TOPIC = Topic.create("program experience months change", PeMonthsNotifier::class.java)
    }
}

interface CountryNotifier : Consumer<Int> {
    companion object {
        val COUNTRY_TOPIC = Topic.create("country change", CountryNotifier::class.java)
    }
}


object ProfileUiData : LanguagePaneUiData() {
    private val countries: List<Country> = PluginServer.countries
    private val genders: List<Gender> = PluginServer.genders

    val age = UiField(0, AgeNotifier.AGE_TOPIC)
    val gender = ListedUiField(genders, -1, GenderNotifier.GENDER_TOPIC)
    val peYears = UiField(-1, PeYearsNotifier.PE_YEARS_TOPIC)
    val peMonths = UiField( -1, PeMonthsNotifier.PE_MONTHS_TOPIC, false)
    val country = ListedUiField(countries, -1, CountryNotifier.COUNTRY_TOPIC)

    override fun getData() = listOf(
        age,
        gender,
        peYears,
        peMonths,
        country,
        language
    )
}

class ProfileController(project: Project, scale: Double, fxPanel: JFXPanel, id: Int) : LanguagePaneController(project, scale, fxPanel, id) {
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
    @FXML private lateinit var peMonthsHBox: HBox
    @FXML private lateinit var peMonthsLabel: Label
    @FXML private lateinit var peMonthsTextField: TextField
    @FXML private lateinit var peMonthsLine: Line

    // Country
    @FXML private lateinit var countryLabel: Label
    @FXML private lateinit var countryComboBox: ComboBox<String>

    // StartWorking
    @FXML private lateinit var startWorkingButton: Button
    @FXML private lateinit var startWorkingText: Text

    override val paneUiData = ProfileUiData
    private val translations = PluginServer.paneText?.surveyPane

    companion object {
        private const val PE_YEARS_NUMBER_TO_SHOW_MONTHS = 2
    }

    override fun initialize() {
        logger.info("${Plugin.PLUGIN_ID}:${this::class.simpleName} init controller")
        initAge()
        initGender()
        initPeYears()
        initPeMonths()
        initCountry()
        initStartWorkingButton()
        makeTranslatable()
        super.initialize()
    }

    private fun initAge() {
        val converter = ageTextField.addIntegerFormatter(regexFilter("[1-9][0-9]{0,1}"))
        ageTextField.textProperty().addListener { _, _, new ->
            paneUiData.age.uiValue = converter.fromString(new) ?: paneUiData.age.defaultUiValue
        }
        subscribe(AgeNotifier.AGE_TOPIC, object : AgeNotifier {
            override fun accept(newAge: Int) {
                ageTextField.text = newAge.toString()
                println("age: ${paneUiData.anyRequiredDataDefault()}")
                startWorkingButton.isDisable = paneUiData.anyRequiredDataDefault()
            }
        })
    }

    private fun initGender() {
        genderRadioButtons = listOf(gender1, gender2, gender3, gender4, gender5, gender6)
        val gendersSize = paneUiData.gender.dataList.size
        genderRadioButtons.forEachIndexed { i, rb ->  rb.isVisible = i < gendersSize }

        genderGroup.selectedToggleProperty().addListener { _, _, new ->
            paneUiData.gender.uiValue = genderRadioButtons.indexOf(new)
        }
        subscribe(GenderNotifier.GENDER_TOPIC, object : GenderNotifier {
            override fun accept(newGenderIndex: Int) {
                if (paneUiData.gender.isValid(newGenderIndex)) {
                    genderGroup.selectToggle(genderRadioButtons[newGenderIndex])
                    println("gender: ${paneUiData.anyRequiredDataDefault()}")
                    startWorkingButton.isDisable = paneUiData.anyRequiredDataDefault()
                }
            }
        })
    }

    private fun initPeYears() {
        val converter = peYearsTextField.addIntegerFormatter(regexFilter("0|[1-9][0-9]{0,1}"))
        peYearsTextField.textProperty().addListener { _, _, new ->
            paneUiData.peYears.uiValue = converter.fromString(new) ?: paneUiData.peYears.defaultUiValue
        }
        subscribe(PeYearsNotifier.PE_YEARS_TOPIC, object : PeYearsNotifier {
            override fun accept(newPeYears: Int) {
                peYearsTextField.text = newPeYears.toString()
                val isPeMonthsRequired = !paneUiData.peYears.isUiValueDefault && newPeYears < PE_YEARS_NUMBER_TO_SHOW_MONTHS
//                todo: set default value if not required?
                paneUiData.peMonths.isRequired = isPeMonthsRequired
                peMonthsHBox.isVisible = isPeMonthsRequired
                println("pe years: ${paneUiData.anyRequiredDataDefault()}")
                startWorkingButton.isDisable = paneUiData.anyRequiredDataDefault()
            }
        })
    }

    private fun initPeMonths() {
        peMonthsHBox.isVisible = paneUiData.peMonths.isRequired
//        change when delete last symbol
        val converter = peMonthsTextField.addIntegerFormatter(regexFilter("[0-9]|1[01]"))
        peMonthsTextField.textProperty().addListener { _, old, new ->
            paneUiData.peMonths.uiValue = converter.fromString(new) ?: paneUiData.peMonths.defaultUiValue
        }
        subscribe(PeMonthsNotifier.PE_MONTHS_TOPIC, object : PeMonthsNotifier {
            override fun accept(newPeMonths: Int) {
                peMonthsTextField.text = newPeMonths.toString()
                println("pe months: ${paneUiData.anyRequiredDataDefault()}")
                startWorkingButton.isDisable = paneUiData.anyRequiredDataDefault()
            }
        })
    }


    private fun initCountry() {
//        Todo: make it autocomplete https://stackoverflow.com/questions/19924852/autocomplete-combobox-in-javafx
//        Todo: make translatable
        val items = FXCollections.observableList(paneUiData.country.dataList.map {
            it.translation[paneUiData.language.currentValue]
        })
        countryComboBox.items = items
        countryComboBox.selectionModel.selectedItemProperty().addListener { _ ->
            paneUiData.country.uiValue = countryComboBox.selectionModel.selectedIndex
        }
        subscribe(CountryNotifier.COUNTRY_TOPIC, object : CountryNotifier {
            override fun accept(newCountryIndex: Int) {
                countryComboBox.selectionModel.select(newCountryIndex)
                startWorkingButton.isDisable = paneUiData.anyRequiredDataDefault()
            }
        })
    }

    private fun initStartWorkingButton() {
        startWorkingButton.onMouseClicked { changeVisiblePane(TaskChooserControllerManager) }
    }

    private fun makeTranslatable() {
        subscribe(LanguageNotifier.LANGUAGE_TOPIC, object : LanguageNotifier {
            override fun accept(newLanguageIndex: Int) {
                val newLanguage = paneUiData.language.dataList[newLanguageIndex]
                val surveyPaneText = translations?.get(newLanguage)
                surveyPaneText?.let {
                    ageLabel.text = it.age
                    genderLabel.text = it.gender
                    experienceLabel.text = it.experience
                    peYearsLabel.text = it.years
                    peMonthsLabel.text = it.months
                    countryLabel.text = it.country
                    startWorkingText.text = it.startSession
                }
                genderRadioButtons.zip(paneUiData.gender.dataList) { rb, g -> rb.text = g.translation[newLanguage] }
            }
        })
    }
}
