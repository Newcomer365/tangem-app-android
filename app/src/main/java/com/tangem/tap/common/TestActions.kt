package com.tangem.tap.common

/**
 * Created by Anton Zhilenkov on 19/04/2022.
 */

object TestActions {

    // It used only for the test actions in debug or debug_beta builds
    var testAmountInjectionForWalletManagerEnabled = false
}

typealias TestAction = Pair<String, () -> Unit>
