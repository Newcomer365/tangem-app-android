package com.tangem.domain.wallets.usecase

import com.tangem.common.extensions.toHexString
import com.tangem.common.services.Result
import com.tangem.domain.common.TwinCardNumber
import com.tangem.domain.common.TwinsHelper
import com.tangem.domain.wallets.models.Artwork
import com.tangem.operations.attestation.OnlineCardVerifier
import com.tangem.operations.attestation.TangemApi

/**
 * Use case for getting card image url
 *
 * @property verifier REST API
 *
 * @author Andrew Khokhlov on 12/04/2023
 */
class GetCardImageUseCase(private val verifier: OnlineCardVerifier = OnlineCardVerifier()) {

    /**
     * Get card image url
     *
     * @param cardId        card id
     * @param cardPublicKey card public key
     */
    suspend operator fun invoke(cardId: String, cardPublicKey: ByteArray): String {
        return when (val result = verifier.getCardInfo(cardId, cardPublicKey)) {
            is Result.Success -> {
                val artworkId = result.data.artwork?.id
                if (artworkId.isNullOrEmpty()) {
                    getFallbackArtworkUrl(cardId)
                } else {
                    getUrlForArtwork(cardId, cardPublicKey.toHexString(), artworkId)
                }
            }

            is Result.Failure -> getFallbackArtworkUrl(cardId)
        }
    }

    fun getDefaultFallbackUrl(): String = Artwork.DEFAULT_IMG_URL

    private fun getFallbackArtworkUrl(cardId: String): String {
        return when {
            cardId.startsWith(Artwork.SERGIO_CARD_ID) -> Artwork.SERGIO_CARD_URL
            cardId.startsWith(Artwork.MARTA_CARD_ID) -> Artwork.MARTA_CARD_URL
            else -> when (TwinsHelper.getTwinCardNumber(cardId)) {
                TwinCardNumber.First -> Artwork.TWIN_CARD_1_URL
                TwinCardNumber.Second -> Artwork.TWIN_CARD_2_URL
                else -> getDefaultFallbackUrl()
            }
        }
    }

    private fun getUrlForArtwork(cardId: String, cardPublicKeyHex: String, artworkId: String): String {
        return TangemApi.Companion.BaseUrl.VERIFY.url + TangemApi.ARTWORK +
            "?artworkId=$artworkId&CID=$cardId&publicKey=$cardPublicKeyHex"
    }
}
