import org.gradle.api.tasks.testing.logging.TestExceptionFormat
import org.gradle.api.tasks.testing.logging.TestLogEvent
import io.papermc.paperweight.tasks.RebuildGitPatches
import java.io.FileOutputStream
import java.security.MessageDigest
import java.util.jar.JarEntry
import java.util.jar.JarOutputStream

plugins {
    java // TODO java launcher tasks
    id("io.papermc.paperweight.patcher") version "2.0.0-beta.21"
}

paperweight {
    filterPatches = false
    upstreams.paper {
        ref = providers.gradleProperty("paperRef")

        patchFile {
            path = "paper-server/build.gradle.kts"
            outputFile = file("folia-server/build.gradle.kts")
            patchFile = file("folia-server/build.gradle.kts.patch")
        }
        patchFile {
            path = "paper-api/build.gradle.kts"
            outputFile = file("folia-api/build.gradle.kts")
            patchFile = file("folia-api/build.gradle.kts.patch")
        }
        patchDir("paperApi") {
            upstreamPath = "paper-api"
            excludes = setOf("build.gradle.kts")
            patchesDir = file("folia-api/paper-patches")
            outputDir = file("paper-api")
        }
    }
}

val paperMavenPublicUrl = "https://repo.papermc.io/repository/maven-public/"

subprojects {
    apply(plugin = "java-library")
    apply(plugin = "maven-publish")

    extensions.configure<JavaPluginExtension> {
        toolchain {
            languageVersion = JavaLanguageVersion.of(25)
        }
    }

    repositories {
        mavenCentral()
        maven(paperMavenPublicUrl)
        maven("https://maven.neoforged.net/releases") // Crelia: FancyModLoader 加载器
    }

    dependencies {
        "testRuntimeOnly"("org.junit.platform:junit-platform-launcher")
    }

    tasks.withType<AbstractArchiveTask>().configureEach {
        isPreserveFileTimestamps = false
        isReproducibleFileOrder = true
    }
    tasks.withType<JavaCompile>().configureEach  {
        options.encoding = Charsets.UTF_8.name()
        options.release = 25
        options.isFork = true
    }
    tasks.withType<Javadoc>().configureEach  {
        options.encoding = Charsets.UTF_8.name()
    }
    tasks.withType<ProcessResources>().configureEach  {
        filteringCharset = Charsets.UTF_8.name()
    }
    tasks.withType<Test>().configureEach  {
        testLogging {
            showStackTraces = true
            exceptionFormat = TestExceptionFormat.FULL
            events(TestLogEvent.STANDARD_OUT)
        }
    }

    extensions.configure<PublishingExtension> {
        repositories {
            maven("https://artifactory.papermc.io/artifactory/releases/") {
                name = "paperReleases"
                credentials(PasswordCredentials::class)
            }
        }
    }
}

tasks.register("printMinecraftVersion") {
    doLast {
        println(providers.gradleProperty("mcVersion").get().trim())
    }
}

tasks.register("printPaperVersion") {
    doLast {
        println(project.version)
    }
}

fun sha256(file: File): String {
    val digest = MessageDigest.getInstance("SHA-256")
    file.inputStream().use { input ->
        val buffer = ByteArray(8192)
        while (true) {
            val read = input.read(buffer)
            if (read < 0) {
                break
            }
            digest.update(buffer, 0, read)
        }
    }
    return digest.digest().joinToString("") { byte -> "%02x".format(byte.toInt() and 0xff) }
}

fun jarDirectory(sourceDir: File, destination: File) {
    JarOutputStream(FileOutputStream(destination)).use { output ->
        sourceDir.walkTopDown()
            .filter { file -> file.isFile }
            .sortedBy { file -> file.relativeTo(sourceDir).invariantSeparatorsPath }
            .forEach { file ->
                val entry = JarEntry(file.relativeTo(sourceDir).invariantSeparatorsPath)
                entry.time = 0
                output.putNextEntry(entry)
                file.inputStream().use { input -> input.copyTo(output) }
                output.closeEntry()
            }
    }
}

project(":folia-server") {
    afterEvaluate {
        val serverJar = tasks.named<Jar>("jar")
        val neoforgeResourcesJar = tasks.named<Jar>("neoforgeResourcesJar")
        val runtimeClasspath = configurations.named("runtimeClasspath")
        val fmlLoader = configurations.named("fmlLoader")
        val paperTransformerJarPrefixes = listOf("folia-api-", "spark-api-", "spark-paper-")
        val stagingDir = layout.buildDirectory.dir("crelia/standalone")

        val compileCreliaLauncher by tasks.registering(JavaCompile::class) {
            description = "Compile the tiny self-contained Crelia jar launcher"
            source(rootProject.fileTree("build-data/crelia-launcher/src/main/java") {
                include("**/*.java")
            })
            classpath = files()
            destinationDirectory.set(layout.buildDirectory.dir("crelia/launcher-classes"))
            options.release.set(21)
        }

        val prepareCreliaStandalone by tasks.registering {
            description = "Stage the exact runServerFml classpath as nested jars"
            dependsOn(serverJar, neoforgeResourcesJar)
            inputs.file(serverJar.flatMap { it.archiveFile })
            inputs.file(neoforgeResourcesJar.flatMap { it.archiveFile })
            inputs.files(runtimeClasspath)
            inputs.files(fmlLoader)
            outputs.dir(stagingDir)

            doLast {
                val outputDir = stagingDir.get().asFile
                val librariesDir = outputDir.resolve("libraries")
                project.delete(outputDir)
                librariesDir.mkdirs()

                val candidates = buildList {
                    add(serverJar.get().archiveFile.get().asFile)
                    add(neoforgeResourcesJar.get().archiveFile.get().asFile)
                    addAll(runtimeClasspath.get().files.filterNot { file ->
                        paperTransformerJarPrefixes.any { prefix -> file.name.startsWith(prefix) }
                    })
                    addAll(fmlLoader.get().files)
                }
                val seenPaths = mutableSetOf<String>()
                val classpathFiles = candidates.filter { file ->
                    file.exists() &&
                    seenPaths.add(file.absoluteFile.normalize().path)
                }

                val indexLines = classpathFiles.mapIndexed { index, file ->
                    val embeddedName = "%03d-%s%s".format(index, file.name, if (file.isDirectory) ".jar" else "")
                    val embeddedFile = librariesDir.resolve(embeddedName)
                    if (file.isDirectory) {
                        jarDirectory(file, embeddedFile)
                    } else {
                        file.copyTo(embeddedFile, overwrite = true)
                    }
                    "${sha256(embeddedFile)}\t$embeddedName"
                }
                outputDir.resolve("crelia-libraries.index")
                    .writeText(indexLines.joinToString(separator = "\n", postfix = "\n"))
            }
        }

        tasks.register<Jar>("creliaStandaloneJar") {
            group = "build"
            description = "Build creliatest2.jar with the full Crelia FML runtime nested inside"
            dependsOn(compileCreliaLauncher, prepareCreliaStandalone)
            archiveFileName.set("creliatest2.jar")
            destinationDirectory.set(rootProject.layout.projectDirectory.dir(".."))
            from(compileCreliaLauncher.flatMap { it.destinationDirectory })
            from(stagingDir.map { it.dir("libraries") }) {
                into("META-INF/crelia-libraries")
            }
            from(stagingDir.map { it.file("crelia-libraries.index") }) {
                into("META-INF")
            }
            manifest {
                attributes(
                    "Main-Class" to "crelia.launcher.Main",
                    "Enable-Native-Access" to "ALL-UNNAMED",
                )
            }
        }
    }
}
