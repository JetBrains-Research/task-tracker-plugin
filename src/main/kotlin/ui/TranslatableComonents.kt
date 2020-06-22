package ui


import javafx.scene.control.*
import javax.annotation.processing.AbstractProcessor
import javax.annotation.processing.Processor
import javax.annotation.processing.RoundEnvironment
import javax.lang.model.element.TypeElement
import kotlin.reflect.KAnnotatedElement
import kotlin.reflect.full.declaredMemberProperties
import kotlin.reflect.full.findAnnotation
import kotlin.reflect.full.memberExtensionProperties
import kotlin.reflect.jvm.javaField

inline class Language(val key: String)

//@Retention(AnnotationRetention.RUNTIME)
//annotation class Translatable(val name: String)

interface ITranslatable {
    fun translate(language: Language)
}

abstract class TranslatableComponent(private val component: Any, val name: String) : ITranslatable {
    private val translations = TranslationManager.componentTranslations[name] ?: throw IllegalArgumentException("No translation for name $name found")
    init {
        TranslationManager.translatableComponents.add(this)
//        Todo: find a way to do it
        translate(TranslationManager.currentLanguage)
    }

    fun getTranslation(language: Language): String {
        return translations[language] ?: throw IllegalArgumentException("No translation for language $language found")
    }

//    override fun translate(language: Language) {
//        val translation = translations[language] ?: throw IllegalArgumentException("No translation for language $language found")
//        println("translate $translation")
//        when (component) {
//            // For Buttons, Labels and so on
//            is Labeled -> component.text = translation
//            // For TextFields, TextAreas and so on
//            is TextInputControl -> component.text = translation
//            else -> throw NotImplementedError("Translation for ${component::class} is not defined")
//        }
//    }
}

object TranslationManager {
    val availableLanguages = PluginServer.getAvailableLanguages()
    val componentTranslations = PluginServer.getComponentTranslations()
    val translatableComponents: MutableList<TranslatableComponent> = arrayListOf()



//    fun findTranslatableComponents(): List<TranslatableComponent> {
//        println("finding components")
//        return MainController.paneControllerManagers.map { it.paneControllerClass }.map { kclass ->
//            println(kclass)
//            kclass.declaredMemberProperties.map { it.javaField }.filter { it?.getAnnotation(Translatable::class.java) != null }.map { t ->
//                println(t!!.getAnnotation(Translatable::class.java).name)
//                TranslatableComponent(t, t.getAnnotation(Translatable::class.java).name)
//            }
//        }.flatten()
//    }

//        arrayListOf<TranslatableComponent>()
//        KlassIndex.getAnnotated(Translatable::class).map {
//        val name = (it as KAnnotatedElement).findAnnotation<Translatable>()!!.name
//        TranslatableComponent(it, name) }.toMutableList()
//        arrayListOf<TranslatableComponent>()

    var currentLanguage: Language = Language("ru")
        set(value) {
            translatableComponents.forEach { it.translate(value) }
        }
}


//@AutoService(Processor::class)
//class TranslatableProcessor : AbstractProcessor() {
//    private val translatableAnnotation = Translatable::class.java
//
//    override fun getSupportedAnnotationTypes(): MutableSet<String> {
//        return mutableSetOf(translatableAnnotation.name)
//    }
//
//    override fun process(annotations: MutableSet<out TypeElement>, roundEnv: RoundEnvironment): Boolean {
//        roundEnv.getElementsAnnotatedWith(translatableAnnotation).forEach {
//            TranslatableComponent(it, it.getAnnotation(translatableAnnotation).name)
//        }
//        return false
//    }
//}
//
//
//
//annotation class data.Example(val name: String)
//
//@AutoService(Processor::class)
//class ExampleProcessor : AbstractProcessor() {
//    private val exampleAnnotation = data.Example::class.java
//
//    override fun getSupportedAnnotationTypes(): MutableSet<String> {
//        return mutableSetOf(exampleAnnotation.name)
//    }
//
//    override fun process(annotations: MutableSet<out TypeElement>, roundEnv: RoundEnvironment): Boolean {
//        roundEnv.getElementsAnnotatedWith(exampleAnnotation).forEach {
//            println(it.getAnnotation(exampleAnnotation)?.name)
//        }
//        return false
//    }
//
//}