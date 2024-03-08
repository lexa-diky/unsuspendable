package io.github.lexadiky.unsuspendable.entity

data class SuspendableMethod(
    val name: String,
    val returns: TypeDescription,
    val parameters: List<MethodParameter>
)