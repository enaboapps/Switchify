package com.enaboapps.switchify.screens.settings.switches

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.runtime.Composable
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.enaboapps.switchify.R
import com.enaboapps.switchify.nav.NavigationRoute
import com.enaboapps.switchify.screens.settings.switches.models.SwitchesScreenModel
import com.enaboapps.switchify.switches.SwitchEvent
import com.enaboapps.switchify.switches.SwitchEventStore
import com.enaboapps.switchify.widgets.PreferenceSection

@Composable
fun SwitchesScreen(navController: NavController) {
    val switchesScreenModel = SwitchesScreenModel(
        SwitchEventStore(LocalContext.current)
    )
    val events: List<SwitchEvent> = switchesScreenModel.events.observeAsState().value ?: listOf()
    val verticalScrollState = rememberScrollState()
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(text = "Switches")
                },
                actions = {
                    IconButton(onClick = {
                        navController.navigate(NavigationRoute.AddNewSwitch.name)
                    }) {
                        Icon(
                            painter = painterResource(R.drawable.ic_baseline_add_24),
                            contentDescription = "Add Switch"
                        )
                    }
                }
            )
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(verticalScrollState)
                .padding(it)
        ) {
            if (events.isEmpty()) {
                Box(modifier = Modifier.padding(16.dp), contentAlignment = Alignment.Center) {
                    Text(
                        text = "No switches found",
                        style = MaterialTheme.typography.h6
                    )
                }
            } else {
                PreferenceSection(title = "Switches") {
                    for (event in events) {
                        SwitchEventItem(
                            switchEvent = event,
                            model = switchesScreenModel
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SwitchEventItem(
    switchEvent: SwitchEvent,
    model: SwitchesScreenModel
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colors.surface)
            .clip(RoundedCornerShape(4.dp))
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = switchEvent.name,
            style = MaterialTheme.typography.subtitle1,
            modifier = Modifier.weight(1f)
        )
        IconButton(onClick = {
            model.deleteEvent(switchEvent)
        }) {
            Icon(
                imageVector = Icons.Default.Delete,
                tint = MaterialTheme.colors.error,
                contentDescription = "Delete Switch"
            )
        }
    }
}

