package com.budidaya.ikan.ui

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

val Biru = Color(0xFF0B6E8A)
val BiruMuda = Color(0xFFCDEFF7)
val Hijau = Color(0xFF2F8F6B)
val HijauMuda = Color(0xFFD5F2E5)
val Oranye = Color(0xFFC77D1E)
val OranyeMuda = Color(0xFFFCEBD2)

private val skema = lightColorScheme(
    primary = Biru,
    onPrimary = Color.White,
    primaryContainer = BiruMuda,
    onPrimaryContainer = Color(0xFF003544),
    secondary = Hijau,
    onSecondary = Color.White,
    secondaryContainer = HijauMuda,
    onSecondaryContainer = Color(0xFF00382A),
    tertiary = Oranye,
    onTertiary = Color.White,
    tertiaryContainer = OranyeMuda,
    onTertiaryContainer = Color(0xFF4A2800),
    background = Color(0xFFF4F9FA),
    onBackground = Color(0xFF14211F),
    surface = Color(0xFFF4F9FA),
    onSurface = Color(0xFF14211F),
    surfaceVariant = Color(0xFFDDE7EA),
    onSurfaceVariant = Color(0xFF41504F),
    surfaceContainerLowest = Color.White,
    surfaceContainerLow = Color(0xFFFBFEFE),
    surfaceContainer = Color(0xFFEFF6F7),
    surfaceContainerHigh = Color(0xFFE8F1F3),
    surfaceContainerHighest = Color(0xFFE1ECEE),
    error = Color(0xFFB3261E)
)

@Composable
fun TemaBudidaya(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = skema, content = content)
}
