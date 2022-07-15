/*
 * Copyright 2010-2022 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

class C {
    fun foo(x: Int) = x
}

// CHECK: define void @"kfun:#main(){}"()
// CHECK-NOT: Int-box
// CHECK-NOT: Int-unbox
// CHECK: ret void
fun main() {
    val c = C()
    val fooref = c::foo
    if( fooref(42) == 42)
        println("ok")
}
