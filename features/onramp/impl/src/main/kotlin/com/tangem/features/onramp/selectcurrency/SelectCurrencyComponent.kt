package com.tangem.features.onramp.selectcurrency

import com.tangem.core.decompose.factory.ComponentFactory
import com.tangem.core.ui.decompose.ComposableBottomSheetComponent
import com.tangem.domain.tokens.model.CryptoCurrency
import com.tangem.domain.wallets.models.UserWallet

internal interface SelectCurrencyComponent : ComposableBottomSheetComponent {

    data class Params(
        val userWallet: UserWallet,
        val cryptoCurrency: CryptoCurrency,
        val onDismiss: () -> Unit,
    )

    interface Factory : ComponentFactory<Params, SelectCurrencyComponent>
}
