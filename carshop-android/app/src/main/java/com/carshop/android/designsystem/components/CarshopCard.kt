package com.carshop.android.designsystem.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.carshop.android.designsystem.tokens.Elevation
import com.carshop.android.designsystem.tokens.Radius
import com.carshop.android.designsystem.tokens.Sizing
import com.carshop.android.designsystem.tokens.Spacing

// 主卡片容器 · e0(无阴影 + 1dp 描边)· 海泡青电车感首选
@Composable
fun CarshopCard(
    modifier: Modifier = Modifier,
    large: Boolean = false,
    selected: Boolean = false,
    content: @Composable ColumnScope.() -> Unit,
) {
    Card(
        modifier = modifier,
        shape = if (large) Radius.CardLarge else Radius.Card,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = Elevation.e0),
        border = BorderStroke(
            width = if (selected) Sizing.borderThick else Sizing.borderDefault,
            color = if (selected) MaterialTheme.colorScheme.tertiary
                    else MaterialTheme.colorScheme.outline,
        ),
    ) {
        Column(
            modifier = Modifier
                .padding(if (large) Spacing.padCardLarge else Spacing.padCard),
            content = content,
        )
    }
}
