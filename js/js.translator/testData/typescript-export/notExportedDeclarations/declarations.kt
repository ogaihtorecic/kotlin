// CHECK_TYPESCRIPT_DECLARATIONS
// RUN_PLAIN_BOX_FUNCTION
// SKIP_MINIFICATION
// SKIP_NODE_JS
// INFER_MAIN_MODULE
// MODULE: JS_TESTS
// WITH_STDLIB
// FILE: declarations.kt

package foo

@JsExport
class OnlyFooParamExported(val foo: String) {
    @JsNotExport
    constructor() : this("TEST")

    @JsNotExport
    inline fun <A, reified B> A.notExportableReified(): Boolean = this is B

    @JsNotExport
    suspend fun notExportableSuspend(): String = "SuspendResult"

    @JsNotExport
    fun notExportableReturn(): List<String> = listOf("1", "2")

    @JsNotExport
    val String.notExportableExentsionProperty: String
        get() = "notExportableExentsionProperty"

    @JsNotExport
    annotation class NotExportableAnnotation

    @JsNotExport
    value class NotExportableInlineClass(val value: Int)
}

@JsExport
interface ExportedInterface {
    @JsNotExport
    class NotExportableNestedInsideInterface

    @JsNotExport
    companion object {
        val foo: String ="FOO"
    }
}
