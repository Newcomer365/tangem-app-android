package com.tangem.domain.quotes

import com.tangem.domain.tokens.model.CryptoCurrency
import com.tangem.domain.tokens.model.Quote

/**
 * Quotes repository
 *
 * @author Andrew Khokhlov on 11/04/2025
 */
interface QuotesRepositoryV2 {

    /** Get quotes by [currenciesIds] synchronously or null */
    suspend fun getMultiQuoteSyncOrNull(currenciesIds: Set<CryptoCurrency.RawID>): Set<Quote>?
}
