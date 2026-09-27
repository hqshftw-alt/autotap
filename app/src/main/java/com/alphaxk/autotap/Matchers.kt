package com.alphaxk.autotap

import android.view.accessibility.AccessibilityNodeInfo

object Matchers {

    /**
     * يبحث بعمق (DFS) عن أول عنصر يحتوي نصه أو وصفه على [text] (بدون حساسية لحالة الأحرف).
     * يرجّع null لو ما لقى شي أو لو [text] فاضي.
     */
    fun findNodeContainingText(
        root: AccessibilityNodeInfo?,
        text: String
    ): AccessibilityNodeInfo? {
        if (root == null || text.isBlank()) return null
        val needle = text.trim()

        val stack = ArrayDeque<AccessibilityNodeInfo>()
        stack.addLast(root)

        while (stack.isNotEmpty()) {
            val node = stack.removeLast()

            val nodeText = node.text?.toString().orEmpty()
            val nodeDesc = node.contentDescription?.toString().orEmpty()

            if (nodeText.contains(needle, ignoreCase = true) ||
                nodeDesc.contains(needle, ignoreCase = true)
            ) {
                return node
            }

            for (i in 0 until node.childCount) {
                node.getChild(i)?.let { stack.addLast(it) }
            }
        }
        return null
    }

    /**
     * يبدأ من [node] ويصعد للأعلى (parents) لين ما يلقى عنصر isClickable == true.
     * كثير أزرار في Compose/Views يكون النص جوه عنصر فرعي، والعنصر القابل للضغط هو الأب.
     */
    fun findClickableSelfOrAncestor(node: AccessibilityNodeInfo?): AccessibilityNodeInfo? {
        var current = node
        var hops = 0
        while (current != null && hops < 12) {
            if (current.isClickable) return current
            current = current.parent
            hops++
        }
        return null
    }
}
