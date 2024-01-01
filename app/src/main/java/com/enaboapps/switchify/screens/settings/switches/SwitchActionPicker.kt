package com.enaboapps.switchify.screens.settings.switches

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Card
import androidx.compose.material.DropdownMenu
import androidx.compose.material.DropdownMenuItem
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.MutableLiveData
import com.enaboapps.switchify.switches.SwitchAction

@Composable
fun SwitchActionPicker(
    title: String,
    action: MutableLiveData<SwitchAction>,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) } // 1. State for controlling dropdown visibility
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 8.dp)
            .clickable(onClick = { expanded = true }),
        elevation = 8.dp,
        backgroundColor = MaterialTheme.colors.surface
    ) {
        Column(
            modifier = Modifier
                .padding(20.dp)
                .fillMaxWidth()
        ) {
            Text(text = title, style = MaterialTheme.typography.h6, fontWeight = FontWeight.Medium)
            if (action.value != null) {
                Text(text = action.value!!.getActionName())
            }
            Spacer(modifier = Modifier.height(8.dp))
            DropdownMenu(
                modifier = modifier,
                expanded = expanded,
                onDismissRequest = {
                    expanded = false
                }, // 2. Dismiss the dropdown when focus changes
            ) {
                SwitchAction.actions.forEach { act ->
                    DropdownMenuItem(onClick = {
                        action.value = act
                        expanded = false
                    }) {
                        Text(text = act.getActionName())
                    }
                }
            }
        }
    }
}