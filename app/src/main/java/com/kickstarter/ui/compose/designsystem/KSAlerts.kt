package com.kickstarter.ui.compose.designsystem

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.Popup
import com.kickstarter.libs.utils.safeLet
import com.kickstarter.ui.compose.designsystem.KSTheme.colors
import com.kickstarter.ui.compose.designsystem.KSTheme.dimensions
import com.kickstarter.ui.compose.designsystem.KSTheme.typographyV2

/**
 * UI component rendering KSDialog.
 *
 * Negative constraints:
 * - Do not perform unmanaged side-effects directly in composition; hoist state to caller.
 */
@Composable
fun KSDialog(
    setShowDialog: (Boolean) -> Unit,
    content: @Composable () -> Unit
) {
    Dialog(onDismissRequest = { setShowDialog(false) }) {
        content()
    }
}

@Composable
fun KSDialogVisual(
    modifier: Modifier,
    setShowDialog: (Boolean) -> Unit,
    headline: String? = null,
    headlineStyle: TextStyle? = null,
    headlineSpacing: Dp? = null,
    bodyText: String,
    bodyStyle: TextStyle,
    rightButtonText: String? = null,
    rightButtonTextStyle: TextStyle? = null,
    rightButtonTextColor: Color? = null,
    rightButtonAction: (() -> Unit)? = null,
    rightButtonModifier: Modifier = Modifier,
    rightButtonColor: Color? = null,
    leftButtonText: String? = null,
    leftButtonTextStyle: TextStyle? = null,
    leftButtonTextColor: Color? = null,
    leftButtonAction: (() -> Unit)? = null,
    leftButtonModifier: Modifier = Modifier,
    leftButtonColor: Color? = null,
    additionalButtonSpacing: Dp? = null,
) {
    Column(
        modifier = modifier
    ) {
        // Headline
        safeLet(headline, headlineStyle, headlineSpacing) { text, style, space ->
            Text(text = text, style = style, color = colors.textPrimary)
            Spacer(modifier = Modifier.height(space))
        }

        // Body
        Text(
            modifier = Modifier.padding(end = dimensions.paddingSmall),
            text = bodyText,
            style = bodyStyle,
            color = colors.textPrimary
        )

        Row(
            Modifier
                .padding(top = dimensions.paddingMedium)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.End
        ) {
            // Left Button
            safeLet(leftButtonText, leftButtonTextStyle) { text, style ->
                Button(
                    modifier = leftButtonModifier,
                    onClick = {
                        leftButtonAction?.invoke()
                        setShowDialog(false)
                    },
                    colors =
                    ButtonDefaults.buttonColors(
                        containerColor = leftButtonColor ?: colors.backgroundSurfacePrimary
                    ),
                    elevation = ButtonDefaults.buttonElevation(dimensions.none)
                ) {
                    Text(
                        text = text,
                        style = style,
                        color = leftButtonTextColor ?: style.color
                    )
                }
            }

            Spacer(
                modifier = Modifier.width(
                    additionalButtonSpacing ?: dimensions.dialogButtonSpacing
                )
            )

            // Right Button
            safeLet(rightButtonText, rightButtonTextStyle) { text, style ->
                Button(
                    modifier = rightButtonModifier,
                    onClick = {
                        rightButtonAction?.invoke()
                        setShowDialog(false)
                    },
                    colors =
                    ButtonDefaults.buttonColors(
                        containerColor = rightButtonColor ?: colors.backgroundSurfacePrimary
                    ),
                    elevation = ButtonDefaults.buttonElevation(dimensions.none)
                ) {
                    Text(
                        text = text,
                        style = style,
                        color = rightButtonTextColor ?: style.color
                    )
                }
            }
        }
    }
}

@Composable
fun KSAlertDialogNoHeadline(
    setShowDialog: (Boolean) -> Unit,
    bodyText: String,
    leftButtonText: String? = null,
    leftButtonAction: (() -> Unit)? = null,
    rightButtonText: String? = null,
    rightButtonAction: (() -> Unit)? = null
) {
    KSDialog(
        setShowDialog = setShowDialog,
        content = {
            KSDialogVisual(
                modifier = Modifier
                    .width(dimensions.dialogWidth)
                    .background(color = colors.backgroundSurfacePrimary, shape = shapes.small)
                    .padding(
                        start = dimensions.paddingLarge,
                        top = dimensions.paddingLarge,
                        bottom = dimensions.paddingSmall,
                        end = dimensions.paddingSmall
                    ),
                setShowDialog = setShowDialog,
                bodyText = bodyText,
                bodyStyle = typographyV2.bodyLG,
                leftButtonText = leftButtonText,
                leftButtonAction = leftButtonAction,
                leftButtonTextStyle = typographyV2.buttonLabel,
                rightButtonText = rightButtonText,
                rightButtonAction = rightButtonAction,
                rightButtonTextStyle = typographyV2.buttonLabel
            )
        }
    )
}

@Composable
fun KSAlertDialog(
    setShowDialog: (Boolean) -> Unit,
    headlineText: String,
    bodyText: String,
    leftButtonText: String?,
    leftButtonAction: (() -> Unit)? = null,
    rightButtonText: String?,
    rightButtonAction: (() -> Unit)? = null
) {
    KSDialog(
        setShowDialog = setShowDialog,
        content = {
            KSDialogVisual(
                modifier = Modifier
                    .width(dimensions.dialogWidth)
                    .background(color = colors.backgroundSurfacePrimary, shape = shapes.small)
                    .padding(
                        start = dimensions.paddingLarge,
                        top = dimensions.paddingLarge,
                        bottom = dimensions.paddingSmall,
                        end = dimensions.paddingSmall
                    ),
                setShowDialog = setShowDialog,
                headline = headlineText,
                headlineStyle = typographyV2.headingXL,
                headlineSpacing = dimensions.paddingMedium,
                bodyText = bodyText,
                bodyStyle = typographyV2.bodyLG,
                leftButtonText = leftButtonText,
                leftButtonAction = leftButtonAction,
                leftButtonTextStyle = typographyV2.buttonLabel,
                rightButtonText = rightButtonText,
                rightButtonAction = rightButtonAction,
                rightButtonTextStyle = typographyV2.buttonLabel
            )
        }
    )
}

@Composable
fun KsTooltip(
    setShowDialog: (Boolean) -> Unit,
    headlineText: String,
    bodyText: String,
    alignment: Alignment? = null
) {
    Popup(
        alignment = alignment ?: Alignment.BottomCenter,
        onDismissRequest = { setShowDialog(false) }
    ) {
        KSDialogVisual(
            modifier = Modifier
                .fillMaxWidth()
                .background(color = colors.kds_white)
                .padding(dimensions.paddingLarge),
            setShowDialog = { },
            bodyText = bodyText,
            bodyStyle = typographyV2.bodyMD,
            headline = headlineText,
            headlineStyle = typographyV2.headLine,
            headlineSpacing = dimensions.paddingSmall
        )
    }
}

@Composable
fun KSIntercept(
    setShowDialog: (Boolean) -> Unit,
    bodyText: String,
    leftButtonText: String,
    leftButtonAction: (() -> Unit)? = null,
    rightButtonText: String,
    rightButtonAction: (() -> Unit)? = null,
    alignment: Alignment? = null
) {
    Popup(
        alignment = alignment ?: Alignment.BottomCenter,
        onDismissRequest = { setShowDialog(false) }
    ) {
        KSDialogVisual(
            modifier = Modifier
                .fillMaxWidth()
                .background(color = colors.kds_support_100, shape = shapes.small)
                .padding(
                    start = dimensions.paddingMedium,
                    top = dimensions.paddingLarge,
                    end = dimensions.paddingMedium,
                    bottom = dimensions.paddingMedium
                ),
            setShowDialog = {},
            bodyText = bodyText,
            bodyStyle = typographyV2.headingLG,
            leftButtonText = leftButtonText,
            leftButtonAction = leftButtonAction,
            leftButtonTextStyle = typographyV2.buttonLabel,
            leftButtonTextColor = facebook_blue,
            leftButtonColor = colors.kds_support_100,
            rightButtonText = rightButtonText,
            rightButtonAction = rightButtonAction,
            rightButtonTextStyle = typographyV2.buttonLabel,
            rightButtonTextColor = colors.kds_white,
            rightButtonColor = colors.kds_trust_500,
            additionalButtonSpacing = dimensions.paddingSmall
        )
    }
}
