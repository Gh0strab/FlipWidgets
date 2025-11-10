package com.flipcover.widgets

import android.appwidget.AppWidgetProviderInfo
import android.content.ComponentName
import android.graphics.drawable.Drawable

data class WidgetInfo(
    val provider: ComponentName,
    val label: String,
    val icon: Drawable?,
    val minWidth: Int,
    val minHeight: Int,
    val providerInfo: AppWidgetProviderInfo
)
