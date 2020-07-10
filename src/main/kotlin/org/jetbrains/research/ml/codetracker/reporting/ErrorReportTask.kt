package org.jetbrains.research.ml.codetracker.reporting

import com.intellij.openapi.diagnostic.SubmittedReportInfo
import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.openapi.progress.Task.Backgroundable
import com.intellij.openapi.project.Project
import com.intellij.util.Consumer

/**
 * Encapsulates the sending of feedback into a background task that is run by {@link GitHubErrorReporter}.
 */
class ErrorReportTask internal constructor(
    project: Project?,
    title: String,
    canBeCancelled: Boolean,
    errorInformation_: ErrorInformation,
    callback_: Consumer<SubmittedReportInfo>
) : Backgroundable(project, title, canBeCancelled) {
    private val callback: Consumer<SubmittedReportInfo> = callback_
    private val errorInformation: ErrorInformation = errorInformation_

    override fun run(indicator: ProgressIndicator) {
        indicator.isIndeterminate = true
        callback.consume(ErrorReport.sendFeedback(errorInformation))
    }
}