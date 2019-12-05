import com.intellij.openapi.diagnostic.Logger

data class Task(var key: String, var name: String) {
    private val diagnosticLogger: Logger = Logger.getInstance(javaClass)

    init {
        diagnosticLogger.info("${Plugin.PLUGIN_ID}: Task ${key} initialized getting")
    }

    override fun toString(): String {
        return "Task(key='$key', name='$name')"
    }
}