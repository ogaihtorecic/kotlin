/*
 * Copyright 2010-2022 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.ir.backend.js.export

import org.jetbrains.kotlin.ir.backend.js.JsIrBackendContext
import org.jetbrains.kotlin.ir.backend.js.lower.isBuiltInClass
import org.jetbrains.kotlin.ir.backend.js.lower.isStdLibClass
import org.jetbrains.kotlin.ir.declarations.*
import org.jetbrains.kotlin.ir.declarations.impl.*
import org.jetbrains.kotlin.ir.symbols.*
import org.jetbrains.kotlin.ir.types.*
import org.jetbrains.kotlin.ir.types.impl.IrSimpleTypeImpl
import org.jetbrains.kotlin.util.collectionUtils.filterIsInstanceAnd

private typealias SubstitutionMap = Map<IrTypeParameterSymbol, IrTypeArgument>
private typealias SymbolsBlackList = MutableSet<IrSymbol>

class TransitiveExportCollector(val context: JsIrBackendContext) {
    private val typesCaches = hashMapOf<ClassWithAppliedArguments, TransitiveHierarchyCollectionResult>()

    fun collectSuperTypesTransitiveHierarchyFor(type: IrSimpleType): TransitiveHierarchyCollectionResult {
        val classSymbol = type.classOrNull ?: return TransitiveHierarchyCollectionResult()

        return typesCaches.getOrPut(ClassWithAppliedArguments(classSymbol, type.arguments)) {
            type.collectSuperTypesTransitiveHierarchy(
                type.calculateTypeSubstitutionMap(emptyMap()),
                classSymbol.collectAlreadyOverriddenSymbols()
            )
        }
    }

    private fun IrClassSymbol.collectAlreadyOverriddenSymbols(): SymbolsBlackList {
        val declaration = owner as? IrClass ?: return mutableSetOf()
        return declaration.declarations
            .filterIsInstanceAnd<IrOverridableDeclaration<*>> { it.overriddenSymbols.isNotEmpty() }
            .flatMap { it.overriddenSymbols }
            .toMutableSet()
    }

    private fun IrSimpleType.collectSuperTypesTransitiveHierarchy(
        typeSubstitutionMap: SubstitutionMap,
        alreadyOverriddenSymbols: SymbolsBlackList,
    ): TransitiveHierarchyCollectionResult {
        val classifier = classOrNull ?: return TransitiveHierarchyCollectionResult()
        return classifier.superTypes()
            .mapNotNull { (it as? IrSimpleType)?.collectTransitiveHierarchy(typeSubstitutionMap, alreadyOverriddenSymbols) }
            .reduce(TransitiveHierarchyCollectionResult::plus)
    }

    private fun IrSimpleType.collectTransitiveHierarchy(
        typeSubstitutionMap: SubstitutionMap,
        alreadyOverriddenSymbols: SymbolsBlackList,
    ): TransitiveHierarchyCollectionResult {
        val owner = classifier.owner as? IrClass ?: return TransitiveHierarchyCollectionResult()
        val substitutionMap = calculateTypeSubstitutionMap(typeSubstitutionMap)
        return when {
            isBuiltInClass(owner) || isStdLibClass(owner) -> TransitiveHierarchyCollectionResult()
            owner.isExported(context) -> generateTransitiveHierarchyCollectionResult(owner, typeSubstitutionMap, alreadyOverriddenSymbols)
            else -> collectSuperTypesTransitiveHierarchy(substitutionMap, alreadyOverriddenSymbols)
        }
    }

    private fun IrSimpleType.generateTransitiveHierarchyCollectionResult(
        owner: IrClass,
        typeSubstitutionMap: SubstitutionMap,
        alreadyOverriddenSymbols: SymbolsBlackList,
    ): TransitiveHierarchyCollectionResult {
        val correctlyAppliedTypes = setOf(getTypeAppliedToRightTypeArguments(typeSubstitutionMap) as IrType)
        val declarationsToCopy = owner.declarations
            .filterIsInstanceAnd<IrOverridableDeclaration<*>> { it.shouldIncludeMember(alreadyOverriddenSymbols) }
            .map { it.getDeclarationWithAppliedTypeArguments(typeSubstitutionMap) }
            .toSet()

        if (declarationsToCopy.isNotEmpty()) {
            declarationsToCopy.forEach {
                alreadyOverriddenSymbols.addAll(it.overriddenSymbols)
            }
        }

        return TransitiveHierarchyCollectionResult(correctlyAppliedTypes, declarationsToCopy)
    }

    private fun IrType.getTypeAppliedToRightTypeArguments(typeSubstitutionMap: SubstitutionMap): IrTypeArgument {
        if (this !is IrSimpleType) return this as IrTypeArgument

        val classifier = when (val classifier = this.classifier) {
            is IrClassSymbol -> classifier
            is IrTypeParameterSymbol -> return typeSubstitutionMap[classifier] ?: this
            else -> return this
        }

        if (classifier.owner.typeParameters.isEmpty()) return this

        return IrSimpleTypeImpl(
            classifier,
            nullability,
            arguments.map { it.getSubstitution(typeSubstitutionMap) },
            this.annotations
        )
    }

    private fun IrOverridableDeclaration<*>.shouldIncludeMember(blackList: SymbolsBlackList): Boolean {
        return overriddenSymbols.any { (it.owner as IrDeclaration).isExported(context) && !blackList.contains(it) }
    }

    private fun IrOverridableDeclaration<*>.getDeclarationWithAppliedTypeArguments(typeSubstitutionMap: SubstitutionMap): IrOverridableDeclaration<*> {
        return when (this) {
            is IrProperty -> applyTypeArguments(typeSubstitutionMap)
            is IrSimpleFunction -> applyTypeArguments(typeSubstitutionMap)
            else -> error("Unexpected overridable member")
        }
    }

    private fun IrProperty.applyTypeArguments(typeSubstitutionMap: SubstitutionMap): IrProperty {
        return IrPropertyImpl(
            startOffset,
            endOffset,
            origin,
            symbol,
            name,
            visibility,
            modality,
            isVar,
            isConst,
            isLateinit,
            isDelegated,
            isExternal,
            isExpect,
            isFakeOverride,
            containerSource,
            factory
        ).apply {
            parent = this@applyTypeArguments.parent
            metadata = this@applyTypeArguments.metadata
            annotations = this@applyTypeArguments.annotations
            attributeOwnerId = this@applyTypeArguments.attributeOwnerId
            metadata = this@applyTypeArguments.metadata
            overriddenSymbols = this@applyTypeArguments.overriddenSymbols

            backingField = this@applyTypeArguments.backingField?.applyTypeArguments(typeSubstitutionMap)
            getter = this@applyTypeArguments.getter?.applyTypeArguments(typeSubstitutionMap)
            setter = this@applyTypeArguments.setter?.applyTypeArguments(typeSubstitutionMap)
        }
    }

    private fun IrField.applyTypeArguments(typeSubstitutionMap: SubstitutionMap): IrField {
        return IrFieldImpl(
            startOffset,
            endOffset,
            origin,
            symbol,
            name,
            type.getTypeAppliedToRightTypeArguments(typeSubstitutionMap) as IrType,
            visibility,
            isFinal,
            isExternal,
            isStatic,
            factory,
        )
    }

    private fun IrSimpleFunction.applyTypeArguments(typeSubstitutionMap: SubstitutionMap): IrSimpleFunction {
        return IrFunctionImpl(
            startOffset,
            endOffset,
            origin,
            symbol,
            name,
            visibility,
            modality,
            returnType.getTypeAppliedToRightTypeArguments(typeSubstitutionMap) as IrType,
            isInline,
            isExternal,
            isTailrec,
            isSuspend,
            isOperator,
            isInfix,
            isExpect,
            isFakeOverride,
            containerSource,
            factory
        ).apply {
            parent = this@applyTypeArguments.parent
            metadata = this@applyTypeArguments.metadata
            annotations = this@applyTypeArguments.annotations
            attributeOwnerId = this@applyTypeArguments.attributeOwnerId
            metadata = this@applyTypeArguments.metadata
            overriddenSymbols = this@applyTypeArguments.overriddenSymbols
            correspondingPropertySymbol = this@applyTypeArguments.correspondingPropertySymbol

            typeParameters = this@applyTypeArguments.typeParameters.map { t ->
                IrTypeParameterImpl(
                    t.startOffset,
                    t.endOffset,
                    t.origin,
                    t.symbol,
                    t.name,
                    t.index,
                    t.isReified,
                    t.variance,
                    t.factory,
                ).apply {
                    superTypes = t.superTypes.map { it.getTypeAppliedToRightTypeArguments(typeSubstitutionMap) as IrType }
                }
            }
            valueParameters = this@applyTypeArguments.valueParameters.map {
                IrValueParameterImpl(
                    it.startOffset,
                    it.endOffset,
                    it.origin,
                    it.symbol,
                    it.name,
                    it.index,
                    it.type.getTypeAppliedToRightTypeArguments(typeSubstitutionMap) as IrType,
                    it.varargElementType,
                    it.isCrossinline,
                    it.isNoinline,
                    it.isHidden,
                    it.isAssignable,
                    it.factory,
                )
            }
        }
    }

    private fun IrTypeArgument.getSubstitution(typeSubstitutionMap: SubstitutionMap): IrTypeArgument {
        return when (this) {
            is IrType -> getTypeAppliedToRightTypeArguments(typeSubstitutionMap)
            is IrTypeProjection -> type.getTypeAppliedToRightTypeArguments(typeSubstitutionMap)
            else -> this
        }
    }

    private fun IrSimpleType.calculateTypeSubstitutionMap(typeSubstitutionMap: SubstitutionMap): SubstitutionMap {
        val classifier = classOrNull ?: error("Unexpected classifier $classifier for collecting transitive hierarchy")

        return typeSubstitutionMap + classifier.owner.typeParameters.zip(arguments).associate {
            it.first.symbol to it.second.getSubstitution(typeSubstitutionMap)
        }
    }

    private data class ClassWithAppliedArguments(val classSymbol: IrClassSymbol, val appliedArguments: List<IrTypeArgument>)

    data class TransitiveHierarchyCollectionResult(
        val superTypes: Set<IrType> = emptySet(),
        val transitivelyOverriddenDeclarations: Set<IrOverridableDeclaration<*>> = emptySet()
    ) {
        operator fun plus(another: TransitiveHierarchyCollectionResult): TransitiveHierarchyCollectionResult {
            return TransitiveHierarchyCollectionResult(
                superTypes + another.superTypes,
                transitivelyOverriddenDeclarations + another.transitivelyOverriddenDeclarations
            )
        }
    }
}
