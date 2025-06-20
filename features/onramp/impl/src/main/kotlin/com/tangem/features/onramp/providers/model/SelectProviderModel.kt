package com.tangem.features.onramp.providers.model

import com.arkivanov.decompose.router.slot.SlotNavigation
import com.arkivanov.decompose.router.slot.activate
import com.arkivanov.decompose.router.slot.dismiss
import com.tangem.core.analytics.api.AnalyticsEventHandler
import com.tangem.core.decompose.di.ModelScoped
import com.tangem.core.decompose.model.Model
import com.tangem.core.decompose.model.ParamsContainer
import com.tangem.core.ui.extensions.resourceReference
import com.tangem.core.ui.extensions.stringReference
import com.tangem.core.ui.extensions.wrappedList
import com.tangem.core.ui.format.bigdecimal.crypto
import com.tangem.core.ui.format.bigdecimal.format
import com.tangem.core.ui.format.bigdecimal.percent
import com.tangem.domain.onramp.GetOnrampPaymentMethodsUseCase
import com.tangem.domain.onramp.GetOnrampProviderWithQuoteUseCase
import com.tangem.domain.onramp.analytics.OnrampAnalyticsEvent
import com.tangem.domain.onramp.model.OnrampPaymentMethod
import com.tangem.domain.onramp.model.OnrampProviderWithQuote
import com.tangem.domain.onramp.model.error.OnrampError
import com.tangem.domain.settings.usercountry.GetUserCountryUseCase
import com.tangem.domain.settings.usercountry.models.UserCountry
import com.tangem.domain.settings.usercountry.models.needApplyFCARestrictions
import com.tangem.features.onramp.impl.R
import com.tangem.features.onramp.paymentmethod.entity.PaymentMethodUM
import com.tangem.features.onramp.providers.SelectProviderComponent
import com.tangem.features.onramp.providers.entity.*
import com.tangem.features.onramp.utils.sendOnrampErrorEvent
import com.tangem.utils.StringsSigns.MINUS
import com.tangem.utils.coroutines.CoroutineDispatcherProvider
import com.tangem.utils.extensions.isSingleItem
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.collections.immutable.toPersistentList
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber
import java.math.BigDecimal
import java.util.Locale
import javax.inject.Inject

@Suppress("LongParameterList")
@ModelScoped
internal class SelectProviderModel @Inject constructor(
    override val dispatchers: CoroutineDispatcherProvider,
    private val analyticsEventHandler: AnalyticsEventHandler,
    private val getOnrampPaymentMethodsUseCase: GetOnrampPaymentMethodsUseCase,
    private val getOnrampProviderWithQuoteUseCase: GetOnrampProviderWithQuoteUseCase,
    getUserCountryUseCase: GetUserCountryUseCase,
    paramsContainer: ParamsContainer,
) : Model() {

    val state: StateFlow<SelectPaymentAndProviderUM> get() = _state.asStateFlow()
    val bottomSheetNavigation: SlotNavigation<ProviderListBottomSheetConfig> = SlotNavigation()
    private val params: SelectProviderComponent.Params = paramsContainer.require()
    private val _state = MutableStateFlow(getInitialState())

    private var userCountry: UserCountry? = null

    init {
        userCountry = getUserCountryUseCase.invokeSync().getOrNull()
            ?: UserCountry.Other(Locale.getDefault().country)

        analyticsEventHandler.send(OnrampAnalyticsEvent.ProvidersScreenOpened)
        getPaymentMethods()
        getProviders(params.selectedPaymentMethod)
    }

    private fun getPaymentMethods() {
        modelScope.launch {
            val methods = getOnrampPaymentMethodsUseCase().fold(
                ifLeft = { error ->
                    sendOnrampErrorEvent(error)
                    emptySet()
                },
                ifRight = { it },
            )

            val filteredEmptyMethods = methods.mapNotNull { method ->
                val providers = getOnrampProviderWithQuoteUseCase(method).getOrNull()
                if (!providers.isNullOrEmpty()) {
                    val allErrorProviders = providers.all {
                        it is OnrampProviderWithQuote.Unavailable.NotSupportedPaymentMethod
                    }
                    PaymentProviderUM(
                        paymentMethod = method,
                        providers = providers.toProvidersListItems(),
                    ).takeIf { !allErrorProviders }
                } else {
                    null
                }
            }
            _state.value = state.value.copy(
                paymentMethods = filteredEmptyMethods.toPersistentList(),
                isPaymentMethodClickEnabled = filteredEmptyMethods.isNotEmpty(),
            )
        }
    }

    private fun getProviders(paymentMethod: OnrampPaymentMethod) {
        modelScope.launch {
            getOnrampProviderWithQuoteUseCase.invoke(paymentMethod)
                .onRight { quotes ->
                    _state.update { state ->
                        state.copy(
                            selectedPaymentMethod = state.selectedPaymentMethod.copy(
                                providers = quotes.toProvidersListItems(),
                            ),
                        )
                    }
                }
                .onLeft { error ->
                    sendOnrampErrorEvent(error)
                    Timber.e(error.toString())
                }
        }
    }

    private fun getInitialState(): SelectPaymentAndProviderUM {
        return SelectPaymentAndProviderUM(
            paymentMethods = persistentListOf(
                PaymentProviderUM(
                    paymentMethod = params.selectedPaymentMethod,
                    providers = emptyList<ProviderListItemUM>().toImmutableList(),
                ),
            ),
            isPaymentMethodClickEnabled = false,
            onPaymentMethodClick = ::openPaymentMethods,
            selectedPaymentMethod = PaymentProviderUM(
                paymentMethod = params.selectedPaymentMethod,
                providers = emptyList<ProviderListItemUM>().toImmutableList(),
            ),
            selectedProviderId = params.selectedProviderId,
        )
    }

    private fun openPaymentMethods() {
        analyticsEventHandler.send(OnrampAnalyticsEvent.PaymentMethodsScreenOpened)
        bottomSheetNavigation.activate(
            ProviderListBottomSheetConfig.PaymentMethods(
                selectedMethodId = state.value.selectedPaymentMethod.paymentMethod.id,
                paymentMethodsUM = state.value.paymentMethods.toPaymentUMList(),
            ),
        )
    }

    private fun ImmutableList<PaymentProviderUM>.toPaymentUMList(): ImmutableList<PaymentMethodUM> = map { method ->
        PaymentMethodUM(
            id = method.paymentMethod.id,
            imageUrl = method.paymentMethod.imageUrl,
            name = method.paymentMethod.name,
            onSelect = { onPaymentMethodSelected(method) },
        )
    }.toPersistentList()

    private fun onPaymentMethodSelected(methodContainer: PaymentProviderUM) {
        val paymentMethod = methodContainer.paymentMethod
        val firstProvider = methodContainer.providers.firstOrNull()
        analyticsEventHandler.send(OnrampAnalyticsEvent.OnPaymentMethodChosen(paymentMethod = paymentMethod.name))
        modelScope.launch {
            _state.update { state ->
                state.copy(
                    selectedPaymentMethod = state.selectedPaymentMethod.copy(
                        paymentMethod = paymentMethod,
                    ),
                )
            }
            getProviders(paymentMethod)
            if (firstProvider is ProviderListItemUM.Available) {
                onProviderSelected(firstProvider.providerResult, firstProvider.isBestRate)
            }

            bottomSheetNavigation.dismiss()
        }
    }

    @Suppress("LongMethod")
    private fun List<OnrampProviderWithQuote>.toProvidersListItems(): ImmutableList<ProviderListItemUM> {
        val sorted = sortByRate()

        val bestProvider = sorted.firstOrNull() as? OnrampProviderWithQuote.Data
        val isMultipleQuotes = !sorted.isSingleItem()
        val isOtherQuotesHasData = sorted.filterNot { it == bestProvider }.any { it is OnrampProviderWithQuote.Data }
        val hasBestProvider = isMultipleQuotes && isOtherQuotesHasData

        return sorted.map { quote ->
            val isSelectedProvider = state.value.selectedProviderId == quote.provider.id

            when (quote) {
                is OnrampProviderWithQuote.Data -> {
                    val rate = quote.toAmount.value.format {
                        crypto(symbol = quote.toAmount.symbol, decimals = quote.toAmount.decimals)
                    }
                    val rateDiff = bestProvider?.toAmount?.value?.let { bestRate ->
                        BigDecimal.ONE - quote.toAmount.value / bestRate
                    }
                    val isBestProvider =
                        quote == bestProvider && hasBestProvider && !userCountry.needApplyFCARestrictions()
                    val providerResult = SelectProviderResult.ProviderWithQuote(
                        paymentMethod = quote.paymentMethod,
                        provider = quote.provider,
                        fromAmount = quote.fromAmount,
                        toAmount = quote.toAmount,
                    )
                    ProviderListItemUM.Available.Content(
                        providerId = quote.provider.id,
                        imageUrl = quote.provider.info.imageLarge,
                        name = quote.provider.info.name,
                        rate = rate,
                        isBestRate = isBestProvider,
                        diffRate = stringReference("$MINUS${rateDiff.format { percent() }}")
                            .takeIf { hasBestProvider },
                        providerResult = providerResult,
                        isSelected = isSelectedProvider,
                        onClick = {
                            onProviderSelected(
                                result = providerResult,
                                isBestRate = isBestProvider,
                            )
                            params.onDismiss()
                        },
                    )
                }
                is OnrampProviderWithQuote.Unavailable.AmountError -> {
                    val quoteError = quote.quoteError
                    val amount = quoteError.error.requiredAmount.format {
                        crypto(symbol = quoteError.fromAmount.symbol, decimals = quoteError.fromAmount.decimals)
                    }
                    val errorSubtitleRes = when (quoteError.error) {
                        is OnrampError.AmountError.TooBigError -> R.string.express_provider_max_amount
                        is OnrampError.AmountError.TooSmallError -> R.string.express_provider_min_amount
                    }
                    val providerResult = SelectProviderResult.ProviderWithError(
                        paymentMethod = quoteError.paymentMethod,
                        provider = quote.provider,
                        quoteError = quote.quoteError,
                    )
                    ProviderListItemUM.Available.WithError(
                        providerId = quote.provider.id,
                        imageUrl = quote.provider.info.imageLarge,
                        name = quote.provider.info.name,
                        subtitle = resourceReference(errorSubtitleRes, wrappedList(amount)),
                        providerResult = providerResult,
                        isBestRate = false,
                        isSelected = isSelectedProvider,
                        onClick = {
                            onProviderSelected(
                                result = providerResult,
                                isBestRate = bestProvider == quote && !userCountry.needApplyFCARestrictions(),
                            )
                            params.onDismiss()
                        },
                    )
                }
                is OnrampProviderWithQuote.Unavailable.NotSupportedPaymentMethod -> {
                    ProviderListItemUM.Unavailable(
                        providerId = quote.provider.id,
                        imageUrl = quote.provider.info.imageLarge,
                        name = quote.provider.info.name,
                        subtitle = resourceReference(
                            id = R.string.onramp_avaiable_with_payment_methods,
                            formatArgs = wrappedList(quote.availablePaymentMethods.joinToString { it.name }),
                        ),
                    )
                }
            }
        }.toImmutableList()
    }

    private fun onProviderSelected(result: SelectProviderResult, isBestRate: Boolean) {
        analyticsEventHandler.send(
            OnrampAnalyticsEvent.OnProviderChosen(
                providerName = result.provider.info.name,
                tokenSymbol = params.cryptoCurrency.symbol,
            ),
        )
        _state.update {
            it.copy(selectedProviderId = result.provider.id)
        }
        params.onProviderClick(result, isBestRate)
    }

    private fun sendOnrampErrorEvent(error: OnrampError) {
        val selectedProvider = state.value.selectedPaymentMethod.providers.firstOrNull {
            it.providerId == params.selectedProviderId
        }
        analyticsEventHandler.sendOnrampErrorEvent(
            error = error,
            tokenSymbol = params.cryptoCurrency.symbol,
            providerName = selectedProvider?.name,
            paymentMethod = state.value.selectedPaymentMethod.paymentMethod.name,
        )
    }

    /**
     * Sorting providers by rule:
     *
     * 1. Highest rate
     * 2. Smallest difference between entered amount and required min/max amount
     */
    private fun List<OnrampProviderWithQuote>.sortByRate() = sortedByDescending {
        when (it) {
            is OnrampProviderWithQuote.Data -> it.toAmount.value

            // negative difference to sort both when data and unavailable is present
            is OnrampProviderWithQuote.Unavailable.AmountError -> {
                when (val error = it.quoteError.error) {
                    is OnrampError.AmountError.TooSmallError -> it.quoteError.fromAmount.value - error.requiredAmount
                    is OnrampError.AmountError.TooBigError -> error.requiredAmount - it.quoteError.fromAmount.value
                    else -> null
                }
            }
            is OnrampProviderWithQuote.Unavailable.NotSupportedPaymentMethod -> null
        }
    }
}
