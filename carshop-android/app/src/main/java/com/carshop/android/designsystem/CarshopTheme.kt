package com.carshop.android.designsystem

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import com.carshop.android.designsystem.tokens.CarshopColors
import com.carshop.android.designsystem.tokens.CarshopTypography
import com.carshop.android.designsystem.tokens.Radius

// 海泡青电车感 · Light only · no dark · no motion(SPEC 决策)
private val CarshopColorScheme = lightColorScheme(
    primary             = CarshopColors.Steel800,
    onPrimary           = CarshopColors.Steel0,
    primaryContainer    = CarshopColors.Steel700,
    onPrimaryContainer  = CarshopColors.Steel50,
    inversePrimary      = CarshopColors.Steel0,

    secondary           = CarshopColors.Steel500,
    onSecondary         = CarshopColors.Steel0,
    secondaryContainer  = CarshopColors.Steel100,
    onSecondaryContainer= CarshopColors.Steel800,

    tertiary            = CarshopColors.Seafoam600,
    onTertiary          = CarshopColors.Steel0,
    tertiaryContainer   = CarshopColors.Seafoam50,
    onTertiaryContainer = CarshopColors.Seafoam700,

    error               = CarshopColors.Signal500,
    onError             = CarshopColors.Steel0,
    errorContainer      = CarshopColors.Signal50,
    onErrorContainer    = CarshopColors.Signal900,

    background          = CarshopColors.Steel50,
    onBackground        = CarshopColors.Steel800,

    surface             = CarshopColors.Steel0,
    onSurface           = CarshopColors.Steel800,
    surfaceVariant      = CarshopColors.Steel100,
    onSurfaceVariant    = CarshopColors.Steel500,

    inverseSurface      = CarshopColors.Steel700,
    inverseOnSurface    = CarshopColors.Steel50,

    outline             = CarshopColors.Steel200,
    outlineVariant      = CarshopColors.Steel100,
)

private val CarshopShapes = Shapes(
    extraSmall = RoundedCornerShape(Radius.xs),
    small      = RoundedCornerShape(Radius.sm),
    medium     = RoundedCornerShape(Radius.md),
    large      = RoundedCornerShape(Radius.lg),
    extraLarge = RoundedCornerShape(Radius.xl),
)

@Composable
fun CarshopTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = CarshopColorScheme,
        typography  = CarshopTypography,
        shapes      = CarshopShapes,
        content     = content,
    )
}
