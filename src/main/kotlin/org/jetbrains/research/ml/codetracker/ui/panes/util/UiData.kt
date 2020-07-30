package org.jetbrains.research.ml.codetracker.ui.panes.util

import com.intellij.openapi.application.ApplicationManager
import com.intellij.util.messages.Topic
import org.jetbrains.research.ml.codetracker.models.Keyed
import org.jetbrains.research.ml.codetracker.tracking.UiLoggedData
import org.jetbrains.research.ml.codetracker.ui.panes.SurveyUiData
import java.util.function.Consumer
import kotlin.properties.Delegates


/**
 * Represents pane data with [uiValue], that triggers notifier when it changes.
 * When it's required, user has to change it to be differ from [defaultUiValue], for example fill out age field in ProfilePane
 */
open class UiField <T> (val defaultUiValue: T, private val notifierTopic: Topic<out Consumer<T>>, var isRequired: Boolean = true) {
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
 * so it can be thought of as an item index with type [Int]. If needed, an additional [comparatorNotifierTopic] can be passed,
 * so when [dataListComparator] is changed, it notifies all subscribers.
 */
open class ListedUiField<T : Keyed>(
    private val data: List<T>,
    defaultValue: Int,
    valueNotifierTopic: Topic<out Consumer<Int>>,
    defaultComparator: Comparator<T>? = null,
    private val comparatorNotifierTopic: Topic<out Consumer<Comparator<T>>>? = null,
    isRequired: Boolean = true) : UiField<Int>(defaultValue, valueNotifierTopic, isRequired) {

    override var uiValue: Int by Delegates.observable(defaultUiValue) { _, old, new ->
        if (old != new) {
            changeUiValue(new)
        }
    }

    var dataList: List<T> = safeSortWith(defaultComparator)
        private set

    var dataListComparator: Comparator<T>? by Delegates.observable(defaultComparator) { _, old, new ->
        if (old != new && new != null) {
            dataList = safeSortWith(new)
            changeComparator(new)
        }
    }

    private fun safeSortWith(comparator: Comparator<T>?): List<T> {
        return comparator?.let { data.sortedWith(comparator) } ?: data
    }

    private fun changeComparator(newComparator: Comparator<T>) {
        comparatorNotifierTopic?.let {
            val publisher = ApplicationManager.getApplication().messageBus.syncPublisher(comparatorNotifierTopic)
            publisher.accept(newComparator)
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
        return currentValue?.key ?: defaultUiValue.toString()
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