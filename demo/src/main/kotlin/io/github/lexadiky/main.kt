package io.github.lexadiky

import io.github.lexadiky.unsuspendable.Unsuspendable
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers

data class UserInfo(val name: String, val code: Int)

@Unsuspendable
interface UserInfoProvider {
    suspend fun provide(code: Int): UserInfo
}

class UserInfoProviderImpl : UserInfoProvider {

    override suspend fun provide(code: Int): UserInfo {
        return UserInfo("alex", code)
    }
}

fun main() {
    val provider: UserInfoProvider = UserInfoProviderImpl()
    val futureProvider: FutureUserInfoProvider = FutureUserInfoProviderImpl(
        scope = CoroutineScope(Dispatchers.IO),
        base = provider
    )
    println(futureProvider.provide(47).join())
}