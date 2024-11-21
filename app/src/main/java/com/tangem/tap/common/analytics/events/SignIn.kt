package com.tangem.tap.common.analytics.events

import com.tangem.core.analytics.models.AnalyticsEvent
import com.tangem.core.analytics.models.EventValue

/**
 * Created by Anton Zhilenkov on 23.01.2023.
 */
sealed class SignIn(
    event: String,
    params: Map<String, EventValue> = mapOf(),
    error: Throwable? = null,
) : AnalyticsEvent("Sign In", event, params, error) {

    class ScreenOpened : SignIn(event = "Sing In Screen Opened")

    class ButtonBiometricSignIn : SignIn(event = "Button - Biometric Sign In")
    class ButtonCardSignIn : SignIn(event = "Button - Card Sign In")
}
