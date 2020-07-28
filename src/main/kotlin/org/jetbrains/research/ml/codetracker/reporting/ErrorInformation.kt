package org.jetbrains.research.ml.codetracker.reporting

import com.intellij.openapi.application.ApplicationNamesInfo
import com.intellij.openapi.application.PermanentInstallationID
import com.intellij.openapi.application.ex.ApplicationInfoEx
import com.intellij.openapi.util.SystemInfo
import com.intellij.util.ExceptionUtil

class ErrorInformation(
    lastAction: String,
    appInfo: ApplicationInfoEx,
    namesInfo: ApplicationNamesInfo,
    throwable: Throwable?) {

    private val userInformation: MutableMap<UserInformationType, String> = mutableMapOf(
        UserInformationType.PLUGIN_NAME to "",
        UserInformationType.PLUGIN_VERSION to "",

        UserInformationType.OS_NAME to SystemInfo.OS_NAME,
        UserInformationType.OS_VERSION to SystemInfo.OS_VERSION,
        UserInformationType.JAVA_VERSION to SystemInfo.JAVA_VERSION,

        UserInformationType.APP_NAME to namesInfo.productName,
        UserInformationType.APP_FULL_NAME to namesInfo.fullProductName,

        UserInformationType.APP_VERSION_NAME to appInfo.versionName,
        UserInformationType.APP_BUILD to appInfo.build.asString(),
        UserInformationType.APP_VERSION to appInfo.fullVersion,

        UserInformationType.LAST_ACTION to lastAction,

        UserInformationType.PERMANENT_INSTALLATION_ID to PermanentInstallationID.get()
    )

    private val errorInformation: MutableMap<ErrorInformationType, String> = mutableMapOf(
        ErrorInformationType.ERROR_STACKTRACE to (throwable?.let { ExceptionUtil.getThrowableText(it) }
            ?: "Invalid stacktrace"),
        ErrorInformationType.ERROR_MESSAGE to (throwable?.message ?: "Unspecified error")
    )

    fun getUserInformation(userInformationType: UserInformationType): String? {
        return userInformation[userInformationType]
    }

    fun setUserInformation(userInformationType: UserInformationType, value: String) {
        userInformation[userInformationType] = value
    }

    fun getErrorInformation(errorInformationType: ErrorInformationType): String? {
        return errorInformation[errorInformationType]
    }

}

enum class UserInformationType(val readableValue: String) {
    PLUGIN_NAME("Plugin Name"),
    PLUGIN_VERSION("Plugin Version"),

    OS_NAME("OS Name"),
    OS_VERSION("OS Version"),

    JAVA_VERSION("Java Version"),

    APP_NAME("App Name"),
    APP_FULL_NAME("App Full Name"),
    APP_VERSION_NAME("App Version Name"),

    APP_BUILD("App Build"),
    APP_VERSION("App Version"),
    LAST_ACTION("Last Action"),

    PERMANENT_INSTALLATION_ID("User's Permanent Installation ID");
}

enum class ErrorInformationType(val readableValue: String) {
    ERROR_STACKTRACE("Error Stacktrace"),
    ERROR_MESSAGE("Error Message");
}