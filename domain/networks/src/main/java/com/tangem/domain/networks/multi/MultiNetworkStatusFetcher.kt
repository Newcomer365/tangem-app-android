package com.tangem.domain.networks.multi

import com.tangem.domain.core.flow.FlowFetcher
import com.tangem.domain.tokens.model.Network
import com.tangem.domain.wallets.models.UserWalletId

/**
 * Fetcher of network status [Network] for wallet with [UserWalletId]
 *
 * @author Andrew Khokhlov on 21/03/2025
 */
interface MultiNetworkStatusFetcher : FlowFetcher<MultiNetworkStatusFetcher.Params> {

    data class Params(val userWalletId: UserWalletId, val networks: Set<Network>)
}
