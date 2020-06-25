package org.jetbrains.research.ml.codetracker.ui


import javafx.scene.control.*
import javafx.scene.text.Text
import org.jetbrains.research.ml.codetracker.models.PaneLanguage

interface ITranslatable {
    fun translate(language: PaneLanguage)
}

fun Labeled.makeTranslatable(name: String, translate: (PaneLanguage) -> Unit = { this.text =
    TranslationManager.getTranslation(name, it)
}) {
    TranslationManager.addTranslatableComponent(translate)
}

fun Labeled.makeTranslatable(translate: (PaneLanguage) -> Unit) {
    TranslationManager.addTranslatableComponent(translate)
}

fun Text.makeTranslatable(name: String, translate: (PaneLanguage) -> Unit = { this.text =
    TranslationManager.getTranslation(name, it)
}) {
    TranslationManager.addTranslatableComponent(translate)
}


object TranslationManager {
    //    Todo: get from server
    val availableLanguages = listOf(PaneLanguage("ru"), PaneLanguage("en"))
//    Todo: split by panes and make consistent with model?
    private val componentTranslations: HashMap<String, HashMap<PaneLanguage, String>> = hashMapOf(
//    profile pane
        "ageLabel" to hashMapOf(
            PaneLanguage("ru") to "возраст", PaneLanguage(
                "en"
            ) to "age"),
        "genderLabel" to hashMapOf(
            PaneLanguage("ru") to "пол", PaneLanguage(
                "en"
            ) to "gender"),
        "experienceLabel" to hashMapOf(
            PaneLanguage("ru") to "опыт программирования", PaneLanguage(
                "en"
            ) to "program experience"),
        "countryLabel" to hashMapOf(
            PaneLanguage("ru") to "страна", PaneLanguage(
                "en"
            ) to "country"),
        "startWorkingText" to hashMapOf(
            PaneLanguage("ru") to "начать работу", PaneLanguage(
                "en"
            ) to "start working"),
//    task chooser pane
        "choseTaskLabel" to hashMapOf(
            PaneLanguage("ru") to "выбрать задачу", PaneLanguage(
                "en"
            ) to "chose task"),
        "startSolvingText" to hashMapOf(
            PaneLanguage("ru") to "начать решать", PaneLanguage(
                "en"
            ) to "start solving"),
        "finishWorkText" to hashMapOf(
            PaneLanguage("ru") to "закончить работу", PaneLanguage(
                "en"
            ) to "finish working"),
//    task pane
        "inputLabel" to hashMapOf(
            PaneLanguage("ru") to "входные данные", PaneLanguage(
                "en"
            ) to "input"),
        "outputLabel" to hashMapOf(
            PaneLanguage("ru") to "выходные данные", PaneLanguage(
                "en"
            ) to "output"),
        "sendSolutionText" to hashMapOf(
            PaneLanguage("ru") to "отправить решение", PaneLanguage(
                "en"
            ) to "submit solution"),
        "name1:taskNameText" to hashMapOf(
            PaneLanguage("ru") to "имя 1", PaneLanguage(
                "end"
            ) to "name 1"),
        "name1:taskDescriptionText" to hashMapOf(
            PaneLanguage("ru") to "описание задачи 1", PaneLanguage(
                "end"
            ) to "description of task 1"),
        "name1:taskInputText" to hashMapOf(
            PaneLanguage("ru") to "входные данные для задачи 1 такие-то", PaneLanguage(
                "end"
            ) to "input data for task 1 should be..."),
        "name1:taskOutputText" to hashMapOf(
            PaneLanguage("ru") to "выходные данные для задачи 1 такие-то", PaneLanguage(
                "end"
            ) to "output data for task 1 should be..."),
        "name2:taskNameText" to hashMapOf(
            PaneLanguage("ru") to "имя 2", PaneLanguage(
                "end"
            ) to "name 2"),
        "name2:taskDescriptionText" to hashMapOf(
            PaneLanguage("ru") to "описание задачи 2", PaneLanguage(
                "end"
            ) to "description of task 2"),
        "name2:taskInputText" to hashMapOf(
            PaneLanguage("ru") to "входные данные для задачи 2 такие-то", PaneLanguage(
                "end"
            ) to "input data for task 2 should be..."),
        "name2:taskOutputText" to hashMapOf(
            PaneLanguage("ru") to "выходные данные для задачи 2 такие-то", PaneLanguage(
                "end"
            ) to "output data for task 1 should be..."),

//        "backToTasksText" to hashMapOf(Language("ru") to "вернуться", Language("en") to "chose task")
//    finish pane
        "backToTasksText" to hashMapOf(
            PaneLanguage("ru") to "вернуться к задачам", PaneLanguage(
                "en"
            ) to "back to tasks"),
        "backToProfileText" to hashMapOf(
            PaneLanguage("ru") to "вернуться к анкете", PaneLanguage(
                "en"
            ) to "back to profile"),
        "greatWorkLabel" to hashMapOf(
            PaneLanguage("ru") to "отличная работа!", PaneLanguage(
                "en"
            ) to "great work!"),
        "messageText" to hashMapOf(
            PaneLanguage("ru") to "не забудьте выключить плагин бла бла", PaneLanguage(
                "en"
            ) to "don't forget to uninstall plugin blah blah")

    )

    private val translatableComponents: MutableList<ITranslatable> = arrayListOf()

    var currentLanguageIndex: Int = 0
        set(value) {
            if (value != field && value < availableLanguages.size) {
                translatableComponents.forEach { it.translate(
                    availableLanguages[value]) }
                field = value
            }
        }

    fun getTranslation(name: String, language: PaneLanguage): String {
        return componentTranslations[name]?.get(language) ?: throw IllegalArgumentException("No translation for name $name and language $language found")
    }

    fun addTranslatableComponent(translate: (PaneLanguage) -> Unit) {
        translatableComponents.add(object :
            ITranslatable {
            override fun translate(language: PaneLanguage) {
                translate(language)
            }
        })
    }
}
