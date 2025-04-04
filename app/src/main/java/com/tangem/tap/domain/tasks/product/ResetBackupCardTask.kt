package com.tangem.tap.domain.tasks.product

import com.tangem.common.CompletionResult
import com.tangem.common.core.CardSession
import com.tangem.common.core.CardSessionRunnable
import com.tangem.common.core.CompletionCallback
import com.tangem.domain.wallets.models.UserWalletId
import com.tangem.operations.PreflightReadMode
import com.tangem.operations.PreflightReadTask
import com.tangem.tap.domain.tasks.UserWalletIdPreflightReadFilter

/**
 * Task for resetting backup card.
 *
 * 1. Read card and check if card is corresponding to expected user wallet id.
 * 2. Reset card.
 *
 * @author Andrew Khokhlov on 01/06/2024
 */
// TODO remove it after test after resolve https://tangem.atlassian.net/browse/AND-8427
internal class ResetBackupCardTask(
    private val userWalletId: UserWalletId,
) : CardSessionRunnable<Boolean> {

    override val allowsRequestAccessCodeFromRepository: Boolean = false

    override fun run(session: CardSession, callback: CompletionCallback<Boolean>) {
        PreflightReadTask(
            readMode = PreflightReadMode.FullCardRead,
            filter = UserWalletIdPreflightReadFilter(expectedUserWalletId = userWalletId),
        ).run(session) { result ->
            when (result) {
                is CompletionResult.Success -> resetCard(session, callback)
                is CompletionResult.Failure -> callback(CompletionResult.Failure(result.error))
            }
        }
    }

    private fun resetCard(session: CardSession, callback: CompletionCallback<Boolean>) {
        ResetToFactorySettingsTask(allowsRequestAccessCodeFromRepository).run(session) { result ->
            when (result) {
                is CompletionResult.Success -> callback(CompletionResult.Success(result.data))
                is CompletionResult.Failure -> callback(CompletionResult.Failure(result.error))
            }
        }
    }
}
