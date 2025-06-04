package com.tangem.tap.features.onboarding

import com.tangem.domain.redux.StateDialog

/**
 * Created by Anton Zhilenkov on 01.02.2023.
 */
sealed class OnboardingDialog : StateDialog {
    data class WalletActivationError(val onConfirm: () -> Unit) : OnboardingDialog()
}
