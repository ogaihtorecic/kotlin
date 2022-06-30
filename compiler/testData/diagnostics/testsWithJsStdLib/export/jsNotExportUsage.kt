// !OPT_IN: kotlin.js.ExperimentalJsExport

package foo

class NotExported {
    <!JS_NOT_EXPORT_ON_NOT_EXPORTED_CLASS!>@JsNotExport<!>
    fun foo(): String = "Foo"
}

@JsExport
class Parent {
    @JsNotExport
    class NotExported {
        <!JS_NOT_EXPORT_ON_NOT_EXPORTED_CLASS!>@JsNotExport<!>
        fun foo(): String = "Foo"
    }
}

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
