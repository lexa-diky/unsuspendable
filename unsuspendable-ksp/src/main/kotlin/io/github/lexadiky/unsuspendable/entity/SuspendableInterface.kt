package io.github.lexadiky.unsuspendable.entity

data class SuspendableInterface(
    val packageName: String,
    val name: String,
    val methods: List<SuspendableMethod>
)
