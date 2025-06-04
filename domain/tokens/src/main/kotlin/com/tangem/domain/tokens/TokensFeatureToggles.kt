package com.tangem.domain.tokens

/**
 * Tokens feature toggles
 *
 * @author Andrew Khokhlov on 14/02/2025
 */
interface TokensFeatureToggles {

    val isNetworksLoadingRefactoringEnabled: Boolean

    val isQuotesLoadingRefactoringEnabled: Boolean

    val isStakingLoadingRefactoringEnabled: Boolean
}
