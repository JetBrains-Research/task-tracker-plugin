package org.jetbrains.research.ml.codetracker.ui.panes

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.project.Project
import com.intellij.util.messages.Topic
import javafx.beans.binding.Bindings
import javafx.collections.FXCollections
import javafx.collections.ObservableList
import javafx.embed.swing.JFXPanel
import javafx.fxml.FXML
import javafx.scene.control.*
import javafx.scene.layout.HBox
import javafx.scene.layout.Pane
import javafx.scene.shape.Polygon
import javafx.scene.text.Text
import javafx.util.Callback
import org.jetbrains.research.ml.codetracker.Plugin
import org.jetbrains.research.ml.codetracker.models.Country
import org.jetbrains.research.ml.codetracker.models.Gender
import org.jetbrains.research.ml.codetracker.models.Language
import org.jetbrains.research.ml.codetracker.server.PluginServer
import org.jetbrains.research.ml.codetracker.tracking.StoredInfoHandler
import org.jetbrains.research.ml.codetracker.tracking.StoredInfoWrapper
import org.jetbrains.research.ml.codetracker.tracking.UiLoggedData
import org.jetbrains.research.ml.codetracker.tracking.UiLoggedDataHeader
import org.jetbrains.research.ml.codetracker.ui.panes.util.*
import java.net.URL
import java.util.*
import java.util.function.Consumer
import kotlin.Comparator
import kotlin.reflect.KClass


object SurveyControllerManager : ServerDependentPane<SurveyController>() {
    override val paneControllerClass: KClass<SurveyController> = SurveyController::class
    override val fxmlFilename: String = "survey-ui-form.fxml"
}


// Maybe its possible to make bounded properties instead?
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

interface CountryComparatorNotifier : Consumer<Comparator<Country>> {
    companion object {
        val COUNTRY_COMPARATOR_TOPIC = Topic.create("country list change", CountryComparatorNotifier::class.java)
    }
}

interface ProgrammingLanguageNotifier : Consumer<Int> {
    companion object {
        val PROGRAMMING_LANGUAGE_TOPIC = Topic.create("programming language change", ProgrammingLanguageNotifier::class.java)
    }
}

object SurveyUiData : LanguagePaneUiData() {
    private val countries: List<Country> = PluginServer.countries
    private val genders: List<Gender> = PluginServer.genders
    private val programmingLanguages: List<Language> = Language.values().toList()

    val age = UiField(-1, AgeNotifier.AGE_TOPIC, StoredInfoHandler.getIntStoredField(UiLoggedDataHeader.AGE, -1))
    val gender = ListedUiField(
        genders,
        -1,
        GenderNotifier.GENDER_TOPIC,
        initValue = StoredInfoHandler.getIndexByStoredKey(UiLoggedDataHeader.GENDER, genders, -1)
    )
    val peYears = UiField(
        -1,
        PeYearsNotifier.PE_YEARS_TOPIC,
        StoredInfoHandler.getIntStoredField(UiLoggedDataHeader.PROGRAM_EXPERIENCE_YEARS, -1)

    )
    val peMonths = UiField(
        -1,
        PeMonthsNotifier.PE_MONTHS_TOPIC,
        StoredInfoHandler.getIntStoredField(UiLoggedDataHeader.PROGRAM_EXPERIENCE_MONTHS, -1),
        false
    )
    val country = ListedUiField(
        countries,
        -1,
        CountryNotifier.COUNTRY_TOPIC,
        compareBy { c -> c.translation.getOrDefault(language.currentValue, "") },
        CountryComparatorNotifier.COUNTRY_COMPARATOR_TOPIC,
        StoredInfoHandler.getIndexByStoredKey(UiLoggedDataHeader.COUNTRY, countries, -1)
    )
    val programmingLanguage = ListedUiField(
        programmingLanguages,
        -1,
        ProgrammingLanguageNotifier.PROGRAMMING_LANGUAGE_TOPIC,
        initValue = StoredInfoHandler.getIndexByStoredKey(UiLoggedDataHeader.PROGRAMMING_LANGUAGE, programmingLanguages, -1)
    )

    override fun getData() = listOf(
        age,
        gender,
        peYears,
        peMonths,
        country,
        programmingLanguage,
        language
    )
}

class SurveyController(project: Project, scale: Double, fxPanel: JFXPanel, id: Int) :
    LanguagePaneController(project, scale, fxPanel, id) {
    // Age
    @FXML
    private lateinit var ageLabel: Label

    @FXML
    private lateinit var ageTextField: TextField

    // Gender
    @FXML
    private lateinit var genderLabel: Label

    @FXML
    private lateinit var genderGroup: ToggleGroup

    @FXML
    private lateinit var gender1: RadioButton

    @FXML
    private lateinit var gender2: RadioButton

    @FXML
    private lateinit var gender3: RadioButton

    @FXML
    private lateinit var gender4: RadioButton

    @FXML
    private lateinit var gender5: RadioButton

    @FXML
    private lateinit var gender6: RadioButton

    @FXML
    private lateinit var genderRadioButtons: List<RadioButton>

    // Program Experience
    @FXML
    private lateinit var experienceLabel: Label

    @FXML
    private lateinit var peYearsLabel: Label

    @FXML
    private lateinit var peYearsTextField: TextField

    @FXML
    private lateinit var peMonthsHBox: HBox

    @FXML
    private lateinit var peMonthsLabel: Label

    @FXML
    private lateinit var peMonthsTextField: TextField

    // Country
    @FXML
    private lateinit var countryLabel: Label

    @FXML
    private lateinit var countryComboBox: ComboBox<Country>
    private lateinit var countryObservableList: ObservableList<Country>

    // Programming language
    @FXML
    private lateinit var programmingLanguageLabel: Label

    @FXML
    private lateinit var programmingLanguageComboBox: ComboBox<String>

    // StartWorking
    @FXML
    private lateinit var startWorkingButton: Button

    @FXML
    private lateinit var startWorkingText: Text

    @FXML
    private lateinit var mainPane: Pane

    @FXML
    private lateinit var orangePolygon: Polygon

    @FXML
    private lateinit var bluePolygon: Polygon

    override val paneUiData = SurveyUiData
    private val translations = PluginServer.paneText?.surveyPane

    companion object {
        private const val PE_YEARS_NUMBER_TO_SHOW_MONTHS = 1
    }

    override fun initialize(url: URL?, resource: ResourceBundle?) {
        logger.info("${Plugin.PLUGIN_NAME}:${this::class.simpleName} init controller")
        mainPane.styleProperty().bind(Bindings.concat("-fx-font-size: ${scale}px;"))
        scalePolygons(arrayListOf(orangePolygon, bluePolygon))
        initAge()
        initGender()
        initPeYears()
        initPeMonths()
        initCountry()
        initProgrammingLanguage()
        initStartWorkingButton()
        makeTranslatable()
        super.initialize(url, resource)
    }

    private fun initAge() {
        ageTextField.addIntegerFormatter(regexFilter("[1-9][0-9]{0,1}"))
        ageTextField.textProperty().addListener { _, _, new ->
            paneUiData.age.uiValue = new.toIntOrNull() ?: paneUiData.age.defaultValue
        }
        subscribe(AgeNotifier.AGE_TOPIC, object : AgeNotifier {
            override fun accept(newAge: Int) {
                ageTextField.text = newAge.toString()
                startWorkingButton.isDisable = paneUiData.anyRequiredDataDefault()
            }
        })
    }

    private fun initGender() {
        genderRadioButtons = listOf(gender1, gender2, gender3, gender4, gender5, gender6)
        val gendersSize = paneUiData.gender.dataList.size
        genderRadioButtons.forEachIndexed { i, rb -> rb.isVisible = i < gendersSize }

        genderGroup.selectedToggleProperty().addListener { _, _, new ->
            paneUiData.gender.uiValue = genderRadioButtons.indexOf(new)
        }
        subscribe(GenderNotifier.GENDER_TOPIC, object : GenderNotifier {
            override fun accept(newGenderIndex: Int) {
                if (paneUiData.gender.isValid(newGenderIndex)) {
                    genderGroup.selectToggle(genderRadioButtons[newGenderIndex])
                    startWorkingButton.isDisable = paneUiData.anyRequiredDataDefault()
                }
            }
        })
    }

    private fun initPeYears() {
        peYearsTextField.addIntegerFormatter(regexFilter("0|[1-9][0-9]{0,1}"))
        peYearsTextField.textProperty().addListener { _, _, new ->
            paneUiData.peYears.uiValue = new.toIntOrNull() ?: paneUiData.peYears.defaultValue
        }
        subscribe(PeYearsNotifier.PE_YEARS_TOPIC, object : PeYearsNotifier {
            override fun accept(newPeYears: Int) {
                peYearsTextField.text = newPeYears.toString()
                val isPeMonthsRequired =
                    !paneUiData.peYears.isUiValueDefault && newPeYears < PE_YEARS_NUMBER_TO_SHOW_MONTHS
                paneUiData.peMonths.isRequired = isPeMonthsRequired
                peMonthsHBox.isVisible = isPeMonthsRequired
                if (!isPeMonthsRequired) {
                    paneUiData.peMonths.uiValue = paneUiData.peMonths.defaultValue
                }
                startWorkingButton.isDisable = paneUiData.anyRequiredDataDefault()
            }
        })
    }

    private fun initPeMonths() {
        peMonthsHBox.isVisible = paneUiData.peMonths.isRequired
        peMonthsTextField.addIntegerFormatter(regexFilter("[0-9]|1[01]"))
        peMonthsTextField.textProperty().addListener { _, old, new ->
            paneUiData.peMonths.uiValue = new.toIntOrNull() ?: paneUiData.peMonths.defaultValue
        }
        subscribe(PeMonthsNotifier.PE_MONTHS_TOPIC, object : PeMonthsNotifier {
            override fun accept(newPeMonths: Int) {
                peMonthsTextField.text = newPeMonths.toString()
                startWorkingButton.isDisable = paneUiData.anyRequiredDataDefault()
            }
        })
    }

    private fun initCountry() {
//        Todo: make it autocomplete https://stackoverflow.com/questions/19924852/autocomplete-combobox-in-javafx
        countryObservableList = FXCollections.observableList(paneUiData.country.dataList)
        countryComboBox.items = countryObservableList

        val cellFactory = Callback<ListView<Country>, ListCell<Country>> {
            object : ListCell<Country>() {
                override fun updateItem(item: Country?, empty: Boolean) {
                    super.updateItem(item, empty)
                    if (item == null || empty) {
                        graphic = null;
                    } else {
                        text = item.translation.getOrDefault(LanguagePaneUiData.language.currentValue, "")
                    }
                }
            }
        }

        countryComboBox.buttonCell = cellFactory.call(null)
        countryComboBox.cellFactory = cellFactory

        countryComboBox.selectionModel.selectedItemProperty().addListener { _ ->
            paneUiData.country.uiValue = countryComboBox.selectionModel.selectedIndex
        }
        subscribe(CountryNotifier.COUNTRY_TOPIC, object : CountryNotifier {
            override fun accept(newCountryIndex: Int) {
                countryComboBox.selectionModel.select(newCountryIndex)
                startWorkingButton.isDisable = paneUiData.anyRequiredDataDefault()
            }
        })

        subscribe(CountryComparatorNotifier.COUNTRY_COMPARATOR_TOPIC, object : CountryComparatorNotifier {
            override fun accept(newComparator: Comparator<Country>) {
                countryComboBox.items = countryObservableList.sorted(newComparator)
            }
        })
    }

    private fun initProgrammingLanguage() {
        programmingLanguageComboBox.items = FXCollections.observableList(paneUiData.programmingLanguage.dataList.map { it.key })

        programmingLanguageComboBox.selectionModel.selectedItemProperty().addListener { _ ->
            paneUiData.programmingLanguage.uiValue = programmingLanguageComboBox.selectionModel.selectedIndex
        }
        subscribe(ProgrammingLanguageNotifier.PROGRAMMING_LANGUAGE_TOPIC, object : ProgrammingLanguageNotifier {
            override fun accept(newLanguageIndex: Int) {
                programmingLanguageComboBox.selectionModel.select(newLanguageIndex)
                startWorkingButton.isDisable = paneUiData.anyRequiredDataDefault()
            }
        })
    }

    private fun initStartWorkingButton() {
        startWorkingButton.onMouseClicked {
            ApplicationManager.getApplication().invokeLater {
                val surveyInfo: Map<String, String> = UiLoggedData.headers.zip(UiLoggedData.getData(Unit)).toMap()
                StoredInfoWrapper.updateStoredInfo(surveyInfo)
            }
            changeVisiblePane(TaskChoosingControllerManager)
        }
    }

    private fun makeTranslatable() {
        subscribe(LanguageNotifier.LANGUAGE_TOPIC, object : LanguageNotifier {
            override fun accept(newLanguageIndex: Int) {
                val newLanguage = LanguagePaneUiData.language.dataList[newLanguageIndex]
                val surveyPaneText = translations?.get(newLanguage)
                surveyPaneText?.let {
                    ageLabel.text = it.age
                    genderLabel.text = it.gender
                    experienceLabel.text = it.experience
                    peYearsLabel.text = it.years
                    peMonthsLabel.text = it.months
                    countryLabel.text = it.country
                    startWorkingText.text = it.startSession
//                  Todo: make programmingLanguageLabel translatable
                    paneUiData.country.dataListComparator =
                        compareBy { c -> c.translation.getOrDefault(newLanguage, "") }
                }
                genderRadioButtons.zip(paneUiData.gender.dataList) { rb, g ->
                    rb.text = g.translation[newLanguage] ?: ""
                }
            }
        })
    }
}
