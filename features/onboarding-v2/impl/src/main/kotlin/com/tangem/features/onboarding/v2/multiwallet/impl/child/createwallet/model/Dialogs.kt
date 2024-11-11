package com.tangem.features.onboarding.v2.multiwallet.impl.child.createwallet.model

import com.tangem.core.ui.extensions.resourceReference
import com.tangem.features.onboarding.v2.impl.R
import com.tangem.features.onboarding.v2.multiwallet.impl.child.createwallet.ui.state.MultiWalletCreateWalletUM

internal fun resetCardDialog(onConfirm: () -> Unit, dismiss: () -> Unit, onDismissButtonClick: () -> Unit) =
    MultiWalletCreateWalletUM.Dialog(
        title = resourceReference(R.string.onboarding_activation_error_title),
        description = resourceReference(R.string.onboarding_activation_error_message),
        confirmButtonText = resourceReference(R.string.common_support),
        dismissButtonText = resourceReference(R.string.common_ok),
        dismissWarningColor = true,
        onDismiss = dismiss,
        onConfirm = {
            onConfirm()
            dismiss()
        },
        onDismissButtonClick = {
            onDismissButtonClick()
            dismiss()
        },
    )