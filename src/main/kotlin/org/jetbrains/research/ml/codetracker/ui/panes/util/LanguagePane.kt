package org.jetbrains.research.ml.codetracker.ui.panes.util

import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.project.Project
import com.intellij.util.messages.Topic
import javafx.collections.FXCollections
import javafx.embed.swing.JFXPanel
import javafx.fxml.FXML
import javafx.geometry.Insets
import javafx.geometry.Pos
import javafx.scene.control.ComboBox
import javafx.scene.control.ListCell
import javafx.scene.control.ListView
import javafx.util.Callback
import org.jetbrains.research.ml.codetracker.Plugin
import org.jetbrains.research.ml.codetracker.server.PluginServer
import java.net.URL
import java.util.*
import java.util.function.Consumer


/**
 * For panes with language combo box
 */
interface LanguageNotifier : Consumer<Int> {
    companion object {
        val LANGUAGE_TOPIC = Topic.create("pane language change", LanguageNotifier::class.java)
    }
}

open class LanguagePaneUiData : PaneUiData() {
    companion object {
        val language = ListedUiField(
            PluginServer.availableLanguages, 0,
            LanguageNotifier.LANGUAGE_TOPIC, isRequired = false
        )
    }

    override fun getData(): List<UiField<*>> = arrayListOf(language)
}

open class LanguagePaneController(project: Project, scale: Double, fxPanel: JFXPanel, id: Int) : PaneController(
    project,
    scale,
    fxPanel,
    id
),
    Updatable {
    @FXML
    private lateinit var languageComboBox: ComboBox<String>
    protected val logger = Logger.getInstance(javaClass)
    protected open val paneUiData: LanguagePaneUiData = LanguagePaneUiData()

    override fun initialize(url: URL?, resource: ResourceBundle?) {
        initLanguageComboBox()
    }

    override fun update() {
        logger.info("${Plugin.PLUGIN_ID} update controller ${this::javaClass.name}, current thread is ${Thread.currentThread().name}")
        paneUiData.updateUiData()
    }

    private fun initLanguageComboBox() {
        languageComboBox.items = FXCollections.observableList(LanguagePaneUiData.language.dataList.map { it.key })
        languageComboBox.selectionModel.selectedItemProperty().addListener { _ ->
            LanguagePaneUiData.language.uiValue = languageComboBox.selectionModel.selectedIndex
        }
        subscribe(LanguageNotifier.LANGUAGE_TOPIC, object : LanguageNotifier {
            override fun accept(newLanguageIndex: Int) {
                languageComboBox.selectionModel.select(newLanguageIndex)
            }
        })
    }
}