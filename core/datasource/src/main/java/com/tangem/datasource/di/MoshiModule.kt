package com.tangem.datasource.di

import com.squareup.moshi.Moshi
import com.squareup.moshi.adapters.PolymorphicJsonAdapterFactory
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import com.tangem.common.json.MoshiJsonConverter
import com.tangem.datasource.api.common.adapter.BigDecimalAdapter
import com.tangem.datasource.api.common.adapter.DateTimeAdapter
import com.tangem.datasource.api.common.adapter.LocalDateAdapter
import com.tangem.datasource.api.common.adapter.addStakeKitEnumFallbackAdapters
import com.tangem.datasource.local.config.providers.models.ProviderModel
import com.tangem.datasource.local.network.entity.NetworkStatusDM
import com.tangem.domain.models.scan.serialization.*
import com.tangem.domain.visa.model.VisaActivationRemoteState
import com.tangem.domain.visa.model.VisaCardActivationStatus
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dev.onenowy.moshipolymorphicadapter.NamePolymorphicAdapterFactory
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class MoshiModule {

    @Provides
    @Singleton
    @NetworkMoshi
    fun provideNetworkMoshi(): Moshi {
        return Moshi.Builder()
            .add(
                PolymorphicJsonAdapterFactory.of(ProviderModel::class.java, "type")
                    .withSubtype(ProviderModel.Public::class.java, "public")
                    .withSubtype(ProviderModel.Private::class.java, "private")
                    .withDefaultValue(ProviderModel.UnsupportedType),
            )
            .add(BigDecimalAdapter())
            .add(LocalDateAdapter())
            .add(DateTimeAdapter())
            .add(VisaActivationRemoteState.jsonAdapter)
            .add(VisaCardActivationStatus.jsonAdapter)
            .add(
                NamePolymorphicAdapterFactory.of(NetworkStatusDM::class.java)
                    .withSubtype(NetworkStatusDM.Verified::class.java, "amounts")
                    .withSubtype(NetworkStatusDM.NoAccount::class.java, "amount_to_create_account"),
            )
            .addLast(KotlinJsonAdapterFactory())
            .addStakeKitEnumFallbackAdapters()
            .build()
    }

    @Provides
    @Singleton
    @SdkMoshi
    fun provideSdkMoshi(): Moshi {
        val adapters = MoshiJsonConverter.getTangemSdkAdapters() +
            listOf(
                BigDecimalAdapter(),
                WalletDerivedKeysMapAdapter(),
                ScanResponseDerivedKeysMapAdapter(),
                ByteArrayKeyAdapter(),
                ExtendedPublicKeysMapAdapter(),
                CardBackupStatusAdapter(),
                DerivationPathAdapterWithMigration(),
                DateTimeAdapter(),
            )

        val typedAdapters = MoshiJsonConverter.getTangemSdkTypedAdapters()

        return Moshi.Builder().apply {
            add(VisaActivationRemoteState.jsonAdapter)
            add(VisaCardActivationStatus.jsonAdapter)
            adapters.forEach { this.add(it) }
            typedAdapters.forEach { add(it.key, it.value) }
            addLast(KotlinJsonAdapterFactory())
        }.build()
    }

    @Provides
    @Singleton
    fun provideSdkMoshiConverter(): MoshiJsonConverter {
        return MoshiJsonConverter(
            adapters = MoshiJsonConverter.getTangemSdkAdapters() +
                listOf(
                    BigDecimalAdapter(),
                    WalletDerivedKeysMapAdapter(),
                    ScanResponseDerivedKeysMapAdapter(),
                    ByteArrayKeyAdapter(),
                    ExtendedPublicKeysMapAdapter(),
                    CardBackupStatusAdapter(),
                    DerivationPathAdapterWithMigration(),
                ),
            typedAdapters = MoshiJsonConverter.getTangemSdkTypedAdapters(),
        )
    }
}
