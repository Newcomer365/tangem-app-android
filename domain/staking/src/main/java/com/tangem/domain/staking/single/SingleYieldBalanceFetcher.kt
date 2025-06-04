package com.tangem.domain.staking.single

import com.tangem.domain.core.flow.FlowFetcher
import com.tangem.domain.staking.fetcher.YieldBalanceFetcherParams

/**
 * Fetcher of yield balance
 *
 * @author Andrew Khokhlov on 17/04/2025
 */
interface SingleYieldBalanceFetcher : FlowFetcher<YieldBalanceFetcherParams.Single>
