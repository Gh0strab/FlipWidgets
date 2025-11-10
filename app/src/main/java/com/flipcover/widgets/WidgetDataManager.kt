package com.flipcover.widgets

import android.content.ComponentName
import android.content.Context
import android.content.SharedPreferences
import android.graphics.Bitmap
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

data class ChildWidgetData(
    val id: String,
    val provider: ComponentName,
    val label: String,
    val icon: Bitmap?,
    val gridX: Int,
    val gridY: Int,
    val gridWidth: Int,
    val gridHeight: Int,
    val containerId: Int
)

data class ChildWidgetSerializable(
    val id: String,
    val providerPackage: String,
    val providerClass: String,
    val label: String,
    val gridX: Int,
    val gridY: Int,
    val gridWidth: Int,
    val gridHeight: Int,
    val containerId: Int
)

class WidgetDataManager(private val context: Context) {
    
    private val prefs: SharedPreferences = context.getSharedPreferences(
        "widget_container_prefs",
        Context.MODE_PRIVATE
    )
    
    private val gson = Gson()

    fun saveWidget(widget: ChildWidgetData) {
        val widgets = getWidgetsForContainer(widget.containerId).toMutableList()
        widgets.removeAll { it.id == widget.id }
        widgets.add(widget)
        saveWidgets(widgets)
    }

    fun getWidgetsForContainer(containerId: Int): List<ChildWidgetData> {
        val json = prefs.getString("widgets_$containerId", "[]") ?: "[]"
        val type = object : TypeToken<List<ChildWidgetSerializable>>() {}.type
        val serializableWidgets: List<ChildWidgetSerializable> = gson.fromJson(json, type)
        
        return serializableWidgets.map { serializable ->
            ChildWidgetData(
                id = serializable.id,
                provider = ComponentName(serializable.providerPackage, serializable.providerClass),
                label = serializable.label,
                icon = null,
                gridX = serializable.gridX,
                gridY = serializable.gridY,
                gridWidth = serializable.gridWidth,
                gridHeight = serializable.gridHeight,
                containerId = serializable.containerId
            )
        }
    }

    fun removeWidget(widgetId: String, containerId: Int) {
        val widgets = getWidgetsForContainer(containerId).toMutableList()
        widgets.removeAll { it.id == widgetId }
        
        val serializableWidgets = widgets.map { widget ->
            ChildWidgetSerializable(
                id = widget.id,
                providerPackage = widget.provider.packageName,
                providerClass = widget.provider.className,
                label = widget.label,
                gridX = widget.gridX,
                gridY = widget.gridY,
                gridWidth = widget.gridWidth,
                gridHeight = widget.gridHeight,
                containerId = widget.containerId
            )
        }
        
        val json = gson.toJson(serializableWidgets)
        prefs.edit().putString("widgets_$containerId", json).apply()
    }

    private fun saveWidgets(widgets: List<ChildWidgetData>) {
        val containerId = if (widgets.isEmpty()) return else widgets.first().containerId
        val serializableWidgets = widgets.map { widget ->
            ChildWidgetSerializable(
                id = widget.id,
                providerPackage = widget.provider.packageName,
                providerClass = widget.provider.className,
                label = widget.label,
                gridX = widget.gridX,
                gridY = widget.gridY,
                gridWidth = widget.gridWidth,
                gridHeight = widget.gridHeight,
                containerId = widget.containerId
            )
        }
        
        val json = gson.toJson(serializableWidgets)
        prefs.edit().putString("widgets_$containerId", json).apply()
    }

    fun getAllContainers(): List<Int> {
        val allKeys = prefs.all.keys
        return allKeys
            .filter { it.startsWith("widgets_") }
            .mapNotNull { it.removePrefix("widgets_").toIntOrNull() }
    }
}
