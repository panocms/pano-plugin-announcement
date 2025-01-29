import java.net.URL

plugins {
    kotlin("jvm") version "2.1.0"
    kotlin("kapt") version "2.1.0"
    id("com.gradleup.shadow") version "8.3.5"
    `maven-publish`
}

group = "com.panomc.plugins"
version =
    (if (project.hasProperty("version") && project.findProperty("version") != "unspecified") project.findProperty("version") else "local-build")!!

val pf4jVersion: String by project
val vertxVersion: String by project
val gsonVersion: String by project
val handlebarsVersion: String by project
val bootstrap = (project.findProperty("bootstrap") as String?)?.toBoolean() ?: false
val pluginsDir: File? by rootProject.extra

val os = System.getProperty("os.name").lowercase()
val arch = System.getProperty("os.arch").lowercase()

val isWindows = os.contains("win")
val isMac = os.contains("mac")
val isLinux = os.contains("nix") || os.contains("nux") || os.contains("linux")

val isAarch64 = arch.contains("aarch64") || arch.contains("arm64")
val isX64 = arch.contains("x86_64") || arch.contains("amd64")

val bunVersion = "1.2.0"
val bunPlatform = when {
    isWindows && isX64 -> "bun-windows-x64"
//    isWindows && isAarch64 -> "bun-windows-aarch64"
    isMac && isX64 -> "bun-darwin-x64"
    isMac && isAarch64 -> "bun-darwin-aarch64"
    isLinux && isX64 -> "bun-linux-x64"
    isLinux && isAarch64 -> "bun-linux-aarch64"
    else -> throw RuntimeException("Unsupported OS or Architecture")
}
val bunUrl = "https://github.com/oven-sh/bun/releases/download/bun-v$bunVersion/$bunPlatform.zip"

val bunDir = File(layout.buildDirectory.asFile.get().absolutePath, "bun" + File.separator + bunPlatform)
val bunBin = if (isWindows) File(bunDir, "bun.exe") else File(bunDir, "bun")


repositories {
    mavenCentral()
    maven("https://jitpack.io")
}

dependencies {
    if (bootstrap) {
        compileOnly(project(mapOf("path" to ":Pano")))
    } else {
        compileOnly("com.github.panomc:pano:v1.0.0-alpha.67")
    }

    compileOnly(kotlin("stdlib-jdk8"))
    compileOnly(kotlin("reflect"))

    compileOnly("org.pf4j:pf4j:${pf4jVersion}")
    kapt("org.pf4j:pf4j:${pf4jVersion}")
}

tasks {
    register("installBun") {
        doLast {
            println(bunBin)
            if (!bunBin.exists()) {
                println("🚀 Couldn't find Bun, downloading: $bunUrl")

                val zipFile = File(bunDir, "$bunPlatform.zip")
                zipFile.parentFile.mkdirs()

                URL(bunUrl).openStream().use { input ->
                    zipFile.outputStream().use { output ->
                        input.copyTo(output)
                    }
                }

                copy {
                    from(zipTree(zipFile))
                    into(bunDir)
                }

                if (!isWindows) {
                    bunBin.setExecutable(true)
                }

                zipFile.delete()
                println("✅ Bun successfully downloaded: ${bunBin.absolutePath}")
            } else {
                println("✅ Bun is downloaded already: ${bunBin.absolutePath}")
            }
        }
    }

    register("buildUI", Exec::class) {
        dependsOn("installBun")
        commandLine(bunBin.absolutePath, "run", "build")
    }

    register("zipPluginUI", Zip::class) {
        dependsOn("buildUI")

        from("src/main/resources/plugin-ui")
        archiveFileName.set("plugin-ui.zip")
        destinationDirectory.set(file("src/main/resources"))

        doLast {
            val pluginUIFolder = file("src/main/resources/plugin-ui")
            if (pluginUIFolder.exists()) {
                pluginUIFolder.deleteRecursively()
            }
        }

        outputs.upToDateWhen { false }
    }

    shadowJar {
        val pluginId: String by project
        val pluginClass: String by project
        val pluginProvider: String by project
        val pluginDescription: String by project
        val pluginLicense: String by project
        val pluginDependencies: String by project
        val pluginSourceUrl: String by project

        manifest {
            attributes["Plugin-Class"] = pluginClass
            attributes["Plugin-Id"] = pluginId
            attributes["Plugin-Version"] = version
            attributes["Plugin-Provider"] = pluginProvider
            attributes["Plugin-Description"] = pluginDescription
            attributes["Plugin-License"] = pluginLicense
            attributes["Plugin-Dependencies"] = pluginDependencies
            attributes["Plugin-Source-Url"] = pluginSourceUrl
        }

        archiveFileName.set("$pluginId-$version.jar")

        dependencies {
            exclude(dependency("io.vertx:vertx-core"))
            exclude {
                it.moduleGroup == "io.netty" || it.moduleGroup == "org.slf4j"
            }
        }
    }

    register("copyJar") {
        pluginsDir?.let {
            doLast {
                copy {
                    from(shadowJar.get().archiveFile.get().asFile.absolutePath)
                    into(it)
                }
            }
        }

        outputs.upToDateWhen { false }
        mustRunAfter(shadowJar)
    }

    jar {
        enabled = false
        dependsOn(shadowJar)
        dependsOn("copyJar")
    }
}

tasks.named("build") {
    dependsOn("zipPluginUI")
}

tasks.named("processResources") {
    dependsOn("zipPluginUI")
}

publishing {
    repositories {
        maven {
            name = "pano-plugin-announcement"
            url = uri("https://maven.pkg.github.com/panocms/pano-plugin-announcement")
            credentials {
                username = project.findProperty("gpr.user") as String? ?: System.getenv("USERNAME_GITHUB")
                password = project.findProperty("gpr.token") as String? ?: System.getenv("TOKEN_GITHUB")
            }
        }
    }

    publications {
        create<MavenPublication>("shadow") {
            project.extensions.configure<com.github.jengelman.gradle.plugins.shadow.ShadowExtension> {
                artifactId = "pano-plugin-announcement"
                component(this@create)
            }
        }
    }
}