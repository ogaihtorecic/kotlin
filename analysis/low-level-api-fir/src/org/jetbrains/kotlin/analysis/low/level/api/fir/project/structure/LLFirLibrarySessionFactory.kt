/*
 * Copyright 2010-2022 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.analysis.low.level.api.fir.project.structure

import com.intellij.openapi.project.Project
import org.jetbrains.kotlin.analysis.low.level.api.fir.providers.LLFirLibrarySessionProvider
import org.jetbrains.kotlin.analysis.low.level.api.fir.sessions.LLFirLibraryLikeSession
import org.jetbrains.kotlin.analysis.low.level.api.fir.sessions.LLFirLibrarySession
import org.jetbrains.kotlin.analysis.project.structure.KtBinaryModule
import org.jetbrains.kotlin.analysis.project.structure.KtModule
import org.jetbrains.kotlin.analysis.project.structure.allDirectDependenciesOfType
import org.jetbrains.kotlin.analysis.providers.createLibrariesModificationTracker
import org.jetbrains.kotlin.analysis.utils.caches.softCachedValue
import org.jetbrains.kotlin.config.LanguageVersionSettingsImpl
import org.jetbrains.kotlin.fir.PrivateSessionConstructor
import org.jetbrains.kotlin.fir.SessionConfiguration
import org.jetbrains.kotlin.fir.backend.jvm.FirJvmTypeMapper
import org.jetbrains.kotlin.fir.resolve.providers.FirProvider
import org.jetbrains.kotlin.fir.resolve.providers.FirSymbolProvider
import org.jetbrains.kotlin.fir.resolve.providers.symbolProvider
import org.jetbrains.kotlin.fir.resolve.scopes.wrapScopeWithJvmMapped
import org.jetbrains.kotlin.fir.resolve.transformers.FirPhaseCheckingPhaseManager
import org.jetbrains.kotlin.fir.scopes.FirKotlinScopeProvider
import org.jetbrains.kotlin.fir.session.registerCommonComponents
import org.jetbrains.kotlin.fir.session.registerCommonJavaComponents
import org.jetbrains.kotlin.fir.session.registerJavaSpecificResolveComponents
import org.jetbrains.kotlin.fir.session.registerModuleData
import org.jetbrains.kotlin.fir.symbols.FirPhaseManager
import org.jetbrains.kotlin.resolve.jvm.modules.JavaModuleResolver
import java.util.concurrent.ConcurrentHashMap
import org.jetbrains.kotlin.analysis.utils.caches.getValue

@OptIn(PrivateSessionConstructor::class, SessionConfiguration::class)
internal class LLFirLibrarySessionFactory(
    private val project: Project,
) {
    private val librarySessionByModule by softCachedValue(project, project.createLibrariesModificationTracker()) {
        ConcurrentHashMap<KtBinaryModule, LLFirLibrarySession>()
    }

    fun getLibrarySession(ktBinaryModule: KtBinaryModule): LLFirLibrarySession {
        return librarySessionByModule.getOrPut(ktBinaryModule) { createModuleLibrariesSession(ktBinaryModule) }
    }


    private fun createModuleLibrariesSession(
        ktLibraryModule: KtBinaryModule,
    ): LLFirLibrarySession {
        val platform = ktLibraryModule.platform
        val builtinsSession = LLFirBuiltinsSessionFactory.getInstance(project).getBuiltinsSession(platform)
        return LLFirLibrarySession(ktLibraryModule, project, builtinsSession.builtinTypes).apply session@{
            val moduleData = LLFirKtModuleBasedModuleData(ktLibraryModule).apply { bindSession(this@session) }
            registerModuleData(moduleData)
            registerIdeComponents(project)
            register(FirPhaseManager::class, FirPhaseCheckingPhaseManager)
            registerCommonComponents(LanguageVersionSettingsImpl.DEFAULT/*TODO*/)
            registerCommonJavaComponents(JavaModuleResolver.getInstance(project))
            registerJavaSpecificResolveComponents()

            val kotlinScopeProvider = FirKotlinScopeProvider(::wrapScopeWithJvmMapped)
            register(FirKotlinScopeProvider::class, kotlinScopeProvider)

            val providers = LLFirLibraryProviderFactory.createLibraryProvidersForSingleBinaryModule(
                this,
                moduleData,
                ktLibraryModule,
                kotlinScopeProvider,
                project,
                builtinTypes
            )

            val symbolProvider = createCompositeSymbolProvider(this) {
                addAll(providers)
                add(builtinsSession.symbolProvider)
            }

            register(FirProvider::class, LLFirLibrarySessionProvider(symbolProvider))
            register(FirSymbolProvider::class, symbolProvider)
            register(FirJvmTypeMapper::class, FirJvmTypeMapper(this))
        }
    }

    companion object {
        fun getInstance(project: Project): LLFirLibrarySessionFactory =
            project.getService(LLFirLibrarySessionFactory::class.java)
    }

}