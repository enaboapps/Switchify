package com.enaboapps.switchify.screens.settings.switches

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.FloatingActionButton
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.enaboapps.switchify.R
import com.enaboapps.switchify.nav.NavigationRoute
import com.enaboapps.switchify.screens.settings.switches.models.SwitchesScreenModel
import com.enaboapps.switchify.switches.SwitchEvent
import com.enaboapps.switchify.switches.SwitchEventStore
import com.enaboapps.switchify.widgets.NavBar
import com.enaboapps.switchify.widgets.PreferenceLink
import com.enaboapps.switchify.widgets.PreferenceSection

@Composable
fun SwitchesScreen(navController: NavController) {
    val switchesScreenModel = SwitchesScreenModel(
        SwitchEventStore(LocalContext.current)
    )
    val events: Collection<SwitchEvent> =
        switchesScreenModel.events.observeAsState().value ?: listOf()
    val verticalScrollState = rememberScrollState()
    Scaffold(
        topBar = {
            NavBar(title = "Switches", navController = navController)
        },
        floatingActionButton = {
            FloatingActionButton(onClick = {
                navController.navigate(NavigationRoute.AddNewSwitch.name)
            }) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_baseline_add_24),
                    contentDescription = "Add"
                )
            }
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(verticalScrollState)
                .padding(it)
                .padding(all = 16.dp),
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
                            navController = navController,
                            switchEvent = event
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SwitchEventItem(
    navController: NavController,
    switchEvent: SwitchEvent
) {
    PreferenceLink(
        title = switchEvent.name,
        summary = "Edit this switch",
        navController = navController,
        route = "${NavigationRoute.EditSwitch.name}/${switchEvent.code}"
    )
}

