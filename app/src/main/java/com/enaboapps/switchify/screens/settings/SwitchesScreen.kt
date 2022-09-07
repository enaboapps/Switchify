package com.enaboapps.switchify.screens.settings

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.enaboapps.switchify.R
import com.enaboapps.switchify.nav.NavigationRoute
import com.enaboapps.switchify.screens.settings.models.SwitchesScreenModel
import com.enaboapps.switchify.switches.SwitchEvent
import com.enaboapps.switchify.switches.SwitchEventStore
import com.enaboapps.switchify.widgets.PreferenceSection

@Composable
fun SwitchesScreen(navController: NavController) {
    val switchesScreenModel = SwitchesScreenModel(
        SwitchEventStore(LocalContext.current)
    )
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
            if (switchesScreenModel.events.value?.isEmpty() == true) {
                Box(modifier = Modifier.padding(16.dp), contentAlignment = Alignment.Center) {
                    Text(
                        text = "No switches found",
                        style = MaterialTheme.typography.h6
                    )
                }
            } else {
                PreferenceSection(title = "Switches") {
                    switchesScreenModel.events.value?.forEach { switch ->
                        SwitchEventItem(switchEvent = switch, model = switchesScreenModel)
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
        Image(imageVector = Icons.Default.ArrowForward, contentDescription = null)
    }
}

