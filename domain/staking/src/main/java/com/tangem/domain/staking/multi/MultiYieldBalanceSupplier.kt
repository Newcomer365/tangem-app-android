package com.tangem.domain.staking.multi

import com.tangem.domain.core.flow.FlowCachingSupplier
import com.tangem.domain.core.flow.FlowProducer
import com.tangem.domain.staking.model.stakekit.YieldBalance

/**
 * Supplier of all yield balances for selected wallet [MultiYieldBalanceProducer.Params]
 *
 * @property factory    factory for creating [MultiYieldBalanceProducer]
 * @property keyCreator key creator
 *
 * @author Andrew Khokhlov on 17/04/2025
 */
abstract class MultiYieldBalanceSupplier(
    override val factory: FlowProducer.Factory<MultiYieldBalanceProducer.Params, MultiYieldBalanceProducer>,
    override val keyCreator: (MultiYieldBalanceProducer.Params) -> String,
) : FlowCachingSupplier<MultiYieldBalanceProducer, MultiYieldBalanceProducer.Params, Set<YieldBalance>>()
