package org.jetbrains.research.ml.codetracker.reporting

import com.intellij.diagnostic.IdeaReportingEvent
import com.intellij.diagnostic.ReportMessages
import com.intellij.ide.DataManager
import com.intellij.idea.IdeaLogger
import com.intellij.notification.NotificationListener
import com.intellij.notification.NotificationType
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.application.ApplicationInfo
import com.intellij.openapi.application.ApplicationNamesInfo
import com.intellij.openapi.application.PathManager
import com.intellij.openapi.application.ex.ApplicationInfoEx
import com.intellij.openapi.diagnostic.ErrorReportSubmitter
import com.intellij.openapi.diagnostic.IdeaLoggingEvent
import com.intellij.openapi.diagnostic.SubmittedReportInfo
import com.intellij.openapi.progress.EmptyProgressIndicator
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.project.Project
import com.intellij.util.Consumer
import java.awt.Component

class GitHubErrorReporter : ErrorReportSubmitter() {
    override fun submit(
        events: Array<IdeaLoggingEvent>, additionalInfo: String?, parentComponent: Component,
        consumer: Consumer<SubmittedReportInfo>
    ): Boolean {
        val errorReportInformation = ErrorInformation(
            IdeaLogger.ourLastActionId,
            ApplicationInfo.getInstance() as ApplicationInfoEx,
            ApplicationNamesInfo.getInstance(),
            events[0].throwable
        )
        return doSubmit(
            events[0],
            parentComponent,
            consumer,
            errorReportInformation
        )
    }

    override fun getReportActionText(): String {
        return "Report to Codetracker Github Issue Tracker"
    }

    /**
     * Provides functionality to show an error report message to the user that gives a clickable link to the created issue.
     */
    internal class CallbackWithNotification(
        private val originalConsumer: Consumer<SubmittedReportInfo>,
        private val project: Project?
    ) :
        Consumer<SubmittedReportInfo> {
        override fun consume(reportInfo: SubmittedReportInfo) {
            originalConsumer.consume(reportInfo)
            val notificationType =
                if (reportInfo.status == SubmittedReportInfo.SubmissionStatus.FAILED) {
                    NotificationType.ERROR
                } else {
                    NotificationType.INFORMATION
                }
            ReportMessages.GROUP.createNotification(
                ReportMessages.ERROR_REPORT,
                reportInfo.linkText,
                notificationType,
                NotificationListener.URL_OPENING_LISTENER
            ).setImportant(false).notify(project)
        }

    }

    private fun doSubmit(
        event: IdeaLoggingEvent,
        parentComponent: Component,
        callback: Consumer<SubmittedReportInfo>,
        errorInformation: ErrorInformation
    ): Boolean {

        if (event is IdeaReportingEvent) {
            event.plugin?.let {
                errorInformation.setUserInformation(UserInformationType.PLUGIN_NAME, it.name)
                errorInformation.setUserInformation(UserInformationType.PLUGIN_VERSION, it.version)
            }
        }
        val dataContext = DataManager.getInstance().getDataContext(parentComponent)
        val project = CommonDataKeys.PROJECT.getData(dataContext)
        val task = ErrorReportTask(
            project,
            "Submitting error report",
            true,
            errorInformation,
            CallbackWithNotification(callback, project)
        )
        project?.let { ProgressManager.getInstance().run(task) } ?: run {
            task.run(EmptyProgressIndicator())
        }
        return true
    }
}