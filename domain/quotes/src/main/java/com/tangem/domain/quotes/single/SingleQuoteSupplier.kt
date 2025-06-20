package com.tangem.domain.quotes.single

import com.tangem.domain.core.flow.FlowCachingSupplier
import com.tangem.domain.tokens.model.Quote

/**
 * Supplier of quote [SingleQuoteProducer.Params]
 *
 * @property factory    factory for creating [SingleQuoteProducer]
 * @property keyCreator key creator
 *
 * @author Andrew Khokhlov on 21/03/2025
 */
abstract class SingleQuoteSupplier(
    override val factory: SingleQuoteProducer.Factory,
    override val keyCreator: (SingleQuoteProducer.Params) -> String,
) : FlowCachingSupplier<SingleQuoteProducer, SingleQuoteProducer.Params, Quote>()
