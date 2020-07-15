package org.jetbrains.research.ml.codetracker.ui

import javafx.beans.NamedArg
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
    var formattedText: String = formatter.format(text)
        set(value) {
            text = formatter.format(value)
            field = text
        }
}

class FormattedText(@NamedArg("formatter") private val formatter: Formatter<String>) : Text() {
    var formattedText: String = formatter.format(text)
        set(value) {
            text = formatter.format(value)
            field = text
        }
}

class FormattedRadioButton(@NamedArg("formatter") private val formatter: Formatter<String>) : RadioButton() {
    var formattedText: String = formatter.format(text)
        set(value) {
            text = formatter.format(value)
            field = text
        }
}

