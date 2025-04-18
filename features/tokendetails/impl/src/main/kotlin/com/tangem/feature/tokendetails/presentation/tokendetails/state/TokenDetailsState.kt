package com.tangem.feature.tokendetails.presentation.tokendetails.state

import com.tangem.common.ui.expressStatus.state.ExpressTransactionStateUM
import com.tangem.core.ui.components.bottomsheets.TangemBottomSheetConfig
import com.tangem.core.ui.components.marketprice.MarketPriceBlockState
import com.tangem.core.ui.components.transactions.state.TransactionState
import com.tangem.core.ui.components.transactions.state.TxHistoryState
import com.tangem.core.ui.components.containers.pullToRefresh.PullToRefreshConfig
import com.tangem.feature.tokendetails.presentation.tokendetails.state.components.TokenDetailsDialogConfig
import com.tangem.feature.tokendetails.presentation.tokendetails.state.components.TokenDetailsNotification
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.PersistentList

internal data class TokenDetailsState(
    val topAppBarConfig: TokenDetailsTopAppBarConfig,
    val tokenInfoBlockState: TokenInfoBlockState,
    val tokenBalanceBlockState: TokenDetailsBalanceBlockState,
    val marketPriceBlockState: MarketPriceBlockState,
    val stakingBlocksState: StakingBlockUM?,
    val notifications: ImmutableList<TokenDetailsNotification>,
    val pendingTxs: PersistentList<TransactionState>,
    val expressTxsToDisplay: PersistentList<ExpressTransactionStateUM>,
    val expressTxs: PersistentList<ExpressTransactionStateUM>,
    val txHistoryState: TxHistoryState,
    val dialogConfig: TokenDetailsDialogConfig?,
    val pullToRefreshConfig: PullToRefreshConfig,
    val bottomSheetConfig: TangemBottomSheetConfig?,
    val isBalanceHidden: Boolean,
    val isMarketPriceAvailable: Boolean,
)
