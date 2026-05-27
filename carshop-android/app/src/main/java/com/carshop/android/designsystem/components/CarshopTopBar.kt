package com.carshop.android.designsystem.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import com.carshop.android.designsystem.tokens.Sizing
import com.carshop.android.designsystem.tokens.Spacing

// 80dp 高 · 沉浸式背景由父布局给(默认 surface),不自带 elevation
// 2026-05-27:加 titleStyle 参数,默认 titleLarge,CategoryProducts 等需要大标题时传 headlineMedium
@Composable
fun CarshopTopBar(
    title: String,
    modifier: Modifier = Modifier,
    titleStyle: TextStyle? = null,
    leading: (@Composable () -> Unit)? = null,
    actions: (@Composable RowScope.() -> Unit)? = null,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(Sizing.touchTarget)
            .padding(horizontal = Spacing.padScreenH),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (leading != null) {
            leading()
            Spacer(Modifier.width(Spacing.s3))
        }
        Text(
            text = title,
            style = titleStyle ?: MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(1f),
        )
        if (actions != null) actions()
    }
}
