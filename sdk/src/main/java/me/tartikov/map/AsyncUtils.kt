package me.tartikov.map

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.Flow
import ru.dgis.sdk.Future
import java.util.concurrent.Executor
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlinx.coroutines.flow.collect

internal object DirectExecutor : Executor {
    override fun execute(r: Runnable) {
        r.run()
    }
}

suspend fun <T> Future<T>.await(): T {
    return suspendCancellableCoroutine { continuation: CancellableContinuation<T> ->
        onComplete(DirectExecutor,
            resultCallback = {
                continuation.resume(it)
            },
            errorCallback = {
                continuation.resumeWithException(it)
            }
        )
        continuation.invokeOnCancellation {
            close()
        }
    }
}

fun <T> Flow<T>.toState(initialValue: T, scope: CoroutineScope, dispatcher: CoroutineDispatcher = Dispatchers.Default): State<T> {
    val state = mutableStateOf(initialValue)
    scope.launch(dispatcher) {
        this@toState.collect {
            state.value = it
        }
    }
    return state
}