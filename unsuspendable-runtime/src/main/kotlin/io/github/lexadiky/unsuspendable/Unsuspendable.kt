package io.github.lexadiky.unsuspendable

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.future.future
import java.util.concurrent.CompletableFuture

@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.SOURCE)
annotation class Unsuspendable

fun <T> unsuspendable(scope: CoroutineScope, fn: suspend () -> T): CompletableFuture<T> =
    scope.future { fn() }
