package com.tangem.feature.tokendetails.presentation.tokendetails.state.factory.txhistory

import com.tangem.core.ui.components.transactions.state.TransactionState
import com.tangem.core.ui.extensions.TextReference
import com.tangem.core.ui.utils.DateTimeFormatters
import com.tangem.domain.txhistory.models.TxHistoryItem
import com.tangem.features.tokendetails.impl.R
import com.tangem.utils.converter.Converter
import com.tangem.utils.toBriefAddressFormat
import com.tangem.utils.toFormattedCurrencyString
import org.joda.time.DateTime
import org.joda.time.DateTimeZone

internal class TokenDetailsTxHistoryToTransactionStateConverter(
    private val symbol: String,
    private val decimals: Int,
) : Converter<TxHistoryItem, TransactionState> {

    override fun convert(value: TxHistoryItem): TransactionState {
        return createTransactionStateItem(item = value)
    }

    // TODO: Finalize transaction types https://tangem.atlassian.net/browse/AND-4636
    private fun createTransactionStateItem(item: TxHistoryItem): TransactionState {
        return when (val type = item.type) {
            TxHistoryItem.TransactionType.Transfer -> mapTransfer(item)
            TxHistoryItem.TransactionType.Approve -> mapApprove(item)
            TxHistoryItem.TransactionType.Swap -> mapSwap(item)
            TxHistoryItem.TransactionType.Deposit -> TransactionState.Custom(
                txHash = item.txHash,
                address = item.direction.extractAddress(),
                amount = item.getAmount(),
                timestamp = item.timestampInMillis.toTimeFormat(),
                status = item.status.tiUiStatus(),
                direction = item.direction.toUiDirection(),
                title = TextReference.Str("Deposit"),
                subtitle = item.direction.extractAddress(),
            )
            TxHistoryItem.TransactionType.Submit -> TransactionState.Custom(
                txHash = item.txHash,
                address = item.direction.extractAddress(),
                amount = item.getAmount(),
                timestamp = item.timestampInMillis.toTimeFormat(),
                status = item.status.tiUiStatus(),
                direction = item.direction.toUiDirection(),
                title = TextReference.Str("Submit"),
                subtitle = item.direction.extractAddress(),
            )
            TxHistoryItem.TransactionType.Supply -> TransactionState.Custom(
                txHash = item.txHash,
                address = item.direction.extractAddress(),
                amount = item.getAmount(),
                timestamp = item.timestampInMillis.toTimeFormat(),
                status = item.status.tiUiStatus(),
                direction = item.direction.toUiDirection(),
                title = TextReference.Str("Supply"),
                subtitle = item.direction.extractAddress(),
            )
            TxHistoryItem.TransactionType.Unoswap -> TransactionState.Custom(
                txHash = item.txHash,
                address = item.direction.extractAddress(),
                amount = item.getAmount(),
                timestamp = item.timestampInMillis.toTimeFormat(),
                status = item.status.tiUiStatus(),
                direction = item.direction.toUiDirection(),
                title = TextReference.Str("Unoswap"),
                subtitle = item.direction.extractAddress(),
            )
            TxHistoryItem.TransactionType.Withdraw -> TransactionState.Custom(
                txHash = item.txHash,
                address = item.direction.extractAddress(),
                amount = item.getAmount(),
                timestamp = item.timestampInMillis.toTimeFormat(),
                status = item.status.tiUiStatus(),
                direction = item.direction.toUiDirection(),
                title = TextReference.Str("Withdraw"),
                subtitle = item.direction.extractAddress(),
            )
            is TxHistoryItem.TransactionType.Custom -> TransactionState.Custom(
                txHash = item.txHash,
                address = item.direction.extractAddress(),
                amount = item.getAmount(),
                timestamp = item.timestampInMillis.toTimeFormat(),
                status = item.status.tiUiStatus(),
                direction = item.direction.toUiDirection(),
                title = TextReference.Str(type.id),
                subtitle = item.direction.extractAddress(),
            )
        }
    }

    private fun mapTransfer(item: TxHistoryItem): TransactionState {
        return TransactionState.Transfer(
            txHash = item.txHash,
            address = item.direction.extractAddress(),
            amount = item.getAmount(),
            timestamp = item.timestampInMillis.toTimeFormat(),
            status = item.status.tiUiStatus(),
            direction = item.direction.toUiDirection(),
        )
    }

    private fun mapApprove(item: TxHistoryItem): TransactionState {
        return TransactionState.Approve(
            txHash = item.txHash,
            address = item.direction.extractAddress(),
            amount = item.getAmount(),
            timestamp = item.timestampInMillis.toTimeFormat(),
            status = item.status.tiUiStatus(),
            direction = item.direction.toUiDirection(),
        )
    }
    private fun mapSwap(item: TxHistoryItem): TransactionState {
        return TransactionState.Swap(
            txHash = item.txHash,
            address = item.direction.extractAddress(),
            amount = item.getAmount(),
            timestamp = item.timestampInMillis.toTimeFormat(),
            status = item.status.tiUiStatus(),
            direction = item.direction.toUiDirection(),
        )
    }

    private fun TxHistoryItem.TransactionDirection.extractAddress(): TextReference = when (val addr = address) {
        TxHistoryItem.Address.Multiple -> TextReference.Res(R.string.transaction_history_multiple_addresses)
        is TxHistoryItem.Address.Single -> TextReference.Str(addr.rawAddress.toBriefAddressFormat())
    }

    private fun TxHistoryItem.TransactionStatus.tiUiStatus() = when (this) {
        TxHistoryItem.TransactionStatus.Confirmed -> TransactionState.Content.Status.Confirmed
        TxHistoryItem.TransactionStatus.Failed -> TransactionState.Content.Status.Failed
        TxHistoryItem.TransactionStatus.Unconfirmed -> TransactionState.Content.Status.Unconfirmed
    }

    private fun Long.toTimeFormat(): String {
        return DateTimeFormatters.formatTime(time = DateTime(this, DateTimeZone.getDefault()))
    }

    private fun TxHistoryItem.TransactionDirection.toUiDirection() = when (this) {
        is TxHistoryItem.TransactionDirection.Incoming -> TransactionState.Content.Direction.INCOMING
        is TxHistoryItem.TransactionDirection.Outgoing -> TransactionState.Content.Direction.OUTGOING
    }

    private fun TxHistoryItem.getAmount(): String {
        val prefix = when (direction) {
            is TxHistoryItem.TransactionDirection.Incoming -> "+"
            is TxHistoryItem.TransactionDirection.Outgoing -> "-"
        }
        return prefix + amount.toFormattedCurrencyString(currency = symbol, decimals = decimals)
    }
}