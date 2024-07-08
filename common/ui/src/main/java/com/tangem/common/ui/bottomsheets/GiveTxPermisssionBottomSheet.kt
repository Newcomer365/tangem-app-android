package com.tangem.common.ui.bottomsheets

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.window.PopupProperties
import com.tangem.common.ui.R
import com.tangem.common.ui.bottomsheets.state.*
import com.tangem.core.ui.components.*
import com.tangem.core.ui.components.appbar.AppBarWithAdditionalButtons
import com.tangem.core.ui.components.appbar.models.AdditionalButton
import com.tangem.core.ui.components.atoms.text.EllipsisText
import com.tangem.core.ui.components.bottomsheets.TangemBottomSheet
import com.tangem.core.ui.components.bottomsheets.TangemBottomSheetConfig
import com.tangem.core.ui.components.containers.FooterContainer
import com.tangem.core.ui.extensions.TextReference
import com.tangem.core.ui.extensions.resolveReference
import com.tangem.core.ui.extensions.resourceReference
import com.tangem.core.ui.res.TangemTheme
import com.tangem.core.ui.res.TangemThemePreview
import kotlinx.collections.immutable.ImmutableList

@Composable
fun GiveTxPermissionBottomSheet(config: TangemBottomSheetConfig) {
    TangemBottomSheet(
        config = config,
        containerColor = TangemTheme.colors.background.secondary,
    ) { content: GiveTxPermissionBottomSheetConfig ->
        GiveTxPermissionBottomSheetContent(content = content)
    }
}

@Composable
private fun GiveTxPermissionBottomSheetContent(content: GiveTxPermissionBottomSheetConfig) {
    var isPermissionAlertShow by remember { mutableStateOf(false) }
    val data = content.data
    Column(
        modifier = Modifier
            .background(color = TangemTheme.colors.background.secondary)
            .fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        AppBarWithAdditionalButtons(
            text = resourceReference(id = R.string.give_permission_title),
            iconColor = TangemTheme.colors.text.tertiary,
            endButton = AdditionalButton(
                iconRes = R.drawable.ic_information_24,
                onIconClicked = { isPermissionAlertShow = true },
            ),
        )
        Text(
            text = stringResource(
                id = R.string.give_permission_subtitle,
                data.currency,
            ),
            color = TangemTheme.colors.text.secondary,
            style = TangemTheme.typography.body2,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = TangemTheme.dimens.spacing24),
        )

        SpacerH16()

        ApprovalBottomSheetInfo(data)

        SpacerH(height = TangemTheme.dimens.spacing20)

        PrimaryButtonIconEnd(
            text = stringResource(id = R.string.common_approve),
            iconResId = R.drawable.ic_tangem_24,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = TangemTheme.dimens.spacing16),
            onClick = data.approveButton.onClick,
        )

        SpacerH12()

        SecondaryButton(
            text = stringResource(id = R.string.common_cancel),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = TangemTheme.dimens.spacing16),
            onClick = content.onCancel,
        )

        SpacerH16()

        // region dialog
        if (isPermissionAlertShow) {
            BasicDialog(
                message = stringResource(id = R.string.swapping_approve_information_text),
                title = stringResource(id = R.string.swapping_approve_information_title),
                confirmButton = DialogButton { isPermissionAlertShow = false },
                onDismissDialog = {},
            )
        }
    }
}

@Composable
private fun ApprovalBottomSheetInfo(data: GiveTxPermissionState.ReadyForRequest) {
    FooterContainer(
        footer = stringResource(id = R.string.give_permission_policy_type_footer),
        modifier = Modifier.padding(horizontal = TangemTheme.dimens.spacing16),
    ) {
        AmountItem(
            currency = data.currency,
            approveType = data.approveType,
            onChangeApproveType = data.onChangeApproveType,
            approveItems = data.approveItems,
        )
    }
    SpacerH16()
    FooterContainer(
        footer = stringResource(id = R.string.give_permission_fee_footer),
        modifier = Modifier.padding(horizontal = TangemTheme.dimens.spacing16),
    ) {
        FeeItem(fee = data.fee)
    }
}

@Composable
private fun FeeItem(fee: TextReference) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(TangemTheme.shapes.roundedCornersXMedium)
            .background(TangemTheme.colors.background.action)
            .padding(
                top = TangemTheme.dimens.spacing16,
                bottom = TangemTheme.dimens.spacing16,
                start = TangemTheme.dimens.spacing12,
                end = TangemTheme.dimens.spacing16,
            ),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = stringResource(R.string.common_network_fee_title),
            color = TangemTheme.colors.text.primary1,
            style = TangemTheme.typography.subtitle1,
            maxLines = 1,
        )
        EllipsisText(
            text = fee.resolveReference(),
            color = TangemTheme.colors.text.tertiary,
            style = TangemTheme.typography.body1,
            modifier = Modifier.padding(start = TangemTheme.dimens.spacing16),
        )
    }
}

@Composable
private fun AmountItem(
    currency: String,
    approveType: ApproveType,
    approveItems: ImmutableList<ApproveType>,
    onChangeApproveType: (ApproveType) -> Unit,
) {
    var isExpandSelector by remember { mutableStateOf(false) }
    var amountSize by remember { mutableStateOf(IntSize.Zero) }
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(TangemTheme.shapes.roundedCornersXMedium)
            .background(TangemTheme.colors.background.action)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = rememberRipple(),
                onClick = { isExpandSelector = true },
            ),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .onSizeChanged { amountSize = it }
                .padding(
                    top = TangemTheme.dimens.spacing16,
                    bottom = TangemTheme.dimens.spacing16,
                    start = TangemTheme.dimens.spacing12,
                    end = TangemTheme.dimens.spacing16,
                ),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = stringResource(id = R.string.give_permission_rows_amount, currency),
                color = TangemTheme.colors.text.primary1,
                style = TangemTheme.typography.subtitle1,
                maxLines = 1,
            )
            when (approveType) {
                ApproveType.LIMITED -> {
                    Text(
                        text = stringResource(id = R.string.give_permission_current_transaction),
                        color = TangemTheme.colors.text.tertiary,
                        style = TangemTheme.typography.body1,
                        maxLines = 1,
                    )
                }
                ApproveType.UNLIMITED -> {
                    Icon(
                        painter = rememberVectorPainter(
                            image = ImageVector.vectorResource(id = R.drawable.ic_infinity_24),
                        ),
                        tint = TangemTheme.colors.text.tertiary,
                        contentDescription = null,
                        modifier = Modifier.size(TangemTheme.dimens.size20),
                    )
                }
            }
        }
        DropdownSelector(
            isExpanded = isExpandSelector,
            onDismiss = { isExpandSelector = false },
            onItemClick = { approveType ->
                isExpandSelector = false
                onChangeApproveType.invoke(approveType)
            },
            items = approveItems,
            selectedType = approveType,
            amountSize = amountSize,
        )
    }
}

@Suppress("LongParameterList")
@Composable
private fun DropdownSelector(
    isExpanded: Boolean,
    onDismiss: () -> Unit,
    onItemClick: (ApproveType) -> Unit,
    items: ImmutableList<ApproveType>,
    selectedType: ApproveType,
    amountSize: IntSize,
) {
    var dropDownWidth by remember { mutableStateOf(IntSize.Zero) }
    val offsetY = amountSize.height.times(-1)
    val offsetX = amountSize.width - dropDownWidth.width

    // Workaround to set color and shape of dropdown menu
    MaterialTheme(
        colorScheme = MaterialTheme.colorScheme.copy(surface = TangemTheme.colors.background.action),
        shapes = MaterialTheme.shapes.copy(extraSmall = RoundedCornerShape(TangemTheme.dimens.radius16)),
    ) {
        DropdownMenu(
            expanded = isExpanded,
            onDismissRequest = onDismiss,
            properties = PopupProperties(clippingEnabled = false),
            offset = with(LocalDensity.current) {
                DpOffset(x = offsetX.toDp(), y = offsetY.toDp())
            },
            modifier = Modifier
                .wrapContentSize()
                .background(TangemTheme.colors.background.action)
                .onSizeChanged { dropDownWidth = it },
        ) {
            items.forEach { item ->
                val color = if (item == selectedType) TangemTheme.colors.icon.accent else Color.Transparent

                DropdownMenuItem(
                    modifier = Modifier.fillMaxWidth(),
                    text = {
                        Row {
                            Text(
                                text = when (item) {
                                    ApproveType.LIMITED -> stringResource(
                                        id = R.string.give_permission_current_transaction,
                                    )
                                    ApproveType.UNLIMITED -> stringResource(id = R.string.give_permission_unlimited)
                                },
                                color = TangemTheme.colors.text.primary1,
                                style = TangemTheme.typography.body1,
                                maxLines = 1,
                            )
                            SpacerWMax()
                            Icon(
                                painter = rememberVectorPainter(
                                    image = ImageVector.vectorResource(id = R.drawable.ic_check_24),
                                ),
                                tint = color,
                                contentDescription = null,
                                modifier = Modifier.padding(start = TangemTheme.dimens.size20),
                            )
                        }
                    },
                    onClick = {
                        onItemClick.invoke(item)
                    },
                )
            }
        }
    }
}

// region preview
@Preview(showBackground = true, widthDp = 360)
@Preview(showBackground = true, widthDp = 360, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun Preview_AgreementBottomSheet() {
    TangemThemePreview {
        GiveTxPermissionBottomSheetContent(content = previewData)
    }
}

private val previewData = GiveTxPermissionBottomSheetConfig(
    data = GiveTxPermissionState.ReadyForRequest(
        currency = "DAI",
        amount = "1",
        walletAddress = "",
        spenderAddress = "",
        fee = TextReference.Str("2,14$"),
        approveType = ApproveType.UNLIMITED,
        approveButton = ApprovePermissionButton(true) {},
        cancelButton = CancelPermissionButton(true),
        onChangeApproveType = { ApproveType.UNLIMITED },
    ),
    onCancel = {},
)