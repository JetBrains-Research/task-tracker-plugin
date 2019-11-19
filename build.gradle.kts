import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    id("org.jetbrains.intellij") version "0.4.10"
    java
    kotlin("jvm") version "1.3.41"
    id("org.openjfx.javafxplugin") version "0.0.8"
}

group = "io.github.elena-lyulina.actanalyzer"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}


dependencies {
    implementation(kotlin("stdlib-jdk8"))
    implementation("com.opencsv","opencsv", "5.0")
    implementation("joda-time", "joda-time", "2.9.2")
    implementation("org.apache.commons", "commons-csv", "1.7")

    testCompile("junit", "junit", "4.12")
    // https://mvnrepository.com/artifact/no.tornado/tornadofx
    testCompile("no.tornado", "tornadofx", "1.7.19")

}

// See https://github.com/JetBrains/gradle-intellij-plugin/
intellij {
    version = "2019.2.2"
}

javafx {
    modules("javafx.controls", "javafx.fxml")
}

configure<JavaPluginConvention> {
    sourceCompatibility = JavaVersion.VERSION_1_8
}
tasks.getByName<org.jetbrains.intellij.tasks.PatchPluginXmlTask>("patchPluginXml") {
    changeNotes("""
      Add change notes here.<br>
      <em>most HTML tags may be used</em>""")
}
tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "1.8"
}

