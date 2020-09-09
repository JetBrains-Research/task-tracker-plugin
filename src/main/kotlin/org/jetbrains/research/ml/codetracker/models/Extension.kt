package org.jetbrains.research.ml.codetracker.models

enum class Language(val extension: Extension) {
    PYTHON(Extension.PY),
    JAVA(Extension.JAVA),
    KOTLIN(Extension.KT),
    CPP(Extension.CPP);
}

enum class Extension(val ext: String) {
    PY(".py"),
    JAVA(".java"),
    KT(".kt"),
    CPP(".cpp"),
    CSV(".csv");
}