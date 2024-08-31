package com.enaboapps.switchify.service.menu.store

import android.accessibilityservice.AccessibilityService
import android.content.Context
import android.content.pm.PackageManager
import android.media.AudioManager
import android.view.accessibility.AccessibilityNodeInfo
import com.enaboapps.switchify.R
import com.enaboapps.switchify.service.SwitchifyAccessibilityService
import com.enaboapps.switchify.service.gestures.GestureManager
import com.enaboapps.switchify.service.gestures.GesturePoint
import com.enaboapps.switchify.service.gestures.SwipeDirection
import com.enaboapps.switchify.service.gestures.ZoomGesturePerformer
import com.enaboapps.switchify.service.menu.MenuItem
import com.enaboapps.switchify.service.menu.MenuManager
import com.enaboapps.switchify.service.nodes.Node
import com.enaboapps.switchify.service.nodes.NodeExaminer
import com.enaboapps.switchify.service.scanning.ScanMethod
import com.enaboapps.switchify.service.utils.ScreenUtils

class MenuItemStore(private val accessibilityService: SwitchifyAccessibilityService) {
    private val tapMenuItem = MenuItem(
        id = "tap",
        text = "Tap",
        action = {
            GestureManager.getInstance().performTap()
        }
    )

    private val toggleGestureLockMenuItem = MenuItem(
        id = "toggle_gesture_lock",
        text = "Toggle Gesture Lock",
        action = { GestureManager.getInstance().toggleGestureLock() }
    )

    /**
     * The system navigation items
     */
    val systemNavItems = listOf(
        MenuItem(
            id = "sys_back",
            drawableId = R.drawable.ic_sys_back,
            drawableDescription = "Back",
            action = { accessibilityService.performGlobalAction(AccessibilityService.GLOBAL_ACTION_BACK) }
        ),
        MenuItem(
            id = "sys_home",
            drawableId = R.drawable.ic_sys_home,
            drawableDescription = "Home",
            action = { accessibilityService.performGlobalAction(AccessibilityService.GLOBAL_ACTION_HOME) }
        )
    )

    /**
     * The navigation menu items
     */
    val menuManipulatorItems = listOfNotNull(
        if (MenuManager.getInstance().menuHierarchy?.getTopMenu() != null) {
            MenuItem(
                id = "previous_menu",
                drawableId = R.drawable.ic_previous_menu,
                drawableDescription = "Previous menu",
                isMenuHierarchyManipulator = true,
                action = { MenuManager.getInstance().menuHierarchy?.popMenu() }
            )
        } else null,
        MenuItem(
            id = "close_menu",
            drawableId = R.drawable.ic_close_menu,
            drawableDescription = "Close menu",
            isMenuHierarchyManipulator = true,
            action = { MenuManager.getInstance().menuHierarchy?.removeAllMenus() }
        )
    )

    /**
     * The main menu item store object
     */
    val mainMenuObject = MenuItemStoreObject(
        id = "main_menu",
        items = listOfNotNull(
            tapMenuItem,
            MenuItem(
                id = "gestures",
                text = "Gestures",
                isLinkToMenu = true,
                action = { MenuManager.getInstance().openGesturesMenu() }
            ),
            if (NodeExaminer.canPerformScrollActions(GesturePoint.getPoint())) {
                MenuItem(
                    id = "scroll",
                    text = "Scroll",
                    isLinkToMenu = true,
                    action = { MenuManager.getInstance().openScrollMenu() }
                )
            } else null,
            if (ScanMethod.getType() != ScanMethod.MethodType.ITEM_SCAN) {
                MenuItem(
                    id = "refine_selection",
                    text = "Refine Selection",
                    action = { GesturePoint.setReselect(true) }
                )
            } else null,
            MenuItem(
                id = "device",
                text = "Device",
                isLinkToMenu = true,
                action = { MenuManager.getInstance().openDeviceMenu() }
            ),
            MenuItem(
                id = "media_control",
                text = "Media Control",
                isLinkToMenu = true,
                action = { MenuManager.getInstance().openMediaControlMenu() }
            ),
            if (NodeExaminer.canPerformEditActions(GesturePoint.getPoint())) {
                MenuItem(
                    id = "edit",
                    text = "Edit",
                    isLinkToMenu = true,
                    action = { MenuManager.getInstance().openEditMenu() }
                )
            } else null,
            MenuItem(
                id = "switch_cursor_item_scan",
                text = MenuManager.getInstance().getTypeToSwitchTo(),
                action = { MenuManager.getInstance().changeBetweenCursorAndItemScan() }
            )
        )
    )

    /**
     * The gestures menu item store object
     */
    val gesturesMenuObject = MenuItemStoreObject(
        id = "gestures_menu",
        items = listOf(
            MenuItem(
                id = "tap_gestures",
                text = "Tap Gestures",
                isLinkToMenu = true,
                action = { MenuManager.getInstance().openTapMenu() }
            ),
            MenuItem(
                id = "swipe_gestures",
                text = "Swipe Gestures",
                isLinkToMenu = true,
                action = { MenuManager.getInstance().openSwipeMenu() }
            ),
            MenuItem(
                id = "drag",
                text = "Drag",
                action = { GestureManager.getInstance().startDragGesture() }
            ),
            MenuItem(
                id = "zoom_gestures",
                text = "Zoom Gestures",
                isLinkToMenu = true,
                action = { MenuManager.getInstance().openZoomGesturesMenu() }
            ),
            toggleGestureLockMenuItem
        )
    )

    /**
     * The swipe gestures menu item store object
     */
    val swipeGesturesMenuObject = MenuItemStoreObject(
        id = "swipe_gestures_menu",
        items = listOf(
            MenuItem(
                id = "swipe_up",
                text = "Swipe Up",
                action = { GestureManager.getInstance().performSwipe(SwipeDirection.UP) }
            ),
            MenuItem(
                id = "swipe_down",
                text = "Swipe Down",
                action = { GestureManager.getInstance().performSwipe(SwipeDirection.DOWN) }
            ),
            MenuItem(
                id = "swipe_left",
                text = "Swipe Left",
                action = { GestureManager.getInstance().performSwipe(SwipeDirection.LEFT) }
            ),
            MenuItem(
                id = "swipe_right",
                text = "Swipe Right",
                action = { GestureManager.getInstance().performSwipe(SwipeDirection.RIGHT) }
            )
        )
    )

    /**
     * The tap gestures menu item store object
     */
    val tapGesturesMenuObject = MenuItemStoreObject(
        id = "tap_gestures_menu",
        items = listOf(
            tapMenuItem,
            MenuItem(
                id = "double_tap",
                text = "Double Tap",
                action = { GestureManager.getInstance().performDoubleTap() }
            ),
            MenuItem(
                id = "tap_and_hold",
                text = "Tap and Hold",
                action = { GestureManager.getInstance().performTapAndHold() }
            )
        )
    )

    /**
     * The zoom gestures menu item store object
     */
    val zoomGesturesMenuObject = MenuItemStoreObject(
        id = "zoom_gestures_menu",
        items = listOf(
            MenuItem(
                id = "zoom_in",
                text = "Zoom In",
                action = {
                    GestureManager.getInstance()
                        .performZoomAction(ZoomGesturePerformer.ZoomAction.ZOOM_IN)
                }
            ),
            MenuItem(
                id = "zoom_out",
                text = "Zoom Out",
                action = {
                    GestureManager.getInstance()
                        .performZoomAction(ZoomGesturePerformer.ZoomAction.ZOOM_OUT)
                }
            )
        )
    )

    /**
     * The device menu item store object
     */
    fun buildDeviceMenuObject(): MenuItemStoreObject {
        val packageManager = accessibilityService.packageManager
        return MenuItemStoreObject(
            id = "device_menu",
            items = listOfNotNull(
                MenuItem(
                    id = "recent_apps",
                    text = "Recent Apps",
                    action = { accessibilityService.performGlobalAction(AccessibilityService.GLOBAL_ACTION_RECENTS) }
                ),
                MenuItem(
                    id = "notifications",
                    text = "Notifications",
                    action = { accessibilityService.performGlobalAction(AccessibilityService.GLOBAL_ACTION_NOTIFICATIONS) }
                ),
                MenuItem(
                    id = "all_apps",
                    text = "All Apps",
                    action = { accessibilityService.performGlobalAction(AccessibilityService.GLOBAL_ACTION_ACCESSIBILITY_ALL_APPS) }
                ),
                if (packageManager.hasSystemFeature(PackageManager.FEATURE_ACTIVITIES_ON_SECONDARY_DISPLAYS) && ScreenUtils.isTablet(
                        accessibilityService
                    )
                ) {
                    MenuItem(
                        id = "toggle_split_screen",
                        text = "Toggle Split Screen",
                        action = { accessibilityService.performGlobalAction(AccessibilityService.GLOBAL_ACTION_TOGGLE_SPLIT_SCREEN) }
                    )
                } else null,
                MenuItem(
                    id = "quick_settings",
                    text = "Quick Settings",
                    action = { accessibilityService.performGlobalAction(AccessibilityService.GLOBAL_ACTION_QUICK_SETTINGS) }
                ),
                MenuItem(
                    id = "lock_screen",
                    text = "Lock Screen",
                    action = { accessibilityService.performGlobalAction(AccessibilityService.GLOBAL_ACTION_LOCK_SCREEN) }
                ),
                MenuItem(
                    id = "power_dialog",
                    text = "Power Dialog",
                    action = { accessibilityService.performGlobalAction(AccessibilityService.GLOBAL_ACTION_POWER_DIALOG) }
                ),
                MenuItem(
                    id = "volume_control",
                    text = "Volume Control",
                    isLinkToMenu = true,
                    action = { MenuManager.getInstance().openVolumeControlMenu() }
                )
            )
        )
    }

    /**
     * The volume control menu item store object
     */
    fun buildVolumeControlMenuObject(): MenuItemStoreObject {
        return MenuItemStoreObject(
            id = "volume_control_menu",
            items = listOf(
                MenuItem(
                    id = "volume_up",
                    text = "Volume Up",
                    action = {
                        val audioManager =
                            accessibilityService.getSystemService(Context.AUDIO_SERVICE) as AudioManager
                        audioManager.adjustStreamVolume(
                            AudioManager.STREAM_ACCESSIBILITY,
                            AudioManager.ADJUST_RAISE,
                            AudioManager.FLAG_SHOW_UI
                        )
                    }
                ),
                MenuItem(
                    id = "volume_down",
                    text = "Volume Down",
                    action = {
                        val audioManager =
                            accessibilityService.getSystemService(Context.AUDIO_SERVICE) as AudioManager
                        audioManager.adjustStreamVolume(
                            AudioManager.STREAM_ACCESSIBILITY,
                            AudioManager.ADJUST_LOWER,
                            AudioManager.FLAG_SHOW_UI
                        )
                    }
                )
            )
        )
    }

    /**
     * The media control menu item store object
     */
    val mediaControlMenuObject = MenuItemStoreObject(
        id = "media_control_menu",
        items = listOf(
            MenuItem(
                id = "play_pause",
                text = "Play/Pause",
                action = { accessibilityService.performGlobalAction(AccessibilityService.GLOBAL_ACTION_KEYCODE_HEADSETHOOK) }
            )
        )
    )

    /**
     * The scroll menu item store object
     */
    fun buildScrollMenuObject(): MenuItemStoreObject {
        val currentPoint = GesturePoint.getPoint()
        val scrollUpNode =
            NodeExaminer.findNodeForAction(currentPoint, Node.ActionType.SCROLL_UP)
        val scrollDownNode =
            NodeExaminer.findNodeForAction(currentPoint, Node.ActionType.SCROLL_DOWN)
        val scrollLeftNode =
            NodeExaminer.findNodeForAction(currentPoint, Node.ActionType.SCROLL_LEFT)
        val scrollRightNode =
            NodeExaminer.findNodeForAction(currentPoint, Node.ActionType.SCROLL_RIGHT)
        return MenuItemStoreObject(
            id = "scroll_menu",
            items = listOfNotNull(
                if (scrollUpNode != null) {
                    MenuItem(
                        id = "scroll_up",
                        text = "Scroll Up",
                        closeOnSelect = false,
                        action = {
                            scrollUpNode.performAction(AccessibilityNodeInfo.AccessibilityAction.ACTION_SCROLL_UP.id)
                        }
                    )
                } else null,
                if (scrollDownNode != null) {
                    MenuItem(
                        id = "scroll_down",
                        text = "Scroll Down",
                        closeOnSelect = false,
                        action = {
                            scrollDownNode.performAction(AccessibilityNodeInfo.AccessibilityAction.ACTION_SCROLL_DOWN.id)
                        }
                    )
                } else null,
                if (scrollLeftNode != null) {
                    MenuItem(
                        id = "scroll_left",
                        text = "Scroll Left",
                        closeOnSelect = false,
                        action = {
                            scrollLeftNode.performAction(AccessibilityNodeInfo.AccessibilityAction.ACTION_SCROLL_LEFT.id)
                        }
                    )
                } else null,
                if (scrollRightNode != null) {
                    MenuItem(
                        id = "scroll_right",
                        text = "Scroll Right",
                        closeOnSelect = false,
                        action = {
                            scrollRightNode.performAction(AccessibilityNodeInfo.AccessibilityAction.ACTION_SCROLL_RIGHT.id)
                        }
                    )
                } else null
            )
        )
    }

    /**
     * The edit menu item store object
     */
    fun buildEditMenuObject(): MenuItemStoreObject {
        val currentPoint = GesturePoint.getPoint()
        val cutNode = NodeExaminer.findNodeForAction(currentPoint, Node.ActionType.CUT)
        val copyNode = NodeExaminer.findNodeForAction(currentPoint, Node.ActionType.COPY)
        val pasteNode = NodeExaminer.findNodeForAction(currentPoint, Node.ActionType.PASTE)
        return MenuItemStoreObject(
            id = "edit_menu",
            items = listOfNotNull(
                if (cutNode != null) {
                    MenuItem(
                        id = "cut",
                        text = "Cut",
                        action = {
                            cutNode.performAction(AccessibilityNodeInfo.ACTION_CUT)
                        }
                    )
                } else null,
                if (copyNode != null) {
                    MenuItem(
                        id = "copy",
                        text = "Copy",
                        action = {
                            copyNode.performAction(AccessibilityNodeInfo.ACTION_COPY)
                        }
                    )
                } else null,
                if (pasteNode != null) {
                    MenuItem(
                        id = "paste",
                        text = "Paste",
                        action = {
                            pasteNode.performAction(AccessibilityNodeInfo.ACTION_PASTE)
                        }
                    )
                } else null
            )
        )
    }
}