package com.tangem.tap.features.tokens.presentation.models

import com.tangem.blockchain.common.Blockchain
import com.tangem.tap.features.tokens.redux.TokenWithBlockchain
import com.tangem.tap.store

/**
 * Required data for tokens list screen
 * FIXME("Necessary to avoid using redux state")
 *
 * @author Andrew Khokhlov on 11/04/2023
 */
class TokensListArgs {
    /** Tokens list screen mode */
    val isManageAccess: Boolean get() = store.state.tokensState.allowToAdd

    /** Tokens list that accessible from the main screen */
    val mainScreenTokenList: List<TokenWithBlockchain> get() = store.state.tokensState.addedTokens

    /** Blockchains list that accessible from the main screen */
    val mainScreenBlockchainList: List<Blockchain> get() = store.state.tokensState.addedBlockchains
}
