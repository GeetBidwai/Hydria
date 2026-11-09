package com.example.widgets

import android.content.Context
import androidx.glance.appwidget.updateAll

object WidgetRefreshHelper {
    /**
     * Refreshes all placed instances of both the compact (2x1) and expanded (4x2) widgets.
     */
    suspend fun refreshAllWidgets(context: Context) {
        try {
            CompactWaterWidget().updateAll(context)
        } catch (_: Exception) {}
        try {
            ExpandedWaterWidget().updateAll(context)
        } catch (_: Exception) {}
    }
}
