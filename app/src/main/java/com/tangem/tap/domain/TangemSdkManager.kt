package com.tangem.tap.domain

import androidx.activity.ComponentActivity
import com.tangem.*
import com.tangem.commands.CommandResponse
import com.tangem.common.CompletionResult
import com.tangem.common.extensions.CardType
import com.tangem.tangem_sdk_new.extensions.init
import com.tangem.tap.domain.tasks.ScanNoteResponse
import com.tangem.tap.domain.tasks.ScanNoteTask
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.*
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class TangemSdkManager(val activity: ComponentActivity) {
    private val tangemSdk = TangemSdk.init(
            activity, Config(cardFilter = CardFilter(EnumSet.allOf(CardType::class.java)))
    )

    suspend fun scanNote(): CompletionResult<ScanNoteResponse> {
        return runTaskAsyncReturnOnMain(ScanNoteTask())
    }

    private suspend fun <T : CommandResponse> runTaskAsync(
            runnable: CardSessionRunnable<T>, cardId: String? = null, initialMessage: Message? = null
    ): CompletionResult<T> =
            suspendCoroutine { continuation ->
                tangemSdk.startSessionWithRunnable(runnable, cardId, initialMessage) { result ->
                    continuation.resume(result)
                }
            }

    private suspend fun <T : CommandResponse> runTaskAsyncReturnOnMain(
            runnable: CardSessionRunnable<T>, cardId: String? = null, initialMessage: Message? = null
    ): CompletionResult<T> {
        val result = runTaskAsync(runnable, cardId, initialMessage)
        return withContext(Dispatchers.Main) { result }
    }
}