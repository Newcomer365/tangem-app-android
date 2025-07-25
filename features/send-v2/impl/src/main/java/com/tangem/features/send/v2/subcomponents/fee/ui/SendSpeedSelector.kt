package com.tangem.features.send.v2.subcomponents.fee.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.ClickableText
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import com.tangem.core.ui.extensions.stringResourceSafe
import com.tangem.core.ui.res.TangemTheme
import com.tangem.features.send.v2.impl.R
import com.tangem.features.send.v2.subcomponents.fee.ui.state.FeeType
import com.tangem.features.send.v2.subcomponents.fee.ui.state.FeeUM
import com.tangem.features.send.v2.subcomponents.fee.model.SendFeeClickIntents

@Suppress("LongMethod")
@Composable
internal fun SendSpeedSelector(
    state: FeeUM.Content,
    clickIntents: SendFeeClickIntents,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(TangemTheme.shapes.roundedCornersXMedium)
                .background(TangemTheme.colors.background.action),
        ) {
            SendSpeedSelectorItem(
                titleRes = R.string.common_fee_selector_option_slow,
                iconRes = R.drawable.ic_tortoise_24,
                feeType = FeeType.Slow,
                state = state,
                onSelect = { clickIntents.onFeeSelectorClick(FeeType.Slow) },
            )
            SendSpeedSelectorItem(
                titleRes = R.string.common_fee_selector_option_market,
                iconRes = R.drawable.ic_bird_24,
                feeType = FeeType.Market,
                state = state,
                onSelect = { clickIntents.onFeeSelectorClick(FeeType.Market) },
            )
            SendSpeedSelectorItem(
                titleRes = R.string.common_fee_selector_option_fast,
                iconRes = R.drawable.ic_hare_24,
                feeType = FeeType.Fast,
                state = state,
                onSelect = { clickIntents.onFeeSelectorClick(FeeType.Fast) },
            )
            SendSpeedSelectorItem(
                titleRes = R.string.common_custom,
                iconRes = R.drawable.ic_edit_24,
                feeType = FeeType.Custom,
                state = state,
                onSelect = { clickIntents.onFeeSelectorClick(FeeType.Custom) },
            )
        }
        FooterText(clickIntents::onReadMoreClick)
    }
}

@Composable
private fun FooterText(onReadMoreClick: () -> Unit) {
    val linkText = stringResourceSafe(R.string.common_read_more)
    val fullString = stringResourceSafe(R.string.common_fee_selector_footer, linkText)
    val linkTextPosition = fullString.length - linkText.length
    val defaultStyle = TangemTheme.colors.text.tertiary
    val linkStyle = TangemTheme.colors.text.accent
    val annotatedString = remember(defaultStyle, linkStyle) {
        buildAnnotatedString {
            withStyle(SpanStyle(defaultStyle)) {
                append(fullString.substring(0, linkTextPosition))
            }
            withStyle(SpanStyle(linkStyle)) {
                append(fullString.substring(linkTextPosition, fullString.length))
            }
        }
    }

    val click = { i: Int ->
        val readMoreStyle = requireNotNull(annotatedString.spanStyles.getOrNull(1))
        if (i in readMoreStyle.start..readMoreStyle.end) {
            onReadMoreClick()
        }
    }

    ClickableText(
        text = annotatedString,
        style = TangemTheme.typography.caption2.copy(textAlign = TextAlign.Start),
        modifier = Modifier.padding(top = 8.dp),
        onClick = click,
    )
}
