package com.enaboapps.switchify.screens.settings.actions.inputs

import androidx.compose.runtime.Composable
import com.enaboapps.switchify.service.custom.actions.store.data.ActionExtra
import com.enaboapps.switchify.widgets.TextArea

@Composable
fun OpenLinkExtraInput(
    selectedExtra: ActionExtra?,
    onExtraUpdated: (ActionExtra?) -> Unit,
    onExtraValidated: (Boolean) -> Unit
) {
    TextArea(
        value = selectedExtra?.linkUrl ?: "",
        onValueChange = { text ->
            onExtraUpdated(
                ActionExtra(
                    linkUrl = text
                )
            )
            val isValid = text.isNotBlank() && text.matches(Regex("^(http|https)://.*$"))
            onExtraValidated(isValid)
        },
        label = "Link URL",
        isError = selectedExtra?.linkUrl.isNullOrBlank() == true,
        supportingText = "Link URL is required"
    )
}
