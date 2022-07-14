import org.jetbrains.kotlin.gradle.dsl.KotlinJvmCompile

plugins {
    kotlin("jvm")
    id("jps-compatible")
}

publish()

testsJar { }

standardPublicJars()

dependencies {
    implementation(kotlinStdlib())
    implementation(project(":kotlin-tooling-core"))
    testImplementation(kotlin("test-junit"))
    testImplementation(project(":core:util.runtime"))
    testImplementation(projectTests(":generators:test-generator"))
    testImplementation(project(":kotlin-reflect"))
}

tasks.withType<KotlinJvmCompile>().configureEach {
    kotlinOptions {
        languageVersion = "1.4"
        apiVersion = "1.4"
        freeCompilerArgs += listOf("-Xskip-prerelease-check", "-Xsuppress-version-warnings")
    }
}

tasks.named<KotlinJvmCompile>("compileTestKotlin") {
    kotlinOptions {
        freeCompilerArgs += listOf(
            "-XXLanguage:+AllowSealedInheritorsInDifferentFilesOfSamePackage",
            "-XXLanguage:+SealedInterfaces",
            "-Xjvm-default=all"
        )
    }
}

tasks.named<Jar>("jar") {
    callGroovy("manifestAttributes", manifest, project)
}
