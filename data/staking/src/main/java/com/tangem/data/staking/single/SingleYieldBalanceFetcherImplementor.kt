package com.tangem.data.staking.single

import com.tangem.data.common.api.safeApiCall
import com.tangem.data.staking.fetcher.YieldBalanceFetcherImplementor
import com.tangem.data.staking.store.YieldsBalancesStore
import com.tangem.data.staking.utils.StakingIdFactory
import com.tangem.data.staking.utils.YieldBalanceRequestBodyFactory
import com.tangem.datasource.api.stakekit.StakeKitApi
import com.tangem.datasource.api.stakekit.models.response.model.YieldBalanceWrapperDTO
import com.tangem.domain.staking.fetcher.YieldBalanceFetcherParams
import com.tangem.domain.staking.model.StakingID
import com.tangem.domain.wallets.models.UserWalletId
import com.tangem.utils.coroutines.CoroutineDispatcherProvider
import kotlinx.coroutines.withContext
import timber.log.Timber

/**
 * Implementor of fetcher for refreshing single yield balance
 *
 * @property yieldsBalancesStore yields balances store
 * @property stakingIdFactory    factory for creating [StakingID]
 * @property stakeKitApi         StakeKit API
 * @property dispatchers         dispatchers
 *
 * @author Andrew Khokhlov on 21/04/2025
 */
internal class SingleYieldBalanceFetcherImplementor(
    private val yieldsBalancesStore: YieldsBalancesStore,
    private val stakingIdFactory: StakingIdFactory,
    private val stakeKitApi: StakeKitApi,
    private val dispatchers: CoroutineDispatcherProvider,
) : YieldBalanceFetcherImplementor<YieldBalanceFetcherParams.Single> {

    override suspend fun createStakingIds(params: YieldBalanceFetcherParams.Single): Set<StakingID> {
        val dataStakingId = stakingIdFactory.createForDefault(
            userWalletId = params.userWalletId,
            currencyId = params.currencyId,
            network = params.network,
        ) ?: return emptySet()

        return setOf(
            StakingID(integrationId = dataStakingId.integrationId, address = dataStakingId.address),
        )
    }

    override suspend fun fetch(params: YieldBalanceFetcherParams.Single, stakingIds: Set<StakingID>) {
        fetchInternal(userWalletId = params.userWalletId, stakingId = stakingIds.first())
    }

    private suspend fun fetchInternal(userWalletId: UserWalletId, stakingId: StakingID) {
        val request = YieldBalanceRequestBodyFactory.create(stakingId)

        safeApiCall(
            call = {
                val result = withContext(dispatchers.io) {
                    stakeKitApi.getSingleYieldBalance(
                        integrationId = stakingId.integrationId,
                        body = request,
                    ).bind()
                }

                yieldsBalancesStore.storeActual(
                    userWalletId = userWalletId,
                    values = setOf(
                        YieldBalanceWrapperDTO(
                            balances = result,
                            integrationId = request.integrationId,
                            addresses = request.addresses,
                        ),
                    ),
                )
            },
            onError = {
                Timber.e(it, "Unable to fetch yield balances $userWalletId")

                yieldsBalancesStore.storeError(userWalletId = userWalletId, stakingIds = setOf(stakingId))

                throw it
            },
        )
    }
}
