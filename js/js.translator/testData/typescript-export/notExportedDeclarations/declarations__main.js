"use strict";
var OnlyFooParamExported = JS_TESTS.foo.OnlyFooParamExported;
function assert(condition) {
    if (!condition) {
        throw "Assertion failed";
    }
}
function box() {
    assert(new OnlyFooParamExported("TEST").foo === "TEST");
    return "OK";
}
