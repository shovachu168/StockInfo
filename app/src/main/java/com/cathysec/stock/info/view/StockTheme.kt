package com.cathysec.stock.info.view

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.colorResource
import com.cathysec.stock.info.R

/**
 * Material 3 theme for Compose content. It uses the same brand color as the XML theme
 * so Compose and XML screens look consistent.
 */
@Composable
fun StockTheme(content: @Composable () -> Unit) {
    val primary = colorResource(R.color.brand_primary)
    val colorScheme = if (isSystemInDarkTheme()) {
        darkColorScheme(primary = primary)
    } else {
        lightColorScheme(primary = primary)
    }
    MaterialTheme(colorScheme = colorScheme, content = content)
}
