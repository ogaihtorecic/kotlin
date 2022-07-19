// WITH_STDLIB

fun main() {
    buildList l1@ { // not enough type information
        buildList {
            for (i in lastIndex downTo 0) {
                add("")
                this@l1.add("")
            }
        }
    }
}
