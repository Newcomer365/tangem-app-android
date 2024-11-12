package com.tangem.features.onboarding.v2.multiwallet.impl.di

import com.tangem.core.decompose.di.DecomposeComponent
import com.tangem.core.decompose.model.Model
import com.tangem.features.onboarding.v2.multiwallet.api.OnboardingMultiWalletComponent
import com.tangem.features.onboarding.v2.multiwallet.impl.DefaultOnboardingMultiWalletComponent
import com.tangem.features.onboarding.v2.multiwallet.impl.model.OnboardingMultiWalletModel
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.ClassKey
import dagger.multibindings.IntoMap
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
internal interface ComponentModule {

    @Binds
    @Singleton
    fun bindComponent(factory: DefaultOnboardingMultiWalletComponent.Factory): OnboardingMultiWalletComponent.Factory
}

@Module
@InstallIn(DecomposeComponent::class)
internal interface ModelModule {

    @Binds
    @IntoMap
    @ClassKey(OnboardingMultiWalletModel::class)
    fun provideModel(model: OnboardingMultiWalletModel): Model
}
