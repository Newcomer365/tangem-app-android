package com.tangem.domain.staking.model.transaction

import com.tangem.domain.staking.model.Token
import com.tangem.domain.staking.model.action.StakingActionCommonType
import java.math.BigDecimal

data class ActionParams(
    val actionCommonType: StakingActionCommonType,
    val integrationId: String,
    val amount: BigDecimal,
    val address: String,
    val validatorAddress: String,
    val token: Token,
)