package com.carshop.android.designsystem.components

import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import com.carshop.android.designsystem.tokens.Radius

// 顶部 20dp 圆角 BottomSheet · Material 3 实验 API
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CarshopBottomSheet(
    onDismiss: () -> Unit,
    sheetState: SheetState = rememberModalBottomSheetState(),
    content: @Composable ColumnScope.() -> Unit,
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        shape = Radius.BottomSheet,
        containerColor = MaterialTheme.colorScheme.surface,
        content = content,
    )
}
