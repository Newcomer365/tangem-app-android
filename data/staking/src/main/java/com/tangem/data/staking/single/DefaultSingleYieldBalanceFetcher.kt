package com.tangem.data.staking.single

import com.tangem.data.staking.fetcher.YieldBalanceFetcherImplementor
import com.tangem.data.staking.fetcher.commonFetcher
import com.tangem.data.staking.store.YieldsBalancesStore
import com.tangem.data.staking.utils.StakingIdFactory
import com.tangem.datasource.api.stakekit.StakeKitApi
import com.tangem.datasource.local.token.StakingYieldsStore
import com.tangem.datasource.local.userwallet.UserWalletsStore
import com.tangem.domain.core.flow.FlowFetcher
import com.tangem.domain.staking.fetcher.YieldBalanceFetcherParams
import com.tangem.domain.staking.multi.MultiYieldBalanceFetcher
import com.tangem.domain.staking.single.SingleYieldBalanceFetcher
import com.tangem.utils.coroutines.CoroutineDispatcherProvider
import javax.inject.Inject

/**
 * Default implementation of [MultiYieldBalanceFetcher]
 *
 * @property userWalletsStore    user wallets store
 * @property stakingYieldsStore  staking yields store
 * @property yieldsBalancesStore yields balances store
 * @property stakingIdFactory    factory for creating StakingID
 * @property stakeKitApi         stake kit API
 * @property dispatchers         dispatchers
 *
 * @author Andrew Khokhlov on 17/04/2025
 */
internal class DefaultSingleYieldBalanceFetcher @Inject constructor(
    private val userWalletsStore: UserWalletsStore,
    private val stakingYieldsStore: StakingYieldsStore,
    private val yieldsBalancesStore: YieldsBalancesStore,
    private val stakingIdFactory: StakingIdFactory,
    private val stakeKitApi: StakeKitApi,
    private val dispatchers: CoroutineDispatcherProvider,
) : SingleYieldBalanceFetcher,
    FlowFetcher<YieldBalanceFetcherParams.Single> by commonFetcher(
        implementor = createSingleFetcherImplementor(yieldsBalancesStore, stakingIdFactory, stakeKitApi, dispatchers),
        userWalletsStore = userWalletsStore,
        stakingYieldsStore = stakingYieldsStore,
        yieldsBalancesStore = yieldsBalancesStore,
        dispatchers = dispatchers,
    )

private fun createSingleFetcherImplementor(
    yieldsBalancesStore: YieldsBalancesStore,
    stakingIdFactory: StakingIdFactory,
    stakeKitApi: StakeKitApi,
    dispatchers: CoroutineDispatcherProvider,
): YieldBalanceFetcherImplementor<YieldBalanceFetcherParams.Single> {
    return SingleYieldBalanceFetcherImplementor(
        yieldsBalancesStore = yieldsBalancesStore,
        stakingIdFactory = stakingIdFactory,
        stakeKitApi = stakeKitApi,
        dispatchers = dispatchers,
    )
}
