package com.tangem.data.networks.store

import com.google.common.truth.Truth
import com.tangem.common.test.datastore.MockStateDataStore
import com.tangem.common.test.domain.network.MockNetworkStatusFactory
import com.tangem.common.test.domain.token.MockCryptoCurrencyFactory
import com.tangem.data.networks.models.SimpleNetworkStatus
import com.tangem.data.networks.toDataModel
import com.tangem.data.networks.toSimple
import com.tangem.datasource.local.datastore.RuntimeSharedStore
import com.tangem.datasource.local.network.entity.NetworkStatusDM
import com.tangem.domain.models.StatusSource
import com.tangem.domain.tokens.model.NetworkStatus
import com.tangem.domain.wallets.models.UserWalletId
import com.tangem.utils.coroutines.TestingCoroutineDispatcherProvider
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.test.runTest
import org.junit.Test

/**
 * @author Andrew Khokhlov on 27/03/2025
 */
internal class NetworkStatusesStoreUpdateMethodsTest {

    private val runtimeStore = RuntimeSharedStore<WalletIdWithSimpleStatus>()
    private val persistenceStore = MockStateDataStore<WalletIdWithStatusDM>(default = emptyMap())

    private val store = DefaultNetworksStatusesStoreV2(
        runtimeStore = runtimeStore,
        persistenceDataStore = persistenceStore,
        dispatchers = TestingCoroutineDispatcherProvider(),
    )

    @Test
    fun `refresh the single network if runtime store is empty`() = runTest {
        store.refresh(userWalletId = userWalletId, network = network)

        val runtimeExpected = mapOf(userWalletId.stringValue to emptySet<NetworkStatus>())

        Truth.assertThat(runtimeStore.getSyncOrNull()).isEqualTo(runtimeExpected)
        Truth.assertThat(persistenceStore.data.firstOrNull()).isEqualTo(emptyMap<String, Set<NetworkStatusDM>>())
    }

    @Test
    fun `refresh the single network if runtime store contains status with this network`() = runTest {
        val status = MockNetworkStatusFactory.createVerified(network).toSimple()

        runtimeStore.store(
            value = mapOf(userWalletId.stringValue to setOf(status)),
        )

        store.refresh(userWalletId = userWalletId, network = network)

        val runtimeExpected = mapOf(
            userWalletId.stringValue to setOf(
                status.copy(value = status.value.copySealed(source = StatusSource.CACHE)),
            ),
        )

        Truth.assertThat(runtimeStore.getSyncOrNull()).isEqualTo(runtimeExpected)
        Truth.assertThat(persistenceStore.data.firstOrNull()).isEqualTo(emptyMap<String, Set<NetworkStatusDM>>())
    }

    @Test
    fun `refresh the multi networks if runtime store is empty`() = runTest {
        store.refresh(userWalletId = userWalletId, networks = networks)

        val runtimeExpected = mapOf(userWalletId.stringValue to emptySet<NetworkStatus>())

        Truth.assertThat(runtimeStore.getSyncOrNull()).isEqualTo(runtimeExpected)
        Truth.assertThat(persistenceStore.data.firstOrNull()).isEqualTo(emptyMap<String, Set<NetworkStatusDM>>())
    }

    @Test
    fun `refresh the multi networks if runtime store contains status with this network`() = runTest {
        val firstStatus = MockNetworkStatusFactory.createVerified(network = networks.first()).toSimple()
        val secondStatus = MockNetworkStatusFactory.createVerified(network = networks.last()).toSimple()

        runtimeStore.store(
            value = mapOf(userWalletId.stringValue to setOf(firstStatus, secondStatus)),
        )

        store.refresh(userWalletId = userWalletId, networks = networks)

        val runtimeExpected = mapOf(
            userWalletId.stringValue to setOf(
                firstStatus.copy(value = firstStatus.value.copySealed(source = StatusSource.CACHE)),
                secondStatus.copy(value = secondStatus.value.copySealed(source = StatusSource.CACHE)),
            ),
        )

        Truth.assertThat(runtimeStore.getSyncOrNull()).isEqualTo(runtimeExpected)
        Truth.assertThat(persistenceStore.data.firstOrNull()).isEqualTo(emptyMap<String, Set<NetworkStatusDM>>())
    }

    @Test
    fun `store actual with any status sources`() = runTest {
        val expectedErrorMessage = "Method storeActual can be called only with StatusSource.ACTUAL"

        // #1: StatusSource.ACTUAL
        val status = MockNetworkStatusFactory.createVerified(network).copy(
            value = MockNetworkStatusFactory.createVerified(network).value.copySealed(source = StatusSource.ACTUAL),
        )

        val actual = runCatching { store.storeSuccess(userWalletId, status) }

        val runtimeExpected = mapOf(userWalletId.stringValue to setOf(status.toSimple()))
        val persistenceExpected = mapOf(userWalletId.stringValue to setOf(status.toDataModel()!!))

        Truth.assertThat(actual.isSuccess).isTrue()
        Truth.assertThat(runtimeStore.getSyncOrNull()).isEqualTo(runtimeExpected)
        Truth.assertThat(persistenceStore.data.firstOrNull()).isEqualTo(persistenceExpected)

        // #2: StatusSource.CACHE
        val cacheStatus = MockNetworkStatusFactory.createVerified(network).copy(
            value = MockNetworkStatusFactory.createVerified(network).value.copySealed(source = StatusSource.CACHE),
        )

        val cacheActual = runCatching { store.storeSuccess(userWalletId, cacheStatus) }

        Truth.assertThat(cacheActual.isFailure).isTrue()
        Truth.assertThat(cacheActual.exceptionOrNull()).isInstanceOf(IllegalStateException::class.java)
        Truth.assertThat(cacheActual.exceptionOrNull()).hasMessageThat().isEqualTo(expectedErrorMessage)
        Truth.assertThat(runtimeStore.getSyncOrNull()).isEqualTo(runtimeExpected)
        Truth.assertThat(persistenceStore.data.firstOrNull()).isEqualTo(persistenceExpected)

        // #3: StatusSource.ONLY_CACHE
        val onlyCacheStatus = MockNetworkStatusFactory.createVerified(network).copy(
            value = MockNetworkStatusFactory.createVerified(network).value.copySealed(source = StatusSource.ONLY_CACHE),
        )

        val onlyCacheActual = runCatching { store.storeSuccess(userWalletId, onlyCacheStatus) }

        Truth.assertThat(onlyCacheActual.isFailure).isTrue()
        Truth.assertThat(onlyCacheActual.exceptionOrNull()).isInstanceOf(IllegalStateException::class.java)
        Truth.assertThat(onlyCacheActual.exceptionOrNull()).hasMessageThat().isEqualTo(expectedErrorMessage)
        Truth.assertThat(runtimeStore.getSyncOrNull()).isEqualTo(runtimeExpected)
        Truth.assertThat(persistenceStore.data.firstOrNull()).isEqualTo(persistenceExpected)
    }

    @Test
    fun `store actual if runtime and cache stores contain status with this network`() = runTest {
        val prevStatus = MockNetworkStatusFactory.createVerified(network).copy(
            value = MockNetworkStatusFactory.createVerified(network).value.copySealed(source = StatusSource.ONLY_CACHE),
        )

        runtimeStore.store(
            value = mapOf(userWalletId.stringValue to setOf(prevStatus.toSimple())),
        )

        persistenceStore.updateData {
            it.toMutableMap().apply {
                put(userWalletId.stringValue, setOf(prevStatus.toDataModel()!!))
            }
        }

        val newStatus = MockNetworkStatusFactory.createVerified(network).copy(
            value = MockNetworkStatusFactory.createVerified(network).value.copySealed(source = StatusSource.ACTUAL),
        )

        store.storeSuccess(userWalletId, newStatus)

        val runtimeExpected = mapOf(
            userWalletId.stringValue to setOf(newStatus.toSimple()),
        )

        val persistenceExpected = mapOf(
            userWalletId.stringValue to setOf(newStatus.toDataModel()!!),
        )

        Truth.assertThat(runtimeStore.getSyncOrNull()).isEqualTo(runtimeExpected)
        Truth.assertThat(persistenceStore.data.firstOrNull()).isEqualTo(persistenceExpected)
    }

    @Test
    fun `store actual unreachable status if cache store contains verified status`() = runTest {
        val prevStatus = MockNetworkStatusFactory.createVerified(network)

        runtimeStore.store(
            value = mapOf(userWalletId.stringValue to setOf(prevStatus.toSimple())),
        )

        persistenceStore.updateData {
            it.toMutableMap().apply {
                put(userWalletId.stringValue, setOf(prevStatus.toDataModel()!!))
            }
        }

        val newStatus = MockNetworkStatusFactory.createUnreachable(network)

        val actual = runCatching { store.storeSuccess(userWalletId, newStatus) }

        val expectedErrorMessage = "Use storeError method to save unreachable status"
        val runtimeExpected = mapOf(userWalletId.stringValue to setOf(prevStatus.toSimple()))
        val persistenceExpected = mapOf(userWalletId.stringValue to setOf(prevStatus.toDataModel()!!))

        Truth.assertThat(actual.isFailure).isTrue()
        Truth.assertThat(actual.exceptionOrNull()).isInstanceOf(IllegalStateException::class.java)
        Truth.assertThat(actual.exceptionOrNull()).hasMessageThat().isEqualTo(expectedErrorMessage)
        Truth.assertThat(runtimeStore.getSyncOrNull()).isEqualTo(runtimeExpected)
        Truth.assertThat(persistenceStore.data.firstOrNull()).isEqualTo(persistenceExpected)
    }

    @Test
    fun `store error if runtime store is empty`() = runTest {
        store.storeError(userWalletId = userWalletId, network = network)

        val runtimeExpected = mapOf(
            userWalletId.stringValue to setOf(
                SimpleNetworkStatus(
                    id = SimpleNetworkStatus.Id(network),
                    value = NetworkStatus.Unreachable(address = null),
                ),
            ),
        )

        Truth.assertThat(runtimeStore.getSyncOrNull()).isEqualTo(runtimeExpected)
        Truth.assertThat(persistenceStore.data.firstOrNull()).isEqualTo(emptyMap<String, Set<NetworkStatusDM>>())
    }

    @Test
    fun `store error if runtime store contains status with this network`() = runTest {
        val status = MockNetworkStatusFactory.createVerified(network).toSimple()

        runtimeStore.store(
            value = mapOf(userWalletId.stringValue to setOf(status)),
        )

        store.storeError(userWalletId = userWalletId, network = network)

        val runtimeExpected = mapOf(
            userWalletId.stringValue to setOf(
                status.copy(value = status.value.copySealed(source = StatusSource.ONLY_CACHE)),
            ),
        )

        Truth.assertThat(runtimeStore.getSyncOrNull()).isEqualTo(runtimeExpected)
        Truth.assertThat(persistenceStore.data.firstOrNull()).isEqualTo(emptyMap<String, Set<NetworkStatusDM>>())
    }

    @Test
    fun `store error if runtime store is empty and unreachable status was passed`() = runTest {
        val status = MockNetworkStatusFactory.createUnreachable(network)

        store.storeError(
            userWalletId = userWalletId,
            network = network,
            value = status.value as NetworkStatus.Unreachable,
        )

        val runtimeExpected = mapOf(
            userWalletId.stringValue to setOf(status.toSimple()),
        )

        Truth.assertThat(runtimeStore.getSyncOrNull()).isEqualTo(runtimeExpected)
        Truth.assertThat(persistenceStore.data.firstOrNull()).isEqualTo(emptyMap<String, Set<NetworkStatusDM>>())
    }

    @Test
    fun `store error if runtime store contains status with this network and unreachable status was passed`() = runTest {
        val status = MockNetworkStatusFactory.createVerified(network)

        runtimeStore.store(
            value = mapOf(userWalletId.stringValue to setOf(status.toSimple())),
        )

        store.storeError(
            userWalletId = userWalletId,
            network = network,
            value = MockNetworkStatusFactory.createUnreachable(network).value as NetworkStatus.Unreachable,
        )

        val runtimeExpected = mapOf(
            userWalletId.stringValue to setOf(
                status.copy(value = status.value.copySealed(source = StatusSource.ONLY_CACHE)).toSimple(),
            ),
        )

        Truth.assertThat(runtimeStore.getSyncOrNull()).isEqualTo(runtimeExpected)
        Truth.assertThat(persistenceStore.data.firstOrNull()).isEqualTo(emptyMap<String, Set<NetworkStatusDM>>())
    }

    @Test
    fun `store error for networks if runtime store is empty`() = runTest {
        store.storeError(userWalletId = userWalletId, networks = networks)

        val runtimeExpected = mapOf(
            userWalletId.stringValue to setOf(
                SimpleNetworkStatus(
                    id = SimpleNetworkStatus.Id(networks.first()),
                    value = NetworkStatus.Unreachable(address = null),
                ),
                SimpleNetworkStatus(
                    id = SimpleNetworkStatus.Id(networks.last()),
                    value = NetworkStatus.Unreachable(address = null),
                ),
            ),
        )

        Truth.assertThat(runtimeStore.getSyncOrNull()).isEqualTo(runtimeExpected)
        Truth.assertThat(persistenceStore.data.firstOrNull()).isEqualTo(emptyMap<String, Set<NetworkStatusDM>>())
    }

    @Test
    fun `store error for networks if runtime store contains status with this network`() = runTest {
        val status = MockNetworkStatusFactory.createVerified(networks.first()).toSimple()

        runtimeStore.store(
            value = mapOf(userWalletId.stringValue to setOf(status)),
        )

        store.storeError(userWalletId = userWalletId, networks = networks)

        val runtimeExpected = mapOf(
            userWalletId.stringValue to setOf(
                status.copy(value = status.value.copySealed(source = StatusSource.ONLY_CACHE)),
                SimpleNetworkStatus(
                    id = SimpleNetworkStatus.Id(networks.last()),
                    value = NetworkStatus.Unreachable(address = null),
                ),
            ),
        )

        Truth.assertThat(runtimeStore.getSyncOrNull()).isEqualTo(runtimeExpected)
        Truth.assertThat(persistenceStore.data.firstOrNull()).isEqualTo(emptyMap<String, Set<NetworkStatusDM>>())
    }

    private companion object {

        val userWalletId = UserWalletId(stringValue = "011")

        val network = MockCryptoCurrencyFactory().ethereum.network

        val networks = MockCryptoCurrencyFactory().ethereumAndStellar.mapTo(hashSetOf()) { it.network }
    }
}
