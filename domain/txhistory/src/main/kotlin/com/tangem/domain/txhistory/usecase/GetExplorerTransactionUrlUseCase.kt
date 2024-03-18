package com.tangem.domain.txhistory.usecase

import arrow.core.Either
import arrow.core.raise.catch
import arrow.core.raise.either
import com.tangem.domain.tokens.model.Network
import com.tangem.domain.txhistory.models.TxStatusError
import com.tangem.domain.txhistory.repository.TxHistoryRepository

class GetExplorerTransactionUrlUseCase(
    private val repository: TxHistoryRepository,
) {
    operator fun invoke(txHash: String, networkId: Network.ID): Either<TxStatusError, String> {
        return either {
            catch(
                block = {
                    repository.getTxExploreUrl(txHash, networkId).ifEmpty {
                        raise(TxStatusError.EmptyUrlError)
                    }
                },
                catch = { raise(TxStatusError.DataError(it)) },
            )
        }
    }
}
