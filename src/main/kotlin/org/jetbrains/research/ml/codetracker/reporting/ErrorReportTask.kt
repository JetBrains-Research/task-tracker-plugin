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
    private val errorInformation: ErrorInformation,
    private val callback: Consumer<SubmittedReportInfo>
) : Backgroundable(project, title, canBeCancelled) {

    override fun run(indicator: ProgressIndicator) {
        indicator.isIndeterminate = true
        callback.consume(ErrorReport.sendFeedback(errorInformation))
    }
}