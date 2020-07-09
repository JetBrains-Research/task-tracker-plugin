package org.jetbrains.research.ml.codetracker.ui

import com.intellij.openapi.application.ApplicationManager
import com.intellij.util.messages.Topic
import java.util.function.Consumer
import kotlin.properties.Delegates



/**
 * Represents pane data with [uiValue], that triggers notifier when it changes.
 * When it's required, user has to change it to be differ from [defaultUiValue], for example fill out age field in ProfilePane
 */
open class UiField <T : Any?> (val defaultUiValue: T, val notifierTopic: Topic<out Consumer<T>>, var isRequired: Boolean = true) {
    /**
     * We need additional field for checking, is [uiValue] default, because it may be in process of changing so we cannot
     * just compare [uiValue] with [defaultUiValue]
     */
    var isUiValueDefault: Boolean = true
        protected set

    open var uiValue: T by Delegates.observable(defaultUiValue) { _, old, new ->
        if (old != new) {
            changeUiValue(new)
        }
    }

    protected fun changeUiValue(new: T) {
        isUiValueDefault = new == defaultUiValue
        val publisher = ApplicationManager.getApplication().messageBus.syncPublisher(notifierTopic)
        publisher.accept(new)
    }

    fun updateUiValue() {
        changeUiValue(uiValue)
    }

}

//    Todo: add some sorting? (alphabetically or by solved status) and use it in ComboBoxes
/**
 * Represents pane data, which [uiValue] is one of the [dataList] items,
 * so it can be thought of as an item index with type [Int].
 */
open class ListedUiField<T: Any?>(
    val dataList: List<T>, defaultValue: Int, notifierTopic: Topic<out Consumer<Int>>,
    isRequired: Boolean = true) : UiField<Int>(defaultValue, notifierTopic, isRequired) {

    override var uiValue: Int by Delegates.observable(defaultUiValue) { _, old, new ->
        if (old != new && isValid(new)) {
            changeUiValue(new)
        }
    }

    fun isValid(uiValue: Int) : Boolean {
        return uiValue in dataList.indices
    }

    val currentValue: T?
        get() {
            return if (isValid(uiValue)) {
                dataList[uiValue]
            } else {
                null
            }
        }

    override fun toString(): String {
        return currentValue?.toString() ?: "undefined"
    }
}

/**
 * Shown on the *pane* data that may change by user actions (for example, selecting from ComboBoxes or ToggleGroups).
 */
abstract class PaneUiData {
    abstract fun getData(): List<UiField<*>>

    /**
     * Return if any data, that is required, is still default
     */
    fun anyRequiredDataDefault() : Boolean {
        println("${this::class.simpleName}:anyDataRequiredAndDefault ${Thread.currentThread().name}")
        println("${this::class.simpleName} anyRequiredDataDefault ${getData().map { "${it.uiValue} ${it.defaultUiValue} ${it.isUiValueDefault} ${it.isRequired}" } }")
        return getData().any { it.isRequired && it.isUiValueDefault }
    }

    fun updateUiData() {
        getData().forEach { it.updateUiValue() }
    }
}