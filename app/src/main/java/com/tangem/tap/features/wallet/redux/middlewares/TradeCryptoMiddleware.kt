package com.tangem.tap.features.wallet.redux.middlewares

import com.tangem.blockchain.blockchains.ethereum.EthereumWalletManager
import com.tangem.blockchain.common.Blockchain
import com.tangem.common.routing.AppRoute
import com.tangem.common.routing.AppRouter
import com.tangem.core.analytics.Analytics
import com.tangem.domain.onramp.model.OnrampSource
import com.tangem.domain.tokens.legacy.TradeCryptoAction
import com.tangem.domain.tokens.model.CryptoCurrency
import com.tangem.domain.tokens.model.NetworkAddress
import com.tangem.domain.wallets.models.UserWalletId
import com.tangem.tap.common.analytics.events.Token
import com.tangem.tap.common.apptheme.MutableAppThemeModeHolder
import com.tangem.tap.common.extensions.dispatchDebugErrorNotification
import com.tangem.tap.common.extensions.dispatchNavigationAction
import com.tangem.tap.common.extensions.dispatchOpenUrl
import com.tangem.tap.common.extensions.inject
import com.tangem.tap.common.redux.AppState
import com.tangem.tap.features.demo.DemoHelper
import com.tangem.tap.network.exchangeServices.CurrencyExchangeManager
import com.tangem.tap.network.exchangeServices.buyErc20TestnetTokens
import com.tangem.tap.proxy.redux.DaggerGraphState
import com.tangem.tap.scope
import com.tangem.tap.store
import kotlinx.coroutines.launch
import org.rekotlin.Middleware

@Deprecated("Will be removed soon")
object TradeCryptoMiddleware {

    val middleware: Middleware<AppState> = { _, appState ->
        { nextDispatch ->
            { action ->
                if (action is TradeCryptoAction) {
                    handle(appState, action)
                }
                nextDispatch(action)
            }
        }
    }

    private fun handle(state: () -> AppState?, action: TradeCryptoAction) {
        if (DemoHelper.tryHandle(state, action)) return

        when (action) {
            is TradeCryptoAction.FinishSelling -> openReceiptUrl(action.transactionId)
            is TradeCryptoAction.Buy -> proceedBuyAction(action)
            is TradeCryptoAction.Sell -> proceedSellAction(action)
        }
    }

    private fun proceedBuyAction(action: TradeCryptoAction.Buy) {
        val isOnrampEnabled = store.inject(DaggerGraphState::onrampFeatureToggles).isFeatureEnabled
        if (isOnrampEnabled) {
            proceedWithOnramp(action.userWallet.walletId, action.cryptoCurrencyStatus.currency, action.source)
        } else {
            proceedWithLegacyBuyAction(action)
        }
    }

    private fun proceedWithOnramp(userWalletId: UserWalletId, cryptoCurrency: CryptoCurrency, source: OnrampSource) {
        store.dispatchNavigationAction {
            push(
                AppRoute.Onramp(
                    userWalletId = userWalletId,
                    currency = cryptoCurrency,
                    source = source,
                ),
            )
        }
    }

    private fun proceedWithLegacyBuyAction(action: TradeCryptoAction.Buy) {
        val networkAddress = action.cryptoCurrencyStatus.value.networkAddress
            ?.defaultAddress
            ?.let(NetworkAddress.Address::value)
            ?: return

        val status = action.cryptoCurrencyStatus
        val currency = status.currency
        val blockchain = Blockchain.fromId(currency.network.id.value)
        val exchangeManager = store.state.globalState.exchangeManager
        val topUrl = exchangeManager.getUrl(
            action = CurrencyExchangeManager.Action.Buy,
            cryptoCurrency = currency,
            fiatCurrencyName = action.appCurrencyCode,
            walletAddress = networkAddress,
            isDarkTheme = MutableAppThemeModeHolder.isDarkThemeActive,
        )

        scope.launch {
            if (currency is CryptoCurrency.Token && currency.network.isTestnet) {
                val walletManager = store.inject(DaggerGraphState::walletManagersFacade)
                    .getOrCreateWalletManager(
                        userWalletId = action.userWallet.walletId,
                        blockchain = blockchain,
                        derivationPath = currency.network.derivationPath.value,
                    )

                if (walletManager !is EthereumWalletManager) {
                    store.dispatchDebugErrorNotification("Testnet tokens available only for the Ethereum")
                    return@launch
                }

                buyErc20TestnetTokens(
                    card = action.userWallet.scanResponse.card,
                    walletManager = walletManager,
                    destinationAddress = currency.contractAddress,
                )
                return@launch
            }

            topUrl?.let {
                store.dispatchOpenUrl(it)
                Analytics.send(Token.Topup.ScreenOpened())
            }
        }
    }

    private fun proceedSellAction(action: TradeCryptoAction.Sell) {
        val networkAddress = action.cryptoCurrencyStatus.value.networkAddress
            ?.defaultAddress
            ?.let(NetworkAddress.Address::value)
            ?: return
        val currency = action.cryptoCurrencyStatus.currency

        store.state.globalState.exchangeManager.getUrl(
            action = CurrencyExchangeManager.Action.Sell,
            cryptoCurrency = currency,
            fiatCurrencyName = action.appCurrencyCode,
            walletAddress = networkAddress,
            isDarkTheme = MutableAppThemeModeHolder.isDarkThemeActive,
        )?.let {
            store.dispatchOpenUrl(it)
            Analytics.send(Token.Withdraw.ScreenOpened())
        }
    }

    private fun openReceiptUrl(transactionId: String) {
        store.dispatchNavigationAction(AppRouter::pop)
        store.state.globalState.exchangeManager.getSellCryptoReceiptUrl(
            action = CurrencyExchangeManager.Action.Sell,
            transactionId = transactionId,
        )?.let { store.dispatchOpenUrl(it) }
    }
}
