package com.flipcover.widgets

import android.appwidget.AppWidgetManager
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import android.widget.RemoteViewsService

class WidgetContainerService : RemoteViewsService() {
    override fun onGetViewFactory(intent: Intent): RemoteViewsFactory {
        return WidgetContainerFactory(this.applicationContext, intent)
    }
}

class WidgetContainerFactory(
    private val context: Context,
    intent: Intent
) : RemoteViewsService.RemoteViewsFactory {

    private val appWidgetId: Int = intent.getIntExtra(
        AppWidgetManager.EXTRA_APPWIDGET_ID,
        AppWidgetManager.INVALID_APPWIDGET_ID
    )

    private var childWidgets: List<ChildWidgetData> = emptyList()
    private lateinit var widgetDataManager: WidgetDataManager

    override fun onCreate() {
        widgetDataManager = WidgetDataManager(context)
        loadWidgets()
    }

    override fun onDataSetChanged() {
        loadWidgets()
    }

    private fun loadWidgets() {
        val mappedId = widgetDataManager.getContainerIdForAppWidget(appWidgetId)
        val containerId = if (mappedId != -1) mappedId else 1  // fallback default
        childWidgets = widgetDataManager.getWidgetsForContainer(containerId)
    }

    override fun onDestroy() {
        childWidgets = emptyList()
    }

    override fun getCount(): Int = childWidgets.size

    override fun getViewAt(position: Int): RemoteViews {
        if (position >= childWidgets.size) {
            return RemoteViews(context.packageName, R.layout.widget_child_item)
        }

        val widget = childWidgets[position]
        val views = RemoteViews(context.packageName, R.layout.widget_child_item)

        if (widget.preview != null) {
            views.setImageViewBitmap(R.id.widgetPreview, widget.preview)
            views.setViewVisibility(R.id.widgetPreview, android.view.View.VISIBLE)
            views.setViewVisibility(R.id.widgetFallback, android.view.View.GONE)
        } else {
            views.setViewVisibility(R.id.widgetPreview, android.view.View.GONE)
            views.setViewVisibility(R.id.widgetFallback, android.view.View.VISIBLE)
            views.setTextViewText(R.id.widgetLabel, widget.label)
            if (widget.icon != null) {
                views.setImageViewBitmap(R.id.widgetIcon, widget.icon)
            }
        }

        return views
    }

    override fun getLoadingView(): RemoteViews? = null
    override fun getViewTypeCount(): Int = 1
    override fun getItemId(position: Int): Long = position.toLong()
    override fun hasStableIds(): Boolean = true
}
