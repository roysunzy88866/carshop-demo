package com.carshop.android.designsystem.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.carshop.android.designsystem.tokens.CarshopTextStyles
import com.carshop.android.designsystem.tokens.Radius
import com.carshop.android.designsystem.tokens.Sizing
import com.carshop.android.designsystem.tokens.Spacing

// 480×480 dp 二维码框 · 中间留给业务侧填二维码图片
// remainingSeconds 显示 mm:ss 倒计时(JetBrains Mono)
@Composable
fun CarshopQrCodeBox(
    modifier: Modifier = Modifier,
    remainingSeconds: Int? = null,
    qrContent: @Composable BoxScope.() -> Unit = {},
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(
            modifier = Modifier
                .size(Sizing.qrCode)
                .background(MaterialTheme.colorScheme.surface, Radius.CardLarge)
                .border(BorderStroke(Sizing.borderDefault, MaterialTheme.colorScheme.outline), Radius.CardLarge),
            contentAlignment = Alignment.Center,
            content = qrContent,
        )
        if (remainingSeconds != null) {
            Spacer(Modifier.height(Spacing.s4))
            val mm = remainingSeconds / 60
            val ss = remainingSeconds % 60
            Text(
                text = "%02d:%02d".format(mm, ss),
                style = CarshopTextStyles.QrCountdown,
                color = MaterialTheme.colorScheme.onSurface,
            )
        }
    }
}
