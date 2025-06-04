package com.tangem.domain.staking.multi

import com.tangem.domain.core.flow.FlowProducer
import com.tangem.domain.staking.model.stakekit.YieldBalance
import com.tangem.domain.wallets.models.UserWalletId

/**
 * Producer of all yield balances for selected wallet [UserWalletId]
 *
 * @author Andrew Khokhlov on 17/04/2025
 */
interface MultiYieldBalanceProducer : FlowProducer<Set<YieldBalance>> {

    data class Params(val userWalletId: UserWalletId)

    interface Factory : FlowProducer.Factory<Params, MultiYieldBalanceProducer>
}
