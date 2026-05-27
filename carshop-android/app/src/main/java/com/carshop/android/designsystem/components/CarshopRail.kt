package com.carshop.android.designsystem.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.carshop.android.designsystem.tokens.Sizing
import com.carshop.android.designsystem.tokens.Spacing

data class RailItem(
    val key: String,
    val label: String,
    val icon: ImageVector,
)

// 240dp 宽 · 自己写,不用 NavigationRail(默认 80dp 太窄)
// 顶部品牌区 "EV·MALL." + 1dp 分隔线 + 导航项
// 选中态:左侧 3dp 海泡青强调条 + 文字/图标 tint = tertiary(seafoam-600)
@Composable
fun CarshopRail(
    items: List<RailItem>,
    selectedKey: String,
    onSelect: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .width(Sizing.widthRail)
            .fillMaxHeight()
            .background(MaterialTheme.colorScheme.surface),
    ) {
        // 品牌 logo 区 · EV·MALL. 与 rail item 等高(80dp)
        RailBrandLogo()
        HorizontalDivider(
            color = MaterialTheme.colorScheme.outlineVariant,
            thickness = 1.dp,
        )
        items.forEach { item ->
            CarshopRailRow(
                item = item,
                selected = item.key == selectedKey,
                onClick = { onSelect(item.key) },
            )
        }
    }
}

// 品牌标识:EV·MALL. · "·" 用 tertiary(seafoam-600)
@Composable
private fun RailBrandLogo() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(Sizing.touchTarget)
            .padding(horizontal = Spacing.s4),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            "EV",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
        )
        Text(
            "·",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.tertiary,
        )
        Text(
            "MALL.",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
        )
    }
}

@Composable
private fun CarshopRailRow(item: RailItem, selected: Boolean, onClick: () -> Unit) {
    // 2026-05-27 选中态:石墨蓝底(primary)+ 白文字/图标 + 左侧海泡青竖条(对齐设计稿截图)
    // 整行水平 padding(margin)让选中态成为悬浮的圆角胶囊,而不是贴边
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = Spacing.s3, vertical = 6.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(
                if (selected) MaterialTheme.colorScheme.primary else Color.Transparent,
            )
            .height(Sizing.touchTarget)
            .clickable { onClick() },
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // 左侧 3dp 强调条(选中态 seafoam,未选中透明)
        Box(
            modifier = Modifier
                .width(Sizing.borderAccent)
                .fillMaxHeight()
                .background(
                    if (selected) MaterialTheme.colorScheme.tertiary else Color.Transparent,
                ),
        )
        Spacer(Modifier.width(Spacing.s4 - Sizing.borderAccent))
        Icon(
            item.icon,
            contentDescription = null,
            modifier = Modifier.size(28.dp),
            tint = if (selected) MaterialTheme.colorScheme.onPrimary
                   else MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(Modifier.width(Spacing.s3))
        Text(
            item.label,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Medium,
            color = if (selected) MaterialTheme.colorScheme.onPrimary
                    else MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}
