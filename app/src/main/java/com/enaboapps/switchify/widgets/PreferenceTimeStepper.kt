package com.enaboapps.switchify.widgets

import androidx.compose.foundation.layout.*
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.enaboapps.switchify.utils.StringUtils

@Composable
fun PreferenceTimeStepper(
    value: Int,
    title: String,
    summary: String,
    min: Int,
    max: Int,
    onValueChanged: (Int) -> Unit
) {
    var time by remember { mutableStateOf(value) }
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, style = MaterialTheme.typography.subtitle1)
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = summary, style = MaterialTheme.typography.caption)
        }
        Text(text = StringUtils.getSecondsString(time), style = MaterialTheme.typography.caption)
        Button(onClick = {
            if (time > min) {
                time -= 100
                onValueChanged(time)
            }
        }) {
            Text(text = "-")
        }
        Button(onClick = {
            if (time < max) {
                time += 100
                onValueChanged(time)
            }
        }) {
            Text(text = "+")
        }
    }
}