package com.tangem.datasource.exchangeservice.hotcrypto

import com.tangem.datasource.api.tangemTech.models.HotCryptoResponse
import com.tangem.datasource.local.datastore.RuntimeStateStore
import com.tangem.domain.wallets.models.UserWalletId
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Store of pair of [UserWalletId] and [HotCryptoResponse]
 *
 * @author Andrew Khokhlov on 21/01/2025
 */
@Singleton
class HotCryptoResponseStore @Inject constructor() :
    RuntimeStateStore<Map<UserWalletId, HotCryptoResponse>> by RuntimeStateStore(defaultValue = emptyMap())
