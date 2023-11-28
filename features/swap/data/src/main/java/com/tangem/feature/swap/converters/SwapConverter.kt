package com.tangem.feature.swap.converters

import com.tangem.datasource.api.oneinch.models.SwapResponse
import com.tangem.datasource.api.oneinch.models.TransactionDto
import com.tangem.feature.swap.domain.models.createFromAmountWithOffset
import com.tangem.feature.swap.domain.models.domain.SwapDataModel
import com.tangem.feature.swap.domain.models.domain.TransactionModel
import com.tangem.utils.converter.Converter

class SwapConverter : Converter<SwapResponse, SwapDataModel> {

    override fun convert(value: SwapResponse): SwapDataModel {
        return SwapDataModel(
            toTokenAmount = createFromAmountWithOffset(value.toTokenAmount, value.toToken.decimals),
            transaction = convertTransaction(value.transaction),
        )
    }

    private fun convertTransaction(transactionDto: TransactionDto): TransactionModel {
        return TransactionModel(
            fromWalletAddress = transactionDto.fromAddress,
            toWalletAddress = transactionDto.toAddress,
            data = transactionDto.data,
            value = transactionDto.value,
            gasPrice = transactionDto.gasPrice,
            gas = transactionDto.gas,
        )
    }
}