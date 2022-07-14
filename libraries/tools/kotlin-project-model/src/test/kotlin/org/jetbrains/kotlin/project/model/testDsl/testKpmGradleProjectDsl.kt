/*
 * Copyright 2010-2022 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.project.model.testDsl

import org.jetbrains.kotlin.project.model.KpmLocalModuleIdentifier
import org.jetbrains.kotlin.project.model.infra.TestKpmGradleProject
import org.jetbrains.kotlin.project.model.infra.TestKpmModule

fun TestKpmGradleProject.allModules(action: TestKpmModule.() -> Unit) {
    modules.withAll(action)
}

fun TestKpmGradleProject.module(name: String, applyDefaults: Boolean = true, configure: TestKpmModule.() -> Unit = { }): TestKpmModule {
    val module = modules.getOrPut(name) {
        val id = KpmLocalModuleIdentifier(
            buildId = "",
            projectId = this.name,
            moduleClassifier = name.takeIf { it != "main" }
        )
        val module = TestKpmModule(this, id)
        if (applyDefaults) module.applyDefaults()

        module
    }
    configure(module)
    return module
}

fun TestKpmGradleProject.moduleNamed(name: String): TestKpmModule =
    modules[name] ?: error("Module with name $name doesn't exist. Existing modules: ${modules.items}")

val TestKpmGradleProject.main get() = moduleNamed("main")
val TestKpmGradleProject.test get() = moduleNamed("test")

fun TestKpmGradleProject.depends(other: TestKpmGradleProject): TestKpmGradleProject {
    main.depends(other)
    return this
}
