package com.tangem.tap.proxy.redux

import com.tangem.tap.common.redux.AppState
import org.rekotlin.Action

object DaggerGraphReducer {
    fun reduce(action: Action, state: AppState): DaggerGraphState {
        if (action !is DaggerGraphAction) return state.daggerGraphState

        return internalReduce(action, state)
    }

    private fun internalReduce(action: DaggerGraphAction, state: AppState): DaggerGraphState {
        return when (action) {
            is DaggerGraphAction.SetActivityDependencies -> state.daggerGraphState.copy(
                scanCardUseCase = action.scanCardUseCase,
                walletConnectInteractor = action.walletConnectInteractor,
                cardSdkConfigRepository = action.cardSdkConfigRepository,
            )
        }
    }
}
