package com.tangem.features.nft.common.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.arkivanov.decompose.extensions.compose.stack.Children
import com.arkivanov.decompose.extensions.compose.stack.animation.slide
import com.arkivanov.decompose.extensions.compose.stack.animation.stackAnimation
import com.arkivanov.decompose.router.stack.ChildStack
import com.tangem.core.ui.decompose.ComposableContentComponent
import com.tangem.core.ui.res.TangemTheme
import com.tangem.features.nft.common.NFTRoute

@Composable
internal fun NFTContent(stackState: ChildStack<NFTRoute, ComposableContentComponent>) {
    Column(
        modifier = Modifier.Companion
            .background(color = TangemTheme.colors.background.tertiary)
            .fillMaxSize()
            .imePadding()
            .systemBarsPadding(),
        horizontalAlignment = Alignment.Companion.CenterHorizontally,
    ) {
        Children(
            stack = stackState,
            animation = stackAnimation(slide()),
            modifier = Modifier.weight(1f),
        ) {
            it.instance.Content(Modifier.weight(1f))
        }
    }
}
