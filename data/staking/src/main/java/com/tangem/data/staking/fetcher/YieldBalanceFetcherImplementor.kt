package com.tangem.data.staking.fetcher

import com.tangem.domain.staking.fetcher.YieldBalanceFetcherParams
import com.tangem.domain.staking.model.StakingID

/**
 * Implementor of internal logic of YieldBalanceFetcher
 *
 * @author Andrew Khokhlov on 21/04/2025
 */
internal interface YieldBalanceFetcherImplementor<in Params : YieldBalanceFetcherParams> {

    /** Create set of [StakingID] */
    suspend fun createStakingIds(params: Params): Set<StakingID>

    /**
     * Fetch yield balances
     *
     * @param params     params
     * @param stakingIds set of [StakingID]
     */
    suspend fun fetch(params: Params, stakingIds: Set<StakingID>)
}
