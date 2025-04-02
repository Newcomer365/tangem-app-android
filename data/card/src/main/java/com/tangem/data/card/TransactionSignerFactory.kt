package com.tangem.data.card

import com.tangem.TangemSdk
import com.tangem.blockchain.common.TransactionSigner
import com.tangem.domain.card.models.TwinKey

/**
 * @author Andrew Khokhlov on 09/10/2024
 */
interface TransactionSignerFactory {

    fun createTransactionSigner(cardId: String?, sdk: TangemSdk, twinKey: TwinKey?): TransactionSigner
}
