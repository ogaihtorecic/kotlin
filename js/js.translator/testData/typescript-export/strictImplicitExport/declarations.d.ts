declare namespace JS_TESTS {
    type Nullable<T> = T | null | undefined
    namespace foo {
        interface NonExportedInterface {
            readonly __doNotUseOrImplementIt: { readonly NonExportedInterface: unique symbol };
        }
        interface NonExportedGenericInterface<T> {
            readonly __doNotUseOrImplementIt: { readonly NonExportedGenericInterface: unique symbol }
        }
        interface NonExportedType {
            readonly __doNotUseOrImplementIt: { readonly NonExportedType: unique symbol }
        }
        interface NonExportedGenericType<T> {
            readonly __doNotUseOrImplementIt: { readonly NonExportedGenericType: unique symbol }
        }
        interface NotExportedChildClass extends NonExportedInterface, NonExportedType {
            readonly __doNotUseOrImplementIt: { readonly NotExportedChildClass: unique symbol } & NonExportedInterface["__doNotUseOrImplementIt"] & NonExportedType["__doNotUseOrImplementIt"]
        }
        interface NotExportedChildGenericClass<T> extends NonExportedInterface, NonExportedGenericInterface<T>, NonExportedGenericType<T> {
            readonly __doNotUseOrImplementIt: { readonly NotExportedChildGenericClass: unique symbol } & NonExportedInterface["__doNotUseOrImplementIt"] & NonExportedGenericInterface<T>["__doNotUseOrImplementIt"] & NonExportedGenericType<T>["__doNotUseOrImplementIt"]
        }

        interface ExportedInterface {
            readonly __doNotUseOrImplementIt: { readonly ExportedInterface: unique symbol };
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
        class B implements foo.NonExportedType {
            constructor(v: number);
            readonly __doNotUseOrImplementIt: foo.NonExportedType["__doNotUseOrImplementIt"]
        }
        class C implements foo.NonExportedInterface {
            constructor();
            readonly __doNotUseOrImplementIt: foo.NonExportedInterface["__doNotUseOrImplementIt"]
        }
        class D implements foo.ExportedInterface, foo.NonExportedInterface {
            constructor();
            readonly __doNotUseOrImplementIt: foo.ExportedInterface["__doNotUseOrImplementIt"] & foo.NonExportedInterface["__doNotUseOrImplementIt"]
        }
        class E  implements foo.NonExportedType, foo.ExportedInterface {
            constructor();
            readonly __doNotUseOrImplementIt: foo.NonExportedType["__doNotUseOrImplementIt"] & foo.ExportedInterface["__doNotUseOrImplementIt"]
        }
        class F extends foo.A implements foo.NonExportedInterface {
            constructor();
            readonly __doNotUseOrImplementIt: foo.NonExportedInterface["__doNotUseOrImplementIt"]
        }
        class G implements foo.NonExportedGenericInterface<foo.NonExportedType> {
            constructor();
            readonly __doNotUseOrImplementIt: foo.NonExportedGenericInterface<foo.NonExportedType>["__doNotUseOrImplementIt"]
        }
        class H implements foo.NonExportedGenericType<foo.NonExportedType> {
            constructor();
            readonly __doNotUseOrImplementIt: foo.NonExportedGenericType<foo.NonExportedType>["__doNotUseOrImplementIt"]
        }
        class I implements foo.NotExportedChildClass {
            constructor();
            readonly __doNotUseOrImplementIt: foo.NotExportedChildClass["__doNotUseOrImplementIt"]
        }
        class J implements foo.NotExportedChildGenericClass<foo.NonExportedType> {
            constructor();
            readonly __doNotUseOrImplementIt: foo.NotExportedChildGenericClass<foo.NonExportedType>["__doNotUseOrImplementIt"]
        }
        function baz(a: number): Promise<number>;
        function bar(): Error;
        const console: Console;
        const error: WebAssembly.CompileError;
    }
}
