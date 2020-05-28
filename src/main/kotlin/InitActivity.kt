import com.intellij.openapi.Disposable
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.project.Project
import com.intellij.openapi.startup.StartupActivity
import com.intellij.openapi.util.Disposer
import ui.ServerDialogWrapper


class InitActivity : StartupActivity {
    private val diagnosticLogger: Logger = Logger.getInstance(javaClass)

    init {
        diagnosticLogger.info("${Plugin.PLUGIN_ID}: startup activity")
        Disposer.register(
            ApplicationManager.getApplication(),
            Disposable {
                diagnosticLogger.info("${Plugin.PLUGIN_ID}: dispose startup activity")
                if(!Plugin.stopTracking()){
//                    Todo: don't run it there....
                    ApplicationManager.getApplication().invokeAndWait {

                        ServerDialogWrapper().show()
                    }
                }
            })
        Plugin.startTracking()
    }

    override fun runActivity(project: Project) {
        diagnosticLogger.info("${Plugin.PLUGIN_ID}: run activity")
    }

}
