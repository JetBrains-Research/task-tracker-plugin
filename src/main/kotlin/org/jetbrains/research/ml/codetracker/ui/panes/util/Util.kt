package org.jetbrains.research.ml.codetracker.ui.panes.util

import com.intellij.openapi.application.ApplicationManager
import com.intellij.util.messages.Topic
import javafx.collections.ObservableList
import javafx.scene.control.Button
import javafx.scene.control.ComboBox
import javafx.scene.control.TextField
import javafx.scene.control.TextFormatter
import javafx.scene.input.MouseEvent
import javafx.util.StringConverter
import javafx.util.converter.IntegerStringConverter
import org.jetbrains.research.ml.codetracker.ui.MainController
import java.util.function.Consumer
import java.util.function.UnaryOperator

fun TextField.addIntegerFormatter(filter: (String?) -> Boolean) : StringConverter<Int> {
    val textFiledFilter: UnaryOperator<TextFormatter.Change?> = UnaryOperator label@ { change: TextFormatter.Change? ->
        val text: String? = change?.controlNewText
        if (filter(text)) {
            return@label change
        }
        null
    }
    this.textFormatter = TextFormatter(TextFormatter.IDENTITY_STRING_CONVERTER, "", textFiledFilter)
    return IntegerStringConverter()
}


fun regexFilter(regexPattern: String) : (String?) -> Boolean {
    return  { text: String? -> text != null && (text.isEmpty() || text.matches(Regex(regexPattern))) }
}

fun <T : Any, C : Consumer<T>> subscribe(topic: Topic<C>, notifier: C) {
    ApplicationManager.getApplication().messageBus.connect().subscribe(topic, notifier)
}

fun Button.switchPaneOnMouseClicked(newPaneControllerManager: PaneControllerManager<out PaneController>) {
    this.addEventHandler(MouseEvent.MOUSE_CLICKED) {
        MainController.visiblePane = newPaneControllerManager
    }
}

fun Button.onMouseClicked(action: () -> Unit) {
    this.addEventHandler(MouseEvent.MOUSE_CLICKED) {
        action()
    }
}

fun changeVisiblePane(newVisiblePane: PaneControllerManager<out PaneController>) {
    MainController.visiblePane = newVisiblePane
}


/**
 * Changes combobox items, considering the previously selected item
 */
fun <T>changeComboBoxItems(comboBox: ComboBox<T>, observableItems: ObservableList<T>, newItems: List<T>) {
    val selectedIndex = comboBox.selectionModel.selectedIndex
    observableItems.setAll(newItems)
    comboBox.selectionModel.select(selectedIndex)
}


