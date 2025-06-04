package com.tangem.common.test.data.quote

import com.tangem.datasource.api.tangemTech.models.QuotesResponse
import java.math.BigDecimal

/**
 * @author Andrew Khokhlov on 05/04/2025
 */
object MockQuoteResponseFactory {

    fun createSinglePrice(value: BigDecimal): QuotesResponse.Quote {
        return QuotesResponse.Quote(
            price = value,
            priceChange24h = value,
            priceChange1w = value,
            priceChange30d = value,
        )
    }
}
