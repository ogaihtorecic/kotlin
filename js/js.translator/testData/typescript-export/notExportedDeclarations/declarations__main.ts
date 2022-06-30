import OnlyFooParamExported = JS_TESTS.foo.OnlyFooParamExported;


function assert(condition: boolean) {
    if (!condition) {
        throw "Assertion failed";
    }
}

function box(): string {
    assert(new OnlyFooParamExported("TEST").foo === "TEST")
    return "OK";
}