declare namespace JS_TESTS {
    type Nullable<T> = T | null | undefined
    namespace foo {
        class OnlyFooParamExported {
            constructor(foo: string);
            get foo(): string;
        }
        interface ExportedInterface {
            readonly __doNotUseOrImplementIt: {
                readonly "foo.ExportedInterface": unique symbol;
            };
        }
    }
}