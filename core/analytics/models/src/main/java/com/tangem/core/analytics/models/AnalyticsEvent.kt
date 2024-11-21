package com.tangem.core.analytics.models

/**
 * Created by Anton Zhilenkov on 28.09.2022.
 */
@Suppress("UnnecessaryAbstractClass")
abstract class AnalyticsEvent(
    val category: String,
    val event: String,
    var params: Map<String, EventValue> = mapOf(),
    val error: Throwable? = null,
) {

    val id: String = "[$category] $event"

    protected companion object {
        fun Int.asStringValue() = EventValue.StringValue(this.toString())

        fun String.asStringValue() = EventValue.StringValue(this)

        fun List<String>.asListValue() = EventValue.ListValue(this)
    }
}
