package com.utkarsh.aitonerewriter.service

import android.accessibilityservice.AccessibilityService
import android.os.Bundle
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo

/**
 * Accessibility Service that detects and manipulates text in input fields
 * across other apps (WhatsApp, Telegram, Instagram, SMS, etc.).
 *
 * Privacy: No text is stored or logged. Text is only read when the user
 * explicitly triggers a rewrite action.
 */
class TextAccessibilityService : AccessibilityService() {

    companion object {
        var instance: TextAccessibilityService? = null
            private set
    }

    override fun onServiceConnected() {
        super.onServiceConnected()
        instance = this
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        // We don't passively monitor events for privacy.
        // Text is only captured on-demand when user taps a mood.
    }

    override fun onInterrupt() {
        // Required override
    }

    override fun onDestroy() {
        instance = null
        super.onDestroy()
    }

    /**
     * Finds the currently focused editable text field and returns its text content.
     * Returns null if no editable field is focused.
     */
    fun captureCurrentText(): String? {
        val rootNode = rootInActiveWindow ?: return null
        val editableNode = findFocusedEditableNode(rootNode)
        val text = editableNode?.text?.toString()
        rootNode.recycle()
        return text
    }

    /**
     * Replaces the text in the currently focused editable field with the new text.
     * Returns true if successful.
     */
    fun replaceText(newText: String): Boolean {
        val rootNode = rootInActiveWindow ?: return false
        val editableNode = findFocusedEditableNode(rootNode)

        if (editableNode != null) {
            val arguments = Bundle().apply {
                putCharSequence(
                    AccessibilityNodeInfo.ACTION_ARGUMENT_SET_TEXT_CHARSEQUENCE,
                    newText
                )
            }
            val success = editableNode.performAction(
                AccessibilityNodeInfo.ACTION_SET_TEXT,
                arguments
            )
            rootNode.recycle()
            return success
        }

        rootNode.recycle()
        return false
    }

    /**
     * Recursively searches the accessibility node tree for the currently
     * focused editable text field.
     */
    private fun findFocusedEditableNode(node: AccessibilityNodeInfo): AccessibilityNodeInfo? {
        // First try to find the input-focused node
        val focusedNode = node.findFocus(AccessibilityNodeInfo.FOCUS_INPUT)
        if (focusedNode != null && focusedNode.isEditable) {
            return focusedNode
        }

        // Fallback: search the tree for any focused editable node
        return findEditableNodeRecursive(node)
    }

    private fun findEditableNodeRecursive(node: AccessibilityNodeInfo): AccessibilityNodeInfo? {
        if (node.isEditable && node.isFocused) {
            return node
        }

        for (i in 0 until node.childCount) {
            val child = node.getChild(i) ?: continue
            val result = findEditableNodeRecursive(child)
            if (result != null) return result
        }

        return null
    }
}
