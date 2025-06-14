package com.tangem.domain.networks.multi

import com.tangem.domain.core.flow.FlowProducer
import com.tangem.domain.tokens.model.NetworkStatus
import com.tangem.domain.wallets.models.UserWalletId

/**
 * Producer of all networks statuses for wallet with [UserWalletId]
 *
 * @author Andrew Khokhlov on 21/03/2025
 */
interface MultiNetworkStatusProducer : FlowProducer<Set<NetworkStatus>> {

    data class Params(val userWalletId: UserWalletId)

    interface Factory : FlowProducer.Factory<Params, MultiNetworkStatusProducer>
}
