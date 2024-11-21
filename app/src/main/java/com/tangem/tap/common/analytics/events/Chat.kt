package com.tangem.tap.common.analytics.events

import com.tangem.core.analytics.models.AnalyticsEvent
import com.tangem.core.analytics.models.EventValue

/**
 * Created by Anton Zhilenkov on 28.09.2022.
 */
sealed class Chat(
    event: String,
    params: Map<String, EventValue> = mapOf(),
) : AnalyticsEvent("Chat", event, params) {

    class ScreenOpened : Chat("Chat Screen Opened")
}
