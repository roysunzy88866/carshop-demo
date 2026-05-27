package com.carshop.android.designsystem.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.carshop.android.designsystem.tokens.Radius
import com.carshop.android.designsystem.tokens.Sizing
import com.carshop.android.designsystem.tokens.Spacing

enum class CarshopButtonVariant { Primary, Secondary, Text }

@Composable
fun CarshopButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    variant: CarshopButtonVariant = CarshopButtonVariant.Primary,
    enabled: Boolean = true,
    compact: Boolean = false,
    leadingIcon: ImageVector? = null,
) {
    val height = if (compact) Sizing.touchTargetCompact else Sizing.touchTarget
    val minW = 160.dp
    val shape = Radius.Button

    when (variant) {
        CarshopButtonVariant.Primary -> Button(
            onClick = onClick,
            enabled = enabled,
            shape = shape,
            modifier = modifier.heightIn(min = height).widthIn(min = minW),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
            ),
            contentPadding = PaddingValues(horizontal = Spacing.padButtonH, vertical = Spacing.s3),
        ) { ButtonContent(text, leadingIcon) }

        CarshopButtonVariant.Secondary -> OutlinedButton(
            onClick = onClick,
            enabled = enabled,
            shape = shape,
            modifier = modifier.heightIn(min = height).widthIn(min = minW),
            border = BorderStroke(Sizing.borderDefault, MaterialTheme.colorScheme.secondary),
            colors = ButtonDefaults.outlinedButtonColors(
                contentColor = MaterialTheme.colorScheme.onSurface,
            ),
            contentPadding = PaddingValues(horizontal = Spacing.padButtonH, vertical = Spacing.s3),
        ) { ButtonContent(text, leadingIcon) }

        CarshopButtonVariant.Text -> TextButton(
            onClick = onClick,
            enabled = enabled,
            shape = shape,
            modifier = modifier.heightIn(min = height),
            colors = ButtonDefaults.textButtonColors(
                contentColor = MaterialTheme.colorScheme.primary,
            ),
            contentPadding = PaddingValues(horizontal = Spacing.padButtonH, vertical = Spacing.s3),
        ) { ButtonContent(text, leadingIcon) }
    }
}

@Composable
private fun ButtonContent(text: String, icon: ImageVector?) {
    if (icon != null) {
        Icon(icon, contentDescription = null, modifier = Modifier.size(Sizing.iconLg))
        Spacer(Modifier.width(Spacing.s2))
    }
    Text(text = text, style = MaterialTheme.typography.labelLarge)
}
