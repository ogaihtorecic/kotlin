/*
 * Copyright 2010-2019 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.js.resolve.diagnostics

import org.jetbrains.kotlin.descriptors.ClassDescriptor
import org.jetbrains.kotlin.descriptors.DeclarationDescriptor
import org.jetbrains.kotlin.descriptors.PackageFragmentDescriptor
import org.jetbrains.kotlin.js.translate.utils.AnnotationsUtils
import org.jetbrains.kotlin.psi.KtDeclaration
import org.jetbrains.kotlin.resolve.DescriptorUtils
import org.jetbrains.kotlin.resolve.checkers.DeclarationChecker
import org.jetbrains.kotlin.resolve.checkers.DeclarationCheckerContext
import org.jetbrains.kotlin.resolve.descriptorUtil.isInsidePrivateClass
import org.jetbrains.kotlin.resolve.source.getPsi

object JsNotExportAnnotationChecker : DeclarationChecker {
    override fun check(declaration: KtDeclaration, descriptor: DeclarationDescriptor, context: DeclarationCheckerContext) {
        val trace = context.trace

        val jsNotExport = AnnotationsUtils.getJsNotExportAnnotation(descriptor) ?: return
        val jsNotExportPsi = jsNotExport.source.getPsi() ?: declaration

        val declarationParent = descriptor.containingDeclaration

        if (declarationParent !is ClassDescriptor) {
            trace.report(ErrorsJs.NON_CLASS_MEMBER_USAGE_OF_JS_NOT_EXPORT.on(jsNotExportPsi))
        } else if (!AnnotationsUtils.isExportedObject(declarationParent, trace.bindingContext)) {
            trace.report(ErrorsJs.JS_NOT_EXPORT_ON_NOT_EXPORTED_CLASS.on(jsNotExportPsi))
        }
    }
}
