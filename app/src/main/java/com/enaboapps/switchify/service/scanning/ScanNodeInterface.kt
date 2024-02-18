package com.enaboapps.switchify.service.scanning

/**
 * This interface represents a node in the scanning tree
 */
interface ScanNodeInterface {
    /**
     * This function gets the X coordinate of the node
     * @return The X coordinate of the node
     */
    fun getX(): Int

    /**
     * This function gets the Y coordinate of the node
     * @return The Y coordinate of the node
     */
    fun getY(): Int

    /**
     * This function highlights the node
     */
    fun highlight()

    /**
     * This function unhighlights the node
     */
    fun unhighlight()

    /**
     * This function selects the node
     */
    fun select()
}