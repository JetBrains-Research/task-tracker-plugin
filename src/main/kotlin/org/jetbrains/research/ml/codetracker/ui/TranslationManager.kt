package org.jetbrains.research.ml.codetracker.ui


import javafx.scene.control.*
import javafx.scene.text.Text
import org.jetbrains.research.ml.codetracker.models.PaneLanguage

interface ITranslatable {
    fun translate(language: PaneLanguage)
}

fun Labeled.makeTranslatable(translate: (PaneLanguage) -> Unit) {
    TranslationManager.addTranslatableComponent(translate)
}

fun Text.makeTranslatable(translate: (PaneLanguage) -> Unit) {
    TranslationManager.addTranslatableComponent(translate)
}


object TranslationManager {
    //    Todo: get from server
    val availableLanguages = listOf(PaneLanguage("ru"), PaneLanguage("en"))
    private val translatableObjects: MutableList<ITranslatable> = arrayListOf()

    var currentLanguageIndex: Int = 0
        set(value) {
            if (value != field && value < availableLanguages.size) {
                translatableObjects.forEach { it.translate(availableLanguages[value]) }
                field = value
            }
        }

    fun addTranslatableComponent(translate: (PaneLanguage) -> Unit) {
        translatableObjects.add(object :
            ITranslatable {
            override fun translate(language: PaneLanguage) {
                translate(language)
            }
        })
    }
}
