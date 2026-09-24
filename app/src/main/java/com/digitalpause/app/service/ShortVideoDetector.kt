package com.digitalpause.app.service

import android.util.Log
import android.view.accessibility.AccessibilityNodeInfo

/**
 * Detects YouTube Shorts and Instagram Reels from the live accessibility node tree.
 *
 * Detection strategy (adapted from curbox-android reference):
 *  - Look for specific view IDs that are ONLY present in the short-video player UI.
 *  - These IDs are stable across YouTube/Instagram versions and don't appear elsewhere.
 *  - Return true as soon as any anchor ID is found and visible on screen.
 *
 * YouTube Shorts anchor:
 *   "reel_recycler" — the RecyclerView that powers the Shorts feed.
 *   It only exists when the Shorts player is active.
 *
 * Instagram Reels anchors:
 *   "clips_viewer_view_pager" — the ViewPager inside the full-screen Reels viewer.
 *   "clips_ufi_component"     — the like/comment/share bar under each Reel.
 *   These only appear when Reels is open (not when browsing the regular feed).
 */
object ShortVideoDetector {

    private const val TAG = "ShortVideoDetector"

    // YouTube Shorts — view IDs that ONLY appear inside the Shorts player
    private val YOUTUBE_ANCHOR_IDS = setOf(
        "com.google.android.youtube:id/reel_recycler",
        "com.google.android.youtube:id/reel_player_page_content",
        "com.google.android.youtube:id/shorts_player_sheet",
        "com.google.android.youtube:id/shorts_shelf",
        // ReVanced / patched YouTube
        "app.revanced.android.youtube:id/reel_recycler",
        "app.revanced.android.youtube:id/reel_player_page_content"
    )

    // Instagram Reels — view IDs that ONLY appear inside the full-screen Reels player
    private val INSTAGRAM_ANCHOR_IDS = setOf(
        "com.instagram.android:id/clips_viewer_view_pager",
        "com.instagram.android:id/clips_ufi_component",
        "com.instagram.android:id/clips_video_container",
        "com.instagram.android:id/clips_action_bar_button",
        "com.instagram.android:id/clips_author_username"
    )

    // Fallback content-description keywords (secondary signal only)
    private val YOUTUBE_DESC_KEYWORDS = listOf(
        "like this short", "dislike this short", "remix this short", "share this short",
        "sound used in this short", "shorts camera"
    )
    private val INSTAGRAM_DESC_KEYWORDS = listOf(
        "reels video", "reel by ", "like reel", "unlike reel", "remix reel"
    )

    fun isShortVideoActive(root: AccessibilityNodeInfo?, packageName: String): Boolean {
        if (root == null) return false

        return try {
            when (packageName) {
                "com.google.android.youtube",
                "app.revanced.android.youtube" -> detectByIds(root, YOUTUBE_ANCHOR_IDS, YOUTUBE_DESC_KEYWORDS, packageName)

                "com.instagram.android" -> detectByIds(root, INSTAGRAM_ANCHOR_IDS, INSTAGRAM_DESC_KEYWORDS, packageName)

                else -> false
            }
        } catch (e: Exception) {
            Log.e(TAG, "Detection error for $packageName: ${e.message}")
            false
        }
    }

    /**
     * Primary detection: find any node whose viewIdResourceName is in the anchor set.
     * This is O(tree-size) but fast in practice (<5ms on modern phones).
     * We do NOT recycle the root here — caller is responsible.
     */
    private fun detectByIds(
        root: AccessibilityNodeInfo,
        anchorIds: Set<String>,
        descKeywords: List<String>,
        packageName: String
    ): Boolean {
        // Strategy 1: findAccessibilityNodeInfosByViewId (fast, O(1) per ID)
        for (id in anchorIds) {
            val nodes = try { root.findAccessibilityNodeInfosByViewId(id) } catch (e: Exception) { null }
            if (!nodes.isNullOrEmpty()) {
                val found = nodes.any { it != null }
                nodes.forEach { try { it?.recycle() } catch (e: Exception) {} }
                if (found) {
                    Log.d(TAG, "[$packageName] Detected via viewId: $id")
                    return true
                }
            }
        }

        // Strategy 2: DFS tree walk for content descriptions (fallback)
        val foundByDesc = walkTreeForDesc(root, descKeywords, depth = 0, maxDepth = 18)
        if (foundByDesc != null) {
            Log.d(TAG, "[$packageName] Detected via desc: $foundByDesc")
            return true
        }

        return false
    }

    private fun walkTreeForDesc(
        node: AccessibilityNodeInfo,
        keywords: List<String>,
        depth: Int,
        maxDepth: Int
    ): String? {
        if (depth > maxDepth) return null
        val desc = node.contentDescription?.toString()?.lowercase() ?: ""
        for (kw in keywords) {
            if (desc.contains(kw)) return kw
        }
        for (i in 0 until node.childCount) {
            val child = try { node.getChild(i) } catch (e: Exception) { null } ?: continue
            val result = walkTreeForDesc(child, keywords, depth + 1, maxDepth)
            try { child.recycle() } catch (e: Exception) {}
            if (result != null) return result
        }
        return null
    }
}
