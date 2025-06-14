package com.tangem.tap.common.redux

import com.tangem.common.extensions.VoidCallback
import com.tangem.domain.redux.StateDialog
import com.tangem.wallet.R

/**
 * Created by Anton Zhilenkov on 25/09/2021.
 */
sealed class AppDialog : StateDialog {
    data class SimpleOkDialogRes(
        val headerId: Int,
        val messageId: Int,
        val args: List<String> = emptyList(),
        val onOk: VoidCallback? = null,
    ) : AppDialog()

    data class RemoveWalletDialog(
        val currencyTitle: String,
        val onOk: () -> Unit,
    ) : AppDialog() {
        val messageRes: Int = R.string.token_details_hide_alert_message
        val titleRes: Int = R.string.token_details_hide_alert_title
        val primaryButtonRes: Int = R.string.token_details_hide_alert_hide
    }

    data class TokensAreLinkedDialog(
        val currencyTitle: String,
        val currencySymbol: String,
        val networkName: String,
    ) : AppDialog() {
        val messageRes: Int = R.string.token_details_unable_hide_alert_message
        val titleRes: Int = R.string.token_details_unable_hide_alert_title
    }

    data class WalletAlreadyWasUsedDialog(
        val onOk: () -> Unit,
        val onSupportClick: () -> Unit,
        val onCancel: () -> Unit,
    ) : AppDialog()
}
