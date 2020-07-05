package org.jetbrains.research.ml.codetracker.ui


import javafx.scene.control.*
import javafx.scene.text.Text
import org.jetbrains.research.ml.codetracker.models.PaneLanguage
import org.jetbrains.research.ml.codetracker.server.PluginServer

interface ITranslatable {
    fun translate(language: PaneLanguage)
}

fun Any?.makeTranslatable(translate: (PaneLanguage) -> Unit) {
    TranslationManager.addTranslatableObject(translate)
}

object TranslationManager {
    val availableLanguages = PluginServer.availableLanguages
    private val translatableObjects: MutableList<ITranslatable> = arrayListOf()

    var currentLanguageIndex: Int = 0
        private set(value) {
            if (value != field && value in availableLanguages.indices) {
                translatableObjects.forEach { it.translate(availableLanguages[value]) }
                field = value
            }
        }

    fun addTranslatableObject(translate: (PaneLanguage) -> Unit) {
        val translatableObject = object :  ITranslatable {
            override fun translate(language: PaneLanguage) {
                translate(language)
            }
        }
        translatableObject.translate(availableLanguages[currentLanguageIndex])
        translatableObjects.add(translatableObject)
    }

    fun switchLanguage(newLanguage: Int) {
        currentLanguageIndex = newLanguage
        MainController.paneControllerManagers.forEach { it.switchUILanguage(currentLanguageIndex) }
    }
}
