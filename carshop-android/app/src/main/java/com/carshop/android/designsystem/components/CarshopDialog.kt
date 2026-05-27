package com.carshop.android.designsystem.components

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import com.carshop.android.designsystem.tokens.Radius

// 16dp 圆角 · 标准 AlertDialog,主题已注入
@Composable
fun CarshopDialog(
    title: String,
    text: String,
    confirmText: String = "确定",
    dismissText: String? = "取消",
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        shape = Radius.Dialog,
        containerColor = MaterialTheme.colorScheme.surface,
        title = { Text(title, style = MaterialTheme.typography.titleMedium) },
        text = { Text(text, style = MaterialTheme.typography.bodyLarge) },
        confirmButton = {
            CarshopButton(text = confirmText, onClick = onConfirm, compact = true)
        },
        dismissButton = dismissText?.let { dt ->
            {
                CarshopButton(
                    text = dt,
                    onClick = onDismiss,
                    variant = CarshopButtonVariant.Text,
                    compact = true,
                )
            }
        },
    )
}
