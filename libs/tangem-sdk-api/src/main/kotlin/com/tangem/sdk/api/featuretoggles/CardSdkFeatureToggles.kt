package com.tangem.sdk.api.featuretoggles

/**
 * Card SDK feature toggles
 *
 * @author Andrew Khokhlov on 13/03/2025
 */
interface CardSdkFeatureToggles {

    val isNewAttestationEnabled: Boolean

    val isNewArtworkLoadingEnabled: Boolean
}
