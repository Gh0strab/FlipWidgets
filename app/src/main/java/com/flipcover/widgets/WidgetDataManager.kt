package com.flipcover.widgets

import android.content.ComponentName
import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import android.graphics.Bitmap

data class ChildWidgetData(
    val id: String,
    val provider: ComponentName,
    val label: String,
    val icon: Bitmap? = null,
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
    val iconBase64: String? = null,
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

    /** Save a single widget entry to the correct container */
    fun saveWidget(widget: ChildWidgetData) {
        val widgets = getWidgetsForContainer(widget.containerId).toMutableList()
        widgets.removeAll { it.id == widget.id }
        widgets.add(widget)
        saveWidgetsForContainer(widget.containerId, widgets)
    }

    /** Load all widgets that belong to a given container id */
    fun getWidgetsForContainer(containerId: Int): List<ChildWidgetData> {
        val json = prefs.getString("widgets_$containerId", "[]") ?: "[]"
        val type = object : TypeToken<List<ChildWidgetSerializable>>() {}.type
        val serializableWidgets: List<ChildWidgetSerializable> = gson.fromJson(json, type)

        return serializableWidgets.map { serializable ->
            ChildWidgetData(
                id = serializable.id,
                provider = ComponentName(serializable.providerPackage, serializable.providerClass),
                label = serializable.label,
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
        saveWidgetsForContainer(containerId, widgets)
    }

    private fun saveWidgetsForContainer(containerId: Int, widgets: List<ChildWidgetData>) {
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

    /** Track which appWidgetId corresponds to which logical container */
    fun setAppWidgetMapping(appWidgetId: Int, containerId: Int) {
        prefs.edit().putInt("map_$appWidgetId", containerId).apply()
    }

    fun getContainerIdForAppWidget(appWidgetId: Int): Int {
        return prefs.getInt("map_$appWidgetId", -1)
    }

    fun getAllContainers(): List<Int> {
        return prefs.all.keys
            .filter { it.startsWith("widgets_") }
            .mapNotNull { it.removePrefix("widgets_").toIntOrNull() }
    }

    fun removeContainer(containerId: Int) {
        prefs.edit().remove("widgets_$containerId").apply()
    }
}
