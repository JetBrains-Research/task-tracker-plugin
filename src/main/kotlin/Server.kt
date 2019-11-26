import java.io.File
import java.util.logging.Logger

interface Server {
    fun getTasks() : List<String>

    fun sendTrackingData(file: File)
}

object DummyServer : Server {
    private val log: Logger = Logger.getLogger(javaClass.name)

    init {
        log.info("init server")
    }

    override fun sendTrackingData(file: File) {
        log.info("...sending file ${file.name}")
    }

    override fun getTasks(): List<String> {
        return arrayListOf("Задача 1", "Задача 2")
    }

}

