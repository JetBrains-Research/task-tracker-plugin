package models

enum class Language {
    PYTHON, JAVA, KOTLIN;

    fun getExtensionByLanguage(): Extension {
        return when (this) {
            PYTHON -> Extension.PY
            JAVA -> Extension.JAVA
            KOTLIN -> Extension.KT
        }
    }
}

enum class Extension(val ext: String){
    PY(".py"),
    JAVA(".java"),
    KT(".kt"),
    CSV(".csv");
}