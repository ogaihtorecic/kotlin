/*
 * Copyright 2010-2021 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.project.model.infra

import org.jetbrains.kotlin.project.model.*
import org.jetbrains.kotlin.project.model.testDsl.*
import org.jetbrains.kotlin.project.model.utils.ObservableIndexedSet
import org.jetbrains.kotlin.tooling.core.MutableExtras
import org.jetbrains.kotlin.tooling.core.mutableExtrasOf
import java.io.File

interface KpmTestEntity {
    val name: String
}

class KpmTestCase(
    override val name: String,
) : KpmTestEntity {
    val projects: ObservableIndexedSet<TestKpmGradleProject> = ObservableIndexedSet()
    val extras: MutableExtras = mutableExtrasOf()

    override fun toString(): String = "Case $name"

    fun applyDefaults() {
        project("p")
    }

    fun fork(): KpmTestCase = KpmTestCase(name).also { forkedCase ->
        projects.mapValuesTo(forkedCase.projects) { it.fork(forkedCase) }
        forkedCase.extras.putAll(extras)
    }
}

class TestKpmGradleProject(
    val containingCase: KpmTestCase,
    override val name: String,
) : KpmTestEntity {
    val modules: ObservableIndexedSet<TestKpmModule> = ObservableIndexedSet()
    val extras: MutableExtras = mutableExtrasOf()

    fun applyDefaults() {
        module("main")
    }

    fun fork(parent: KpmTestCase): TestKpmGradleProject = TestKpmGradleProject(parent, name).also { forkedProject ->
        modules.mapValuesTo(forkedProject.modules) { it.fork(this) }
        forkedProject.extras.putAll(extras)
    }

    override fun toString(): String = ":$name"
}

class TestKpmModule(
    val containingProject: TestKpmGradleProject,
    override val moduleIdentifier: KpmModuleIdentifier,
) : KpmTestEntity, KpmModule {
    override val fragments: ObservableIndexedSet<TestKpmFragment> = ObservableIndexedSet()
    override val plugins: MutableSet<KpmCompilerPlugin> = mutableSetOf()
    val extras: MutableExtras = mutableExtrasOf()

    override val name: String
        get() = moduleIdentifier.moduleClassifier ?: "main"

    fun applyDefaults() {
        fragment("common")
    }

    fun fork(parent: TestKpmGradleProject): TestKpmModule =
        TestKpmModule(parent, moduleIdentifier).also { forkedModule ->
            fragments.mapValuesTo(forkedModule.fragments) { it.fork(this) }
            forkedModule.plugins.addAll(plugins)
            forkedModule.extras.putAll(extras)
        }
}

open class TestKpmFragment(
    override val containingModule: TestKpmModule,
    override val fragmentName: String,
) : KpmTestEntity, KpmFragment {
    override var languageSettings: LanguageSettings? = null
    val extras: MutableExtras = mutableExtrasOf()
    override val kotlinSourceRoots: MutableList<File> = mutableListOf()
    override val declaredModuleDependencies: MutableList<KpmModuleDependency> = mutableListOf()
    override val declaredRefinesDependencies: MutableList<TestKpmFragment> = mutableListOf()
    override val name: String get() = fragmentName

    fun applyDefaults() {
        refines(containingModule.common)
    }

    open fun fork(parent: TestKpmModule): TestKpmFragment =
        TestKpmFragment(parent, fragmentName).also { forkedFragment ->
            forkedFragment.languageSettings = this.languageSettings
            forkedFragment.extras.putAll(this.extras)
            forkedFragment.kotlinSourceRoots.addAll(this.kotlinSourceRoots)
            forkedFragment.declaredModuleDependencies.addAll(this.declaredModuleDependencies)
            forkedFragment.declaredRefinesDependencies.addAll(this.declaredRefinesDependencies)
        }
}

class TestKpmVariant(
    containingModule: TestKpmModule,
    fragmentName: String,
) : TestKpmFragment(containingModule, fragmentName), KpmTestEntity, KpmVariant {
    override val variantAttributes: MutableMap<KotlinAttributeKey, String> = mutableMapOf()

    override fun fork(parent: TestKpmModule): TestKpmFragment = TestKpmVariant(parent, fragmentName).also { forkedVariant ->
        forkedVariant.languageSettings = this.languageSettings
        forkedVariant.extras.putAll(this.extras)
        forkedVariant.kotlinSourceRoots.addAll(this.kotlinSourceRoots)
        forkedVariant.declaredModuleDependencies.addAll(this.declaredModuleDependencies)
        forkedVariant.declaredRefinesDependencies.addAll(this.declaredRefinesDependencies)

        forkedVariant.variantAttributes.putAll(this.variantAttributes)
    }
}
