package com.tangem.tap.di.domain

import com.tangem.domain.card.repository.DerivationsRepository
import com.tangem.domain.managetokens.*
import com.tangem.domain.managetokens.repository.CustomTokensRepository
import com.tangem.domain.managetokens.repository.ManageTokensRepository
import com.tangem.domain.networks.multi.MultiNetworkStatusFetcher
import com.tangem.domain.quotes.multi.MultiQuoteFetcher
import com.tangem.domain.staking.multi.MultiYieldBalanceFetcher
import com.tangem.domain.staking.repositories.StakingRepository
import com.tangem.domain.tokens.TokensFeatureToggles
import com.tangem.domain.tokens.repository.CurrenciesRepository
import com.tangem.domain.tokens.repository.NetworksRepository
import com.tangem.domain.tokens.repository.QuotesRepository
import com.tangem.domain.walletmanager.WalletManagersFacade
import com.tangem.utils.coroutines.CoroutineDispatcherProvider
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
internal object ManageTokensDomainModule {

    @Provides
    @Singleton
    fun provideGetManageTokensUseCase(manageTokensRepository: ManageTokensRepository): GetManagedTokensUseCase {
        return GetManagedTokensUseCase(manageTokensRepository)
    }

    @Provides
    @Singleton
    fun provideValidateTokenFormatUseCase(customTokensRepository: CustomTokensRepository): ValidateTokenFormUseCase {
        return ValidateTokenFormUseCase(customTokensRepository)
    }

    @Provides
    @Singleton
    fun provideCreateCurrencyUseCase(customTokensRepository: CustomTokensRepository): CreateCurrencyUseCase {
        return CreateCurrencyUseCase(customTokensRepository)
    }

    @Provides
    @Singleton
    fun provideFindTokenUseCase(customTokensRepository: CustomTokensRepository): FindTokenUseCase {
        return FindTokenUseCase(customTokensRepository)
    }

    @Provides
    @Singleton
    fun provideCheckIsCurrencyNotAddedUseCase(
        customTokensRepository: CustomTokensRepository,
    ): CheckIsCurrencyNotAddedUseCase {
        return CheckIsCurrencyNotAddedUseCase(customTokensRepository)
    }

    @Provides
    @Singleton
    fun provideRemoveCustomManagedCryptoCurrencyUseCase(
        customTokensRepository: CustomTokensRepository,
    ): RemoveCustomManagedCryptoCurrencyUseCase {
        return RemoveCustomManagedCryptoCurrencyUseCase(customTokensRepository)
    }

    @Provides
    @Singleton
    fun provideSaveManagedTokensUseCase(
        customTokensRepository: CustomTokensRepository,
        walletManagersFacade: WalletManagersFacade,
        currenciesRepository: CurrenciesRepository,
        networksRepository: NetworksRepository,
        derivationsRepository: DerivationsRepository,
        stakingRepository: StakingRepository,
        quotesRepository: QuotesRepository,
        multiNetworkStatusFetcher: MultiNetworkStatusFetcher,
        multiQuoteFetcher: MultiQuoteFetcher,
        multiYieldBalanceFetcher: MultiYieldBalanceFetcher,
        tokensFeatureToggles: TokensFeatureToggles,
    ): SaveManagedTokensUseCase {
        return SaveManagedTokensUseCase(
            customTokensRepository = customTokensRepository,
            walletManagersFacade = walletManagersFacade,
            currenciesRepository = currenciesRepository,
            networksRepository = networksRepository,
            derivationsRepository = derivationsRepository,
            stakingRepository = stakingRepository,
            quotesRepository = quotesRepository,
            multiNetworkStatusFetcher = multiNetworkStatusFetcher,
            multiQuoteFetcher = multiQuoteFetcher,
            multiYieldBalanceFetcher = multiYieldBalanceFetcher,
            tokensFeatureToggles = tokensFeatureToggles,
        )
    }

    @Provides
    @Singleton
    fun provideGetSupportedNetworksUseCase(
        customTokensRepository: CustomTokensRepository,
    ): GetSupportedNetworksUseCase {
        return GetSupportedNetworksUseCase(customTokensRepository)
    }

    @Provides
    @Singleton
    fun provideValidateDerivationPathUseCase(
        customTokensRepository: CustomTokensRepository,
    ): ValidateDerivationPathUseCase {
        return ValidateDerivationPathUseCase(customTokensRepository)
    }

    @Provides
    @Singleton
    fun provideCheckHasLinkedTokensUseCase(repository: ManageTokensRepository): CheckHasLinkedTokensUseCase {
        return CheckHasLinkedTokensUseCase(repository)
    }

    @Provides
    @Singleton
    fun provideCheckCurrencyUnsupportedUseCase(repository: ManageTokensRepository): CheckCurrencyUnsupportedUseCase {
        return CheckCurrencyUnsupportedUseCase(repository)
    }

    @Provides
    @Singleton
    fun provideDistinctManagedCurrenciesTokenUseCase(
        coroutineDispatchersProvider: CoroutineDispatcherProvider,
    ): GetDistinctManagedCurrenciesUseCase {
        return GetDistinctManagedCurrenciesUseCase(coroutineDispatchersProvider)
    }
}
