package com.tangem.features.markets.portfolio.impl.loader

import com.tangem.domain.appcurrency.model.AppCurrency
import com.tangem.domain.core.lce.Lce
import com.tangem.domain.tokens.error.TokenListError
import com.tangem.domain.tokens.model.CryptoCurrencyStatus
import com.tangem.domain.tokens.model.TotalFiatBalance
import com.tangem.domain.wallets.models.UserWallet
import com.tangem.domain.wallets.models.UserWalletId

/**
 * Portfolio data. Combined data from all flows that required to setup portfolio
 *
 * @property walletsWithCurrencyStatuses wallets with crypto currency statuses
 * @property appCurrency                 app currency
 * @property isBalanceHidden             flag that indicates if balance should be hidden
 * @property walletsWithBalance          wallets with total balance
 *
 * @author Andrew Khokhlov on 28/08/2024
 */
internal data class PortfolioData(
    val walletsWithCurrencyStatuses: Map<UserWallet, List<CryptoCurrencyStatus>>,
    val appCurrency: AppCurrency,
    val isBalanceHidden: Boolean,
    val walletsWithBalance: Map<UserWalletId, Lce<TokenListError, TotalFiatBalance>>,
)