package com.flipcover.widgets

import android.app.Activity
import android.appwidget.AppWidgetHost
import android.appwidget.AppWidgetHostView
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProviderInfo
import android.content.ComponentName
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.widget.GridLayout
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
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
    private var containerId: Int = 1
    private var pendingAppWidgetId: Int = -1

    // ✅ Modern ActivityResult Launchers
    private val pickWidgetLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val data = result.data
        if (result.resultCode == Activity.RESULT_OK) configureWidget(data)
        else cleanupAppWidgetId(data)
    }

    private val bindWidgetLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val data = result.data
        if (result.resultCode == Activity.RESULT_OK && pendingAppWidgetId != -1)
            configureWidget(data, skipBinding = true)
        else cleanupAppWidgetId(data)
    }

    private val createWidgetLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val data = result.data
        if (result.resultCode == Activity.RESULT_OK) createWidget(data)
        else cleanupAppWidgetId(data)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_widget_host)

        containerId = intent.getIntExtra("container_id", 1)

        appWidgetManager = AppWidgetManager.getInstance(this)
        appWidgetHost = AppWidgetHost(this, appWidgetHostId)
        widgetDataManager = WidgetDataManager(this)

        setupViews()
        loadWidgets()
    }

    private fun setupViews() {
        val toolbar = findViewById<MaterialToolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.title = "Cover Screen Widgets"
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        widgetContainer = findViewById(R.id.widgetHostContainer)
        widgetContainer.columnCount = 6
        widgetContainer.rowCount = 6

        addWidgetFab = findViewById(R.id.addWidgetFab)
        addWidgetFab.setOnClickListener { selectWidget() }
    }

    // ✅ Updated using ActivityResultLauncher
    private fun selectWidget() {
        val appWidgetId = appWidgetHost.allocateAppWidgetId()
        pendingAppWidgetId = appWidgetId

        val pickIntent = Intent(AppWidgetManager.ACTION_APPWIDGET_PICK)
        pickIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)

        pickWidgetLauncher.launch(pickIntent)
    }

    private fun cleanupAppWidgetId(data: Intent?) {
        val id = data?.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, pendingAppWidgetId)
        if (id != null && id != -1) try {
            appWidgetHost.deleteAppWidgetId(id)
        } catch (_: Exception) {}
        pendingAppWidgetId = -1
    }

    private fun configureWidget(data: Intent?, skipBinding: Boolean = false) {
        val appWidgetId = data?.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, -1)
            ?: pendingAppWidgetId

        if (appWidgetId == -1) return

        val info = appWidgetManager.getAppWidgetInfo(appWidgetId) ?: return

        if (!skipBinding) {
            val allowed = appWidgetManager.bindAppWidgetIdIfAllowed(appWidgetId, info.provider)
            if (!allowed) {
                val bindIntent = Intent(AppWidgetManager.ACTION_APPWIDGET_BIND).apply {
                    putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
                    putExtra(AppWidgetManager.EXTRA_APPWIDGET_PROVIDER, info.provider)
                }
                bindWidgetLauncher.launch(bindIntent)
                return
            }
        }

        if (info.configure != null) {
            val configIntent = Intent(AppWidgetManager.ACTION_APPWIDGET_CONFIGURE).apply {
                component = info.configure
                putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
            }
            createWidgetLauncher.launch(configIntent)
        } else createWidget(data)
    }

    private fun createWidget(data: Intent?) {
        val appWidgetId = data?.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, -1)
            ?: pendingAppWidgetId

        val info = appWidgetManager.getAppWidgetInfo(appWidgetId) ?: return
        val hostView = appWidgetHost.createView(this, appWidgetId, info)

        addWidgetToContainer(hostView, appWidgetId, info)
        saveWidgetData(appWidgetId, info)

        pendingAppWidgetId = -1
        Toast.makeText(this, "Widget added!", Toast.LENGTH_SHORT).show()
    }

    private fun addWidgetToContainer(
        hostView: AppWidgetHostView,
        appWidgetId: Int,
        info: AppWidgetProviderInfo
    ) {
        val gridSize = WidgetSizeCalculator.calculateGridSize(info)
        val px = resources.displayMetrics.density
        val w = WidgetSizeCalculator.calculateWidgetSizePx(gridSize.columnSpan, px)
        val h = WidgetSizeCalculator.calculateWidgetSizePx(gridSize.rowSpan, px)

        val params = GridLayout.LayoutParams().apply {
            width = w
            height = h
            columnSpec = GridLayout.spec(GridLayout.UNDEFINED, gridSize.columnSpan, 1f)
            rowSpec = GridLayout.spec(GridLayout.UNDEFINED, gridSize.rowSpan, 1f)
        }

        hostView.layoutParams = params
        hostView.tag = appWidgetId
        widgetContainer.addView(hostView)
    }

    private fun saveWidgetData(appWidgetId: Int, info: AppWidgetProviderInfo) {
        val gs = WidgetSizeCalculator.calculateGridSize(info)
        
        val iconBitmap = try {
            val drawable = info.loadIcon(this, resources.displayMetrics.densityDpi)
            drawableToBitmap(drawable)
        } catch (e: Exception) {
            null
        }

        widgetDataManager.saveWidget(
            ChildWidgetData(
                id = appWidgetId.toString(),
                provider = info.provider,
                label = info.loadLabel(packageManager),
                icon = iconBitmap,
                gridX = 0,
                gridY = 0,
                gridWidth = gs.columnSpan,
                gridHeight = gs.rowSpan,
                containerId = containerId
            )
        )
        
        updateCoverScreenWidget()
    }
    
    private fun drawableToBitmap(drawable: Drawable): Bitmap {
        if (drawable is BitmapDrawable) {
            return drawable.bitmap
        }
        
        val width = if (drawable.intrinsicWidth > 0) drawable.intrinsicWidth else 96
        val height = if (drawable.intrinsicHeight > 0) drawable.intrinsicHeight else 96
        
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        drawable.setBounds(0, 0, canvas.width, canvas.height)
        drawable.draw(canvas)
        return bitmap
    }
    
    private fun updateCoverScreenWidget() {
        val appWidgetIds = appWidgetManager.getAppWidgetIds(
            ComponentName(this, CoverWidgetProvider::class.java)
        )
        for (widgetId in appWidgetIds) {
            CoverWidgetProvider.updateAppWidget(this, appWidgetManager, widgetId)
        }
    }

    private fun loadWidgets() {
        widgetContainer.removeAllViews()

        widgetDataManager.getWidgetsForContainer(containerId).forEach { widget ->
            val id = widget.id.toIntOrNull() ?: return@forEach
            val info = appWidgetManager.getAppWidgetInfo(id) ?: return@forEach
            val hostView = appWidgetHost.createView(this, id, info)
            addWidgetToContainer(hostView, id, info)
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
