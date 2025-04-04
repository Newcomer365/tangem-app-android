package com.tangem.datasource.local.visa

import com.tangem.domain.visa.model.VisaAuthTokens

interface VisaAuthTokenStorage {

    suspend fun store(cardId: String, tokens: VisaAuthTokens)

    suspend fun get(cardId: String): VisaAuthTokens?

    fun remove(cardId: String)
}
