package com.tangem.domain.transaction.usecase

import arrow.core.Either
import com.tangem.blockchain.common.transaction.Fee
import com.tangem.blockchain.nft.models.NFTAsset
import com.tangem.domain.tokens.model.Network
import com.tangem.domain.transaction.TransactionRepository
import com.tangem.domain.wallets.models.UserWalletId

class CreateNFTTransferTransactionUseCase(
    private val transactionRepository: TransactionRepository,
) {
    /**
     * Todo wrap params into a model https://tangem.atlassian.net/browse/AND-5741
     */
    @Suppress("LongParameterList")
    suspend operator fun invoke(
        ownerAddress: String,
        nftAsset: NFTAsset,
        fee: Fee,
        memo: String?,
        destinationAddress: String,
        userWalletId: UserWalletId,
        network: Network,
    ) = Either.catch {
        transactionRepository.createNFTTransferTransaction(
            ownerAddress = ownerAddress,
            nftAsset = nftAsset,
            destinationAddress = destinationAddress,
            fee = fee,
            memo = memo,
            userWalletId = userWalletId,
            network = network,
        )
    }

    /**
     * Todo wrap params into a model https://tangem.atlassian.net/browse/AND-5741
     */
    @Suppress("LongParameterList")
    suspend operator fun invoke(
        ownerAddress: String,
        nftAsset: NFTAsset,
        memo: String?,
        destinationAddress: String,
        userWalletId: UserWalletId,
        network: Network,
    ) = Either.catch {
        transactionRepository.createNFTTransferTransaction(
            ownerAddress = ownerAddress,
            nftAsset = nftAsset,
            destinationAddress = destinationAddress,
            fee = null,
            memo = memo,
            userWalletId = userWalletId,
            network = network,
        )
    }
}
