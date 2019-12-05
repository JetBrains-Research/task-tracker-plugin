import com.intellij.openapi.project.Project
import com.intellij.openapi.startup.StartupActivity
import com.intellij.openapi.diagnostic.Logger


class InitActivity : StartupActivity {
    private val diagnosticLogger: Logger = Logger.getInstance(javaClass)

    init {

        diagnosticLogger.info("${Plugin.PLUGIN_ID}: startup activity")
        Plugin.startTracking()
    }

    override fun runActivity(project: Project) {
        diagnosticLogger.info("${Plugin.PLUGIN_ID}: run activity")
        Plugin.addProjectManagerListener(project)
    }
}