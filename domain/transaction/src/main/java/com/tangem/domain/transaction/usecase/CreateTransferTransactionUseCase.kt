package com.tangem.domain.transaction.usecase

import arrow.core.Either
import com.tangem.blockchain.common.Amount
import com.tangem.blockchain.common.transaction.Fee
import com.tangem.domain.tokens.model.Network
import com.tangem.domain.transaction.TransactionRepository
import com.tangem.domain.wallets.models.UserWalletId

/**
 * Use case to create and get transfer transaction
 *
 * !!!IMPORTANT
 * Use when transaction data is compiled by us using BlockchainSDK methods
 */
class CreateTransferTransactionUseCase(
    private val transactionRepository: TransactionRepository,
) {

    /**
     * Todo wrap params into a model https://tangem.atlassian.net/browse/AND-5741
     */
    @Suppress("LongParameterList")
    suspend operator fun invoke(
        amount: Amount,
        fee: Fee,
        memo: String?,
        destination: String,
        userWalletId: UserWalletId,
        network: Network,
    ) = Either.catch {
        transactionRepository.createTransferTransaction(
            amount = amount,
            fee = fee,
            memo = memo,
            destination = destination,
            userWalletId = userWalletId,
            network = network,
        )
    }

    /**
     * Todo wrap params into a model https://tangem.atlassian.net/browse/AND-5741
     */
    @Suppress("LongParameterList")
    suspend operator fun invoke(
        amount: Amount,
        memo: String?,
        destination: String,
        userWalletId: UserWalletId,
        network: Network,
    ) = Either.catch {
        transactionRepository.createTransferTransaction(
            amount = amount,
            memo = memo,
            fee = null,
            destination = destination,
            userWalletId = userWalletId,
            network = network,
        )
    }
}
