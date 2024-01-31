package com.tangem.feature.wallet.presentation.wallet.state2

import com.tangem.core.ui.components.bottomsheets.TangemBottomSheetConfigContent
import com.tangem.core.ui.event.consumedEvent
import com.tangem.domain.wallets.models.UserWalletId
import com.tangem.feature.wallet.presentation.wallet.state.components.WalletTopBarConfig
import com.tangem.feature.wallet.presentation.wallet.state2.model.NOT_INITIALIZED_WALLET_INDEX
import com.tangem.feature.wallet.presentation.wallet.state2.model.WalletScreenState
import com.tangem.feature.wallet.presentation.wallet.state2.model.WalletState
import com.tangem.feature.wallet.presentation.wallet.state2.transformers.CloseBottomSheetTransformer
import com.tangem.feature.wallet.presentation.wallet.state2.transformers.OpenBottomSheetTransformer
import com.tangem.feature.wallet.presentation.wallet.state2.transformers.WalletScreenStateTransformer
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Wallet state holder
 *
 * @author Andrew Khokhlov on 15/11/2023
 */
@Singleton
internal class WalletStateController @Inject constructor() {

    val uiState: StateFlow<WalletScreenState> get() = mutableUiState
    val value: WalletScreenState get() = uiState.value

    private val mutableUiState: MutableStateFlow<WalletScreenState> = MutableStateFlow(value = getInitialState())

    fun update(function: (WalletScreenState) -> WalletScreenState) {
        mutableUiState.update(function = function)
    }

    fun update(transformer: WalletScreenStateTransformer) {
        mutableUiState.update(function = transformer::transform)
    }

    fun clear() {
        mutableUiState.update { getInitialState() }
    }

    fun getWalletIfSelected(walletId: UserWalletId): WalletState? {
        val selectedWalletId = getSelectedWalletId()

        return value.wallets.firstOrNull {
            it.walletCardState.id == walletId && it.walletCardState.id == selectedWalletId
        }
    }

    fun getSelectedWallet(): WalletState {
        return with(value) { wallets[selectedWalletIndex] }
    }

    fun getSelectedWalletId(): UserWalletId {
        return with(value) { wallets[selectedWalletIndex].walletCardState.id }
    }

    fun showBottomSheet(content: TangemBottomSheetConfigContent, userWalletId: UserWalletId = getSelectedWalletId()) {
        update(
            OpenBottomSheetTransformer(
                userWalletId = userWalletId,
                content = content,
                onDismissBottomSheet = { update(CloseBottomSheetTransformer(userWalletId)) },
            ),
        )
    }

    private fun getInitialState(): WalletScreenState {
        return WalletScreenState(
            onBackClick = {},
            topBarConfig = WalletTopBarConfig(onDetailsClick = {}),
            selectedWalletIndex = NOT_INITIALIZED_WALLET_INDEX,
            wallets = persistentListOf(),
            onWalletChange = {},
            event = consumedEvent(),
            isHidingMode = false,
        )
    }
}