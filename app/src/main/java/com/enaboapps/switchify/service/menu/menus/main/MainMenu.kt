package com.enaboapps.switchify.service.menu.menus.main

import com.enaboapps.switchify.service.core.SwitchifyAccessibilityService
import com.enaboapps.switchify.service.menu.MenuItem
import com.enaboapps.switchify.service.menu.MenuManager
import com.enaboapps.switchify.service.menu.menus.BaseMenu
import com.enaboapps.switchify.service.menu.structure.MenuConstants
import com.enaboapps.switchify.service.menu.structure.MenuStructureHolder
import com.enaboapps.switchify.service.gestures.GesturePoint
import com.enaboapps.switchify.service.techniques.nodes.NodeExaminer

class MainMenu private constructor(
    accessibilityService: SwitchifyAccessibilityService,
    content: MainMenuContent
) :
    BaseMenu(
        accessibilityService,
        content.items,
        MenuConstants.MenuIds.MAIN_MENU,
        leadingItems = content.leadingItems
    ) {

    constructor(accessibilityService: SwitchifyAccessibilityService) : this(
        accessibilityService,
        buildContent(accessibilityService)
    )

    companion object {
        private fun buildContent(
            accessibilityService: SwitchifyAccessibilityService
        ): MainMenuContent {
            val target = NodeExaminer.findActionTarget(
                GesturePoint.getPoint(),
                accessibilityService
            )
            val actionsItem = target?.let {
                MenuItem(
                    id = MenuConstants.ItemIds.Main.ACCESSIBILITY_ACTIONS,
                    labelResource = com.enaboapps.switchify.R.string.menu_title_accessibility_actions,
                    descriptionResource = com.enaboapps.switchify.R.string.menu_item_accessibility_actions_description,
                    isLinkToMenu = true,
                    action = { MenuManager.getInstance().openAccessibilityActionsMenu(it) }
                )
            }
            return MainMenuContent(
                items = MenuStructureHolder(accessibilityService)
                    .buildMainMenuObject()
                    .getMenuItems(),
                leadingItems = listOfNotNull(actionsItem)
            )
        }
    }

    private data class MainMenuContent(
        val items: List<MenuItem>,
        val leadingItems: List<MenuItem>
    )
}
