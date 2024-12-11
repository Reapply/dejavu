plugins {
    kotlin("jvm") version "2.0.21"
    id("io.papermc.paperweight.userdev") version "1.7.5"
    id("io.github.goooler.shadow") version "8.1.8"
    id("xyz.jpenilla.run-paper") version "2.3.1"
}

group = "com.sakurastudios"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
    maven(url = "https://jitpack.io")
    maven(url = "https://repo.flyte.gg/releases")
    maven(url = "https://repo.codemc.io/repository/maven-releases/")
    maven(url = "https://repo.codemc.io/repository/maven-snapshots/")
}

dependencies {
    paperweight.paperDevBundle("1.21.1-R0.1-SNAPSHOT")
    implementation("io.github.revxrsal:lamp.common:4.0.0-beta.17")
    implementation("io.github.revxrsal:lamp.bukkit:4.0.0-beta.17")
    implementation("gg.flyte:twilight:1.1.16")
    implementation("com.github.retrooper:packetevents-spigot:2.6.0")
}

kotlin {
    jvmToolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
}

tasks.withType(xyz.jpenilla.runtask.task.AbstractRun::class) {
    javaLauncher = javaToolchains.launcherFor {
        vendor = JvmVendorSpec.JETBRAINS
        languageVersion = JavaLanguageVersion.of(21)
    }
    jvmArgs("-XX:+AllowEnhancedClassRedefinition")
}

tasks {
    shadowJar {
        minimize()
        relocate("com.github.retrooper.packetevents", "com.dejavu.packetevents")
        relocate("io.github.retrooper.packetevents", "com.dejavu.packetevents")
    }

    build {
        dependsOn(shadowJar)
    }

    processResources {
        filteringCharset = "UTF-8"
    }

    runServer {
        minecraftVersion("1.21.1")
        downloadPlugins {}
    }
}