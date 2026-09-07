package com.enaboapps.switchify.service.menu.menus

import com.enaboapps.switchify.service.menu.MenuItem
import com.enaboapps.switchify.service.menu.structure.MenuConstants
import org.junit.Assert.assertEquals
import org.junit.Test

class BaseMenuFilterRemoteItemsTest {

    private fun item(id: String) = MenuItem(
        id = id,
        descriptionResource = null,
        action = {}
    )

    private val items = listOf(
        item(MenuConstants.ItemIds.Main.SETTINGS),
        item(MenuConstants.ItemIds.Main.CONTROL_PC),
        item(MenuConstants.ItemIds.Main.PC_SWITCH_FORWARDING),
        item(MenuConstants.ItemIds.Main.DEVICE)
    )

    @Test
    fun keepsAllItemsWhenRemoteEntriesAreShown() {
        val filtered = BaseMenu.filterRemoteItems(items, true)

        assertEquals(items.map { it.id }, filtered.map { it.id })
    }

    @Test
    fun dropsBothRemoteItemsWhenHidden() {
        val filtered = BaseMenu.filterRemoteItems(items, false)

        assertEquals(
            listOf(MenuConstants.ItemIds.Main.SETTINGS, MenuConstants.ItemIds.Main.DEVICE),
            filtered.map { it.id }
        )
    }

    @Test
    fun dropsUserAddedCopiesOfRemoteItemsInOtherMenus() {
        val deviceMenuItems = listOf(
            item(MenuConstants.ItemIds.Main.DEVICE),
            item(MenuConstants.ItemIds.Main.CONTROL_PC)
        )

        val filtered = BaseMenu.filterRemoteItems(deviceMenuItems, false)

        assertEquals(listOf(MenuConstants.ItemIds.Main.DEVICE), filtered.map { it.id })
    }
}
