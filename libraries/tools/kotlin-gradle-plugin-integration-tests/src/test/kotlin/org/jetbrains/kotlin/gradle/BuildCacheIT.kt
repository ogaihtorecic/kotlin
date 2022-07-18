/*
 * Copyright 2010-2017 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.jetbrains.kotlin.gradle

import org.gradle.api.logging.LogLevel
import org.gradle.internal.hash.DefaultFileHasher
import org.gradle.internal.hash.DefaultStreamHasher
import org.gradle.internal.hash.HashCode
import org.gradle.util.GradleVersion
import org.jetbrains.kotlin.gradle.report.BuildReportType
import org.jetbrains.kotlin.gradle.testbase.*
import org.junit.jupiter.api.DisplayName
import java.nio.file.*
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.io.path.*

@ExperimentalPathApi
@DisplayName("Local build cache")
@JvmGradlePluginTests
class BuildCacheIT : KGPBaseTest() {

    override val defaultBuildOptions: BuildOptions =
        super.defaultBuildOptions.copy(buildCacheEnabled = true)

    private val localBuildCacheDir get() = workingDir.resolve("custom-jdk-build-cache")

    @DisplayName("kotlin.caching.enabled flag should enable caching for Kotlin tasks")
    @GradleTest
    fun testKotlinCachingEnabledFlag(gradleVersion: GradleVersion) {
        project("simpleProject", gradleVersion) {
            enableLocalBuildCache(localBuildCacheDir)

            build("assemble") {
                assertTasksPackedToCache(":compileKotlin")
            }

            build("clean", "assemble", "-Dkotlin.caching.enabled=false") {
                assertTasksExecuted(":compileKotlin")
            }
        }
    }

    @DisplayName("Kotlin JVM task should be taken from cache")
    @GradleTest
    fun testCacheHitAfterClean(gradleVersion: GradleVersion) {
        project("simpleProject", gradleVersion) {
            enableLocalBuildCache(localBuildCacheDir)

            build("assemble") {
                assertTasksPackedToCache(":compileKotlin")
            }

            build("clean", "assemble") {
                assertTasksFromCache(":compileKotlin", ":compileJava")
            }
        }
    }

    @DisplayName("Should correctly handle modification/restoration of source file")
    @GradleTest
    fun testCacheHitAfterCacheHit(gradleVersion: GradleVersion) {
        project("simpleProject", gradleVersion) {
            enableLocalBuildCache(localBuildCacheDir)

            val fileHasher = DefaultFileHasher(DefaultStreamHasher())

            val taskOutput = projectPath.resolve("build/kotlin/compileKotlin/cacheable")
            val watchfsThread = FileWatcher(taskOutput)

            try {
                build("assemble", forceOutput = true) {
                    // Should store the output into the cache:
                    assertTasksPackedToCache(":compileKotlin")
                }
                watchfsThread.start()

                fun historyFileHash(): HashCode = fileHasher.hash(
                    projectPath.resolve("build/kotlin/compileKotlin/cacheable/last-build.bin").toFile()
                )
                println("XXX: first build history file hash ${historyFileHash()}")

                val sourceFile = kotlinSourcesDir().resolve("helloWorld.kt")
                val originalSource: String = sourceFile.readText()
                val modifiedSource: String = originalSource.replace(" and ", " + ")
                sourceFile.writeText(modifiedSource)

                build("assemble", forceOutput = true) {
                    assertTasksPackedToCache(":compileKotlin")
                }
                println("XXX: second build history file hash ${historyFileHash()}")

                sourceFile.writeText(originalSource)

                build("assemble", forceOutput = true) {
                    // Should load the output from cache:
                    assertTasksFromCache(":compileKotlin")
                }
                println("XXX: third build history file hash ${historyFileHash()}")

                sourceFile.writeText(modifiedSource)

                println("XXX: before 4th build history file hash ${historyFileHash()}")
                build("assemble", forceOutput = true) {
                    // And should load the output from cache again, without compilation:
                    assertTasksFromCache(":compileKotlin")
                }
            } finally {
                if (!watchfsThread.isStopped) watchfsThread.stopThread()
            }
        }
    }

    class FileWatcher(private val file: Path) : Thread("watch-fs") {
        private val stop: AtomicBoolean = AtomicBoolean(false)

        val isStopped: Boolean
            get() = stop.get()

        fun stopThread() {
            stop.set(true)
        }

        fun doOnChange(file: Path) {
            println("KKK: ${System.currentTimeMillis()} ms - file was changed: ${file.pathString}")
        }

        fun doOnCreate(file: Path) {
            println("KKK: ${System.currentTimeMillis()} ms file was created: ${file.pathString}")
        }

        fun doOnDelete(file: Path) {
            println("KKK: ${System.currentTimeMillis()} ms file was deleted: ${file.pathString}")
        }

        override fun run() {
            try {
                FileSystems.getDefault().newWatchService().use { watcher ->
                    val path: Path = file
                    path.register(
                        watcher,
                        StandardWatchEventKinds.ENTRY_MODIFY,
                        StandardWatchEventKinds.ENTRY_CREATE,
                        StandardWatchEventKinds.ENTRY_DELETE
                    )
                    var key: WatchKey?
                    while (!isStopped) {
                        key = try {
                            watcher.poll(25, TimeUnit.MILLISECONDS)
                        } catch (e: InterruptedException) {
                            return
                        }
                        if (key == null) {
                            yield()
                            continue
                        }
                        for (event in key.pollEvents()) {
                            @Suppress("UNCHECKED_CAST") val ev: WatchEvent<Path> = event as WatchEvent<Path>
                            when (ev.kind()) {
                                StandardWatchEventKinds.OVERFLOW -> {
                                    yield()
                                    continue
                                }
                                StandardWatchEventKinds.ENTRY_MODIFY -> doOnChange(ev.context())
                                StandardWatchEventKinds.ENTRY_CREATE -> doOnCreate(ev.context())
                                StandardWatchEventKinds.ENTRY_DELETE -> doOnDelete(ev.context())
                            }
                            val valid = key.reset()
                            if (!valid) {
                                break
                            }
                        }
                        yield()
                    }
                }
            } catch (e: Throwable) {
                // Log or rethrow the error
            }
        }
    }


    @DisplayName("Debug log level should not break build cache")
    @GradleTest
    fun testDebugLogLevelCaching(gradleVersion: GradleVersion) {
        project("simpleProject", gradleVersion) {
            enableLocalBuildCache(localBuildCacheDir)

            build(
                ":assemble",
                buildOptions = defaultBuildOptions.copy(logLevel = LogLevel.DEBUG)
            ) {
                assertTasksPackedToCache(":compileKotlin")
            }

            build("clean", ":assemble") {
                assertTasksFromCache(":compileKotlin")
            }
        }
    }

    @DisplayName("Enabled statistic should not break build cache")
    @GradleTest
    fun testCacheWithStatistic(gradleVersion: GradleVersion) {
        project("simpleProject", gradleVersion) {
            enableLocalBuildCache(localBuildCacheDir)

            build(
                ":assemble"
            ) {
                assertTasksPackedToCache(":compileKotlin")
            }

            build(
                "clean", ":assemble",
                buildOptions = defaultBuildOptions.copy(buildReport = listOf(BuildReportType.FILE))
            ) {
                assertTasksFromCache(":compileKotlin")
            }
        }
    }

    //doesn't work for build history files approach
    @DisplayName("Restore from build cache should not break incremental compilation")
    @GradleTest
    fun testIncrementalCompilationAfterCacheHit(gradleVersion: GradleVersion) {
        project("incrementalMultiproject", gradleVersion, buildOptions = defaultBuildOptions.copy(useICClasspathSnapshot = true)) {
            enableLocalBuildCache(localBuildCacheDir)
            build("assemble")
            build("clean", "assemble") {
                assertTasksFromCache(":lib:compileKotlin")
                assertTasksFromCache(":app:compileKotlin")
            }
            val bKtSourceFile = projectPath.resolve("lib/src/main/kotlin/bar/B.kt")

            bKtSourceFile.modify { it.replace("fun b() {}", "fun b() {}\nfun b2() {}") }

            build("assemble", buildOptions = defaultBuildOptions.copy(useICClasspathSnapshot = true, logLevel = LogLevel.DEBUG)) {
                assertOutputDoesNotContain("[KOTLIN] [IC] Non-incremental compilation will be performed")
                assertOutputContains("Incremental compilation with ABI snapshot enabled")
                assertCompiledKotlinSources(setOf(bKtSourceFile).map { it.relativeTo(projectPath)}, output)
            }

        }
    }

}
