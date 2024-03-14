package com.enaboapps.switchify.service.nodes

import android.widget.RelativeLayout
import com.enaboapps.switchify.R
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
            itemBoundsLayout = RelativeLayout(window.getContext())
            itemBoundsLayout?.background =
                window.getContext()?.getDrawable(R.drawable.scan_item_border)
            itemBoundsLayout?.let {
                window.addView(it, x, y, width, height)
            }
        }
    }

    fun showRowBounds(x: Int, y: Int, width: Int, height: Int) {
        handler.post {
            rowBoundsLayout = RelativeLayout(window.getContext())
            rowBoundsLayout?.background =
                window.getContext()?.getDrawable(R.drawable.scan_row_border)
            rowBoundsLayout?.let {
                window.addView(it, x, y, width, height)
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