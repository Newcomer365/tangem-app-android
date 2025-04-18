package com.tangem.domain.onramp.model

import com.tangem.domain.tokens.model.CryptoCurrency
import com.tangem.domain.tokens.model.Quote

/**
 * Hot crypto currency
 *
 * @property cryptoCurrency crypto currency
 * @property quote          quote
 *
 * @author Andrew Khokhlov on 20/01/2025
 */
data class HotCryptoCurrency(
    val cryptoCurrency: CryptoCurrency,
    val quote: Quote,
)
