package com.tangem.features.onboarding.v2.note.impl.model

import com.tangem.core.ui.components.artwork.ArtworkUM
import com.tangem.domain.models.scan.ScanResponse
import com.tangem.domain.tokens.model.CryptoCurrencyStatus
import com.tangem.domain.wallets.models.UserWallet

internal data class OnboardingNoteCommonState(
    val scanResponse: ScanResponse,
    val userWallet: UserWallet? = null,
    val cardArtwork: ArtworkUM? = null,
    val cryptoCurrencyStatus: CryptoCurrencyStatus? = null,
)
