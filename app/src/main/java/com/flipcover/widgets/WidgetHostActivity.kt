package com.flipcover.widgets

import android.appwidget.AppWidgetHost
import android.appwidget.AppWidgetHostView
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProviderInfo
import android.content.Intent
import android.os.Bundle
import android.widget.FrameLayout
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
    private var containerId = 1
    private var pendingAppWidgetId: Int = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_widget_host)

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
        addWidgetFab.setOnClickListener {
            selectWidget()
        }
    }

    private fun selectWidget() {
        val appWidgetId = appWidgetHost.allocateAppWidgetId()
        pendingAppWidgetId = appWidgetId
        val pickIntent = Intent(AppWidgetManager.ACTION_APPWIDGET_PICK)
        pickIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
        startActivityForResult(pickIntent, requestPickWidget)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        
        if (resultCode == RESULT_OK) {
            when (requestCode) {
                requestPickWidget -> {
                    configureWidget(data)
                }
                requestBindWidget -> {
                    if (pendingAppWidgetId != -1) {
                        val newData = Intent()
                        newData.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, pendingAppWidgetId)
                        val savedId = pendingAppWidgetId
                        pendingAppWidgetId = -1
                        configureWidget(newData, skipBinding = true)
                    }
                }
                requestCreateWidget -> {
                    createWidget(data)
                }
            }
        } else if (resultCode == RESULT_CANCELED) {
            val appWidgetId = data?.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, -1) ?: pendingAppWidgetId
            if (appWidgetId != -1) {
                appWidgetHost.deleteAppWidgetId(appWidgetId)
            }
            pendingAppWidgetId = -1
        }
    }

    private fun configureWidget(data: Intent?, skipBinding: Boolean = false) {
        val extras = data?.extras
        var appWidgetId = extras?.getInt(AppWidgetManager.EXTRA_APPWIDGET_ID, -1) ?: -1
        
        if (appWidgetId == -1 && pendingAppWidgetId != -1) {
            appWidgetId = pendingAppWidgetId
        }
        
        if (appWidgetId == -1) {
            Toast.makeText(this, "Failed to get widget ID", Toast.LENGTH_SHORT).show()
            return
        }
        
        val appWidgetInfo = appWidgetManager.getAppWidgetInfo(appWidgetId)

        if (appWidgetInfo == null) {
            Toast.makeText(this, "Widget info not available", Toast.LENGTH_SHORT).show()
            appWidgetHost.deleteAppWidgetId(appWidgetId)
            pendingAppWidgetId = -1
            return
        }

        if (!skipBinding) {
            val hasPermission = appWidgetManager.bindAppWidgetIdIfAllowed(
                appWidgetId,
                appWidgetInfo.provider
            )

            if (!hasPermission) {
                pendingAppWidgetId = appWidgetId
                val intent = Intent(AppWidgetManager.ACTION_APPWIDGET_BIND)
                intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
                intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_PROVIDER, appWidgetInfo.provider)
                startActivityForResult(intent, requestBindWidget)
                return
            }
        }

        if (appWidgetInfo.configure != null) {
            val intent = Intent(AppWidgetManager.ACTION_APPWIDGET_CONFIGURE)
            intent.component = appWidgetInfo.configure
            intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
            startActivityForResult(intent, requestCreateWidget)
        } else {
            createWidget(data)
        }
    }

    private fun createWidget(data: Intent?) {
        val extras = data?.extras
        var appWidgetId = extras?.getInt(AppWidgetManager.EXTRA_APPWIDGET_ID, -1) ?: -1
        
        if (appWidgetId == -1 && pendingAppWidgetId != -1) {
            appWidgetId = pendingAppWidgetId
        }
        
        if (appWidgetId == -1) {
            Toast.makeText(this, "Failed to add widget", Toast.LENGTH_SHORT).show()
            return
        }

        val appWidgetInfo = appWidgetManager.getAppWidgetInfo(appWidgetId)
        
        if (appWidgetInfo == null) {
            Toast.makeText(this, "Widget info not available", Toast.LENGTH_SHORT).show()
            appWidgetHost.deleteAppWidgetId(appWidgetId)
            pendingAppWidgetId = -1
            return
        }
        
        val hostView = appWidgetHost.createView(this, appWidgetId, appWidgetInfo)
        
        addWidgetToContainer(hostView, appWidgetId, appWidgetInfo)
        saveWidgetData(appWidgetId, appWidgetInfo)
        
        pendingAppWidgetId = -1
        
        Toast.makeText(this, "Widget added!", Toast.LENGTH_SHORT).show()
    }

    private fun addWidgetToContainer(
        hostView: AppWidgetHostView,
        appWidgetId: Int,
        appWidgetInfo: AppWidgetProviderInfo
    ) {
        val metrics = resources.displayMetrics
        val density = metrics.density
        
        val gridSize = WidgetSizeCalculator.calculateGridSize(appWidgetInfo)
        
        if (gridSize.isClamped) {
            Toast.makeText(
                this,
                "Widget is too large - displaying at ${gridSize.columnSpan}x${gridSize.rowSpan} (may be cropped)",
                Toast.LENGTH_LONG
            ).show()
        }
        
        val widthPx = WidgetSizeCalculator.calculateWidgetSizePx(gridSize.columnSpan, density)
        val heightPx = WidgetSizeCalculator.calculateWidgetSizePx(gridSize.rowSpan, density)
        
        val marginPx = (4 * density).toInt()
        val params = GridLayout.LayoutParams().apply {
            width = widthPx
            height = heightPx
            columnSpec = GridLayout.spec(GridLayout.UNDEFINED, gridSize.columnSpan, 1f)
            rowSpec = GridLayout.spec(GridLayout.UNDEFINED, gridSize.rowSpan, 1f)
            setMargins(marginPx, marginPx, marginPx, marginPx)
        }
        
        hostView.layoutParams = params
        hostView.tag = appWidgetId
        
        val widthDp = (widthPx / density).toInt()
        val heightDp = (heightPx / density).toInt()
        hostView.updateAppWidgetSize(null, widthDp, heightDp, widthDp, heightDp)
        
        widgetContainer.addView(hostView)
    }

    private fun saveWidgetData(appWidgetId: Int, appWidgetInfo: AppWidgetProviderInfo) {
        val gridSize = WidgetSizeCalculator.calculateGridSize(appWidgetInfo)
        
        val widgetData = ChildWidgetData(
            id = appWidgetId.toString(),
            provider = appWidgetInfo.provider,
            label = appWidgetInfo.loadLabel(packageManager),
            icon = null,
            gridX = 0,
            gridY = 0,
            gridWidth = gridSize.columnSpan,
            gridHeight = gridSize.rowSpan,
            containerId = containerId
        )
        
        widgetDataManager.saveWidget(widgetData)
    }

    private fun loadWidgets() {
        widgetContainer.removeAllViews()
        
        val savedWidgets = widgetDataManager.getWidgetsForContainer(containerId)
        
        for (widget in savedWidgets) {
            val appWidgetId = widget.id.toIntOrNull() ?: continue
            
            try {
                val appWidgetInfo = appWidgetManager.getAppWidgetInfo(appWidgetId)
                if (appWidgetInfo != null) {
                    val hostView = appWidgetHost.createView(this, appWidgetId, appWidgetInfo)
                    addWidgetToContainer(hostView, appWidgetId, appWidgetInfo)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    override fun onStart() {
        super.onStart()
        appWidgetHost.startListening()
    }

    override fun onStop() {
        super.onStop()
        appWidgetHost.stopListening()
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
}
