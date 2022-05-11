/*
 * Copyright 2010-2022 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.backend.konan.lower

import org.jetbrains.kotlin.backend.konan.Context
import org.jetbrains.kotlin.backend.konan.DECLARATION_ORIGIN_FUNCTION_CLASS
import org.jetbrains.kotlin.backend.konan.serialization.KonanIrLinker
import org.jetbrains.kotlin.backend.konan.serialization.KonanManglerIr
import org.jetbrains.kotlin.descriptors.DescriptorVisibilities
import org.jetbrains.kotlin.ir.IrElement
import org.jetbrains.kotlin.ir.declarations.IrClass
import org.jetbrains.kotlin.ir.declarations.IrExternalPackageFragment
import org.jetbrains.kotlin.ir.declarations.IrFunction
import org.jetbrains.kotlin.ir.expressions.IrCall
import org.jetbrains.kotlin.ir.expressions.IrFunctionReference
import org.jetbrains.kotlin.ir.expressions.IrPropertyReference
import org.jetbrains.kotlin.ir.util.getPackageFragment
import org.jetbrains.kotlin.ir.util.isFakeOverride
import org.jetbrains.kotlin.ir.util.isInterface
import org.jetbrains.kotlin.ir.visitors.IrElementVisitorVoid
import org.jetbrains.kotlin.ir.visitors.acceptChildrenVoid
import org.jetbrains.kotlin.ir.visitors.acceptVoid

internal class CacheInfoBuilder(private val context: Context, private val moduleDeserializer: KonanIrLinker.KonanPartialModuleDeserializer) {
    fun build() = with(moduleDeserializer) {
        moduleFragment.acceptChildrenVoid(object : IrElementVisitorVoid {
            override fun visitElement(element: IrElement) {
                element.acceptChildrenVoid(this)
            }

            override fun visitClass(declaration: IrClass) {
                declaration.acceptChildrenVoid(this)

                if (!declaration.isInterface && declaration.visibility != DescriptorVisibilities.LOCAL
                        && declaration.isExported && declaration.origin != DECLARATION_ORIGIN_FUNCTION_CLASS
                ) {
                    context.classFields.add(buildClassFields(declaration, context.getLayoutBuilder(declaration).getDeclaredFields()))
                }
            }

            override fun visitFunction(declaration: IrFunction) {
                declaration.acceptChildrenVoid(this)

                if (!declaration.isFakeOverride && declaration.isExportedInlineFunction) {
                    context.inlineFunctionBodies.add(buildInlineFunctionReference(declaration))
                    trackCallees(declaration)
                }
            }

            private val IrClass.isExported
                get() = with(KonanManglerIr) { isExported(compatibilityMode.oldSignatures) }

            private val IrFunction.isExportedInlineFunction
                get() = isInline && with(KonanManglerIr) { isExported(compatibilityMode.oldSignatures) }
        })
    }

    private fun trackCallees(irFunction: IrFunction) {
        irFunction.acceptVoid(object : IrElementVisitorVoid {
            override fun visitElement(element: IrElement) {
                element.acceptChildrenVoid(this)
            }

            private fun processFunction(function: IrFunction) {
                if (function.getPackageFragment() !is IrExternalPackageFragment)
                    context.calledFromExportedInlineFunctions.add(function)
            }

            override fun visitCall(expression: IrCall) {
                expression.acceptChildrenVoid(this)

                processFunction(expression.symbol.owner)
            }

            override fun visitFunctionReference(expression: IrFunctionReference) {
                expression.acceptChildrenVoid(this)

                processFunction(expression.symbol.owner)
            }

            override fun visitPropertyReference(expression: IrPropertyReference) {
                expression.acceptChildrenVoid(this)

                expression.getter?.owner?.let { processFunction(it) }
                expression.setter?.owner?.let { processFunction(it) }
            }
        })
    }
}