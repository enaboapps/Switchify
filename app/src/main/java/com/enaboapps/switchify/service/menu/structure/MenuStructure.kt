package com.enaboapps.switchify.service.menu.structure

import com.enaboapps.switchify.service.menu.MenuItem

class MenuStructure(
    val id: String,
    private val items: List<MenuItem>
) {
    fun getMenuItems(): List<MenuItem> {
        return items
    }

    fun getMenuItemIdList(): List<String> {
        return items.map { it.id }
    }
}