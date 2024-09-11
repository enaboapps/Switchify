package com.enaboapps.switchify.switches

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp

/**
 * A banner that is displayed when a switch configuration is invalid
 * Nothing is displayed if the configuration is valid
 */

@Composable
fun SwitchConfigInvalidBanner() {
    val bannerText = SwitchEventStore(LocalContext.current).isConfigInvalid()
    if (bannerText != null) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = bannerText,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}