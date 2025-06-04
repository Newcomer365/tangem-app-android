package com.tangem.domain.quotes.single

import com.tangem.domain.core.flow.FlowProducer
import com.tangem.domain.tokens.model.CryptoCurrency
import com.tangem.domain.tokens.model.Quote

/**
 * Producer of quote [CryptoCurrency.RawID]
 *
 * @author Andrew Khokhlov on 05/04/2025
 */
interface SingleQuoteProducer : FlowProducer<Quote> {

    data class Params(val rawCurrencyId: CryptoCurrency.RawID)

    interface Factory : FlowProducer.Factory<Params, SingleQuoteProducer>
}
