package com.tangem.features.send.impl.presentation.state.amount

import com.tangem.domain.tokens.model.CryptoCurrencyStatus
import com.tangem.features.send.impl.presentation.state.SendUiState
import com.tangem.features.send.impl.presentation.state.StateRouter
import com.tangem.features.send.impl.presentation.state.fields.SendAmountFieldChangeConverter
import com.tangem.features.send.impl.presentation.state.fields.SendAmountFieldMaxAmountConverter
import com.tangem.utils.Provider
import java.math.BigDecimal

/**
 * Factory to produce amount state for [SendUiState]
 */
internal class AmountStateFactory(
    private val stateRouterProvider: Provider<StateRouter>,
    private val currentStateProvider: Provider<SendUiState>,
    private val cryptoCurrencyStatusProvider: Provider<CryptoCurrencyStatus>,
) {

    private val amountFieldChangeConverter by lazy(LazyThreadSafetyMode.NONE) {
        SendAmountFieldChangeConverter(
            stateRouterProvider = stateRouterProvider,
            currentStateProvider = currentStateProvider,
            cryptoCurrencyStatusProvider = cryptoCurrencyStatusProvider,
        )
    }
    private val amountFieldMaxAmountConverter by lazy(LazyThreadSafetyMode.NONE) {
        SendAmountFieldMaxAmountConverter(
            stateRouterProvider = stateRouterProvider,
            currentStateProvider = currentStateProvider,
            cryptoCurrencyStatusProvider = cryptoCurrencyStatusProvider,
        )
    }

    private val amountCurrencyConverter by lazy(LazyThreadSafetyMode.NONE) {
        SendAmountCurrencyConverter(
            stateRouterProvider = stateRouterProvider,
            currentStateProvider = currentStateProvider,
            cryptoCurrencyStatusProvider = cryptoCurrencyStatusProvider,
        )
    }
    private val amountPasteConverter by lazy(LazyThreadSafetyMode.NONE) {
        SendAmountPastedTriggerDismissConverter(
            stateRouterProvider = stateRouterProvider,
            currentStateProvider = currentStateProvider,
        )
    }
    private val amountReducedConverter by lazy {
        SendAmountReducedConverter(
            stateRouterProvider = stateRouterProvider,
            currentStateProvider = currentStateProvider,
            cryptoCurrencyStatusProvider = cryptoCurrencyStatusProvider,
        )
    }

    fun getOnAmountValueChange(value: String) = amountFieldChangeConverter.convert(value)

    fun getOnAmountReducedState(reduceAmountBy: BigDecimal) = amountReducedConverter.convert(reduceAmountBy)

    fun getOnMaxAmountClick(): SendUiState {
        return amountFieldMaxAmountConverter.convert(Unit)
    }

    fun getOnCurrencyChangedState(isFiat: Boolean) = amountCurrencyConverter.convert(isFiat)

    fun getOnAmountPastedTriggerDismiss() = amountPasteConverter.convert(false)
}
