/*
 * Copyright 2010-2019 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.js.resolve.diagnostics

import org.jetbrains.kotlin.descriptors.ClassDescriptor
import org.jetbrains.kotlin.descriptors.DeclarationDescriptor
import org.jetbrains.kotlin.descriptors.PropertyDescriptor
import org.jetbrains.kotlin.js.translate.utils.AnnotationsUtils
import org.jetbrains.kotlin.psi.KtDeclaration
import org.jetbrains.kotlin.resolve.DescriptorUtils
import org.jetbrains.kotlin.resolve.checkers.DeclarationChecker
import org.jetbrains.kotlin.resolve.checkers.DeclarationCheckerContext
import org.jetbrains.kotlin.types.checker.SimpleClassicTypeSystemContext.isMarkedNullable

object JsOptionalAnnotationChecker : DeclarationChecker {
    override fun check(declaration: KtDeclaration, descriptor: DeclarationDescriptor, context: DeclarationCheckerContext) {
        if (AnnotationsUtils.getJsOptionalAnnotation(descriptor) == null) return

        val property = descriptor as PropertyDescriptor

        if (!property.type.isMarkedNullable()) {
            context.trace.report(ErrorsJs.NON_NULLABLE_OPTIONAL.on(declaration))
        }

        var topLevelParent: DeclarationDescriptor? = descriptor.containingDeclaration

        if (topLevelParent !is ClassDescriptor) {
            context.trace.report(ErrorsJs.NON_MEMBER_PROPERTY_OPTIONAL.on(declaration))
        }

        while (topLevelParent != null && !DescriptorUtils.isTopLevelDeclaration(topLevelParent)) {
            topLevelParent = topLevelParent.containingDeclaration
        }

        if (topLevelParent != null && AnnotationsUtils.getJsExportAnnotation(topLevelParent) == null) {
            context.trace.report(ErrorsJs.NOT_EXPORTED_OPTIONAL.on(declaration))
        }
    }
}
