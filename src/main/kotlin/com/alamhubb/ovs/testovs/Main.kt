object ParentObject {
    var name = "ParentObject"
    fun echoName() = "name:${name}"
}

// 使用
fun main() {
    println(ParentObject.echoName())     // 输出: ParentObject
}