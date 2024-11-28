package com.tangem.features.onboarding.v2.multiwallet.impl.child.seedphrase

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.view.WindowManager
import androidx.activity.compose.BackHandler
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.tangem.core.decompose.context.AppComponentContext
import com.tangem.core.decompose.model.getOrCreateModel
import com.tangem.features.onboarding.v2.multiwallet.impl.child.MultiWalletChildComponent
import com.tangem.features.onboarding.v2.multiwallet.impl.child.MultiWalletChildParams
import com.tangem.features.onboarding.v2.multiwallet.impl.child.seedphrase.model.MultiWalletSeedPhraseModel
import com.tangem.features.onboarding.v2.multiwallet.impl.child.seedphrase.ui.MultiWalletSeedPhrase
import com.tangem.features.onboarding.v2.multiwallet.impl.model.OnboardingMultiWalletState
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.launch

@Suppress("UnusedPrivateMember")
internal class MultiWalletSeedPhraseComponent(
    context: AppComponentContext,
    params: MultiWalletChildParams,
    onNextStep: (OnboardingMultiWalletState.Step) -> Unit,
    backButtonClickFlow: SharedFlow<Unit>,
) : AppComponentContext by context, MultiWalletChildComponent {

    private val model: MultiWalletSeedPhraseModel = getOrCreateModel(params)

    init {
        componentScope.launch {
            backButtonClickFlow.collect { model.onBack() }
        }
        componentScope.launch {
            model.onDone.collect { onNextStep(OnboardingMultiWalletState.Step.AddBackupDevice) }
        }
        componentScope.launch {
            model.navigateBack.collect { onNextStep(OnboardingMultiWalletState.Step.CreateWallet) }
        }
    }

    @Composable
    override fun Content(modifier: Modifier) {
        val state by model.uiState.collectAsStateWithLifecycle()

        BackHandler(enabled = true) {
            model.onBack()
        }

        // disable screenshots
        val activity = LocalContext.current.findActivity()
        DisposableEffect(activity) {
            activity.window.setFlags(
                WindowManager.LayoutParams.FLAG_SECURE,
                WindowManager.LayoutParams.FLAG_SECURE,
            )
            onDispose {
                activity.window.clearFlags(WindowManager.LayoutParams.FLAG_SECURE)
            }
        }

        MultiWalletSeedPhrase(
            modifier = modifier,
            state = state,
        )
    }

    private fun Context.findActivity(): Activity {
        var context = this
        while (context is ContextWrapper) {
            if (context is Activity) return context
            context = context.baseContext
        }
        error("")
    }
}