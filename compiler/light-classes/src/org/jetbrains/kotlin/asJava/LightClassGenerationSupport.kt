/*
 * Copyright 2010-2022 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.asJava

import com.intellij.openapi.project.Project
import com.intellij.psi.PsiManager
import org.jetbrains.kotlin.asJava.classes.*
import org.jetbrains.kotlin.config.AnalysisFlags
import org.jetbrains.kotlin.descriptors.DeclarationDescriptor
import org.jetbrains.kotlin.descriptors.annotations.AnnotationDescriptor
import org.jetbrains.kotlin.lexer.KtTokens
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.psi.*
import org.jetbrains.kotlin.resolve.BindingContext
import org.jetbrains.kotlin.resolve.constants.evaluate.ConstantExpressionEvaluator
import org.jetbrains.kotlin.resolve.lazy.descriptors.LazyClassDescriptor

abstract class LightClassGenerationSupport {
    abstract fun resolveToDescriptor(declaration: KtDeclaration): DeclarationDescriptor?

    abstract fun analyze(element: KtElement): BindingContext

    abstract fun analyzeAnnotation(element: KtAnnotationEntry): AnnotationDescriptor?

    abstract fun analyzeWithContent(element: KtClassOrObject): BindingContext

    protected abstract fun getUltraLightClassSupport(element: KtElement): KtUltraLightSupport

    fun createConstantEvaluator(expression: KtExpression): ConstantExpressionEvaluator = getUltraLightClassSupport(expression).run {
        ConstantExpressionEvaluator(moduleDescriptor, languageVersionSettings, expression.project)
    }

    internal fun canCreateUltraLightClassForFacade(files: Collection<KtFile>): Boolean = files.none { it.isScript() }

    fun createUltraLightClassForFacade(
        manager: PsiManager,
        facadeClassFqName: FqName,
        files: Collection<KtFile>,
    ): KtUltraLightClassForFacade? {
        if (!canCreateUltraLightClassForFacade(files)) return null

        val filesToSupports: List<Pair<KtFile, KtUltraLightSupport>> = files.map {
            it to getUltraLightClassSupport(it)
        }

        return KtUltraLightClassForFacade(
            manager,
            facadeClassFqName,
            files,
            filesToSupports
        )
    }

    fun createUltraLightClass(element: KtClassOrObject): KtUltraLightClass? {
        if (element.shouldNotBeVisibleAsLightClass()) {
            return null
        }

        return getUltraLightClassSupport(element).let { support ->
            if (support.languageVersionSettings.getFlag(AnalysisFlags.eagerResolveOfLightClasses)) {
                val descriptor = resolveToDescriptor(element)
                (descriptor as? LazyClassDescriptor)?.forceResolveAllContents()
            }

            when {
                element is KtObjectDeclaration && element.isObjectLiteral() ->
                    KtUltraLightClassForAnonymousDeclaration(element, support)

                element.safeIsLocal() ->
                    KtUltraLightClassForLocalDeclaration(element, support)

                (element.hasModifier(KtTokens.INLINE_KEYWORD)) ->
                    KtUltraLightInlineClass(element, support)

                else -> KtUltraLightClass(element, support)
            }
        }
    }

    fun createUltraLightClassForScript(script: KtScript): KtUltraLightClassForScript =
        KtUltraLightClassForScript(script, support = getUltraLightClassSupport(script))

    companion object {
        @JvmStatic
        fun getInstance(project: Project): LightClassGenerationSupport {
            return project.getService(LightClassGenerationSupport::class.java)
        }
    }
}
