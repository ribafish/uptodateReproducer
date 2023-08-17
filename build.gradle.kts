import com.gradle.enterprise.gradleplugin.GradleEnterpriseExtension

plugins {
    kotlin("jvm") version "1.9.0"
    application
}

group = "org.example"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(kotlin("test"))
}

tasks.test {
    useJUnitPlatform()
}

kotlin {
    jvmToolchain(8)
}

application {
    mainClass.set("MainKt")
}

val test1 = tasks.register("test1", DefaultTask::class.java) {
    description = "Output file only specified, not created."
    val outputFileProvider = project.layout.buildDirectory.file("$name/test-output.txt")
    configureUptodateTestDefaults(outputFileProvider)
}

val test2 = tasks.register("test2", DefaultTask::class.java) {
    description = "Output file specified and all the folders to it created, but the file itself is not."
    val outputFileProvider = project.layout.buildDirectory.file("$name/test-output.txt")
    configureUptodateTestDefaults(outputFileProvider)
    doLast {
        outputFileProvider.get().asFile.parentFile.mkdirs()
    }
}

val test3 = tasks.register("test3", DefaultTask::class.java) {
    description = "Output file specified, created and written to."
    val outputFileProvider = project.layout.buildDirectory.file("$name/test-output.txt")
    configureUptodateTestDefaults(outputFileProvider)
    inputs.properties("prop2" to "propVal1")
    doLast {
        val outputFile = outputFileProvider.get().asFile
        outputFile.parentFile.mkdirs()
        outputFile.writeText("thing")
    }
}

val test4 = tasks.register("test4", DefaultTask::class.java) {
    description = "Output file specified, created and written to, but there is an ever chaning input property."
    val time = System.currentTimeMillis()
    inputs.properties("prop-time" to "$time")
    val outputFileProvider = project.layout.buildDirectory.file("$name/test-output.txt")
    configureUptodateTestDefaults(outputFileProvider)
    doLast {
        val outputFile = outputFileProvider.get().asFile
        outputFile.parentFile.mkdirs()
        outputFile.writeText("thing")
    }
}

tasks.run.configure {
    val outputFileProvider = project.layout.buildDirectory.file("$name/test-output.txt")
    configureUptodateTestDefaults(outputFileProvider)
}

tasks.register("uptodate") {
    dependsOn(test1, test2, test3, test4, tasks.run)
}

tasks.register("purgeCache", Delete::class.java) {
    delete = setOf(rootProject.projectDir.resolve("build-cache"))
}

fun Task.configureUptodateTestDefaults(outputFileProvider: Provider<RegularFile>) {
    inputs.properties("prop1" to "prop1")
    outputs.files(outputFileProvider).withPropertyName("outputFile")
    outputs.cacheIf { true }
    captureOutputs()
}

fun Task.captureOutputs() {
    val task = this
    rootProject.extensions.configure<GradleEnterpriseExtension> {
        buildScan {
            task.outputs.files.files.forEach {
                value("${task.name}-output:", it.toRelativeString(rootProject.projectDir))
            }
        }
    }
}
