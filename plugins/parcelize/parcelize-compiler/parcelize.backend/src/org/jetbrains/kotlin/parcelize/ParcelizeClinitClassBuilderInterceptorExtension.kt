/*
 * Copyright 2010-2022 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.parcelize

import com.intellij.psi.PsiElement
import org.jetbrains.kotlin.codegen.AbstractClassBuilder
import org.jetbrains.kotlin.codegen.ClassBuilder
import org.jetbrains.kotlin.codegen.ClassBuilderFactory
import org.jetbrains.kotlin.codegen.DelegatingClassBuilder
import org.jetbrains.kotlin.codegen.extensions.ClassBuilderInterceptorExtension
import org.jetbrains.kotlin.diagnostics.DiagnosticSink
import org.jetbrains.kotlin.psi.KtClassOrObject
import org.jetbrains.kotlin.resolve.BindingContext
import org.jetbrains.kotlin.resolve.jvm.diagnostics.JvmDeclarationOrigin
import org.jetbrains.org.objectweb.asm.MethodVisitor
import org.jetbrains.org.objectweb.asm.Opcodes
import org.jetbrains.org.objectweb.asm.RecordComponentVisitor
import org.jetbrains.org.objectweb.asm.Type
import org.jetbrains.org.objectweb.asm.commons.InstructionAdapter

class ParcelizeClinitClassBuilderInterceptorExtension : ClassBuilderInterceptorExtension {
    override fun interceptClassBuilderFactory(
        interceptedFactory: ClassBuilderFactory,
        bindingContext: BindingContext,
        diagnostics: DiagnosticSink
    ): ClassBuilderFactory {
        return ParcelableClinitClassBuilderFactory(interceptedFactory, bindingContext)
    }

    private inner class ParcelableClinitClassBuilderFactory(
        private val delegateFactory: ClassBuilderFactory,
        val bindingContext: BindingContext
    ) : ClassBuilderFactory {

        override fun newClassBuilder(origin: JvmDeclarationOrigin): ClassBuilder {
            return AndroidOnDestroyCollectorClassBuilder(origin, delegateFactory.newClassBuilder(origin), bindingContext)
        }

        override fun getClassBuilderMode() = delegateFactory.classBuilderMode

        override fun asText(builder: ClassBuilder?): String? {
            return delegateFactory.asText((builder as AndroidOnDestroyCollectorClassBuilder).delegateClassBuilder)
        }

        override fun asBytes(builder: ClassBuilder?): ByteArray? {
            return delegateFactory.asBytes((builder as AndroidOnDestroyCollectorClassBuilder).delegateClassBuilder)
        }

        override fun close() {
            delegateFactory.close()
        }
    }

    private class AndroidOnDestroyCollectorClassBuilder(
        val declarationOrigin: JvmDeclarationOrigin,
        val delegateClassBuilder: ClassBuilder,
        val bindingContext: BindingContext
    ) : DelegatingClassBuilder() {
        private var currentClass: KtClassOrObject? = null
        private var currentClassName: String? = null
        private var isClinitGenerated = false

        override fun getDelegate() = delegateClassBuilder

        override fun defineClass(
            origin: PsiElement?,
            version: Int,
            access: Int,
            name: String,
            signature: String?,
            superName: String,
            interfaces: Array<out String>
        ) {
            currentClass = origin as? KtClassOrObject
            currentClassName = name
            isClinitGenerated = false

            super.defineClass(origin, version, access, name, signature, superName, interfaces)
        }

        override fun done() {
            if (!isClinitGenerated && currentClass != null && currentClassName != null) {
                val descriptor = bindingContext[BindingContext.CLASS, currentClass]
                if (descriptor != null && declarationOrigin.descriptor == descriptor && descriptor.isParcelize) {
                    val baseVisitor = super.newMethod(JvmDeclarationOrigin.NO_ORIGIN, Opcodes.ACC_STATIC, "<clinit>", "()V", null, null)
                    val visitor = ClinitAwareMethodVisitor(currentClassName!!, baseVisitor)

                    visitor.visitCode()
                    visitor.visitInsn(Opcodes.RETURN)
                    visitor.visitEnd()
                }
            }

            super.done()
        }

        override fun newMethod(
            origin: JvmDeclarationOrigin,
            access: Int,
            name: String,
            desc: String,
            signature: String?,
            exceptions: Array<out String>?
        ): MethodVisitor {
            if (name == "<clinit>" && currentClass != null && currentClassName != null) {
                isClinitGenerated = true

                val descriptor = bindingContext[BindingContext.CLASS, currentClass]
                if (descriptor != null && declarationOrigin.descriptor == descriptor && descriptor.isParcelize) {
                    return ClinitAwareMethodVisitor(
                        currentClassName!!,
                        super.newMethod(origin, access, name, desc, signature, exceptions)
                    )
                }
            }

            return super.newMethod(origin, access, name, desc, signature, exceptions)
        }

        override fun newRecordComponent(name: String, desc: String, signature: String?): RecordComponentVisitor {
            return AbstractClassBuilder.EMPTY_RECORD_VISITOR
        }
    }

    private class ClinitAwareMethodVisitor(val parcelableName: String, mv: MethodVisitor) : MethodVisitor(Opcodes.API_VERSION, mv) {
        override fun visitInsn(opcode: Int) {
            if (opcode == Opcodes.RETURN) {
                val iv = InstructionAdapter(this)
                val creatorName = "$parcelableName\$Creator"
                val creatorType = Type.getObjectType(creatorName)

                iv.anew(creatorType)
                iv.dup()
                iv.invokespecial(creatorName, "<init>", "()V", false)
                iv.putstatic(parcelableName, "CREATOR", "Landroid/os/Parcelable\$Creator;")
            }

            super.visitInsn(opcode)
        }
    }
}
