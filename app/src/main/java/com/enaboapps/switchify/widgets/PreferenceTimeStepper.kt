package com.enaboapps.switchify.widgets

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.material.Button
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
    min: Int,
    max: Int,
    onValueChanged: (Int) -> Unit
) {
    var time by remember { mutableStateOf(value) }
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Text("$title ${StringUtils.getSecondsString(time)}")
        Spacer(modifier = Modifier.weight(1f))
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