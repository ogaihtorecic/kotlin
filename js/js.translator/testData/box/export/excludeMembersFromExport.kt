// RUN_PLAIN_BOX_FUNCTION
// IGNORE_BACKEND: JS

// MODULE: lib
// FILE: lib.kt
@JsExport
class Bar(val value: String) {
    @JsNotExport
    constructor(): this("SECONDARY")

    @JsNotExport
    val excludedValue: Int = 42

    fun foo(): String = "FOO"

    @JsNotExport
    fun excludedFun(): String = "EXCLUDED_FUN"

    class Nested

    @JsNotExport
    class ExcludedNested {
        fun doSomething(): String = "SOMETHING"
    }

    companion object {
        fun baz(): String = "BAZ"

        @JsNotExport
        fun excludedFun(): String = "STATIC EXCLUDED_FUN"
    }
}

// MODULE: main(lib)
// FILE: main.js
function box() {
    var Bar = kotlin_lib.Bar;
    var bar = new Bar("TEST");

    if (bar.value !== "TEST") return "Error: exported property was not exported"
    if (bar.excludedValue === 42) return "Error: not exported property was exported"

    if (bar.foo() !== "FOO") return "Error: exported function was not exported"
    if (typeof bar.excludedFun === "function") return "Error: not exported function was exported"

    if (typeof Bar.Nested !== "function") return "Error: exported nested class was not exported"
    if (typeof Bar.ExcludedNested === "function") return "Error: not exported nested class was exported"

    if (typeof Bar.Companion.bar() !== "BAZ") return "Error: exported companion function was not exported"
    if (typeof Bar.Companion.excludedFun === "function") return "Error: not exported companion function was exported"

    return "OK"
}