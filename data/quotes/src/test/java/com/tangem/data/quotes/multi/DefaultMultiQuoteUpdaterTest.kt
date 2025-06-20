package com.tangem.data.quotes.multi

import arrow.core.right
import com.google.common.truth.Truth
import com.tangem.common.test.utils.getEmittedValues
import com.tangem.data.quotes.store.QuotesStoreV2
import com.tangem.datasource.api.tangemTech.models.CurrenciesResponse
import com.tangem.datasource.appcurrency.AppCurrencyResponseStore
import com.tangem.domain.quotes.multi.MultiQuoteFetcher
import com.tangem.utils.coroutines.TestingCoroutineDispatcherProvider
import io.mockk.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.test.runTest
import org.junit.Test

/**
 * @author Andrew Khokhlov on 10/04/2025
 */
internal class DefaultMultiQuoteUpdaterTest {

    private val appCurrencyResponseStore: AppCurrencyResponseStore = mockk()
    private val quotesStore: QuotesStoreV2 = mockk()
    private val multiQuoteFetcher: MultiQuoteFetcher = mockk()

    private val multiQuoteUpdater = DefaultMultiQuoteUpdater(
        appCurrencyResponseStore = appCurrencyResponseStore,
        quotesStore = quotesStore,
        multiQuoteFetcher = multiQuoteFetcher,
        dispatchers = TestingCoroutineDispatcherProvider(),
    )

    @Test
    fun `test that initial app currency is skipped`() = runTest {
        val appCurrencyFlow = flowOf(null, usdAppCurrency)

        every { appCurrencyResponseStore.get() } returns appCurrencyFlow
        coEvery { quotesStore.getAllSyncOrNull() } returns emptySet()

        val params = MultiQuoteFetcher.Params(currenciesIds = emptySet(), appCurrencyId = usdAppCurrency.id)
        coEvery { multiQuoteFetcher(params) } returns Unit.right()

        val actual = multiQuoteUpdater.getMultiQuoteUpdatesFlow()

        coVerify(exactly = 1) { appCurrencyResponseStore.get() }

        val values = getEmittedValues(actual)
        Truth.assertThat(values).isEqualTo(listOf(Unit.right()))

        coVerifyOrder {
            quotesStore.getAllSyncOrNull()
            multiQuoteFetcher(params)
        }
    }

    @Test
    fun `test that flow is filtered the same status`() = runTest {
        val appCurrencyFlow = flowOf(usdAppCurrency, usdAppCurrency)

        every { appCurrencyResponseStore.get() } returns appCurrencyFlow
        coEvery { quotesStore.getAllSyncOrNull() } returns emptySet()

        val params = MultiQuoteFetcher.Params(currenciesIds = emptySet(), appCurrencyId = usdAppCurrency.id)
        coEvery { multiQuoteFetcher(params) } returns Unit.right()

        val actual = multiQuoteUpdater.getMultiQuoteUpdatesFlow()

        coVerify(exactly = 1) { appCurrencyResponseStore.get() }

        val values = getEmittedValues(actual)
        Truth.assertThat(values).isEqualTo(listOf(Unit.right()))

        coVerifyOrder {
            quotesStore.getAllSyncOrNull()
            multiQuoteFetcher(params)
        }
    }

    @Test
    fun `test that flow is filtered the null`() = runTest {
        val appCurrencyFlow = flowOf(null, null)

        every { appCurrencyResponseStore.get() } returns appCurrencyFlow

        val actual = multiQuoteUpdater.getMultiQuoteUpdatesFlow()

        coVerify(exactly = 1) { appCurrencyResponseStore.get() }

        val values = getEmittedValues(actual)
        Truth.assertThat(values.size).isEqualTo(0)

        coVerify(inverse = true) {
            quotesStore.getAllSyncOrNull()
            multiQuoteFetcher(any())
        }
    }

    @Test
    fun `test if flow throws exception`() = runTest {
        val exception = IllegalStateException()

        val innerFlow = MutableStateFlow(value = false)
        val appCurrencyFlow = flow {
            if (innerFlow.value) {
                emitAll(flowOf(null, usdAppCurrency))
            } else {
                throw exception
            }
        }
            .buffer(capacity = 5)

        every { appCurrencyResponseStore.get() } returns appCurrencyFlow
        coEvery { quotesStore.getAllSyncOrNull() } returns emptySet()

        val params = MultiQuoteFetcher.Params(currenciesIds = emptySet(), appCurrencyId = usdAppCurrency.id)
        coEvery { multiQuoteFetcher(params) } returns Unit.right()

        val actual = multiQuoteUpdater.getMultiQuoteUpdatesFlow()

        coVerify(exactly = 1) { appCurrencyResponseStore.get() }

        val values1 = getEmittedValues(actual)
        Truth.assertThat(values1.size).isEqualTo(1)
        Truth.assertThat(values1.first().isLeft()).isTrue()
        Truth.assertThat(values1.first().leftOrNull()).isInstanceOf(exception::class.java)
        Truth.assertThat(values1.first().leftOrNull()).hasMessageThat().isEqualTo(exception.message)

        coVerify(inverse = true) {
            quotesStore.getAllSyncOrNull()
            multiQuoteFetcher(any())
        }

        innerFlow.emit(value = true)

        val values2 = getEmittedValues(flow = actual)
        Truth.assertThat(values2).isEqualTo(listOf(Unit.right()))

        coVerifyOrder {
            quotesStore.getAllSyncOrNull()
            multiQuoteFetcher(params)
        }
    }

    @Test
    fun `subscribe and unsubscribe successfully`() {
        every { appCurrencyResponseStore.get() } returns emptyFlow()

        val updaterJobHolder = multiQuoteUpdater.getUpdaterJobHolder()
        Truth.assertThat(updaterJobHolder.isEmpty()).isTrue()

        multiQuoteUpdater.subscribe()

        Truth.assertThat(updaterJobHolder.isEmpty()).isFalse()

        multiQuoteUpdater.unsubscribe()

        Truth.assertThat(updaterJobHolder.isEmpty()).isTrue()
    }

    private companion object {
        val usdAppCurrency = CurrenciesResponse.Currency(
            id = "USD".lowercase(),
            code = "USD",
            name = "US Dollar",
            unit = "$",
            type = "fiat",
            rateBTC = "",
        )
    }
}
