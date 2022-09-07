package com.enaboapps.switchify.widgets

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.enaboapps.switchify.nav.NavigationRoute

@Composable
fun PreferenceLink(
    title: String,
    summary: String,
    navController: NavController,
    route: NavigationRoute
) {
    Row(
        modifier = Modifier
            .clickable {
                navController.navigate(route.name)
            }
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(end = 16.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.subtitle1
            )
            Text(
                text = summary,
                style = MaterialTheme.typography.body2
            )
        }
        Icon(
            imageVector = Icons.Filled.ArrowForward,
            contentDescription = null,
            tint = MaterialTheme.colors.onSurface.copy(alpha = 0.6f)
        )
    }
}