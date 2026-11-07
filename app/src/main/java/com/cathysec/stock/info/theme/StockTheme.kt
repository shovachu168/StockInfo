package com.cathysec.stock.info.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.colorResource
import com.cathysec.stock.info.R

/**
 * Material 3 theme for Compose content. It reads the same theme_* color resources as the XML
 * theme, so Compose and XML screens look the same in light and dark mode.
 */
@Composable
fun StockTheme(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = stockColorScheme(isSystemInDarkTheme()), content = content)
}

// The colors are resources, so the values of values-night are used automatically in dark mode.
// Only the base (light or dark) differs: it provides the roles that are not listed here.
@Composable
private fun stockColorScheme(isDark: Boolean): ColorScheme =
    if (isDark) {
        darkColorScheme(
            primary = colorResource(R.color.theme_primary),
            onPrimary = colorResource(R.color.theme_on_primary),
            primaryContainer = colorResource(R.color.theme_primary_container),
            onPrimaryContainer = colorResource(R.color.theme_on_primary_container),
            secondary = colorResource(R.color.theme_secondary),
            onSecondary = colorResource(R.color.theme_on_secondary),
            secondaryContainer = colorResource(R.color.theme_secondary_container),
            onSecondaryContainer = colorResource(R.color.theme_on_secondary_container),
            tertiary = colorResource(R.color.theme_tertiary),
            onTertiary = colorResource(R.color.theme_on_tertiary),
            tertiaryContainer = colorResource(R.color.theme_tertiary_container),
            onTertiaryContainer = colorResource(R.color.theme_on_tertiary_container),
            error = colorResource(R.color.theme_error),
            onError = colorResource(R.color.theme_on_error),
            errorContainer = colorResource(R.color.theme_error_container),
            onErrorContainer = colorResource(R.color.theme_on_error_container),
            background = colorResource(R.color.theme_background),
            onBackground = colorResource(R.color.theme_on_background),
            surface = colorResource(R.color.theme_surface),
            onSurface = colorResource(R.color.theme_on_surface),
            surfaceVariant = colorResource(R.color.theme_surface_variant),
            onSurfaceVariant = colorResource(R.color.theme_on_surface_variant),
            outline = colorResource(R.color.theme_outline),
            outlineVariant = colorResource(R.color.theme_outline_variant),
            inverseSurface = colorResource(R.color.theme_inverse_surface),
            inverseOnSurface = colorResource(R.color.theme_inverse_on_surface),
            inversePrimary = colorResource(R.color.theme_inverse_primary),
            surfaceDim = colorResource(R.color.theme_surface_dim),
            surfaceBright = colorResource(R.color.theme_surface_bright),
            surfaceContainerLowest = colorResource(R.color.theme_surface_container_lowest),
            surfaceContainerLow = colorResource(R.color.theme_surface_container_low),
            surfaceContainer = colorResource(R.color.theme_surface_container),
            surfaceContainerHigh = colorResource(R.color.theme_surface_container_high),
            surfaceContainerHighest = colorResource(R.color.theme_surface_container_highest),
        )
    } else {
        lightColorScheme(
            primary = colorResource(R.color.theme_primary),
            onPrimary = colorResource(R.color.theme_on_primary),
            primaryContainer = colorResource(R.color.theme_primary_container),
            onPrimaryContainer = colorResource(R.color.theme_on_primary_container),
            secondary = colorResource(R.color.theme_secondary),
            onSecondary = colorResource(R.color.theme_on_secondary),
            secondaryContainer = colorResource(R.color.theme_secondary_container),
            onSecondaryContainer = colorResource(R.color.theme_on_secondary_container),
            tertiary = colorResource(R.color.theme_tertiary),
            onTertiary = colorResource(R.color.theme_on_tertiary),
            tertiaryContainer = colorResource(R.color.theme_tertiary_container),
            onTertiaryContainer = colorResource(R.color.theme_on_tertiary_container),
            error = colorResource(R.color.theme_error),
            onError = colorResource(R.color.theme_on_error),
            errorContainer = colorResource(R.color.theme_error_container),
            onErrorContainer = colorResource(R.color.theme_on_error_container),
            background = colorResource(R.color.theme_background),
            onBackground = colorResource(R.color.theme_on_background),
            surface = colorResource(R.color.theme_surface),
            onSurface = colorResource(R.color.theme_on_surface),
            surfaceVariant = colorResource(R.color.theme_surface_variant),
            onSurfaceVariant = colorResource(R.color.theme_on_surface_variant),
            outline = colorResource(R.color.theme_outline),
            outlineVariant = colorResource(R.color.theme_outline_variant),
            inverseSurface = colorResource(R.color.theme_inverse_surface),
            inverseOnSurface = colorResource(R.color.theme_inverse_on_surface),
            inversePrimary = colorResource(R.color.theme_inverse_primary),
            surfaceDim = colorResource(R.color.theme_surface_dim),
            surfaceBright = colorResource(R.color.theme_surface_bright),
            surfaceContainerLowest = colorResource(R.color.theme_surface_container_lowest),
            surfaceContainerLow = colorResource(R.color.theme_surface_container_low),
            surfaceContainer = colorResource(R.color.theme_surface_container),
            surfaceContainerHigh = colorResource(R.color.theme_surface_container_high),
            surfaceContainerHighest = colorResource(R.color.theme_surface_container_highest),
        )
    }
