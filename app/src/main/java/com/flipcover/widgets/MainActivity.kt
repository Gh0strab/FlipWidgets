package com.flipcover.widgets

import android.app.AppOpsManager
import android.appwidget.AppWidgetManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import com.google.android.material.appbar.MaterialToolbar

class MainActivity : AppCompatActivity() {

    private lateinit var widgetRecyclerView: RecyclerView
    private lateinit var openWidgetHostButton: MaterialButton
    private lateinit var widgetDataManager: WidgetDataManager
    private var defaultContainerId = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        widgetDataManager = WidgetDataManager(this)

        setupViews()
        checkPermissions()
        loadCoverScreenWidgets()
    }

    private fun setupViews() {
        val toolbar = findViewById<MaterialToolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.title = getString(R.string.app_name)

        widgetRecyclerView = findViewById(R.id.widgetRecyclerView)
        widgetRecyclerView.layoutManager = LinearLayoutManager(this)

        openWidgetHostButton = findViewById(R.id.openWidgetHostButton)
        openWidgetHostButton.setOnClickListener {
            val intent = Intent(this, WidgetHostActivity::class.java)
            intent.putExtra("container_id", defaultContainerId)
            startActivity(intent)
        }
    }

    private fun checkPermissions() {
        if (!hasBindWidgetPermission()) {
            requestBindWidgetPermission()
        }
    }

    private fun hasBindWidgetPermission(): Boolean {
        return try {
            val appOps = getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager
            val mode = appOps.checkOpNoThrow(
                "android:bind_appwidget",
                android.os.Process.myUid(),
                packageName
            )
            mode == AppOpsManager.MODE_ALLOWED
        } catch (e: Exception) {
            true
        }
    }

    private fun requestBindWidgetPermission() {
        Toast.makeText(
            this,
            getString(R.string.grant_permission),
            Toast.LENGTH_LONG
        ).show()
        
        try {
            val intent = Intent(Settings.ACTION_MANAGE_APPLICATIONS_SETTINGS)
            startActivity(intent)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun loadCoverScreenWidgets() {
        val childWidgets = widgetDataManager.getWidgetsForContainer(defaultContainerId)
        val coverWidgets = childWidgets.map { child ->
            CoverWidget(
                id = child.id,
                label = child.label,
                provider = child.provider
            )
        }
        
        val adapter = CoverWidgetAdapter(coverWidgets) { widget ->
            widgetDataManager.removeWidget(widget.id, defaultContainerId)
            Toast.makeText(this, getString(R.string.widget_removed), Toast.LENGTH_SHORT).show()
            loadCoverScreenWidgets()
            updateWidget()
        }
        widgetRecyclerView.adapter = adapter
    }
    
    private fun updateWidget() {
        val appWidgetManager = AppWidgetManager.getInstance(this)
        val appWidgetIds = appWidgetManager.getAppWidgetIds(
            android.content.ComponentName(this, CoverWidgetProvider::class.java)
        )
        for (appWidgetId in appWidgetIds) {
            CoverWidgetProvider.updateAppWidget(this, appWidgetManager, appWidgetId)
        }
    }

    override fun onResume() {
        super.onResume()
        loadCoverScreenWidgets()
    }
}
