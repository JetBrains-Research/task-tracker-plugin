package org.jetbrains.research.ml.codetracker.ui

import javafx.beans.NamedArg
import javafx.beans.property.SimpleStringProperty
import javafx.beans.value.ChangeListener
import javafx.beans.value.ObservableValue
import javafx.scene.control.Label
import javafx.scene.control.RadioButton
import javafx.scene.text.Text


interface Formatter<T> {
    fun format(t: T): T
}


class LowerCaseFormatter : Formatter<String> {
    override fun format(t: String): String {
        return t.toLowerCase()
    }
}

class LowerCaseWithColonFormatter : Formatter<String> {
    override fun format(t: String): String {
//  Drop all ':' to be sure there will be only one ':'
        val noColonsString = t.dropLastWhile { it == ':' }
        return "${noColonsString.toLowerCase()}:"
    }
}

class CapitalCaseFormatter : Formatter<String> {
    override fun format(t: String): String {
        return t.capitalize()
    }

}


class FormattedLabel(@NamedArg("formatter") private val formatter: Formatter<String>) : Label() {
    init {
        textProperty().addListener { _, _, new -> text = formatter.format(new) }
    }
}

class FormattedText(@NamedArg("formatter") private val formatter: Formatter<String>) : Text() {
    init {
        textProperty().addListener { _, _, new -> text = formatter.format(new) }
    }
}

class FormattedRadioButton(@NamedArg("formatter") private val formatter: Formatter<String>) : RadioButton() {
    init {
        textProperty().addListener { _, _, new -> text = formatter.format(new) }
    }
}

