package org.jetbrains.research.ml.codetracker.ui.panes.util

import javafx.beans.NamedArg
import javafx.scene.control.Label
import javafx.scene.control.Labeled
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


/**
 * Several quite similar classes looks strange but it's tha only way to extend
 * existing classes and makes them formatted.
 * It's possible not to make additional classes and call textProperty().addListener
 * directly while initializing component in Controller, but since we had a lot of
 * formatted labels/buttons/texts it's a little annoying. So it's better to move it
 * to the .fxml files, which requires class extension.
 */


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

