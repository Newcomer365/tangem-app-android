package com.tangem.data.networks.multi

/**
 * @author Andrew Khokhlov on 31/03/2025
 */
// internal class DefaultMultiNetworkStatusFetcherTest {
//
//     private val singleNetworkStatusFetcher: SingleNetworkStatusFetcher = mockk()
//     private val networksStatusesStore: NetworksStatusesStoreV2 = mockk(relaxed = true)
//
//     private val fetcher = DefaultMultiNetworkStatusFetcher(
//         singleNetworkStatusFetcher = singleNetworkStatusFetcher,
//         networksStatusesStore = networksStatusesStore,
//     )
//
//     @Test
//     fun `fetch networks statuses successfully`() = runTest {
//         val params = MultiNetworkStatusFetcher.Params(userWalletId = userWalletId, networks = ethereumAndStellar)
//
//         val ethParams = SingleNetworkStatusFetcher.Params(
//             userWalletId = userWalletId,
//             network = ethereumAndStellar.first(),
//             applyRefresh = false,
//         )
//
//         val stellarParams = SingleNetworkStatusFetcher.Params(
//             userWalletId = userWalletId,
//             network = ethereumAndStellar.last(),
//             applyRefresh = false,
//         )
//
//         coEvery { singleNetworkStatusFetcher(ethParams) } returns Unit.right()
//         coEvery { singleNetworkStatusFetcher(stellarParams) } returns Unit.right()
//
//         val actual = fetcher(params)
//
//         coVerify {
//             networksStatusesStore.refresh(userWalletId = userWalletId, networks = ethereumAndStellar)
//             singleNetworkStatusFetcher(ethParams)
//             singleNetworkStatusFetcher(stellarParams)
//         }
//
//         Truth.assertThat(actual.isRight()).isTrue()
//     }
//
//     @Test
//     fun `fetch networks statuses failure if one of them fails`() = runTest {
//         val params = MultiNetworkStatusFetcher.Params(userWalletId = userWalletId, networks = ethereumAndStellar)
//
//         val ethParams = SingleNetworkStatusFetcher.Params(
//             userWalletId = userWalletId,
//             network = ethereumAndStellar.first(),
//             applyRefresh = false,
//         )
//
//         val stellarParams = SingleNetworkStatusFetcher.Params(
//             userWalletId = userWalletId,
//             network = ethereumAndStellar.last(),
//             applyRefresh = false,
//         )
//
//         val ethException = IllegalStateException("eth")
//         coEvery { singleNetworkStatusFetcher(ethParams) } returns ethException.left()
//         coEvery { singleNetworkStatusFetcher(stellarParams) } returns Unit.right()
//
//         val actual = fetcher(params)
//
//         coVerify {
//             networksStatusesStore.refresh(userWalletId = userWalletId, networks = ethereumAndStellar)
//             singleNetworkStatusFetcher(ethParams)
//             singleNetworkStatusFetcher(stellarParams)
//         }
//
//         val expected = IllegalStateException("Failed to fetch network statuses")
//
//         Truth.assertThat(actual.isLeft()).isTrue()
//         Truth.assertThat(actual.leftOrNull()).isInstanceOf(expected::class.java)
//         Truth.assertThat(actual.leftOrNull()).hasMessageThat().isEqualTo(expected.message)
//     }
//
//     @Test
//     fun `fetch networks statuses failure if all of them fails`() = runTest {
//         val params = MultiNetworkStatusFetcher.Params(userWalletId = userWalletId, networks = ethereumAndStellar)
//
//         val ethParams = SingleNetworkStatusFetcher.Params(
//             userWalletId = userWalletId,
//             network = ethereumAndStellar.first(),
//             applyRefresh = false,
//         )
//
//         val stellarParams = SingleNetworkStatusFetcher.Params(
//             userWalletId = userWalletId,
//             network = ethereumAndStellar.last(),
//             applyRefresh = false,
//         )
//
//         coEvery { singleNetworkStatusFetcher(ethParams) } returns IllegalStateException("eth").left()
//         coEvery { singleNetworkStatusFetcher(stellarParams) } returns IllegalStateException("stellar").left()
//
//         val actual = fetcher(params)
//
//         coVerify {
//             networksStatusesStore.refresh(userWalletId = userWalletId, networks = ethereumAndStellar)
//             singleNetworkStatusFetcher(ethParams)
//             singleNetworkStatusFetcher(stellarParams)
//         }
//
//         val expected = IllegalStateException("Failed to fetch network statuses")
//
//         Truth.assertThat(actual.isLeft()).isTrue()
//         Truth.assertThat(actual.leftOrNull()).isInstanceOf(expected::class.java)
//         Truth.assertThat(actual.leftOrNull()).hasMessageThat().isEqualTo(expected.message)
//     }
//
//     private companion object {
//         val userWalletId = UserWalletId("011")
//         val ethereumAndStellar = MockCryptoCurrencyFactory().ethereumAndStellar.map { it.network }.toSet()
//     }
// }
