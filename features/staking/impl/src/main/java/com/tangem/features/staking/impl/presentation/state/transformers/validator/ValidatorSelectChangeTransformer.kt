package com.tangem.features.staking.impl.presentation.state.transformers.validator

import com.tangem.domain.staking.model.Yield
import com.tangem.features.staking.impl.presentation.state.StakingStates
import com.tangem.features.staking.impl.presentation.state.StakingUiState
import com.tangem.features.staking.impl.presentation.state.ValidatorState
import com.tangem.utils.transformer.Transformer

internal class ValidatorSelectChangeTransformer(
    private val selectedValidator: Yield.Validator,
) : Transformer<StakingUiState> {
    override fun transform(prevState: StakingUiState): StakingUiState {
        val confirmationState = prevState.confirmationState as? StakingStates.ConfirmationState.Data ?: return prevState
        val validatorState = confirmationState.validatorState as? ValidatorState.Content ?: return prevState

        return prevState.copy(
            confirmationState = confirmationState.copy(
                validatorState = validatorState.copy(
                    chosenValidator = selectedValidator,
                ),
            ),
        )
    }
}