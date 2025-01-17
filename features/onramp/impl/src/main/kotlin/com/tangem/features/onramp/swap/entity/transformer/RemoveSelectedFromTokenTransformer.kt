package com.tangem.features.onramp.swap.entity.transformer

import com.tangem.features.onramp.swap.entity.SwapSelectTokensUM
import com.tangem.features.onramp.swap.entity.SwapSelectTokensUMTransformer
import com.tangem.features.onramp.swap.entity.utils.createEmptyExchangeFrom
import com.tangem.features.onramp.swap.entity.utils.createEmptyExchangeTo

/**
 * Transformer for removing selected "from" token
 *
 * @author Andrew Khokhlov on 02/11/2024
 */
internal object RemoveSelectedFromTokenTransformer : SwapSelectTokensUMTransformer {

    override fun transform(prevState: SwapSelectTokensUM): SwapSelectTokensUM {
        return prevState.copy(
            exchangeFrom = createEmptyExchangeFrom(),
            exchangeTo = createEmptyExchangeTo(),
        )
    }
}
