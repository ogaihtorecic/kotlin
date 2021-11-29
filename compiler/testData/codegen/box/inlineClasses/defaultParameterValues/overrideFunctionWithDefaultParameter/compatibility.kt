// WITH_STDLIB

// !JVM_DEFAULT_MODE: compatibility
// TARGET_BACKEND: JVM
// IGNORE_BACKEND: JVM
// JVM_TARGET: 1.8

interface Path {
    fun dispatch(maxDepth: Int = 42)
    fun Int.extension(maxDepth: Int = 42)
}

@JvmInline
value class RealPath(val x: Int) : Path {
    override fun dispatch(maxDepth: Int) = Unit

    fun childrenDispatch(recursively: Boolean): Unit =
        if (recursively) dispatch() else dispatch()

    override fun Int.extension(maxDepth: Int) = Unit

    fun Int.childrenExtension(recursively: Boolean): Unit =
        if (recursively) extension() else extension()
}

fun box(): String {
    RealPath(1)
    return "OK"
}
