declare namespace JS_TESTS {
    type Nullable<T> = T | null | undefined
    const __doNotImplementIt: unique symbol
    type __doNotImplementIt = typeof __doNotImplementIt
    namespace foo {
        type NonExportedInterface = { __thePropertyDoesntExist: unique symbol }
        type NonExportedGenericInterface<T> = { __thePropertyDoesntExist: unique symbol }
        type NonExportedType = { __thePropertyDoesntExist: unique symbol }
        type NonExportedGenericType<T> = { __thePropertyDoesntExist: unique symbol }
        type NotExportedChildClass = { __thePropertyDoesntExist: unique symbol } & NonExportedInterface & NonExportedType
        type NotExportedChildGenericClass<T> = { __thePropertyDoesntExist: unique symbol } & NonExportedInterface & NonExportedGenericInterface<T> & NonExportedGenericType<T>

        interface ExportedInterface {
            readonly __doNotUseIt: __doNotImplementIt;
        }
        function producer(value: number): foo.NonExportedType;
        function consumer(value: foo.NonExportedType): number;
        function childProducer(value: number): foo.NotExportedChildClass;
        function childConsumer(value: foo.NotExportedChildClass): number;
        function genericChildProducer<T extends NonExportedGenericType<number>>(value: number): foo.NotExportedChildGenericClass<T>;
        function genericChildConsumer<T extends NonExportedGenericType<number>>(value: foo.NotExportedChildGenericClass<T>): number;
        class A {
            constructor(value: foo.NonExportedType);
            get value(): foo.NonExportedType;
            set value(value: foo.NonExportedType);
            increment<T>(t: T): foo.NonExportedType;
        }
        class B /* extends foo.NonExportedType */ {
            constructor(v: number);
        }
        class C /* implements foo.NonExportedInterface */ {
            constructor();
        }
        class D implements foo.ExportedInterface/*, foo.NonExportedInterface */ {
            constructor();
            readonly __doNotUseIt: __doNotImplementIt;
        }
        class E /* extends foo.NonExportedType */ implements foo.ExportedInterface {
            constructor();
            readonly __doNotUseIt: __doNotImplementIt;
        }
        class F extends foo.A /* implements foo.NonExportedInterface */ {
            constructor();
        }
        class G /* implements foo.NonExportedGenericInterface<foo.NonExportedType> */ {
            constructor();
        }
        class H /* extends foo.NonExportedGenericType<foo.NonExportedType> */ {
            constructor();
        }
        function baz(a: number): Promise<number>;
        function bar(): Error;
        const console: Console;
        const error: WebAssembly.CompileError;
    }
}
