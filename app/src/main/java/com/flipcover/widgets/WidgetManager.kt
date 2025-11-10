package com.flipcover.widgets

import android.content.ComponentName
import android.content.Context

class WidgetManager(private val context: Context) {

    fun getCoverScreenWidgets(): List<CoverWidget> {
        return listOf()
    }

    fun addWidgetToCoverScreen(
        provider: ComponentName,
        label: String,
        minWidth: Int,
        minHeight: Int
    ): Boolean {
        return true
    }

    fun removeWidget(widgetId: String) {
    }
}

data class CoverWidget(
    val id: String,
    val label: String,
    val provider: ComponentName
)
