package com.enaboapps.switchify.widgets

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.material.Button
import androidx.compose.material.Card
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.enaboapps.switchify.utils.StringUtils

@Composable
fun PreferenceTimeStepper(
    value: Long,
    title: String,
    summary: String,
    min: Long,
    max: Long,
    onValueChanged: (Long) -> Unit
) {
    var time by remember { mutableLongStateOf(value) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 8.dp),
        elevation = 8.dp,
        backgroundColor = MaterialTheme.colors.surface
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = title, style = MaterialTheme.typography.h6)
                Spacer(modifier = Modifier.height(4.dp))
                Text(text = summary, style = MaterialTheme.typography.caption)
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(
                modifier = Modifier.wrapContentWidth(Alignment.End),
                horizontalAlignment = Alignment.End
            ) {
                Text(
                    text = StringUtils.getSecondsString(time),
                    style = MaterialTheme.typography.caption
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(onClick = {
                        if (time > min) {
                            time -= 100
                            onValueChanged(time)
                        }
                    }) {
                        Text(text = "-", style = MaterialTheme.typography.button)
                    }
                    Button(onClick = {
                        if (time < max) {
                            time += 100
                            onValueChanged(time)
                        }
                    }) {
                        Text(text = "+", style = MaterialTheme.typography.button)
                    }
                }
            }
        }
    }
}