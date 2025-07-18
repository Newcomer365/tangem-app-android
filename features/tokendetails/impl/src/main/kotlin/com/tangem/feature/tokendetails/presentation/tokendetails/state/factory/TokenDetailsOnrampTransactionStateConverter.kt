package com.tangem.feature.tokendetails.presentation.tokendetails.state.factory

import com.tangem.common.ui.expressStatus.state.*
import com.tangem.common.ui.notifications.ExpressNotificationsUM
import com.tangem.common.ui.notifications.NotificationUM
import com.tangem.core.analytics.api.AnalyticsEventHandler
import com.tangem.core.ui.components.currency.icon.CurrencyIconState
import com.tangem.core.ui.components.currency.icon.converter.CryptoCurrencyToIconStateConverter
import com.tangem.core.ui.extensions.resourceReference
import com.tangem.core.ui.extensions.stringReference
import com.tangem.core.ui.extensions.wrappedList
import com.tangem.core.ui.format.bigdecimal.crypto
import com.tangem.core.ui.format.bigdecimal.fiat
import com.tangem.core.ui.format.bigdecimal.format
import com.tangem.core.ui.utils.toDateFormatWithTodayYesterday
import com.tangem.core.ui.utils.toTimeFormat
import com.tangem.domain.appcurrency.model.AppCurrency
import com.tangem.domain.onramp.model.OnrampStatus
import com.tangem.domain.onramp.model.cache.OnrampTransaction
import com.tangem.domain.tokens.model.CryptoCurrency
import com.tangem.domain.tokens.model.CryptoCurrencyStatus
import com.tangem.domain.tokens.model.analytics.TokenOnrampAnalyticsEvent
import com.tangem.feature.tokendetails.presentation.tokendetails.model.TokenDetailsClickIntents
import com.tangem.features.tokendetails.impl.R
import com.tangem.utils.Provider
import com.tangem.utils.converter.Converter
import kotlinx.collections.immutable.persistentListOf

internal class TokenDetailsOnrampTransactionStateConverter(
    private val clickIntents: TokenDetailsClickIntents,
    private val cryptoCurrency: CryptoCurrency,
    private val cryptoCurrencyStatusProvider: Provider<CryptoCurrencyStatus?>,
    private val appCurrencyProvider: Provider<AppCurrency>,
    private val analyticsEventHandler: AnalyticsEventHandler,
) : Converter<OnrampTransaction, ExpressTransactionStateUM.OnrampUM> {

    private val iconStateConverter = CryptoCurrencyToIconStateConverter()

    override fun convert(value: OnrampTransaction): ExpressTransactionStateUM.OnrampUM {
        val cryptoCurrencyStatus = cryptoCurrencyStatusProvider()
        val appCurrency = appCurrencyProvider()
        return ExpressTransactionStateUM.OnrampUM(
            info = ExpressTransactionStateInfoUM(
                title = resourceReference(id = R.string.express_status_buying, wrappedList(cryptoCurrency.name)),
                status = convertStatuses(value.status, value.externalTxUrl),
                notification = getNotification(value.status, value.externalTxUrl, value.providerName),
                txId = value.txId,
                txExternalId = value.externalTxId,
                txExternalUrl = value.externalTxUrl,
                timestamp = value.timestamp,
                timestampFormatted = resourceReference(
                    R.string.send_date_format,
                    wrappedList(
                        value.timestamp.toDateFormatWithTodayYesterday(),
                        value.timestamp.toTimeFormat(),
                    ),
                ),
                toAmount = stringReference(
                    value.toAmount.format { crypto(cryptoCurrency) },
                ),
                toFiatAmount = stringReference(
                    cryptoCurrencyStatus?.value?.fiatRate?.multiply(value.toAmount).format {
                        fiat(
                            fiatCurrencyCode = appCurrency.code,
                            fiatCurrencySymbol = appCurrency.symbol,
                        )
                    },
                ),
                toAmountSymbol = cryptoCurrency.symbol,
                toCurrencyIcon = iconStateConverter.convert(cryptoCurrency),
                fromAmount = stringReference(
                    value.fromAmount.format {
                        fiat(
                            fiatCurrencyCode = value.fromCurrency.name,
                            fiatCurrencySymbol = value.fromCurrency.code,
                        )
                    },
                ),
                fromFiatAmount = null,
                fromAmountSymbol = value.fromCurrency.code,
                fromCurrencyIcon = CurrencyIconState.FiatIcon(
                    url = value.fromCurrency.image,
                    fallbackResId = R.drawable.ic_currency_24,
                ),
                iconState = getIconState(value.status),
                onGoToProviderClick = {
                    analyticsEventHandler.send(TokenOnrampAnalyticsEvent.GoToProvider)
                    clickIntents.onGoToProviderClick(it)
                },
                onClick = { clickIntents.onExpressTransactionClick(value.txId) },
                onDisposeExpressStatus = clickIntents::onConfirmDisposeExpressStatus,
            ),
            providerName = value.providerName,
            providerImageUrl = value.providerImageUrl,
            providerType = value.providerType,
            activeStatus = value.status,
            fromCurrencyCode = value.fromCurrency.code,
        )
    }

    private fun getNotification(
        status: OnrampStatus.Status,
        externalTxUrl: String?,
        providerName: String,
    ): NotificationUM? {
        return when (status) {
            OnrampStatus.Status.Verifying -> {
                analyticsEventHandler.send(TokenOnrampAnalyticsEvent.NoticeKYC(cryptoCurrency.symbol, providerName))
                ExpressNotificationsUM.NeedVerification(onGoToProviderClick = onProviderClick(externalTxUrl))
            }
            OnrampStatus.Status.Failed -> {
                ExpressNotificationsUM.FailedByProvider(onGoToProviderClick = onProviderClick(externalTxUrl))
            }
            else -> null
        }
    }

    private fun onProviderClick(externalTxUrl: String?) = if (externalTxUrl != null) {
        {
            analyticsEventHandler.send(TokenOnrampAnalyticsEvent.GoToProvider)
            clickIntents.onGoToProviderClick(externalTxUrl)
        }
    } else {
        null
    }

    private fun getIconState(status: OnrampStatus.Status): ExpressTransactionStateIconUM {
        return when (status) {
            OnrampStatus.Status.RefundInProgress,
            OnrampStatus.Status.Verifying,
            -> ExpressTransactionStateIconUM.Warning
            OnrampStatus.Status.Refunded,
            OnrampStatus.Status.Failed,
            -> ExpressTransactionStateIconUM.Error
            else -> ExpressTransactionStateIconUM.None
        }
    }

    private fun convertStatuses(status: OnrampStatus.Status, externalTxUrl: String?): ExpressStatusUM {
        val statuses = with(status) {
            persistentListOf(
                getAwaitingDepositItem(),
                getPaymentProcessingItem(),
                getBuyingItem(),
                getSendingItem(),
            )
        }

        return ExpressStatusUM(
            title = resourceReference(R.string.common_transaction_status),
            link = getStatusLink(externalTxUrl),
            statuses = statuses,
        )
    }

    private fun OnrampStatus.Status.getAwaitingDepositItem() = ExpressStatusItemUM(
        text = when {
            order < OnrampStatus.Status.WaitingForPayment.order -> {
                resourceReference(R.string.express_exchange_status_receiving)
            }
            this == OnrampStatus.Status.WaitingForPayment -> {
                resourceReference(R.string.express_exchange_status_receiving_active)
            }
            else -> {
                resourceReference(R.string.express_exchange_status_received)
            }
        },
        state = getStatusState(OnrampStatus.Status.WaitingForPayment),
    )

    private fun OnrampStatus.Status.getPaymentProcessingItem() = ExpressStatusItemUM(
        text = when {
            order < OnrampStatus.Status.PaymentProcessing.order -> {
                resourceReference(R.string.express_exchange_status_confirming)
            }
            this == OnrampStatus.Status.PaymentProcessing -> {
                resourceReference(R.string.express_exchange_status_confirming_active)
            }
            this == OnrampStatus.Status.Verifying -> {
                resourceReference(R.string.express_exchange_status_verifying)
            }
            this == OnrampStatus.Status.Failed -> {
                resourceReference(R.string.express_exchange_status_failed)
            }
            else -> resourceReference(R.string.express_exchange_status_confirmed)
        },
        state = when {
            order < OnrampStatus.Status.PaymentProcessing.order -> {
                ExpressStatusItemState.Default
            }
            this == OnrampStatus.Status.PaymentProcessing -> {
                ExpressStatusItemState.Active
            }
            this == OnrampStatus.Status.Verifying -> {
                ExpressStatusItemState.Warning
            }
            this == OnrampStatus.Status.Failed -> {
                ExpressStatusItemState.Error
            }
            else -> {
                ExpressStatusItemState.Done
            }
        },
    )

    private fun OnrampStatus.Status.getBuyingItem() = ExpressStatusItemUM(
        text = when {
            order < OnrampStatus.Status.Paid.order -> {
                resourceReference(R.string.express_status_buying, wrappedList(cryptoCurrency.name))
            }
            this == OnrampStatus.Status.Paid -> {
                resourceReference(
                    R.string.express_status_buying_active,
                    wrappedList(cryptoCurrency.name),
                )
            }
            this == OnrampStatus.Status.RefundInProgress ||
                this == OnrampStatus.Status.Refunded -> {
                resourceReference(R.string.express_exchange_status_failed)
            }
            else -> {
                resourceReference(R.string.express_status_bought, wrappedList(cryptoCurrency.name))
            }
        },
        state = when {
            this == OnrampStatus.Status.RefundInProgress ||
                this == OnrampStatus.Status.Refunded -> ExpressStatusItemState.Error
            else -> getStatusState(OnrampStatus.Status.Paid)
        },
    )

    private fun OnrampStatus.Status.getSendingItem() = ExpressStatusItemUM(
        text = when {
            order < OnrampStatus.Status.Sending.order -> {
                resourceReference(
                    R.string.express_exchange_status_sending,
                    wrappedList(cryptoCurrency.name),
                )
            }
            this == OnrampStatus.Status.Sending -> {
                resourceReference(
                    R.string.express_exchange_status_sending_active,
                    wrappedList(cryptoCurrency.name),
                )
            }
            this == OnrampStatus.Status.RefundInProgress -> {
                resourceReference(R.string.express_exchange_status_refunding)
            }
            this == OnrampStatus.Status.Refunded -> {
                resourceReference(R.string.express_exchange_status_refunded)
            }
            else -> {
                resourceReference(
                    R.string.express_exchange_status_sent,
                    wrappedList(cryptoCurrency.name),
                )
            }
        },
        state = when {
            this == OnrampStatus.Status.RefundInProgress -> ExpressStatusItemState.Active
            this == OnrampStatus.Status.Refunded -> ExpressStatusItemState.Done
            else -> getStatusState(OnrampStatus.Status.Sending)
        },
    )

    private fun getStatusLink(externalTxUrl: String?): ExpressLinkUM {
        if (externalTxUrl == null) return ExpressLinkUM.Empty
        return ExpressLinkUM.Content(
            icon = R.drawable.ic_arrow_top_right_24,
            text = resourceReference(R.string.common_go_to_provider),
            onClick = {
                analyticsEventHandler.send(TokenOnrampAnalyticsEvent.GoToProvider)
                clickIntents.onGoToProviderClick(externalTxUrl)
            },
        )
    }

    private fun OnrampStatus.Status.getStatusState(targetState: OnrampStatus.Status) = when {
        order < targetState.order -> ExpressStatusItemState.Default
        this == targetState -> ExpressStatusItemState.Active
        else -> ExpressStatusItemState.Done
    }
}
