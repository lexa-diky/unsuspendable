package io.github.lexadiky.unsuspendable.generator

import com.squareup.kotlinpoet.ClassName

object TypeNameLib {
    val generatedAnnotation = ClassName.bestGuess("javax.annotation.processing.Generated")
    val completableFuture = ClassName.bestGuess("java.util.concurrent.CompletableFuture")
    val coroutineScope = ClassName.bestGuess("kotlinx.coroutines.CoroutineScope")
    val optional = ClassName.bestGuess("java.util.Optional")
}