import com.github.jk1.tcdeps.KotlinScriptDslAdapter.tc
import com.github.jk1.tcdeps.KotlinScriptDslAdapter.teamcityServer

plugins {
    base
    id("com.github.jk1.tcdeps") version "1.1"
}

rootProject.apply {
    from(project.file("../../../gradle/kotlinUltimateProperties.gradle.kts"))
}

repositories {
    teamcityServer {
        setUrl("https://buildserver.labs.intellij.net")
    }
}

if (rootProject.extra.has("nativeDebugRepo")) {
    val nativeDebugRepo: String by rootProject.extra
    val nativeDebugVersion: String by rootProject.extra
    val nativeDebugPluginDir: File by rootProject.extra

    val nativeDebugPluginZip: Configuration by configurations.creating


    dependencies {
        var urlPath = "IU-plugins/auto-uploading/nativeDebug-plugin.zip"

        for (version in listOf("183", "191", "192")) {
            if (nativeDebugVersion.startsWith(version)) {
                urlPath = "IU-plugins/nativeDebug-plugin.zip"
            }
        }

        nativeDebugPluginZip(tc("$nativeDebugRepo:$nativeDebugVersion:$urlPath"))
    }

    val downloadNativeDebugPlugin: Task by downloading(
        nativeDebugPluginZip,
        nativeDebugPluginDir,
        pathRemap = { it.substringAfterLast('/') }
    ) { zipTree(it.singleFile) }

    tasks["build"].dependsOn(
        downloadNativeDebugPlugin
    )
}

fun Project.downloading(
    sourceConfiguration: Configuration,
    targetDir: File,
    pathRemap: (String) -> String = { it },
    extractor: (Configuration) -> Any = { it }
) = tasks.creating {
    // don't re-check status of the artifact at the remote server if the artifact is already downloaded
    val isUpToDate = targetDir.isDirectory && targetDir.walkTopDown().firstOrNull { !it.isDirectory } != null
    outputs.upToDateWhen { isUpToDate }

    if (!isUpToDate) {
        doFirst {
            copy {
                from(extractor(sourceConfiguration))
                into(targetDir)
                includeEmptyDirs = false
                duplicatesStrategy = DuplicatesStrategy.FAIL
                eachFile {
                    path = pathRemap(path)
                }
            }
        }
    }
}
