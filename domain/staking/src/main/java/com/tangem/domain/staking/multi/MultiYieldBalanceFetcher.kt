package com.tangem.domain.staking.multi

import com.tangem.domain.core.flow.FlowFetcher
import com.tangem.domain.staking.fetcher.YieldBalanceFetcherParams

/**
 * Fetcher of yields balances
 *
 * @author Andrew Khokhlov on 17/04/2025
 */
interface MultiYieldBalanceFetcher : FlowFetcher<YieldBalanceFetcherParams.Multi>
