package com.tangem.core.ui.utils

import com.tangem.core.decompose.ui.UiMessageSender
import com.tangem.core.error.UniversalError
import com.tangem.core.ui.message.dialog.Dialogs

fun UiMessageSender.showErrorDialog(universalError: UniversalError, onDismiss: () -> Unit = {}) {
    send(Dialogs.universalErrorDialog(universalError, onDismiss = onDismiss))
}
