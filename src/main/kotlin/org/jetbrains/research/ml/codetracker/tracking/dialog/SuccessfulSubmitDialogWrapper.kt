package org.jetbrains.research.ml.codetracker.tracking.dialog


import org.jetbrains.research.ml.codetracker.models.SuccessfulSubmitText
import org.jetbrains.research.ml.codetracker.models.Task
import org.jetbrains.research.ml.codetracker.server.PluginServer
import org.jetbrains.research.ml.codetracker.ui.panes.SurveyUiData
import java.awt.Dimension


class SuccessfulSubmitDialogWrapper(private val task: Task) : CustomDialogWrapper() {
    private val defaultSuccessfulSubmitDialogText = SuccessfulSubmitText(
        "Successful submit",
        "The data for the %s task has been submitted successfully."
    )
    override val customPreferredSize: Dimension? = Dimension(400, 100)

    override fun createMessage(): String {
        val dialogText = PluginServer.successfulSubmitDialogText?.translation?.get(currentLanguage)?.description
            ?: defaultSuccessfulSubmitDialogText.description
        return "<html>${java.lang.String.format(
            dialogText,
            task.infoTranslation[currentLanguage]?.name)}</html>"
    }

    init {
        init()
        val currentLanguage = SurveyUiData.language.currentValue
        title = PluginServer.successfulSubmitDialogText?.translation?.get(currentLanguage)?.header
            ?: defaultSuccessfulSubmitDialogText.header
    }
}