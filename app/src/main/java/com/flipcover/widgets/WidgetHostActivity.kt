package com.flipcover.widgets

import android.app.Activity
import android.appwidget.AppWidgetHost
import android.appwidget.AppWidgetHostView
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProviderInfo
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.GridLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.floatingactionbutton.FloatingActionButton

class WidgetHostActivity : AppCompatActivity() {

    private lateinit var widgetContainer: GridLayout
    private lateinit var appWidgetHost: AppWidgetHost
    private lateinit var appWidgetManager: AppWidgetManager
    private lateinit var widgetDataManager: WidgetDataManager
    private lateinit var addWidgetFab: FloatingActionButton

    private val appWidgetHostId = 1024

    private val requestPickWidget = 1001
    private val requestCreateWidget = 1002
    private val requestBindWidget = 1003

    private var containerId: Int = -1
    private var pendingAppWidgetId: Int = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_widget_host)

        containerId = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, -1)
        if (containerId == -1) {
            Toast.makeText(this, "Missing container ID", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        appWidgetManager = AppWidgetManager.getInstance(this)
        appWidgetHost = AppWidgetHost(this, appWidgetHostId)
        widgetDataManager = WidgetDataManager(this)

        setupViews()
        loadWidgets()
    }

    private fun setupViews() {
        val toolbar = findViewById<MaterialToolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.title = "Widget Container"
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        widgetContainer = findViewById(R.id.widgetHostContainer)
        widgetContainer.columnCount = 6
        widgetContainer.rowCount = 6

        addWidgetFab = findViewById(R.id.addWidgetFab)
        addWidgetFab.setOnClickListener { selectWidget() }
    }

    private fun selectWidget() {
        val appWidgetId = appWidgetHost.allocateAppWidgetId()
        pendingAppWidgetId = appWidgetId
        val pickIntent = Intent(AppWidgetManager.ACTION_APPWIDGET_PICK)
        pickIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
        startActivityForResult(pickIntent, requestPickWidget)
    }

    override fun onStart() {
        super.onStart()
        appWidgetHost.startListening()
    }

    override fun onStop() {
        super.onStop()
        appWidgetHost.stopListening()
    }

    @Deprecated("Deprecated API but still required for widget config flow")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        // IMPORTANT — allows widgets that have config activities to complete
        appWidgetHost.onActivityResult(requestCode, resultCode, data)

        if (resultCode == Activity.RESULT_OK) {
            when (requestCode) {
                requestPickWidget -> configureWidget(data)
                requestBindWidget -> {
                    if (pendingAppWidgetId != -1) {
                        configureWidget(data, skipBinding = true)
                    }
                }
                requestCreateWidget -> createWidget(data)
            }
        } else if (resultCode == Activity.RESULT_CANCELED) {
            cleanupAppWidgetId(data)
        }
    }

    private fun cleanupAppWidgetId(data: Intent?) {
        val id = data?.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, pendingAppWidgetId)
        if (id != -1) {
            try { appWidgetHost.deleteAppWidgetId(id!!) } catch (_: Exception) {}
        }
        pendingAppWidgetId = -1
    }

    private fun configureWidget(data: Intent?, skipBinding: Boolean = false) {
        var appWidgetId = data?.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, -1) ?: pendingAppWidgetId
        if (appWidgetId == -1) return

        val appWidgetInfo = appWidgetManager.getAppWidgetInfo(appWidgetId)
        if (appWidgetInfo == null) {
            appWidgetHost.deleteAppWidgetId(appWidgetId)
            pendingAppWidgetId = -1
            return
        }

        if (!skipBinding) {
            val hasPermission = appWidgetManager.bindAppWidgetIdIfAllowed(appWidgetId, appWidgetInfo.provider)
            if (!hasPermission) {
                val intent = Intent(AppWidgetManager.ACTION_APPWIDGET_BIND).apply {
                    putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
                    putExtra(AppWidgetManager.EXTRA_APPWIDGET_PROVIDER, appWidgetInfo.provider)
                }
                startActivityForResult(intent, requestBindWidget)
                return
            }
        }

        if (appWidgetInfo.configure != null) {
            val intent = Intent(AppWidgetManager.ACTION_APPWIDGET_CONFIGURE).apply {
                component = appWidgetInfo.configure
                putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
            }
            startActivityForResult(intent, requestCreateWidget)
        } else {
            createWidget(data)
        }
    }

    private fun createWidget(data: Intent?) {
        var appWidgetId = data?.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, -1)
            ?: pendingAppWidgetId
        if (appWidgetId == -1) return

        val appWidgetInfo = appWidgetManager.getAppWidgetInfo(appWidgetId) ?: return

        val hostView = appWidgetHost.createView(this, appWidgetId, appWidgetInfo)
        addWidgetToContainer(hostView, appWidgetId, appWidgetInfo)

        saveWidgetData(appWidgetId, appWidgetInfo)
        pendingAppWidgetId = -1
        Toast.makeText(this, "Widget added!", Toast.LENGTH_SHORT).show()
    }

    private fun safeAddToContainer(hostView: AppWidgetHostView, params: GridLayout.LayoutParams) {
        widgetContainer.post {
            widgetContainer.addView(hostView, params)
            hostView.post {
                hostView.requestLayout()
            }
        }
    }

    private fun addWidgetToContainer(
        hostView: AppWidgetHostView,
        appWidgetId: Int,
        appWidgetInfo: AppWidgetProviderInfo
    ) {
        val gridSize = WidgetSizeCalculator.calculateGridSize(appWidgetInfo)

        val widthPx = WidgetSizeCalculator.calculateWidgetSizePx(gridSize.columnSpan, resources.displayMetrics.density)
        val heightPx = WidgetSizeCalculator.calculateWidgetSizePx(gridSize.rowSpan, resources.displayMetrics.density)

        val params = GridLayout.LayoutParams().apply {
            width = widthPx
            height = heightPx
            columnSpec = GridLayout.spec(GridLayout.UNDEFINED, gridSize.columnSpan, 1f)
            rowSpec = GridLayout.spec(GridLayout.UNDEFINED, gridSize.rowSpan, 1f)
        }

        hostView.layoutParams = params
        hostView.tag = appWidgetId

        safeAddToContainer(hostView, params)
    }

    private fun saveWidgetData(appWidgetId: Int, appWidgetInfo: AppWidgetProviderInfo) {
        val gridSize = WidgetSizeCalculator.calculateGridSize(appWidgetInfo)

        val widgetData = ChildWidgetData(
            id = appWidgetId.toString(),
            provider = appWidgetInfo.provider,
            label = appWidgetInfo.loadLabel(packageManager),
            gridX = 0, gridY = 0,
            gridWidth = gridSize.columnSpan,
            gridHeight = gridSize.rowSpan,
            containerId = containerId
        )

        widgetDataManager.saveWidget(widgetData)
    }

    private fun loadWidgets() {
        widgetContainer.removeAllViews()
        for (widget in widgetDataManager.getWidgetsForContainer(containerId)) {
            val appWidgetId = widget.id.toIntOrNull() ?: continue
            val appWidgetInfo = appWidgetManager.getAppWidgetInfo(appWidgetId) ?: continue
            val hostView = appWidgetHost.createView(this, appWidgetId, appWidgetInfo)
            addWidgetToContainer(hostView, appWidgetId, appWidgetInfo)
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
}
