package com.tangem.feature.swap.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.padding
import androidx.compose.material.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.tangem.core.ui.components.appbar.AppBarWithBackButton
import com.tangem.core.ui.res.TangemTheme
import com.tangem.feature.swap.models.SwapStateHolder
import com.tangem.feature.swap.models.states.ChooseProviderBottomSheetConfig
import com.tangem.feature.swap.models.states.GivePermissionBottomSheetConfig
import com.tangem.feature.swap.presentation.R

@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun SwapScreen(stateHolder: SwapStateHolder) {
    BackHandler(onBack = stateHolder.onBackClicked)

    Scaffold(
        containerColor = TangemTheme.colors.background.secondary,
    ) { scaffoldPaddings ->

        SwapScreenContent(
            state = stateHolder,
            modifier = Modifier.padding(scaffoldPaddings),
        )

        stateHolder.bottomSheetConfig?.let { config ->
            when (config.content) {
                is GivePermissionBottomSheetConfig -> {
                    SwapPermissionBottomSheet(config = config)
                }
                is ChooseProviderBottomSheetConfig -> {
                    ChooseProviderBottomSheet(config = config)
                }
            }
        }
    }
}
