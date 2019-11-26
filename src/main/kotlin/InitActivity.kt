import com.intellij.openapi.project.Project
import com.intellij.openapi.startup.StartupActivity
import java.util.logging.Logger


class InitActivity : StartupActivity {
    private val log: Logger = Logger.getLogger(javaClass.name)

    init {
        log.info("startup activity")
    }

    override fun runActivity(project: Project) {
        log.info("run activity")
        Plugin.addProjectManagerListener(project)
        Plugin.startTracking(project)
    }
}