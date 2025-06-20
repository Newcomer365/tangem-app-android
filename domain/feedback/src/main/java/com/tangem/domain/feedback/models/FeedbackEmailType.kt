package com.tangem.domain.feedback.models

import com.tangem.domain.visa.model.VisaTxDetails

/**
 * Email feedback type
 *
 * @author Andrew Khokhlov on 17/05/2024
 */
sealed interface FeedbackEmailType {

    val cardInfo: CardInfo?

    /** User initiate request yourself. Example, button on DetailsScreen or OnboardingScreen */
    data class DirectUserRequest(override val cardInfo: CardInfo) : FeedbackEmailType

    /** User rate the app as "can be better" */
    data class RateCanBeBetter(override val cardInfo: CardInfo) : FeedbackEmailType

    /** User has problem with scanning */
    data object ScanningProblem : FeedbackEmailType {
        override val cardInfo: CardInfo? = null
    }

    /** User has problem with sending transaction */
    data class TransactionSendingProblem(override val cardInfo: CardInfo) : FeedbackEmailType

    /** User has problem with staking */
    data class StakingProblem(
        override val cardInfo: CardInfo,
        val validatorName: String?,
        val transactionTypes: List<String>,
        val unsignedTransactions: List<String?>,
    ) : FeedbackEmailType

    data class SwapProblem(
        override val cardInfo: CardInfo,
        val providerName: String,
        val txId: String,
    ) : FeedbackEmailType

    /**
     * Error in currency description
     *
     * @property currencyId   currency id
     * @property currencyName currency name
     */
    data class CurrencyDescriptionError(val currencyId: String, val currencyName: String) : FeedbackEmailType {
        override val cardInfo: CardInfo? = null
    }

    data class PreActivatedWallet(override val cardInfo: CardInfo) : FeedbackEmailType

    data object CardAttestationFailed : FeedbackEmailType {
        override val cardInfo: CardInfo? = null
    }

    sealed class Visa : FeedbackEmailType {
        data class DirectUserRequest(override val cardInfo: CardInfo) : Visa()

        data class Activation(override val cardInfo: CardInfo) : Visa()

        data class Dispute(
            val visaTxDetails: VisaTxDetails,
            override val cardInfo: CardInfo,
        ) : Visa()
    }
}
