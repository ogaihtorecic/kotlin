/*
 * Copyright 2010-2022 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.project.model.utils

import org.jetbrains.kotlin.project.model.infra.KpmTestEntity

class ObservableIndexedSet<T : KpmTestEntity> private constructor(
    val items: MutableMap<String, T>
) : Collection<T> by items.values {
    constructor() : this(mutableMapOf())

    private val allItemsActions = mutableListOf<T.() -> Unit>()

    fun add(item: T) {
        allItemsActions.forEach { action -> action(item) }
        items[item.name] = item
    }

    fun withAll(action: T.() -> Unit) {
        items.values.forEach(action)
        allItemsActions.add(action)
    }

    fun getOrPut(name: String, defaultValue: () -> T): T = items.getOrPut(name, defaultValue)

    operator fun get(name: String): T? = items[name]
    operator fun set(name: String, value: T) {
        items[name] = value
    }

    fun <V : KpmTestEntity> mapValuesTo(other: ObservableIndexedSet<V>, action: (T) -> V) =
        other.items.putAll(items.mapValues { action(it.value) })
}
