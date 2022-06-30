// !OPT_IN: kotlin.js.ExperimentalJsExport
@file:JsExport
package foo

<!NON_CLASS_MEMBER_USAGE_OF_JS_NOT_EXPORT!>@JsNotExport<!>
fun topLevelFun() {}

<!NON_CLASS_MEMBER_USAGE_OF_JS_NOT_EXPORT!>@JsNotExport<!>
val pep: String = "Test"

<!NON_CLASS_MEMBER_USAGE_OF_JS_NOT_EXPORT!>@JsNotExport<!>
class A

<!NON_CLASS_MEMBER_USAGE_OF_JS_NOT_EXPORT!>@JsNotExport<!>
interface IA