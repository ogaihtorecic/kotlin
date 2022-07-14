/*
 * Copyright 2010-2022 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.project.model.infra

import org.jetbrains.kotlin.project.model.KpmLocalModuleIdentifier
import org.jetbrains.kotlin.project.model.KpmMavenModuleIdentifier
import org.jetbrains.kotlin.project.model.platform
import org.jetbrains.kotlin.utils.Printer

fun KpmTestCase.renderDeclarationDsl(): String {
    val p = Printer(StringBuilder())
    p.render(this)
    return p.toString()
}

private fun Printer.render(case: KpmTestCase) {
    println("val ${case.name} = describeCase(\"${case.name}\") {")
    pushIndent()
    for (project in case.projects) {
        render(project)
        println()
    }
    popIndent()
    println("}")

}

private fun Printer.render(project: TestKpmGradleProject) {
    println("project(\"${project.name}\") {")
    pushIndent()
    for (module in project.modules) {
        render(module)
        println()
    }
    popIndent()
    println("}")
}

private fun Printer.render(module: TestKpmModule) {
    println("module(\"${module.name}\" {")
    pushIndent()
    // first declare all fragments
    for (fragment in module.fragments) {
        renderFragmentDeclaration(fragment)
    }

    // then declare dependencies
    for (fragment in module.fragments) {
        renderFragmentDependencies(fragment)
    }
}

private fun Printer.renderFragmentDeclaration(fragment: TestKpmFragment) {
    if (fragment.name == "common") return
    when {
        fragment is TestKpmVariant && fragment.platform == "jvm" -> println("jvm()")
        fragment is TestKpmVariant && fragment.platform == "js" -> println("js()")
        fragment is TestKpmVariant && fragment.platform == "linux" -> println("linux()")
        fragment is TestKpmVariant && fragment.platform == "macosX64" -> println("macosX64()")
        fragment is TestKpmVariant && fragment.platform == "android" -> println("android()")
        fragment !is TestKpmVariant -> println("fragment(\"${fragment.name}\")")
    }
}

private fun Printer.renderFragmentDependencies(fragment: TestKpmFragment) {
    var printedAnything = false

    for (refinedFragment in fragment.declaredRefinesDependencies) {
        if (refinedFragment.name == "common") continue
        printedAnything = true
        println("${fragment.name} refines ${refinedFragment.name}")
    }

    for (dependencyModule in fragment.declaredModuleDependencies) {
        if (dependencyModule.moduleIdentifier is KpmLocalModuleIdentifier) {
            val projectId = (dependencyModule.moduleIdentifier as KpmLocalModuleIdentifier).projectId
            printedAnything = true
            println("${fragment.name} depends ${"project(\"$projectId\")"}")
        } else if (dependencyModule.moduleIdentifier is KpmMavenModuleIdentifier) {
            val (group, name) = (dependencyModule.moduleIdentifier as KpmMavenModuleIdentifier).let { it.group to it.name }
            printedAnything = true
            println("${fragment.name} depends ${"maven(\"$group\", \"$name\")"}")
        }
    }
    if (printedAnything) println()
}
