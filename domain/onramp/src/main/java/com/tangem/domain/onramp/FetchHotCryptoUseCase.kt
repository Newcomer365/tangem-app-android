package com.tangem.domain.onramp

import com.tangem.domain.onramp.repositories.HotCryptoRepository

/**
 * Fetch hot crypto use case
 *
 * @property hotCryptoRepository hot crypto repository
 *
 * @author Andrew Khokhlov on 11/02/2025
 */
class FetchHotCryptoUseCase(
    private val hotCryptoRepository: HotCryptoRepository,
) {

    operator fun invoke() {
        hotCryptoRepository.fetchHotCrypto()
    }
}
