package org.jetbrains.research.ml.codetracker.ui.panes.util

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

/**
 * Represents pane data, which [uiValue] is one of the [dataList] items,
 * so it can be thought of as an item index with type [Int]. If needed, an additional [listNotifierTopic] can be passed,
 * so when [dataList] is changed, it notifies all subscribers.
 */
open class ListedUiField<T: Any?>(private val defaultDataList: List<T>,
                                  defaultValue: Int,
                                  valueNotifierTopic: Topic<out Consumer<Int>>,
                                  private val listNotifierTopic: Topic<out Consumer<List<T>>>? = null,
                                  isRequired: Boolean = true) : UiField<Int>(defaultValue, valueNotifierTopic, isRequired) {

    override var uiValue: Int by Delegates.observable(defaultUiValue) { _, old, new ->
        if (old != new && isValid(new)) {
            changeUiValue(new)
        }
    }

    var dataList: List<T> by Delegates.observable(defaultDataList) { _, old, new ->
        if (old != new) {
            changeDataList(new)
        }
    }

    inline fun <R : Comparable<R>> sortDataListBy(crossinline order: (T) -> R) {
        val currentValue = currentValue
        val sortedList = dataList.sortedBy { order(it) }
        val newUiValue = sortedList.indexOf(currentValue)
        dataList = sortedList
        uiValue = newUiValue
    }

    private fun changeDataList(new: List<T>) {
        listNotifierTopic?.let {
            val publisher = ApplicationManager.getApplication().messageBus.syncPublisher(listNotifierTopic)
            publisher.accept(new)
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
        return getData().any { it.isRequired && it.isUiValueDefault }
    }

    fun updateUiData() {
        getData().forEach { it.updateUiValue() }
    }
}