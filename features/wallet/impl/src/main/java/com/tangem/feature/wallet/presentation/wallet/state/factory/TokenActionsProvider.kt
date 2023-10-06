package com.tangem.feature.wallet.presentation.wallet.state.factory

import com.tangem.common.extensions.isZero
import com.tangem.core.ui.extensions.TextReference
import com.tangem.core.ui.extensions.resourceReference
import com.tangem.domain.tokens.model.CryptoCurrencyStatus
import com.tangem.domain.tokens.model.TokenActionsState
import com.tangem.feature.wallet.impl.R
import com.tangem.feature.wallet.presentation.common.state.TokenItemState
import com.tangem.feature.wallet.presentation.wallet.state.TokenActionButtonConfig
import com.tangem.feature.wallet.presentation.wallet.viewmodels.WalletClickIntents
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList

/**
 * Converter from loaded [TokenItemState.Content] to ImmutableList<[TokenActionButtonConfig]>
 *
 * @property clickIntents screen click intents
 *
 */
@Suppress("UnusedPrivateMember") // will be used in next PRs
internal class TokenActionsProvider(private val clickIntents: WalletClickIntents) {

    fun provideActions(tokenActions: TokenActionsState): ImmutableList<TokenActionButtonConfig> {
        return tokenActions.states
            .mapNotNull {
                mapTokenActionState(it, tokenActions.cryptoCurrencyStatus)
            }
            .toImmutableList()
    }

    private fun mapTokenActionState(
        actionsState: TokenActionsState.ActionState,
        cryptoCurrencyStatus: CryptoCurrencyStatus,
    ): TokenActionButtonConfig? {
        if (actionsState is TokenActionsState.ActionState.Send &&
            cryptoCurrencyStatus.value.amount?.isZero() == true
        ) {
            return null
        }
        val title: TextReference
        val icon: Int
        val action: () -> Unit
        when (actionsState) {
            is TokenActionsState.ActionState.Buy -> {
                title = resourceReference(R.string.common_buy)
                icon = R.drawable.ic_plus_24
                action = { clickIntents.onBuyClick(cryptoCurrencyStatus) }
            }
            is TokenActionsState.ActionState.Receive -> {
                title = resourceReference(R.string.common_receive)
                icon = R.drawable.ic_arrow_down_24
                action = { clickIntents.onReceiveClick(cryptoCurrencyStatus) }
            }
            is TokenActionsState.ActionState.Sell -> {
                title = resourceReference(R.string.common_sell)
                icon = R.drawable.ic_currency_24
                action = { clickIntents.onSellClick(cryptoCurrencyStatus) }
            }
            is TokenActionsState.ActionState.Send -> {
                title = resourceReference(R.string.common_send)
                icon = R.drawable.ic_arrow_up_24
                action = { clickIntents.onMultiCurrencySendClick(cryptoCurrencyStatus) }
            }
            is TokenActionsState.ActionState.Swap -> {
                title = resourceReference(R.string.common_swap)
                icon = R.drawable.ic_exchange_horizontal_24
                action = { clickIntents.onSwapClick(cryptoCurrencyStatus) }
            }
            is TokenActionsState.ActionState.CopyAddress -> {
                title = resourceReference(R.string.common_copy_address)
                icon = R.drawable.ic_copy_24
                action = { clickIntents.onCopyAddressClick(cryptoCurrencyStatus) }
            }
            is TokenActionsState.ActionState.HideToken -> {
                title = resourceReference(R.string.token_details_hide_token)
                icon = R.drawable.ic_trash_24
                action = { clickIntents.onHideTokensClick(cryptoCurrencyStatus) }
            }
        }
        return TokenActionButtonConfig(
            text = title,
            iconResId = icon,
            onClick = action,
            enabled = actionsState.enabled,
        )
    }
}