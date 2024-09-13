package com.enaboapps.switchify.screens.settings.switches.actions

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Card
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.MutableLiveData
import com.enaboapps.switchify.switches.SwitchAction

@Composable
fun SwitchActionPicker(
    title: String,
    switchAction: SwitchAction,
    modifier: Modifier = Modifier,
    onChange: ((SwitchAction) -> Unit),
    onDelete: (() -> Unit)? = null // Optional delete callback
) {
    var expanded by remember { mutableStateOf(false) } // State for controlling dropdown visibility

    val action = remember { MutableLiveData(switchAction) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 8.dp)
            .clickable(onClick = { expanded = true })
    ) {
        Row(
            modifier = Modifier
                .padding(20.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium
                )
                if (action.value != null) {
                    Text(text = action.value!!.getActionName())
                    Text(text = action.value!!.getActionDescription())
                } else {
                    Text(text = "No action selected", style = MaterialTheme.typography.bodyMedium)
                }
            }

            // Delete button on the right side
            if (onDelete != null) {
                IconButton(onClick = {
                    action.value = null // Clear the selected action
                    onDelete() // Trigger delete callback if provided
                }) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete Action",
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }
        }

        DropdownMenu(
            modifier = modifier,
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            SwitchAction.actions.forEach { act ->
                DropdownMenuItem(
                    text = { Text(text = act.getActionName()) },
                    onClick = {
                        action.value = act
                        expanded = false
                        onChange(act) // Trigger onChange callback with the new action
                    }
                )
            }
        }
    }
}