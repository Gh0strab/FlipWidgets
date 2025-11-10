package com.flipcover.widgets

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews

class CoverWidgetProvider : AppWidgetProvider() {

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        for (appWidgetId in appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId)
        }
    }

    override fun onEnabled(context: Context) {
        super.onEnabled(context)
    }

    override fun onDisabled(context: Context) {
        super.onDisabled(context)
    }

companion object {
    fun updateAppWidget(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetId: Int
    ) {
        val views = RemoteViews(context.packageName, R.layout.widget_container)

        // --- Make the RemoteViewsService Intent unique per appWidgetId ---
        val intent = Intent(context, WidgetContainerService::class.java)
        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)

        // IMPORTANT: make the intent unique by encoding it as a Uri so adapter instances aren't shared
        intent.data = android.net.Uri.parse(intent.toUri(android.content.Intent.URI_INTENT_SCHEME))

        views.setRemoteAdapter(R.id.widgetContainer, intent)

        // --- Create a click Intent that includes the appWidgetId and use a unique request code ---
        val clickIntent = Intent(context, WidgetHostActivity::class.java).apply {
            putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }

        val activityOptions = android.app.ActivityOptions.makeBasic().apply {
            launchDisplayId = 1
        }

        // Use appWidgetId as requestCode so each widget instance gets a unique PendingIntent
        val clickPendingIntent = android.app.PendingIntent.getActivity(
            context,
            appWidgetId, // << unique request code per widget
            clickIntent,
            android.app.PendingIntent.FLAG_UPDATE_CURRENT or android.app.PendingIntent.FLAG_IMMUTABLE,
            activityOptions.toBundle()
        )

        views.setOnClickPendingIntent(R.id.widgetContainerRoot, clickPendingIntent)

        appWidgetManager.updateAppWidget(appWidgetId, views)

        // Notify that the collection data changed for this widget id specifically
        appWidgetManager.notifyAppWidgetViewDataChanged(appWidgetId, R.id.widgetContainer)
        }
    }
}
