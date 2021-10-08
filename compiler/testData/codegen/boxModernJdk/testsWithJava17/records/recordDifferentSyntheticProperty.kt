// ENABLE_JVM_PREVIEW
// IGNORE_BACKEND_FIR: JVM_IR
// FILE: MyRec.java
public record MyRec(String name) {
    public String getName() {
        return "OK";
    }
}

// FILE: main.kt
fun box(): String {
    val r = MyRec("fail")
    if (r.name() != "fail") return "fail 1"
    if (r.getName() != "OK") return "fail 2"

    return r.name
}
