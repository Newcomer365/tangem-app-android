package com.tangem.tap.common.analytics.events

import com.tangem.core.analytics.models.AnalyticsEvent

/**
 * Created by Anton Zhilenkov on 23.01.2023.
 */
sealed class SignIn(
    event: String,
    params: Map<String, String> = mapOf(),
) : AnalyticsEvent("Sign In", event, params) {

    class ScreenOpened : SignIn(event = "Sign In Screen Opened")

    class ButtonBiometricSignIn : SignIn(event = "Button - Biometric Sign In")
    class ButtonCardSignIn : SignIn(event = "Button - Card Sign In")
}
