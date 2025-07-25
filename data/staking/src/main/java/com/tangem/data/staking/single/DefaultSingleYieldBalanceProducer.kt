package com.tangem.data.staking.single

import com.tangem.data.staking.utils.StakingIdFactory
import com.tangem.domain.staking.model.StakingID
import com.tangem.domain.staking.model.stakekit.YieldBalance
import com.tangem.domain.staking.multi.MultiYieldBalanceProducer
import com.tangem.domain.staking.multi.MultiYieldBalanceSupplier
import com.tangem.domain.staking.single.SingleYieldBalanceProducer
import com.tangem.utils.coroutines.CoroutineDispatcherProvider
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.mapNotNull

/**
 * Default implementation of [SingleYieldBalanceProducer]
 *
 * @property params                    params
 * @property multiYieldBalanceSupplier multi yield balance supplier
 * @property stakingIdFactory          factory for creating [StakingID]
 * @property dispatchers               dispatchers
 *
 * @author Andrew Khokhlov on 17/04/2025
 */
internal class DefaultSingleYieldBalanceProducer @AssistedInject constructor(
    @Assisted private val params: SingleYieldBalanceProducer.Params,
    private val multiYieldBalanceSupplier: MultiYieldBalanceSupplier,
    private val stakingIdFactory: StakingIdFactory,
    private val dispatchers: CoroutineDispatcherProvider,
) : SingleYieldBalanceProducer {

    override val fallback: YieldBalance by lazy {
        YieldBalance.Error(
            integrationId = stakingIdFactory.createIntegrationId(currencyId = params.currencyId),
            address = null,
        )
    }

    private var stakingIds: Set<StakingID>? = null

    override fun produce(): Flow<YieldBalance> {
        return multiYieldBalanceSupplier(
            params = MultiYieldBalanceProducer.Params(userWalletId = params.userWalletId),
        )
            .mapNotNull {
                val currentStakingIds = getStakingIds()

                it.firstOrNull { balance ->
                    currentStakingIds.contains(balance.getStakingId())
                }
            }
            .distinctUntilChanged()
            .flowOn(dispatchers.default)
    }

    private suspend fun getStakingIds(): Set<StakingID> {
        val saved = stakingIds

        if (saved != null) return saved

        return stakingIdFactory.create(
            userWalletId = params.userWalletId,
            currencyId = params.currencyId,
            network = params.network,
        )
            .also { stakingIds = it }
    }

    @AssistedFactory
    interface Factory : SingleYieldBalanceProducer.Factory {
        override fun create(params: SingleYieldBalanceProducer.Params): DefaultSingleYieldBalanceProducer
    }
}
