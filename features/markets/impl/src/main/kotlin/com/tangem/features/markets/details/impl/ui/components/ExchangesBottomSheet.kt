package com.tangem.features.markets.details.impl.ui.components

import android.content.res.Configuration
import androidx.annotation.StringRes
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.datasource.CollectionPreviewParameterProvider
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.fastForEach
import com.tangem.core.ui.components.SpacerH
import com.tangem.core.ui.components.SpacerH12
import com.tangem.core.ui.components.appbar.TangemTopAppBar
import com.tangem.core.ui.components.appbar.models.TopAppBarButtonUM
import com.tangem.core.ui.components.audits.AuditLabelUM
import com.tangem.core.ui.components.bottomsheets.TangemBottomSheet
import com.tangem.core.ui.components.bottomsheets.TangemBottomSheetConfig
import com.tangem.core.ui.components.buttons.SecondarySmallButton
import com.tangem.core.ui.components.buttons.SmallButtonConfig
import com.tangem.core.ui.components.currency.icon.CurrencyIconState
import com.tangem.core.ui.components.token.TokenItem
import com.tangem.core.ui.components.token.state.TokenItemState
import com.tangem.core.ui.extensions.TextReference
import com.tangem.core.ui.extensions.resolveReference
import com.tangem.core.ui.extensions.resourceReference
import com.tangem.core.ui.extensions.stringReference
import com.tangem.core.ui.res.TangemTheme
import com.tangem.core.ui.res.TangemThemePreview
import com.tangem.features.markets.details.impl.ui.state.ExchangesBottomSheetContent
import com.tangem.features.markets.impl.R
import kotlinx.collections.immutable.toImmutableList

/**
 * Exchanges bottom sheet
 *
 * @param config bottom sheet config
 *
 * @author Andrew Khokhlov on 02/10/2024
 */
@Composable
internal fun ExchangesBottomSheet(config: TangemBottomSheetConfig) {
    val bottomBarHeight = with(LocalDensity.current) { WindowInsets.systemBars.getBottom(this).toDp() }

    TangemBottomSheet<ExchangesBottomSheetContent>(
        config = config,
        addBottomInsets = false,
        title = { Title(textResId = it.titleResId, onBackClick = config.onDismissRequest) },
        content = { content ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(state = rememberScrollState()),
            ) {
                Subtitle(
                    subtitleRes = content.subtitleResId,
                    volumeReference = content.volumeReference,
                    modifier = Modifier.padding(
                        start = TangemTheme.dimens.spacing16,
                        top = TangemTheme.dimens.spacing12,
                        end = TangemTheme.dimens.spacing16,
                        bottom = TangemTheme.dimens.spacing8,
                    ),
                )

                when (content) {
                    is ExchangesBottomSheetContent.Content,
                    is ExchangesBottomSheetContent.Loading,
                    -> {
                        content.exchangeItems.fastForEach { item ->
                            key(item.id) {
                                TokenItem(state = item, isBalanceHidden = false)
                            }
                        }
                    }
                    is ExchangesBottomSheetContent.Error -> {
                        Error(
                            content = content,
                            modifier = Modifier
                                .align(alignment = Alignment.CenterHorizontally)
                                .padding(horizontal = 16.dp)
                                .weight(1f),
                        )
                    }
                }

                SpacerH(bottomBarHeight)
            }
        },
    )
}

@Composable
private fun Title(@StringRes textResId: Int, onBackClick: () -> Unit) {
    TangemTopAppBar(
        title = stringResource(id = textResId),
        startButton = TopAppBarButtonUM.Back(onBackClicked = onBackClick),
    )
}

@Composable
private fun Subtitle(@StringRes subtitleRes: Int, volumeReference: TextReference, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        SubtitleText(textReference = resourceReference(id = subtitleRes))

        SubtitleText(textReference = volumeReference)
    }
}

@Composable
private fun SubtitleText(textReference: TextReference) {
    Text(
        text = textReference.resolveReference(),
        color = TangemTheme.colors.text.tertiary,
        overflow = TextOverflow.Ellipsis,
        maxLines = 1,
        style = TangemTheme.typography.body2,
    )
}

@Composable
private fun Error(content: ExchangesBottomSheetContent.Error, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = stringResource(id = content.message),
            color = TangemTheme.colors.text.tertiary,
            textAlign = TextAlign.Center,
            style = TangemTheme.typography.caption1,
        )

        SpacerH12()

        SecondarySmallButton(
            config = SmallButtonConfig(
                text = resourceReference(id = R.string.alert_button_try_again),
                onClick = content.onRetryClick,
            ),
        )
    }
}

@Preview
@Preview(name = "Dark Theme", uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun Preview_ExchangesBottomSheet(
    @PreviewParameter(ExchangesBottomSheetContentProvider::class) content: ExchangesBottomSheetContent,
) {
    TangemThemePreview {
        ExchangesBottomSheet(
            config = TangemBottomSheetConfig(
                onDismissRequest = {},
                content = content,
                isShow = true,
            ),
        )
    }
}

private class ExchangesBottomSheetContentProvider : CollectionPreviewParameterProvider<ExchangesBottomSheetContent>(
    listOf(
        ExchangesBottomSheetContent.Loading(exchangesCount = 13),
        ExchangesBottomSheetContent.Error(onRetryClick = {}),
        ExchangesBottomSheetContent.Content(
            exchangeItems = List(size = 13) { index ->
                TokenItemState.Content(
                    id = index.toString(),
                    iconState = CurrencyIconState.CoinIcon(
                        url = null,
                        fallbackResId = R.drawable.ic_facebook_24,
                        isGrayscale = false,
                        showCustomBadge = false,
                    ),
                    titleState = TokenItemState.TitleState.Content(text = "OKX"),
                    fiatAmountState = TokenItemState.FiatAmountState.Content(text = "$67.52M"),
                    subtitleState = TokenItemState.SubtitleState.TextContent(value = "CEX"),
                    subtitle2State = TokenItemState.Subtitle2State.LabelContent(
                        auditLabelUM = AuditLabelUM(
                            text = stringReference("Caution"),
                            type = AuditLabelUM.Type.Warning,
                        ),
                    ),
                    onItemClick = {},
                    onItemLongClick = {},
                )
            }
                .toImmutableList(),
        ),
    ),
)