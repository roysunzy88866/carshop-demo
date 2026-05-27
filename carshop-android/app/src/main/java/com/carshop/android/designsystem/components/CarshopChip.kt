package com.carshop.android.designsystem.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.carshop.android.designsystem.tokens.Radius
import com.carshop.android.designsystem.tokens.Sizing
import com.carshop.android.designsystem.tokens.Spacing

// 48dp 高 / 胶囊 · 可选中态(底色变 tertiaryContainer + 描边变 tertiary)
@Composable
fun CarshopChip(
    text: String,
    modifier: Modifier = Modifier,
    selected: Boolean = false,
    onClick: (() -> Unit)? = null,
) {
    val bg = if (selected) MaterialTheme.colorScheme.tertiaryContainer else MaterialTheme.colorScheme.surface
    val borderColor = if (selected) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.outline
    val textColor = if (selected) MaterialTheme.colorScheme.onTertiaryContainer else MaterialTheme.colorScheme.onSurface

    Row(
        modifier = modifier
            .heightIn(min = Sizing.heightChip)
            .background(bg, Radius.Chip)
            .border(BorderStroke(Sizing.borderDefault, borderColor), Radius.Chip)
            .let { if (onClick != null) it.clickable { onClick() } else it }
            .padding(horizontal = Spacing.s4),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(text, style = MaterialTheme.typography.labelMedium, color = textColor)
    }
}
