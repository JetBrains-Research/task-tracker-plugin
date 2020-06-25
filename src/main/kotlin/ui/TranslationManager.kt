package ui


import javafx.scene.control.*
import javafx.scene.text.Text

inline class Language(val key: String)

interface ITranslatable {
    fun translate(language: Language)
}

fun Labeled.makeTranslatable(name: String, translate: (Language) -> Unit = { this.text = TranslationManager.getTranslation(name, it) }) {
    TranslationManager.addTranslatableComponent(translate)
}

fun Text.makeTranslatable(name: String, translate: (Language) -> Unit = { this.text = TranslationManager.getTranslation(name, it) }) {
    TranslationManager.addTranslatableComponent(translate)
}


object TranslationManager {
    //    Todo: get from server
    val availableLanguages = PluginServer.getAvailableLanguages()
//    Todo: split by panes and make consistent with model?
    private val componentTranslations: HashMap<String, HashMap<Language, String>> = hashMapOf(
//    profile pane
        "ageLabel" to hashMapOf(Language("ru") to "возраст", Language("eng") to "age"),
        "genderLabel" to hashMapOf(Language("ru") to "пол", Language("eng") to "gender"),
        "experienceLabel" to hashMapOf(Language("ru") to "опыт программирования", Language("eng") to "program experience"),
        "countryLabel" to hashMapOf(Language("ru") to "страна", Language("eng") to "country"),
        "startWorkingText" to hashMapOf(Language("ru") to "начать работу", Language("eng") to "start working"),
//    task chooser pane
        "choseTaskLabel" to hashMapOf(Language("ru") to "выбрать задачу", Language("eng") to "chose task"),
        "startSolvingText" to hashMapOf(Language("ru") to "начать решать", Language("eng") to "start solving"),
        "finishWorkText" to hashMapOf(Language("ru") to "закончить работу", Language("eng") to "finish working"),
//    task pane
        "inputLabel" to hashMapOf(Language("ru") to "входные данные", Language("eng") to "input"),
        "outputLabel" to hashMapOf(Language("ru") to "выходные данные", Language("eng") to "output"),
        "sendSolutionText" to hashMapOf(Language("ru") to "отправить решение", Language("eng") to "submit solution"),
        "name1:taskNameText" to hashMapOf(Language("ru") to "имя 1", Language("end") to "name 1"),
        "name1:taskDescriptionText" to hashMapOf(Language("ru") to "описание задачи 1", Language("end") to "description of task 1"),
        "name1:taskInputText" to hashMapOf(Language("ru") to "входные данные для задачи 1 такие-то", Language("end") to "input data for task 1 should be..."),
        "name1:taskOutputText" to hashMapOf(Language("ru") to "выходные данные для задачи 1 такие-то", Language("end") to "output data for task 1 should be..."),
        "name2:taskNameText" to hashMapOf(Language("ru") to "имя 2", Language("end") to "name 2"),
        "name2:taskDescriptionText" to hashMapOf(Language("ru") to "описание задачи 2", Language("end") to "description of task 2"),
        "name2:taskInputText" to hashMapOf(Language("ru") to "входные данные для задачи 2 такие-то", Language("end") to "input data for task 2 should be..."),
        "name2:taskOutputText" to hashMapOf(Language("ru") to "выходные данные для задачи 2 такие-то", Language("end") to "output data for task 1 should be..."),

//        "backToTasksText" to hashMapOf(Language("ru") to "вернуться", Language("eng") to "chose task")
//    finish pane
        "backToTasksText" to hashMapOf(Language("ru") to "вернуться к задачам", Language("eng") to "back to tasks"),
        "backToProfileText" to hashMapOf(Language("ru") to "вернуться к анкете", Language("eng") to "back to profile"),
        "greatWorkLabel" to hashMapOf(Language("ru") to "отличная работа!", Language("eng") to "great work!"),
        "messageText" to hashMapOf(Language("ru") to "не забудьте выключить плагин бла бла", Language("eng") to "don't forget to uninstall plugin blah blah")

    )

    private val translatableComponents: MutableList<ITranslatable> = arrayListOf()

    var currentLanguageIndex: Int = 0
        set(value) {
            if (value != field && value < availableLanguages.size) {
                translatableComponents.forEach { it.translate(availableLanguages[value]) }
                field = value
            }
        }

    fun getTranslation(name: String, language: Language): String {
        return componentTranslations[name]?.get(language) ?: throw IllegalArgumentException("No translation for name $name and language $language found")
    }

    fun addTranslatableComponent(translate: (Language) -> Unit) {
        translatableComponents.add(object : ITranslatable {
            override fun translate(language: Language) {
                translate(language)
            }
        })
    }
}
