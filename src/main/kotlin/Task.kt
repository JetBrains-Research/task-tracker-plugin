import java.util.logging.Logger

data class Task(var key: String, var name: String) {
    private val log: Logger = Logger.getLogger(javaClass.name)

    init {
        log.info("Task ${key} initialized getting")
    }

    override fun toString(): String {
        return "Task(key='$key', name='$name')"
    }
}