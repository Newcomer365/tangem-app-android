package com.tangem.core.ui.components.inputrow

import android.content.res.Configuration
import androidx.annotation.DrawableRes
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.tooling.preview.Preview
import com.tangem.core.ui.R
import com.tangem.core.ui.components.SpacerW12
import com.tangem.core.ui.components.SpacerWMax
import com.tangem.core.ui.components.inputrow.inner.InputRowAsyncImage
import com.tangem.core.ui.extensions.TextReference
import com.tangem.core.ui.extensions.resolveAnnotatedReference
import com.tangem.core.ui.extensions.resolveReference
import com.tangem.core.ui.res.TangemTheme
import com.tangem.core.ui.res.TangemThemePreview

/**
 * Input row component with selector
 * [Input Row Image](https://www.figma.com/design/14ISV23YB1yVW1uNVwqrKv/Android?node-id=2841-1589&t=u6pOF6lsdpvWLELb-4)
 *
 * @param subtitle subtitle text
 * @param modifier modifier
 * @param caption caption text
 * @param imageUrl icon to load
 * @param iconRes icon resource
 * @param subtitleColor subtitle text color
 * @param captionColor caption text color
 * @param iconTint icon tint
 * @param isGrayscaleImage whether to display grayscale image
 * @param iconEndRes icon to end of row
 * @param onImageError composable to show if image loading failed
 * @param subtitleExtraContent subtitle extra content
 * @param extraContent extra content
 */
@Suppress("LongMethod")
@Composable
internal fun InputRowImageBase(
    subtitle: TextReference,
    modifier: Modifier = Modifier,
    caption: TextReference? = null,
    imageUrl: String? = null,
    @DrawableRes iconRes: Int? = null,
    subtitleColor: Color = TangemTheme.colors.text.primary1,
    captionColor: Color = TangemTheme.colors.text.tertiary,
    iconTint: Color = TangemTheme.colors.icon.informative,
    isGrayscaleImage: Boolean = false,
    @DrawableRes iconEndRes: Int? = null,
    onImageError: (@Composable () -> Unit)? = null,
    subtitleExtraContent: (@Composable RowScope.() -> Unit)? = null,
    extraContent: (@Composable RowScope.() -> Unit)? = null,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier,
    ) {
        if (imageUrl != null) {
            InputRowAsyncImage(
                imageUrl = imageUrl,
                isGrayscale = isGrayscaleImage,
                onImageError = onImageError,
                modifier = Modifier
                    .size(TangemTheme.dimens.spacing36)
                    .clip(TangemTheme.shapes.roundedCornersXLarge),
            )
            SpacerW12()
        } else if (iconRes != null) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(TangemTheme.dimens.spacing36)
                    .clip(TangemTheme.shapes.roundedCornersXLarge)
                    .background(iconTint.copy(alpha = 0.08f)),
            ) {
                Icon(
                    painter = rememberVectorPainter(image = ImageVector.vectorResource(id = iconRes)),
                    tint = iconTint,
                    contentDescription = null,
                    modifier = Modifier.size(TangemTheme.dimens.size18),
                )
            }
            SpacerW12()
        }
        Column {
            Row {
                Text(
                    text = subtitle.resolveReference(),
                    style = TangemTheme.typography.subtitle2,
                    color = subtitleColor,
                )
                subtitleExtraContent?.invoke(this)
            }
            if (caption != null) {
                Text(
                    text = caption.resolveAnnotatedReference(),
                    style = TangemTheme.typography.caption2,
                    color = captionColor,
                    modifier = Modifier.padding(top = TangemTheme.dimens.spacing2),
                )
            }
        }
        SpacerW12()
        if (extraContent != null) {
            extraContent()
        } else {
            SpacerWMax()
        }
        InputRowEndIcon(iconEndRes)
    }
}

@Composable
private fun RowScope.InputRowEndIcon(iconRes: Int?) {
    AnimatedVisibility(
        visible = iconRes != null,
        label = "End icon visibility animation",
    ) {
        val icon = remember(this) { requireNotNull(iconRes) }
        Icon(
            painter = rememberVectorPainter(image = ImageVector.vectorResource(id = icon)),
            tint = TangemTheme.colors.icon.informative,
            contentDescription = null,
            modifier = Modifier.padding(start = TangemTheme.dimens.spacing6),
        )
    }
}

// region Preview
@Preview(showBackground = true, widthDp = 360)
@Preview(showBackground = true, widthDp = 360, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun InputRowImageBase_Preview() {
    TangemThemePreview {
        InputRowImageBase(
            subtitle = TextReference.Str("Binance"),
            caption = TextReference.Str("APR 3,54%"),
            imageUrl = "",
            iconEndRes = R.drawable.ic_chevron_right_24,
        )
    }
}
// endregion
