package eu.kanade.presentation.theme.colorscheme

import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color

/**
 * Mochiku OLED Theme (adapted from Mochy-Music).
 * Pure dark OLED canvas (#000000), neutral typography (#E0E0E0 / #8E8E8E),
 * frosted dark containers (#121214), and zero colorful gradients.
 */
internal object MochikuColorScheme : BaseColorScheme() {

    override val darkScheme = darkColorScheme(
        primary = Color(0xFFE0E0E0),
        onPrimary = Color(0xFF000000),
        primaryContainer = Color(0xFF222226),
        onPrimaryContainer = Color(0xFFFFFFFF),
        secondary = Color(0xFF8E8E8E),
        onSecondary = Color(0xFF000000),
        secondaryContainer = Color(0xFF18181A),
        onSecondaryContainer = Color(0xFFE0E0E0),
        tertiary = Color(0xFF8E8E8E),
        onTertiary = Color(0xFFFFFFFF),
        tertiaryContainer = Color(0xFF222226),
        onTertiaryContainer = Color(0xFFFFFFFF),
        error = Color(0xFFCF6679),
        onError = Color(0xFF000000),
        errorContainer = Color(0xFFB00020),
        onErrorContainer = Color(0xFFFFFFFF),
        background = Color(0xFF000000),
        onBackground = Color(0xFFE0E0E0),
        surface = Color(0xFF000000),
        onSurface = Color(0xFFE0E0E0),
        surfaceVariant = Color(0xFF121214),
        onSurfaceVariant = Color(0xFF8E8E8E),
        outline = Color(0xFF26262A),
        outlineVariant = Color(0xFF1E1E22),
        scrim = Color(0xFF000000),
        inverseSurface = Color(0xFFE0E0E0),
        inverseOnSurface = Color(0xFF000000),
        inversePrimary = Color(0xFF000000),
        surfaceDim = Color(0xFF000000),
        surfaceBright = Color(0xFF1A1A1E),
        surfaceContainerLowest = Color(0xFF000000),
        surfaceContainerLow = Color(0xFF0A0A0C),
        surfaceContainer = Color(0xFF101012),
        surfaceContainerHigh = Color(0xFF18181A),
        surfaceContainerHighest = Color(0xFF202024),
    )

    override val lightScheme = darkScheme // Mochy renders dark-only
}
