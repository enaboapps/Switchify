package com.enaboapps.switchify.service.nodes

import android.widget.RelativeLayout
import com.enaboapps.switchify.service.scanning.ScanColorManager
import com.enaboapps.switchify.service.scanning.ScanHighlightDrawable
import com.enaboapps.switchify.service.window.SwitchifyAccessibilityWindow

class NodeScannerUI {
    companion object {
        val instance: NodeScannerUI by lazy { NodeScannerUI() }
    }

    private val window = SwitchifyAccessibilityWindow.instance

    private var itemBoundsLayout: RelativeLayout? = null
    private var rowBoundsLayout: RelativeLayout? = null

    private val handler = android.os.Handler(android.os.Looper.getMainLooper())

    fun showItemBounds(x: Int, y: Int, width: Int, height: Int) {
        handler.post {
            window.getContext()?.let {
                itemBoundsLayout = RelativeLayout(it)
                val border =
                    ScanHighlightDrawable(ScanColorManager.getScanColorSetFromPreferences(it).secondaryColor)
                itemBoundsLayout?.background = border
                itemBoundsLayout?.let { layout ->
                    window.addView(layout, x, y, width, height)
                }
            }
        }
    }

    fun showRowBounds(x: Int, y: Int, width: Int, height: Int) {
        handler.post {
            window.getContext()?.let {
                rowBoundsLayout = RelativeLayout(it)
                val border =
                    ScanHighlightDrawable(ScanColorManager.getScanColorSetFromPreferences(it).primaryColor)
                rowBoundsLayout?.background = border
                rowBoundsLayout?.let { layout ->
                    window.addView(layout, x, y, width, height)
                }
            }
        }
    }

    fun hideItemBounds() {
        handler.post {
            itemBoundsLayout?.let {
                window.removeView(it)
                itemBoundsLayout = null
            }
        }
    }

    fun hideRowBounds() {
        handler.post {
            rowBoundsLayout?.let {
                window.removeView(it)
                rowBoundsLayout = null
            }
        }
    }

    fun hideAll() {
        hideItemBounds()
        hideRowBounds()
    }
}