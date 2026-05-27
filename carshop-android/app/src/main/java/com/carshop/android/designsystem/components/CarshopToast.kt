package com.carshop.android.designsystem.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.carshop.android.designsystem.tokens.Radius
import com.carshop.android.designsystem.tokens.Spacing

// 简单 Toast 视图(不带定时消失逻辑,由调用方控制)· 反色底白字
@Composable
fun CarshopToast(text: String, modifier: Modifier = Modifier) {
    Text(
        text = text,
        style = MaterialTheme.typography.bodyLarge,
        color = MaterialTheme.colorScheme.inverseOnSurface,
        modifier = modifier
            .background(MaterialTheme.colorScheme.inverseSurface, RoundedCornerShape(Radius.md))
            .padding(horizontal = Spacing.s4, vertical = Spacing.s3),
    )
}
