package com.tangem.tap.common.analytics.handlers.firebase

import com.tangem.common.json.MoshiJsonConverter
import com.tangem.tap.common.analytics.AnalyticsEventsLogger

/**
 * Created by Anton Zhilenkov on 22/09/2022.
 */
internal class FirebaseLogClient(
    jsonConverter: MoshiJsonConverter,
) : FirebaseAnalyticsClient {

    private val logger: AnalyticsEventsLogger = AnalyticsEventsLogger(FirebaseAnalyticsHandler.ID, jsonConverter)

    override fun logEvent(event: String, params: Map<String, String>) {
        logger.logEvent(event, params)
    }

    override fun logException(error: Throwable, params: Map<String, String>) {
        logger.logException(error, params)
    }
}
