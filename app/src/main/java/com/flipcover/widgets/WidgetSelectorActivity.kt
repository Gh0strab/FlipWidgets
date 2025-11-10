package com.flipcover.widgets

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProviderInfo
import android.content.ComponentName
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.appbar.MaterialToolbar

class WidgetSelectorActivity : AppCompatActivity() {

    private lateinit var widgetGridView: RecyclerView
    private lateinit var appWidgetManager: AppWidgetManager
    private lateinit var widgetDataManager: WidgetDataManager
    private var defaultContainerId = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_widget_selector)

        appWidgetManager = AppWidgetManager.getInstance(this)
        widgetDataManager = WidgetDataManager(this)

        setupViews()
        loadAvailableWidgets()
    }

    private fun setupViews() {
        val toolbar = findViewById<MaterialToolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.title = getString(R.string.select_widget)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        widgetGridView = findViewById(R.id.widgetGridView)
        widgetGridView.layoutManager = GridLayoutManager(this, 2)
    }

    private fun loadAvailableWidgets() {
        val installedProviders = appWidgetManager.installedProviders
        
        val widgetInfoList = installedProviders.map { provider ->
            WidgetInfo(
                provider = provider.provider,
                label = provider.loadLabel(packageManager),
                icon = provider.loadIcon(this, resources.displayMetrics.densityDpi),
                minWidth = provider.minWidth,
                minHeight = provider.minHeight,
                providerInfo = provider
            )
        }

        if (widgetInfoList.isEmpty()) {
            Toast.makeText(this, getString(R.string.no_widgets), Toast.LENGTH_SHORT).show()
        }

        val adapter = WidgetSelectorAdapter(widgetInfoList) { widgetInfo ->
            addWidgetToCoverScreen(widgetInfo)
        }
        widgetGridView.adapter = adapter
    }

    private fun addWidgetToCoverScreen(widgetInfo: WidgetInfo) {
        val widgetData = ChildWidgetData(
            id = java.util.UUID.randomUUID().toString(),
            provider = widgetInfo.provider,
            label = widgetInfo.label,
            icon = null,
            gridX = 0,
            gridY = 0,
            gridWidth = 1,
            gridHeight = 1,
            containerId = defaultContainerId
        )
        
        widgetDataManager.saveWidget(widgetData)
        
        val appWidgetManager = AppWidgetManager.getInstance(this)
        val appWidgetIds = appWidgetManager.getAppWidgetIds(
            ComponentName(this, CoverWidgetProvider::class.java)
        )
        for (appWidgetId in appWidgetIds) {
            CoverWidgetProvider.updateAppWidget(this, appWidgetManager, appWidgetId)
        }
        
        Toast.makeText(
            this,
            getString(R.string.widget_added),
            Toast.LENGTH_SHORT
        ).show()
        finish()
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
}
