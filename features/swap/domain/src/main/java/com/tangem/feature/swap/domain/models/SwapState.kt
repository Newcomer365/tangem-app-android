package com.tangem.feature.swap.domain.models

import java.math.BigDecimal

sealed interface SwapState {

    data class QuotesLoadedState(
        val fromTokenAmount: SwapAmount,
        val toTokenAmount: SwapAmount,
        val fromTokenAddress: String,
        val toTokenAddress: String,
        val fromTokenWalletBalance: String,
        val fromTokenFiatBalance: String,
        val toTokenWalletBalance: String,
        val toTokenFiatBalance: String,
        val fee: String,
        val isAllowedToSpend: Boolean = false,
        val permissionState: PermissionDataState = PermissionDataState.Empty,
    ) : SwapState

    data class SwapSuccess(
        val fromTokenAmount: SwapAmount,
        val toTokenAmount: SwapAmount,
    ) : SwapState

    data class SwapError(
        val errorType: DataError,
    ) : SwapState
}

sealed class PermissionDataState {

    data class PermissionReadyForRequest(
        val currency: String,
        val amount: String,
        val walletAddress: String,
        val spenderAddress: String,
        val fee: String,
    ) : PermissionDataState()

    object PermissionFailed : PermissionDataState()

    object PermissionLoading : PermissionDataState()

    object Empty : PermissionDataState()
}
