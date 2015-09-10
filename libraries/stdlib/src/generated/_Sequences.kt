package kotlin

//
// NOTE THIS FILE IS AUTO-GENERATED by the GenerateStandardLib.kt
// See: https://github.com/JetBrains/kotlin/tree/master/libraries/stdlib
//

import kotlin.platform.*
import java.util.*

import java.util.Collections // TODO: it's temporary while we have java.util.Collections in js

/**
 * Returns a sequence from the given collection.
 */
public fun <T> Array<out T>.asSequence(): Sequence<T> {
    if (isEmpty()) return emptySequence()
    return object : Sequence<T> {
        override fun iterator(): Iterator<T> {
            return this@asSequence.iterator()
        }
    }
}

/**
 * Returns a sequence from the given collection.
 */
public fun BooleanArray.asSequence(): Sequence<Boolean> {
    if (isEmpty()) return emptySequence()
    return object : Sequence<Boolean> {
        override fun iterator(): Iterator<Boolean> {
            return this@asSequence.iterator()
        }
    }
}

/**
 * Returns a sequence from the given collection.
 */
public fun ByteArray.asSequence(): Sequence<Byte> {
    if (isEmpty()) return emptySequence()
    return object : Sequence<Byte> {
        override fun iterator(): Iterator<Byte> {
            return this@asSequence.iterator()
        }
    }
}

/**
 * Returns a sequence from the given collection.
 */
public fun CharArray.asSequence(): Sequence<Char> {
    if (isEmpty()) return emptySequence()
    return object : Sequence<Char> {
        override fun iterator(): Iterator<Char> {
            return this@asSequence.iterator()
        }
    }
}

/**
 * Returns a sequence from the given collection.
 */
public fun DoubleArray.asSequence(): Sequence<Double> {
    if (isEmpty()) return emptySequence()
    return object : Sequence<Double> {
        override fun iterator(): Iterator<Double> {
            return this@asSequence.iterator()
        }
    }
}

/**
 * Returns a sequence from the given collection.
 */
public fun FloatArray.asSequence(): Sequence<Float> {
    if (isEmpty()) return emptySequence()
    return object : Sequence<Float> {
        override fun iterator(): Iterator<Float> {
            return this@asSequence.iterator()
        }
    }
}

/**
 * Returns a sequence from the given collection.
 */
public fun IntArray.asSequence(): Sequence<Int> {
    if (isEmpty()) return emptySequence()
    return object : Sequence<Int> {
        override fun iterator(): Iterator<Int> {
            return this@asSequence.iterator()
        }
    }
}

/**
 * Returns a sequence from the given collection.
 */
public fun LongArray.asSequence(): Sequence<Long> {
    if (isEmpty()) return emptySequence()
    return object : Sequence<Long> {
        override fun iterator(): Iterator<Long> {
            return this@asSequence.iterator()
        }
    }
}

/**
 * Returns a sequence from the given collection.
 */
public fun ShortArray.asSequence(): Sequence<Short> {
    if (isEmpty()) return emptySequence()
    return object : Sequence<Short> {
        override fun iterator(): Iterator<Short> {
            return this@asSequence.iterator()
        }
    }
}

/**
 * Returns a sequence from the given collection.
 */
public fun <T> Iterable<T>.asSequence(): Sequence<T> {
    return object : Sequence<T> {
        override fun iterator(): Iterator<T> {
            return this@asSequence.iterator()
        }
    }
}

/**
 * Returns a sequence from the given collection.
 */
public fun <K, V> Map<K, V>.asSequence(): Sequence<Map.Entry<K, V>> {
    return object : Sequence<Map.Entry<K, V>> {
        override fun iterator(): Iterator<Map.Entry<K, V>> {
            return this@asSequence.iterator()
        }
    }
}

/**
 * Returns a sequence from the given collection.
 */
public fun <T> Sequence<T>.asSequence(): Sequence<T> {
    return this
}

/**
 * Returns a sequence from the given collection.
 */
public fun String.asSequence(): Sequence<Char> {
    if (isEmpty()) return emptySequence()
    return object : Sequence<Char> {
        override fun iterator(): Iterator<Char> {
            return this@asSequence.iterator()
        }
    }
}

/**
 * Returns a sequence from the given collection
 */
@Deprecated("Use asSequence() instead", ReplaceWith("asSequence()"))
public fun <T> Array<out T>.sequence(): Sequence<T> {
    return asSequence()
}

/**
 * Returns a sequence from the given collection
 */
@Deprecated("Use asSequence() instead", ReplaceWith("asSequence()"))
public fun BooleanArray.sequence(): Sequence<Boolean> {
    return asSequence()
}

/**
 * Returns a sequence from the given collection
 */
@Deprecated("Use asSequence() instead", ReplaceWith("asSequence()"))
public fun ByteArray.sequence(): Sequence<Byte> {
    return asSequence()
}

/**
 * Returns a sequence from the given collection
 */
@Deprecated("Use asSequence() instead", ReplaceWith("asSequence()"))
public fun CharArray.sequence(): Sequence<Char> {
    return asSequence()
}

/**
 * Returns a sequence from the given collection
 */
@Deprecated("Use asSequence() instead", ReplaceWith("asSequence()"))
public fun DoubleArray.sequence(): Sequence<Double> {
    return asSequence()
}

/**
 * Returns a sequence from the given collection
 */
@Deprecated("Use asSequence() instead", ReplaceWith("asSequence()"))
public fun FloatArray.sequence(): Sequence<Float> {
    return asSequence()
}

/**
 * Returns a sequence from the given collection
 */
@Deprecated("Use asSequence() instead", ReplaceWith("asSequence()"))
public fun IntArray.sequence(): Sequence<Int> {
    return asSequence()
}

/**
 * Returns a sequence from the given collection
 */
@Deprecated("Use asSequence() instead", ReplaceWith("asSequence()"))
public fun LongArray.sequence(): Sequence<Long> {
    return asSequence()
}

/**
 * Returns a sequence from the given collection
 */
@Deprecated("Use asSequence() instead", ReplaceWith("asSequence()"))
public fun ShortArray.sequence(): Sequence<Short> {
    return asSequence()
}

/**
 * Returns a sequence from the given collection
 */
@Deprecated("Use asSequence() instead", ReplaceWith("asSequence()"))
public fun <T> Iterable<T>.sequence(): Sequence<T> {
    return asSequence()
}

/**
 * Returns a sequence from the given collection
 */
@Deprecated("Use asSequence() instead", ReplaceWith("asSequence()"))
public fun <K, V> Map<K, V>.sequence(): Sequence<Map.Entry<K, V>> {
    return asSequence()
}

/**
 * Returns a sequence from the given collection
 */
@Deprecated("Use asSequence() instead", ReplaceWith("asSequence()"))
public fun <T> Sequence<T>.sequence(): Sequence<T> {
    return this
}

/**
 * Returns a sequence from the given collection
 */
@Deprecated("Use asSequence() instead", ReplaceWith("asSequence()"))
public fun String.sequence(): Sequence<Char> {
    return asSequence()
}

