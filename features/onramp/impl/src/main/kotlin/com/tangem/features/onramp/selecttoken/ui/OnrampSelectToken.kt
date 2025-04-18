package com.tangem.features.onramp.selecttoken.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.unit.dp
import com.tangem.core.ui.components.appbar.AppBarWithBackButton
import com.tangem.core.ui.extensions.stringResourceSafe
import com.tangem.core.ui.res.TangemTheme
import com.tangem.core.ui.utils.rememberHideKeyboardNestedScrollConnection
import com.tangem.features.onramp.hottokens.HotCryptoComponent
import com.tangem.features.onramp.impl.R
import com.tangem.features.onramp.selecttoken.entity.OnrampOperationUM
import com.tangem.features.onramp.tokenlist.OnrampTokenListComponent

@OptIn(ExperimentalFoundationApi::class)
@Composable
internal fun OnrampSelectToken(
    state: OnrampOperationUM,
    onrampTokenListComponent: OnrampTokenListComponent,
    hotCryptoComponent: HotCryptoComponent?,
    modifier: Modifier = Modifier,
) {
    BackHandler(onBack = state.onBackClick)

    val nestedScrollConnection = rememberHideKeyboardNestedScrollConnection()
    LazyColumn(
        modifier = modifier
            .nestedScroll(nestedScrollConnection)
            .background(TangemTheme.colors.background.secondary)
            .imePadding()
            .systemBarsPadding(),
    ) {
        stickyHeader(key = "header") {
            AppBarWithBackButton(
                onBackClick = state.onBackClick,
                text = stringResourceSafe(id = state.titleResId),
                iconRes = R.drawable.ic_close_24,
                containerColor = TangemTheme.colors.background.secondary,
            )
        }

        item(key = "token_list", contentType = "token_list") {
            onrampTokenListComponent.Content(
                modifier = Modifier
                    .padding(top = 8.dp)
                    .padding(horizontal = 16.dp)
                    .then(
                        if (hotCryptoComponent != null) {
                            Modifier
                        } else {
                            Modifier.padding(bottom = 8.dp)
                        },
                    ),
            )
        }

        if (hotCryptoComponent != null && state.isHotCryptoVisible) {
            item(key = "hot_crypto", contentType = "hot_crypto") {
                hotCryptoComponent.Content(
                    modifier = Modifier
                        .padding(top = 12.dp, bottom = 8.dp)
                        .padding(horizontal = 16.dp),
                )
            }
        }
    }
}
