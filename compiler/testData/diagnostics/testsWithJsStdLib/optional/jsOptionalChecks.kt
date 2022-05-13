// !OPT_IN: kotlin.js.ExperimentalJsExport

package foo

@JsExport
class RegularCase {
    @JsOptional var value: Int? = null
}


<!NON_MEMBER_PROPERTY_OPTIONAL!>@JsExport
@JsOptional
val topLevelParameter: Int? = 2<!>

class NotExportedClass {
    <!NOT_EXPORTED_OPTIONAL!>@JsOptional var value: Int? = null<!>
}

class NotExporetedParent {
    class NestedNotExportedClass {
        <!NOT_EXPORTED_OPTIONAL!>@JsOptional var value: Int? = null<!>
    }

    companion object {
        <!NOT_EXPORTED_OPTIONAL!>@JsOptional var value: String? = null<!>
    }
}

@JsExport
class ExportedParent {
    class NestedExportedClass {
        @JsOptional var value: Int? = null
    }
    companion object {
        @JsOptional var value: String? = null
    }
}

@JsExport
class NonNullableType {
    <!NON_NULLABLE_OPTIONAL!>@JsOptional var value: Int = 42<!>
}