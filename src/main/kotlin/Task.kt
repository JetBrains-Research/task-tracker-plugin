import com.intellij.openapi.diagnostic.Logger
import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.list
import kotlinx.serialization.json.*


//data class Task(var key: String, var name: String) {
//    private val diagnosticLogger: Logger = Logger.getInstance(javaClass)
//
//    init {
//        diagnosticLogger.info("${Plugin.PLUGIN_ID}: Task ${key} initialized getting")
//    }
//
//    override fun toString(): String {
//        return "Task(key='$key', name='$name')"
//    }
//}

@Serializable
data class Example(val input: String = "", val output: String = "")

@Serializable
data class Task(val key: String,
                val name: String,
                val description: String = "Нет описания",
                val input: String = "",
                val output: String = "",
                val id: Int = -1,
                @Serializable val example_1: Example = Example(),
                @Serializable val example_2: Example = Example(),
                @Serializable val example_3: Example = Example())
