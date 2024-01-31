package com.tangem.features.send.impl.presentation.state.fee

import com.tangem.core.ui.components.notifications.NotificationConfig
import com.tangem.core.ui.extensions.TextReference
import com.tangem.core.ui.extensions.resourceReference
import com.tangem.core.ui.extensions.wrappedList
import com.tangem.features.send.impl.R

sealed class SendFeeNotification(val config: NotificationConfig) {

    sealed class Informational(
        val title: TextReference,
        val subtitle: TextReference,
    ) : SendFeeNotification(
        config = NotificationConfig(
            title = title,
            subtitle = subtitle,
            iconResId = R.drawable.ic_alert_circle_24,
        ),
    ) {
        object TooLow : Informational(
            title = resourceReference(id = R.string.send_notification_transaction_delay_title),
            subtitle = resourceReference(id = R.string.send_notification_transaction_delay_text),
        )
    }

    sealed class Warning(
        val title: TextReference,
        val subtitle: TextReference,
        val buttonsState: NotificationConfig.ButtonsState? = null,
    ) : SendFeeNotification(
        config = NotificationConfig(
            title = title,
            subtitle = subtitle,
            iconResId = R.drawable.img_attention_20,
            buttonsState = buttonsState,
        ),
    ) {
        data class TooHigh(
            val value: String,
        ) : Warning(
            title = resourceReference(id = R.string.send_notification_fee_too_high_title),
            subtitle = resourceReference(id = R.string.send_notification_fee_too_high_text, wrappedList(value)),
        )

        object NetworkCoverage : Warning(
            title = resourceReference(id = R.string.send_network_fee_warning_title),
            subtitle = resourceReference(id = R.string.send_network_fee_warning_content),
        )

        data class NetworkFeeUnreachable(val onRefresh: () -> Unit) : Warning(
            title = resourceReference(R.string.send_fee_unreachable_error_title),
            subtitle = resourceReference(R.string.send_fee_unreachable_error_text),
            buttonsState = NotificationConfig.ButtonsState.SecondaryButtonConfig(
                text = resourceReference(R.string.warning_button_refresh),
                onClick = onRefresh,
            ),
        )
    }

    sealed class Error(
        val title: TextReference,
        val subtitle: TextReference,
        val iconResId: Int,
        val buttonsState: NotificationConfig.ButtonsState,
    ) : SendFeeNotification(
        config = NotificationConfig(
            title = title,
            subtitle = subtitle,
            iconResId = iconResId,
            buttonsState = buttonsState,
        ),
    ) {
        data class ExceedsBalance(
            val networkIconId: Int,
            val onClick: () -> Unit,
        ) : Error(
            title = resourceReference(id = R.string.send_notification_exceed_fee_title),
            subtitle = resourceReference(id = R.string.send_notification_exceed_fee_text),
            iconResId = networkIconId,
            buttonsState = NotificationConfig.ButtonsState.SecondaryButtonConfig(
                text = resourceReference(R.string.common_go_to_provider),
                onClick = onClick,
            ),
        )
    }
}