// This test only fails on native because the test framework compiles all
// the files in this directory together which leads to redeclaration errors.
// IGNORE_BACKEND: NATIVE
// IGNORE_BACKEND: JS_IR
// MODULE: lib
// FILE: 2.kt
abstract class A {
    protected val value = "O"
    fun f(k: String = "K") = value + k
}

abstract class B : A()

// FILE: 3.kt
abstract class C : B()

// MODULE: main(lib)
// FILE: 1.kt
class D : C()

fun box(): String = D().f()

// FILE: 2.kt
abstract class A {
    protected val value = "O"
    fun f(k: String = "K") = value + k
}

abstract class B : A()
