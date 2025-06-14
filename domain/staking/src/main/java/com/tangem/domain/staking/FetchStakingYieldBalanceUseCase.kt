package com.tangem.domain.staking

import arrow.core.Either
import arrow.core.raise.catch
import arrow.core.raise.either
import com.tangem.domain.staking.fetcher.YieldBalanceFetcherParams
import com.tangem.domain.staking.model.stakekit.StakingError
import com.tangem.domain.staking.repositories.StakingErrorResolver
import com.tangem.domain.staking.repositories.StakingRepository
import com.tangem.domain.staking.single.SingleYieldBalanceFetcher
import com.tangem.domain.tokens.model.CryptoCurrency
import com.tangem.domain.wallets.models.UserWalletId

class FetchStakingYieldBalanceUseCase(
    private val stakingRepository: StakingRepository,
    private val stakingErrorResolver: StakingErrorResolver,
    private val singleYieldBalanceFetcher: SingleYieldBalanceFetcher,
) {

    suspend operator fun invoke(
        userWalletId: UserWalletId,
        cryptoCurrency: CryptoCurrency,
        isRefactoringEnabled: Boolean,
        refresh: Boolean = false,
    ): Either<StakingError, Unit> {
        return either {
            catch(
                block = {
                    if (isRefactoringEnabled) {
                        singleYieldBalanceFetcher(
                            params = YieldBalanceFetcherParams.Single(
                                userWalletId = userWalletId,
                                currencyId = cryptoCurrency.id,
                                network = cryptoCurrency.network,
                            ),
                        )
                    } else {
                        stakingRepository.fetchSingleYieldBalance(
                            userWalletId = userWalletId,
                            cryptoCurrency = cryptoCurrency,
                            refresh = refresh,
                        )
                    }
                },
                catch = { stakingErrorResolver.resolve(it) },
            )
        }
    }
}
