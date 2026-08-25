package com.enaboapps.switchify.service.menu.menus.actions

import com.enaboapps.switchify.R
import com.enaboapps.switchify.service.core.SwitchifyAccessibilityService
import com.enaboapps.switchify.service.menu.MenuItem
import com.enaboapps.switchify.service.menu.MenuManager
import com.enaboapps.switchify.service.menu.menus.BaseMenu
import com.enaboapps.switchify.service.menu.structure.MenuConstants
import com.enaboapps.switchify.service.techniques.nodes.NodeActionExecutor
import com.enaboapps.switchify.service.techniques.nodes.NodeActionTarget
import com.enaboapps.switchify.service.window.MessageSeverity
import com.enaboapps.switchify.service.window.ServiceMessageHUD

internal class AccessibilityActionsMenu(
    accessibilityService: SwitchifyAccessibilityService,
    target: NodeActionTarget
) : BaseMenu(
    accessibilityService = accessibilityService,
    items = buildItems(accessibilityService, target),
    menuId = MenuConstants.MenuIds.ACCESSIBILITY_ACTIONS_MENU
) {
    private companion object {
        fun buildItems(
            accessibilityService: SwitchifyAccessibilityService,
            target: NodeActionTarget
        ): List<MenuItem> {
            val executor = NodeActionExecutor(
                closeMenus = { MenuManager.getInstance().closeMenuHierarchy() },
                showUnavailable = {
                    ServiceMessageHUD.instance.showMessage(
                        R.string.accessibility_action_unavailable,
                        ServiceMessageHUD.MessageType.DISAPPEARING,
                        severity = MessageSeverity.Warning
                    )
                }
            )
            return target.actions.map { action ->
                MenuItem(
                    id = "accessibility_action_${action.id}",
                    userProvidedText = action.label,
                    descriptionResource = R.string.menu_item_accessibility_action_description,
                    closeOnSelect = false,
                    action = { executor.execute(target, action.id) }
                )
            }
        }
    }
}
